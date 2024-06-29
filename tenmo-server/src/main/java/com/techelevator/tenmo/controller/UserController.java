package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserDao userDao;

    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(path = "/allusers", method = RequestMethod.GET)
    public List<User> getUsers() {
        return userDao.getUsers();
    }

    @RequestMapping(path = "/userId/{id}", method = RequestMethod.GET)
    public User getUserById(@PathVariable int id) {
        return userDao.getUserById(id);
    }

    @RequestMapping(path = "/username/{username}", method = RequestMethod.GET)
    public User getUserByUsername(@PathVariable String username) {
        return userDao.getUserByUsername(username);
    }
}
