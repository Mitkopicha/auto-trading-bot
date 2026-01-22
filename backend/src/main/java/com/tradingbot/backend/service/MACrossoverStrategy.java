package com.tradingbot.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MACrossoverStrategy {

    public enum Signal { BUY, SELL, HOLD }

    private final int shortWindow;
    private final int longWindow;

    public MACrossoverStrategy() {
        this(5, 20);
    }

    public MACrossoverStrategy(int shortWindow, int longWindow) {
        if (shortWindow <= 0) throw new IllegalArgumentException("shortWindow must be > 0");
        if (longWindow <= 0) throw new IllegalArgumentException("longWindow must be > 0");
        if (shortWindow >= longWindow) {
            // Not required, but prevents weird configs like 20/5
            throw new IllegalArgumentException("shortWindow must be < longWindow");
        }
        this.shortWindow = shortWindow;
        this.longWindow = longWindow;
    }

    // Classic crossover (good for training/backtests)
    public Signal decide(List<BigDecimal> closes) {
        if (closes == null) return Signal.HOLD;
        if (closes.size() < longWindow + 1) return Signal.HOLD;

        int last = closes.size() - 1;

        BigDecimal shortNow = sma(closes, last, shortWindow);
        BigDecimal longNow  = sma(closes, last, longWindow);

        BigDecimal shortPrev = sma(closes, last - 1, shortWindow);
        BigDecimal longPrev  = sma(closes, last - 1, longWindow);

        boolean crossUp = shortPrev.compareTo(longPrev) <= 0 && shortNow.compareTo(longNow) > 0;
        boolean crossDown = shortPrev.compareTo(longPrev) >= 0 && shortNow.compareTo(longNow) < 0;

        if (crossUp) return Signal.BUY;
        if (crossDown) return Signal.SELL;
        return Signal.HOLD;
    }

    /**
     * Trend-following (better for live stepping; does not require a perfect cross).
     * BUY when shortMA > longMA
     * SELL when shortMA < longMA
     *
     * epsilonPct can be used as a "dead zone" to reduce whipsaw:
     * example: 0.001 = 0.1% band
     */
    public Signal trendSignal(List<BigDecimal> closes, BigDecimal epsilonPct) {
        if (closes == null) return Signal.HOLD;

        // Need enough candles for BOTH MAs at the last index
        if (closes.size() < longWindow) return Signal.HOLD;

        int last = closes.size() - 1;

        BigDecimal shortNow = sma(closes, last, shortWindow);
        BigDecimal longNow  = sma(closes, last, longWindow);

        // optional dead zone
        if (epsilonPct != null && epsilonPct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = shortNow.subtract(longNow).abs();
            BigDecimal threshold = longNow.abs().multiply(epsilonPct);
            if (diff.compareTo(threshold) <= 0) return Signal.HOLD;
        }

        int cmp = shortNow.compareTo(longNow);
        if (cmp > 0) return Signal.BUY;
        if (cmp < 0) return Signal.SELL;
        return Signal.HOLD;
    }

    private BigDecimal sma(List<BigDecimal> closes, int endIndex, int window) {
        if (closes == null) return BigDecimal.ZERO;
        if (window <= 0) return BigDecimal.ZERO;
        if (endIndex < 0 || endIndex >= closes.size()) return BigDecimal.ZERO;

        int start = endIndex - window + 1;
        if (start < 0) return BigDecimal.ZERO; // safety guard

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = start; i <= endIndex; i++) {
            BigDecimal v = closes.get(i);
            if (v == null) v = BigDecimal.ZERO;
            sum = sum.add(v);
        }
        return sum.divide(BigDecimal.valueOf(window), 8, RoundingMode.HALF_UP);
    }
}
