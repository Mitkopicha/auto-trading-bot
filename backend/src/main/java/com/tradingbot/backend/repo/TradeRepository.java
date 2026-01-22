package com.tradingbot.backend.repo;
// trade repository handles database operations related to trades
// it uses JdbcTemplate for database interactions
// it includes methods to insert trades, retrieve trades for an account, delete trades, and get
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public class TradeRepository {

    private final JdbcTemplate jdbc;

    public TradeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insertTrade(long accountId,
                           String mode,
                           String symbol,
                           String side,
                           BigDecimal quantity,
                           BigDecimal price,
                           BigDecimal fee,
                           BigDecimal pnl) {

        return jdbc.update(
                "INSERT INTO trade (account_id, mode, symbol, side, quantity, price, fee, pnl, timestamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                accountId, mode, symbol, side, quantity, price, fee, pnl
        );
    }

    // Normalize candle timestamp so it's ALWAYS stored correctly.
    // Binance can be ms (13 digits) or seconds (10 digits).
    private long toEpochSeconds(Long candleTs) {
        if (candleTs == null) return 0L;
        long ts = candleTs;

        // If it's milliseconds (>= 1e12), convert to seconds.
        if (ts >= 1_000_000_000_000L) {
            return ts / 1000L;
        }

        // Otherwise assume it's already seconds.
        return ts;
    }

    public int insertTradeWithTimestamp(long accountId,
                                        String mode,
                                        String symbol,
                                        String side,
                                        BigDecimal quantity,
                                        BigDecimal price,
                                        BigDecimal fee,
                                        BigDecimal pnl,
                                        Long candleTs) {

        if (candleTs == null) {
            return insertTrade(accountId, mode, symbol, side, quantity, price, fee, pnl);
        }

        long epochSeconds = toEpochSeconds(candleTs);

        // Store as FROM_UNIXTIME(seconds) so DB timestamp is correct.
        return jdbc.update(
                "INSERT INTO trade (account_id, mode, symbol, side, quantity, price, fee, pnl, timestamp) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, FROM_UNIXTIME(?))",
                accountId, mode, symbol, side, quantity, price, fee, pnl, epochSeconds
        );
    }

    /**
     * Return timestamp as epoch milliseconds for frontend alignment.
     */
    public List<Map<String, Object>> getTrades(long accountId) {
        return jdbc.queryForList(
                "SELECT (UNIX_TIMESTAMP(`timestamp`) * 1000) AS timestamp, " +
                        "side, symbol, quantity, price, pnl, mode " +
                        "FROM trade WHERE account_id = ? " +
                        "ORDER BY `timestamp` DESC",
                accountId
        );
    }

    public int deleteTradesForAccount(long accountId) {
        return jdbc.update("DELETE FROM trade WHERE account_id = ?", accountId);
    }

    public Timestamp getLastTradeTimestamp(long accountId, String mode, String symbol) {
        return jdbc.query(
                "SELECT `timestamp` FROM trade " +
                        "WHERE account_id=? AND mode=? AND symbol=? " +
                        "ORDER BY `timestamp` DESC LIMIT 1",
                rs -> rs.next() ? rs.getTimestamp(1) : null,
                accountId, mode, symbol
        );
    }

    public String getLastTradeSide(long accountId, String mode, String symbol) {
        return jdbc.query(
                "SELECT side FROM trade " +
                        "WHERE account_id=? AND mode=? AND symbol=? " +
                        "ORDER BY `timestamp` DESC LIMIT 1",
                rs -> rs.next() ? rs.getString(1) : null,
                accountId, mode, symbol
        );
    }
}
