package com.tradingbot.backend.repo;
// account repository handles database operations related to accounts
// including retrieval and updating of cash balances and account details
// it uses JdbcTemplate for database interactions
// it includes methods to get cash balance, update cash balance, get account details, and reset cash
// for a given account ID 

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