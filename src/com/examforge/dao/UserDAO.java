package com.examforge.dao;

import com.examforge.exception.DatabaseException;
import com.examforge.model.User;
import com.examforge.model.UserRole;
import java.util.List;

public interface UserDAO {
    User findByUsername(String username) throws DatabaseException;
    User findById(int id) throws DatabaseException;
    List<User> findAll() throws DatabaseException;
    List<User> findByRole(UserRole role) throws DatabaseException;
    int insert(User user) throws DatabaseException;
    boolean update(User user) throws DatabaseException;
    boolean deactivate(int userId) throws DatabaseException;
    boolean validateCredentials(String username, String password) throws DatabaseException;
}
