package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcAccountDao implements AccountDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Account> getAccounts() {
        List<Account> allAccounts = new ArrayList<>();
        String sql = "SELECT * FROM account;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
            while (results.next()) {
                Account accountResults = mapRowToAccount(results);
                allAccounts.add(accountResults);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return allAccounts;
    }

    @Override
    public Account getAccountByUserId(int userId) {
        List<Account> accountList = getAccounts();
        Account userAccount = null;
        for (Account account : accountList) {
            if (account.getUser_id() == userId) {
                userAccount = account;
                break;
            }
        }
        return userAccount;
    }

    @Override
    public Account getAccountObjByAccountId(int userId, int accountId) {
        Account account = null;
        String sql = "SELECT * FROM account WHERE account_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            if (results.next()) {
                account = mapRowToAccount(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return account;
    }

    private Account mapRowToAccount(SqlRowSet results) {
        Account account = new Account();
        account.setUser_id(results.getInt("user_id"));
        account.setAccount_id(results.getInt("account_id"));
        account.setBalance(results.getBigDecimal("balance"));
        return account;
    }
}
