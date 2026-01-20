package com.tradingbot.backend.repo;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Repository
public class PortfolioRepository {
    private final JdbcTemplate jdbc;

    public PortfolioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void upsertBuy(long accountId, String symbol, BigDecimal buyQty, BigDecimal buyPrice) {
        List<Map<String, Object>> rows = jdbc.queryForList(
            "SELECT quantity, avg_entry_price FROM portfolio " +
            "WHERE account_id = ? AND symbol = ? FOR UPDATE",
            accountId, symbol
        );

        if (rows.isEmpty()) {
            jdbc.update(
                "INSERT INTO portfolio (account_id, symbol, quantity, avg_entry_price) " +
                "VALUES (?, ?, ?, ?)",
                accountId, symbol, buyQty, buyPrice
            );
            return;
        }

        BigDecimal oldQty = (BigDecimal) rows.get(0).get("quantity");
        BigDecimal oldAvg = (BigDecimal) rows.get(0).get("avg_entry_price");

        if (oldAvg == null) oldAvg = BigDecimal.ZERO;

        BigDecimal newQty = oldQty.add(buyQty);

        BigDecimal numerator = oldQty.multiply(oldAvg).add(buyQty.multiply(buyPrice));
        BigDecimal newAvg = numerator.divide(newQty, 8, RoundingMode.HALF_UP);

        jdbc.update(
            "UPDATE portfolio " +
            "SET quantity = ?, avg_entry_price = ? " +
            "WHERE account_id = ? AND symbol = ?",
            newQty, newAvg, accountId, symbol
        );
    }
    public BigDecimal getPositionQtyForUpdate(long accountId, String symbol) {
    return jdbc.queryForObject(
        "SELECT quantity FROM portfolio WHERE account_id = ? AND symbol = ? FOR UPDATE",
        BigDecimal.class,
        accountId, symbol
    );
}
    public BigDecimal getAvgEntryPriceForUpdate(long accountId, String symbol) {
        return jdbc.queryForObject(
            "SELECT avg_entry_price FROM portfolio WHERE account_id = ? AND symbol = ? FOR UPDATE",
            BigDecimal.class,
            accountId, symbol
        );
    }

public int reducePosition(long accountId, String symbol, BigDecimal sellQty) {
    return jdbc.update(
        "UPDATE portfolio " +
        "SET quantity = quantity - ?, updated_at = NOW() " +
        "WHERE account_id = ? AND symbol = ? AND quantity >= ?",
        sellQty, accountId, symbol, sellQty
    );
}

public List<Map<String, Object>> getPortfolio(long accountId) {
    return jdbc.queryForList(
        "SELECT symbol, quantity, avg_entry_price, updated_at " +
        "FROM portfolio WHERE account_id = ? ORDER BY symbol",
        accountId
    );
}
public int deleteIfZero(long accountId, String symbol) {
    return jdbc.update(
        "DELETE FROM portfolio WHERE account_id = ? AND symbol = ? AND quantity <= 0",
        accountId, symbol
    );
}
public int deletePortfolioForAccount(long accountId) {
    return jdbc.update("DELETE FROM portfolio WHERE account_id = ?", accountId);
}
}   