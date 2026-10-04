package com.examforge.service;

import com.examforge.dao.UserDAO;
import com.examforge.dao.impl.UserDAOImpl;
import com.examforge.exception.DatabaseException;
import com.examforge.exception.ExamForgeException;
import com.examforge.model.User;
import com.examforge.model.UserRole;

import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    public User login(String username, String password) throws ExamForgeException {
        try {
            if (userDAO.validateCredentials(username, password)) {
                return userDAO.findByUsername(username);
            }
            throw new ExamForgeException("Invalid username or password", "AUTH_FAILED");
        } catch (DatabaseException e) {
            throw new ExamForgeException("Login failed: " + e.getMessage(), e);
        }
    }

    public User register(String username, String password, String fullName, UserRole role) throws ExamForgeException {
        if (role != UserRole.STUDENT) {
            throw new ExamForgeException("Self-registration is only available for students", "ROLE_NOT_ALLOWED");
        }
        return createUser(username, password, fullName, role);
    }

    public User createUser(User actor, String username, String password, String fullName, UserRole role) throws ExamForgeException {
        requireSuperAdmin(actor);
        return createUser(username, password, fullName, role);
    }

    private User createUser(String username, String password, String fullName, UserRole role) throws ExamForgeException {
        try {
            if (userDAO.findByUsername(username) != null) {
                throw new ExamForgeException("Username already exists", "DUPLICATE_USERNAME");
            }
            User user = new User(username, password, fullName, role);
            int id = userDAO.insert(user);
            user.setUserId(id);
            return user;
        } catch (DatabaseException e) {
            throw new ExamForgeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public void deactivateUser(User actor, int userId) throws ExamForgeException {
        requireSuperAdmin(actor);
        if (actor.getUserId() == userId) {
            throw new ExamForgeException("You cannot remove your own account", "SELF_REMOVAL");
        }
        try {
            userDAO.deactivate(userId);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Failed to remove user: " + e.getMessage(), e);
        }
    }

    public List<User> findManageableUsers(User actor, UserRole category) throws ExamForgeException {
        requireSuperAdmin(actor);
        try {
            if (category == UserRole.TEACHER) {
                List<User> all = userDAO.findAll();
                all.removeIf(u -> u.getRole() != UserRole.TEACHER && u.getRole() != UserRole.SUPER_ADMIN);
                return all;
            }
            return userDAO.findByRole(UserRole.STUDENT);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing users: " + e.getMessage(), e);
        }
    }

    private void requireSuperAdmin(User actor) throws ExamForgeException {
        if (actor == null || actor.getRole() != UserRole.SUPER_ADMIN) {
            throw new ExamForgeException("Super Admin access required", "FORBIDDEN");
        }
    }

    public User findById(int id) throws ExamForgeException {
        try {
            return userDAO.findById(id);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error finding user: " + e.getMessage(), e);
        }
    }

    public List<User> findAll() throws ExamForgeException {
        try {
            return userDAO.findAll();
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing users: " + e.getMessage(), e);
        }
    }

    public List<User> findByRole(UserRole role) throws ExamForgeException {
        try {
            return userDAO.findByRole(role);
        } catch (DatabaseException e) {
            throw new ExamForgeException("Error listing users by role: " + e.getMessage(), e);
        }
    }
}