package com.restman.controller;

import com.restman.dao.UserDAO3;
import com.restman.dao.UserDAOImpl3;
import com.restman.model.User3;
import java.sql.SQLException;
import java.util.Optional;

public class UserController3 {
    private final UserDAO3 userDAO;

    public UserController3() {
        this(new UserDAOImpl3());
    }

    public UserController3(UserDAO3 userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<User3> login(String username, String password) throws SQLException {
        return userDAO.findByUsernameAndPassword(username, password);
    }

    public String getRoleByUserId(int userId) throws SQLException {
        return userDAO.findRoleByUserId(userId);
    }

    public String getDisplayNameByUserId(int userId, String role) throws SQLException {
        return userDAO.findDisplayNameByUserId(userId, role);
    }

    public boolean registerCustomer(String username, String password, String fullName, String phone, String email,
                                    String address) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()
                || fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập, mật khẩu và họ tên.");
        }
        return userDAO.registerCustomer(username, password, fullName, phone, email, address);
    }
}
