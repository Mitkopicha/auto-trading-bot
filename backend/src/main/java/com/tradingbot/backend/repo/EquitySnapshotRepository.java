package com.tradingbot.backend.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public class EquitySnapshotRepository {

    private final JdbcTemplate jdbc;

    public EquitySnapshotRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insertSnapshot(long accountId, String mode,
                              BigDecimal cash, BigDecimal portfolioValue, BigDecimal totalEquity) {
        return jdbc.update(
                "INSERT INTO equity_snapshot (account_id, mode, timestamp, cash_balance, portfolio_value, total_equity) " +
                        "VALUES (?, ?, NOW(), ?, ?, ?)",
                accountId, mode, cash, portfolioValue, totalEquity
        );
    }

    public List<Map<String, Object>> getSnapshots(long accountId, String mode, int limit) {
        return jdbc.queryForList(
                "SELECT `timestamp`, cash_balance, portfolio_value, total_equity " +
                        "FROM equity_snapshot " +
                        "WHERE account_id = ? AND mode = ? " +
                        "ORDER BY `timestamp` ASC " +
                        "LIMIT ?",
                accountId, mode, limit
        );
    }

    public int deleteSnapshotsForAccount(long accountId) {
        return jdbc.update(
                "DELETE FROM equity_snapshot WHERE account_id = ?",
                accountId
        );
    }
}