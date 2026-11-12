package com.restman.dao;

import com.restman.model.User3;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO3 {
    Optional<User3> findByUsernameAndPassword(String username, String password) throws SQLException;

    String findRoleByUserId(int userId) throws SQLException;

    String findDisplayNameByUserId(int userId, String role) throws SQLException;

    boolean registerCustomer(String username, String password, String fullName, String phone, String email,
                             String address) throws SQLException;
}
