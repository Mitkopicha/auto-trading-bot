package com.tradingbot.backend;
// unit tests for MACrossoverStrategy
// tests include scenarios for buy and sell signals based on moving average crossovers
// uses JUnit 5 for testing framework
import org.junit.jupiter.api.Test;

import com.tradingbot.backend.service.MACrossoverStrategy;
import com.tradingbot.backend.service.MACrossoverStrategy.Signal;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MACrossoverStrategyTest {

    @Test
    void shouldBuyWhenShortMovingAverageCrossesAboveLong() {

        // ARRANGE (BigDecimal prices)
       List<BigDecimal> prices = List.of(
    new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),
    new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),
    new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),
    new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),
    new BigDecimal("50"),   // dip makes short average below long average
    new BigDecimal("200")   // spike makes short average cross above long average
);


        MACrossoverStrategy strategy = new MACrossoverStrategy(5, 20);

        // ACT
        Signal action = strategy.decide(prices);
assertEquals(Signal.BUY, action);

        // ASSERT
        assertEquals(MACrossoverStrategy.Signal.BUY, action);

    }


@Test
void shouldSellWhenShortMovingAverageCrossesBelowLong() {

    List<BigDecimal> prices = List.of(
            // 20 stable values keep long MA ~100
            new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),
            new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),
            new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),
            new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"),

            // these create the crossover near the end
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("110"),  // makes short MA slightly ABOVE long at t-1
            new BigDecimal("80")    // makes short MA drop BELOW long at t
    );

    MACrossoverStrategy strategy = new MACrossoverStrategy(5, 20);

    MACrossoverStrategy.Signal action = strategy.decide(prices);

    assertEquals(MACrossoverStrategy.Signal.SELL, action);
}


}