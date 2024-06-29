package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/user")
public class AccountController {

    private AccountDao accountDao;

    public AccountController(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/all/account", method = RequestMethod.GET)
    public List<Account> list() {
        return accountDao.getAccounts();
    }

    @RequestMapping(path = "/{id}/account", method = RequestMethod.GET)
    public Account get(@PathVariable int id) {
        Account account = accountDao.getAccountByUserId(id);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        } else {
            return account;
        }
    }

    @RequestMapping(path = "/{id}/account/{accountId}", method = RequestMethod.GET)
    public Account getAccountByAccountId(@PathVariable int id, @PathVariable int accountId) {
        try {
            return accountDao.getAccountObjByAccountId(id, accountId);
        } catch(DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
    }
}
