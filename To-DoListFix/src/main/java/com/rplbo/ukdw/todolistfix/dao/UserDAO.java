package com.rplbo.ukdw.todolistfix.dao;
import com.rplbo.ukdw.todolistfix.model.User;

public interface UserDAO {
    boolean registerUser(User user);
    User getUserByUsername(String username);
    boolean userExists(String username);
}
