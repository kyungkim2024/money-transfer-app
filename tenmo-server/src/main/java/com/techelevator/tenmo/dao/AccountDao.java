package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.util.List;

public interface AccountDao {

    List<Account> getAccounts();

    Account getAccountByUserId(int userId);

    Account getAccountObjByAccountId(int userId, int accountId);
}
