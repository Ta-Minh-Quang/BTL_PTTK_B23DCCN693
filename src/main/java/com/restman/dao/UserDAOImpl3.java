package com.restman.dao;

import com.restman.config.DatabaseConfig3;
import com.restman.model.User3;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAOImpl3 implements UserDAO3 {
    @Override
    public Optional<User3> findByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT id, username, passwordHash, email, phone, status " +
                "FROM tblUser3 WHERE username = ? AND passwordHash = ? AND status = 'ACTIVE'";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username == null ? "" : username.trim());
            statement.setString(2, password == null ? "" : password.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User3(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("passwordHash"),
                            resultSet.getString("email"),
                            resultSet.getString("phone"),
                            resultSet.getString("status")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public String findRoleByUserId(int userId) throws SQLException {
        String[] checks = {
                "SELECT 'Manager' FROM tblManager3 WHERE userId = ?",
                "SELECT 'SalesStaff' FROM tblSalesStaff3 WHERE userId = ?",
                "SELECT 'InventoryStaff' FROM tblInventoryStaff3 WHERE userId = ?",
                "SELECT 'Customer' FROM tblCustomer3 WHERE userId = ?"
        };

        for (String sql : checks) {
            try (Connection connection = DatabaseConfig3.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            }
        }
        return "Customer";
    }

    @Override
    public String findDisplayNameByUserId(int userId, String role) throws SQLException {
        String sql;
        if ("Manager".equalsIgnoreCase(role)) {
            sql = "SELECT fullName FROM tblManager3 WHERE userId = ?";
        } else if ("SalesStaff".equalsIgnoreCase(role)) {
            sql = "SELECT fullName FROM tblSalesStaff3 WHERE userId = ?";
        } else if ("InventoryStaff".equalsIgnoreCase(role)) {
            sql = "SELECT fullName FROM tblInventoryStaff3 WHERE userId = ?";
        } else {
            sql = "SELECT CONCAT(firstName, ' ', lastName) FROM tblCustomer3 WHERE userId = ?";
        }

        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String value = resultSet.getString(1);
                    if (value != null && !value.trim().isEmpty()) {
                        return value.trim();
                    }
                }
            }
        }

        return "Khách hàng";
    }

    @Override
    public boolean registerCustomer(String username, String password, String fullName, String phone, String email,
                                    String address) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (exists(connection, "SELECT id FROM tblUser3 WHERE username=?", username)) {
                    connection.rollback();
                    return false;
                }
                if (email != null && !email.isBlank()
                        && exists(connection, "SELECT id FROM tblUser3 WHERE email=?", email)) {
                    throw new SQLException("Email đã được sử dụng.");
                }

                int userId;
                String insertUser = "INSERT INTO tblUser3 "
                        + "(username,passwordHash,email,phone,role,status) VALUES (?,?,?,?,'CUSTOMER','ACTIVE')";
                try (PreparedStatement statement = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, username.trim());
                    statement.setString(2, password.trim());
                    setNullableString(statement, 3, email);
                    setNullableString(statement, 4, phone);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không nhận được mã tài khoản vừa tạo.");
                        userId = keys.getInt(1);
                    }
                }

                String normalizedName = fullName.trim().replaceAll("\\s+", " ");
                int lastSpace = normalizedName.lastIndexOf(' ');
                String firstName = lastSpace < 0 ? normalizedName : normalizedName.substring(0, lastSpace);
                String lastName = lastSpace < 0 ? normalizedName : normalizedName.substring(lastSpace + 1);
                int customerId;
                String insertCustomer = "INSERT INTO tblCustomer3 "
                        + "(userId,customerCode,firstName,lastName,street) VALUES (?,NULL,?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, userId);
                    statement.setString(2, firstName);
                    statement.setString(3, lastName);
                    setNullableString(statement, 4, address);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không nhận được mã khách hàng vừa tạo.");
                        customerId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE tblCustomer3 SET customerCode=CONCAT('KH',id) WHERE id=?")) {
                    statement.setInt(1, customerId);
                    if (statement.executeUpdate() != 1) throw new SQLException("Không thể tạo mã khách hàng.");
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                if (exception instanceof SQLException sqlException && sqlException.getErrorCode() == 1062) {
                    throw new SQLException("Tên đăng nhập hoặc email đã được sử dụng.", sqlException.getSQLState(),
                            sqlException.getErrorCode(), sqlException);
                }
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private boolean exists(Connection connection, String sql, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void setNullableString(PreparedStatement statement, int parameter, String value) throws SQLException {
        if (value == null || value.isBlank()) statement.setNull(parameter, java.sql.Types.VARCHAR);
        else statement.setString(parameter, value.trim());
    }
}
