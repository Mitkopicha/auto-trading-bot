package com.tradingbot.backend.repo;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

import java.math.BigDecimal;

@Repository
public class AccountRepository {
    private final JdbcTemplate jdbc;

    public AccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    
    public BigDecimal getCashBalance(long accountId) {
    return jdbc.queryForObject(
        "SELECT cash_balance FROM account WHERE id = ?",
        BigDecimal.class,
        accountId
    );
}

    public BigDecimal getCashBalanceForUpdate(long accountId) {
        return jdbc.queryForObject(
            "SELECT cash_balance FROM account WHERE id = ? FOR UPDATE",
            BigDecimal.class,
            accountId
        );
    }

    public int updateCashBalance(long accountId, BigDecimal newCash) {
        return jdbc.update(
            "UPDATE account SET cash_balance = ? WHERE id = ?",
            newCash, accountId
        );
    }
    public Map<String, Object> getAccount(long accountId) {
    return jdbc.queryForMap(
        "SELECT id, cash_balance, created_at FROM account WHERE id = ?",
        accountId
    );
}

public int resetCash(long accountId, BigDecimal cash) {
    return jdbc.update("UPDATE account SET cash_balance = ? WHERE id = ?", cash, accountId);
}
}