package com.tradingbot.backend.service;

import com.tradingbot.backend.repo.AccountRepository;
import com.tradingbot.backend.repo.PortfolioRepository;
import com.tradingbot.backend.repo.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

@Service
public class TradeService {

    private final AccountRepository accountRepo;
    private final PortfolioRepository portfolioRepo;
    private final TradeRepository tradeRepo;

    public TradeService(AccountRepository accountRepo,
                        PortfolioRepository portfolioRepo,
                        TradeRepository tradeRepo) {
        this.accountRepo = accountRepo;
        this.portfolioRepo = portfolioRepo;
        this.tradeRepo = tradeRepo;
    }

    @Transactional
public void buy(long accountId, String symbol, BigDecimal price, String mode) {
    BigDecimal cash = accountRepo.getCashBalanceForUpdate(accountId);
    if (cash == null || cash.compareTo(BigDecimal.ZERO) <= 0) return;
    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return;

    // Default: 10% of available cash
    BigDecimal spend = cash.multiply(new BigDecimal("0.10"));

    // ✅ Demo floor: force trades to remain visible (prevents qty rounding to 0)
    BigDecimal minSpend = new BigDecimal("25.00"); // $25 minimum trade size
    if (spend.compareTo(minSpend) < 0) spend = minSpend;

    // Can't spend more than we have
    if (cash.compareTo(spend) < 0) return;

    BigDecimal qty = spend.divide(price, 8, RoundingMode.DOWN);

    // ✅ Safety floor: if rounding makes qty zero, skip (avoids meaningless trades)
    BigDecimal minQty = new BigDecimal("0.00001000");
    if (qty.compareTo(minQty) < 0) return;

    tradeRepo.insertTrade(accountId, mode, symbol, "BUY", qty, price, null, null);

    BigDecimal newCash = cash.subtract(spend);
    accountRepo.updateCashBalance(accountId, newCash);

    portfolioRepo.upsertBuy(accountId, symbol, qty, price);
}

    @Transactional
    public void sell(long accountId, String symbol, BigDecimal price, String mode) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal positionQty;
        BigDecimal avgEntry;
        try {
            positionQty = portfolioRepo.getPositionQtyForUpdate(accountId, symbol);
            avgEntry = portfolioRepo.getAvgEntryPriceForUpdate(accountId, symbol);
        } catch (Exception e) {
            return;
        }

        if (positionQty == null || positionQty.compareTo(BigDecimal.ZERO) <= 0) return;
        if (avgEntry == null) avgEntry = BigDecimal.ZERO;

        BigDecimal sellQty = positionQty.setScale(8, RoundingMode.DOWN);
                

        if (sellQty.compareTo(BigDecimal.ZERO) <= 0) return;

        int updated = portfolioRepo.reducePosition(accountId, symbol, sellQty);
        if (updated == 0) return;

        BigDecimal cash = accountRepo.getCashBalanceForUpdate(accountId);
        BigDecimal proceeds = sellQty.multiply(price).setScale(8, RoundingMode.HALF_UP);
        BigDecimal newCash = cash.add(proceeds);
        accountRepo.updateCashBalance(accountId, newCash);

        BigDecimal pnl = price.subtract(avgEntry)
                .multiply(sellQty)
                .setScale(8, RoundingMode.HALF_UP);

        tradeRepo.insertTrade(accountId, mode, symbol, "SELL", sellQty, price, null, pnl);
        portfolioRepo.deleteIfZero(accountId, symbol);
    }

    // ===== Timestamp versions (used for training + trading marker alignment) =====

    @Transactional
public void buyAtTimestamp(long accountId, String symbol, BigDecimal price, String mode, Long ts) {
    BigDecimal cash = accountRepo.getCashBalanceForUpdate(accountId);
    if (cash == null || cash.compareTo(BigDecimal.ZERO) <= 0) return;
    if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) return;

    BigDecimal spend = cash.multiply(new BigDecimal("0.10"));

    BigDecimal minSpend = new BigDecimal("25.00");
    if (spend.compareTo(minSpend) < 0) spend = minSpend;

    if (cash.compareTo(spend) < 0) return;

    BigDecimal qty = spend.divide(price, 8, RoundingMode.DOWN);

    BigDecimal minQty = new BigDecimal("0.00001000");
    if (qty.compareTo(minQty) < 0) return;

    tradeRepo.insertTradeWithTimestamp(
        accountId, mode, symbol, "BUY", qty, price, null, null, ts
    );

    accountRepo.updateCashBalance(accountId, cash.subtract(spend));
    portfolioRepo.upsertBuy(accountId, symbol, qty, price);
}

    @Transactional
    public void sellAtTimestamp(long accountId, String symbol, BigDecimal price, String mode, Long ts) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal positionQty;
        BigDecimal avgEntry;
        try {
            positionQty = portfolioRepo.getPositionQtyForUpdate(accountId, symbol);
            avgEntry = portfolioRepo.getAvgEntryPriceForUpdate(accountId, symbol);
        } catch (Exception e) {
            return;
        }

        if (positionQty == null || positionQty.compareTo(BigDecimal.ZERO) <= 0) return;
        if (avgEntry == null) avgEntry = BigDecimal.ZERO;

       BigDecimal sellQty = positionQty.setScale(8, RoundingMode.DOWN);



        if (sellQty.compareTo(BigDecimal.ZERO) <= 0) return;

        int updated = portfolioRepo.reducePosition(accountId, symbol, sellQty);
        if (updated == 0) return;

        BigDecimal cash = accountRepo.getCashBalanceForUpdate(accountId);
        BigDecimal proceeds = sellQty.multiply(price).setScale(8, RoundingMode.HALF_UP);
        BigDecimal newCash = cash.add(proceeds);
        accountRepo.updateCashBalance(accountId, newCash);

        BigDecimal pnl = price.subtract(avgEntry)
                .multiply(sellQty)
                .setScale(8, RoundingMode.HALF_UP);

        tradeRepo.insertTradeWithTimestamp(accountId, mode, symbol, "SELL", sellQty, price, null, pnl, ts);

        portfolioRepo.deleteIfZero(accountId, symbol);
    }

    // ===== Helpers used by BotService =====

    public boolean hasPosition(long accountId, String symbol) {
        return portfolioRepo.hasPosition(accountId, symbol);
    }

    public boolean isCooldownOver(long accountId, String symbol, String mode, long cooldownMillis) {
        Timestamp ts = tradeRepo.getLastTradeTimestamp(accountId, mode, symbol);
        if (ts == null) return true;
        return (System.currentTimeMillis() - ts.getTime()) >= cooldownMillis;
    }

public String getLastTradeSide(long accountId, String symbol, String mode) {
    return tradeRepo.getLastTradeSide(accountId, mode, symbol);
}

}