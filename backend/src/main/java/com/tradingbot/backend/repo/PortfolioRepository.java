package com.tradingbot.backend.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
// portfolio repository handles database operations related to portfolios
// it uses JdbcTemplate for database interactions
// it includes methods to upsert buys, reduce positions on sells, and retrieve portfolio information
// it also includes methods to delete positions and check for existing positions
@Repository
public class PortfolioRepository {

    private final JdbcTemplate jdbc;

    public PortfolioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ===== BUY UPSERT (default = NOW) =====
    public void upsertBuy(long accountId, String symbol, BigDecimal buyQty, BigDecimal buyPrice) {
        upsertBuyAtTimestamp(accountId, symbol, buyQty, buyPrice, null);
    }

    // ===== BUY UPSERT (timestamp-aware; used for training/trading marker alignment) =====
    public void upsertBuyAtTimestamp(long accountId,
                                     String symbol,
                                     BigDecimal buyQty,
                                     BigDecimal buyPrice,
                                     Long ts) {

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT quantity, avg_entry_price FROM portfolio " +
                        "WHERE account_id = ? AND symbol = ? FOR UPDATE",
                accountId, symbol
        );

        if (rows.isEmpty()) {
            if (ts == null) {
                jdbc.update(
                        "INSERT INTO portfolio (account_id, symbol, quantity, avg_entry_price, updated_at) " +
                                "VALUES (?, ?, ?, ?, NOW())",
                        accountId, symbol, buyQty, buyPrice
                );
            } else {
                jdbc.update(
                        "INSERT INTO portfolio (account_id, symbol, quantity, avg_entry_price, updated_at) " +
                                "VALUES (?, ?, ?, ?, FROM_UNIXTIME(?/1000))",
                        accountId, symbol, buyQty, buyPrice, ts
                );
            }
            return;
        }

        BigDecimal oldQty = (BigDecimal) rows.get(0).get("quantity");
        BigDecimal oldAvg = (BigDecimal) rows.get(0).get("avg_entry_price");
        if (oldAvg == null) oldAvg = BigDecimal.ZERO;

        BigDecimal newQty = oldQty.add(buyQty);

        BigDecimal numerator = oldQty.multiply(oldAvg).add(buyQty.multiply(buyPrice));
        BigDecimal newAvg = numerator.divide(newQty, 8, RoundingMode.HALF_UP);

        if (ts == null) {
            jdbc.update(
                    "UPDATE portfolio " +
                            "SET quantity = ?, avg_entry_price = ?, updated_at = NOW() " +
                            "WHERE account_id = ? AND symbol = ?",
                    newQty, newAvg, accountId, symbol
            );
        } else {
            jdbc.update(
                    "UPDATE portfolio " +
                            "SET quantity = ?, avg_entry_price = ?, updated_at = FROM_UNIXTIME(?/1000) " +
                            "WHERE account_id = ? AND symbol = ?",
                    newQty, newAvg, ts, accountId, symbol
            );
        }
    }

    // ===== SELECTS FOR TRADE SERVICE =====

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

    // ===== SELL (default = NOW) =====
    public int reducePosition(long accountId, String symbol, BigDecimal sellQty) {
        return reducePositionAtTimestamp(accountId, symbol, sellQty, null);
    }

    // ===== SELL (timestamp-aware; used for training/trading marker alignment) =====
    public int reducePositionAtTimestamp(long accountId, String symbol, BigDecimal sellQty, Long ts) {
        if (ts == null) {
            return jdbc.update(
                    "UPDATE portfolio " +
                            "SET quantity = quantity - ?, updated_at = NOW() " +
                            "WHERE account_id = ? AND symbol = ? AND quantity >= ?",
                    sellQty, accountId, symbol, sellQty
            );
        }

        return jdbc.update(
                "UPDATE portfolio " +
                        "SET quantity = quantity - ?, updated_at = FROM_UNIXTIME(?/1000) " +
                        "WHERE account_id = ? AND symbol = ? AND quantity >= ?",
                sellQty, ts, accountId, symbol, sellQty
        );
    }

    // ===== READS =====

    public List<Map<String, Object>> getPortfolio(long accountId) {
        return jdbc.queryForList(
                "SELECT symbol, quantity, avg_entry_price, updated_at " +
                        "FROM portfolio WHERE account_id = ? ORDER BY symbol",
                accountId
        );
    }

    // ===== CLEANUP =====

    public int deleteIfZero(long accountId, String symbol) {
        return jdbc.update(
                "DELETE FROM portfolio WHERE account_id = ? AND symbol = ? AND quantity <= 0",
                accountId, symbol
        );
    }

    public int deletePortfolioForAccount(long accountId) {
        return jdbc.update("DELETE FROM portfolio WHERE account_id = ?", accountId);
    }

    // ===== HELPERS =====

    public boolean hasPosition(long accountId, String symbol) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM portfolio WHERE account_id = ? AND symbol = ? AND quantity > 0",
                Integer.class,
                accountId, symbol
        );
        return count != null && count > 0;
    }
}
