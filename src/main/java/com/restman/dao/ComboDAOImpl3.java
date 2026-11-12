package com.restman.dao;

import com.restman.config.DatabaseConfig3;
import com.restman.model.Combo3;
import com.restman.model.Dish3;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComboDAOImpl3 implements ComboDAO3 {
    @Override
    public int createCombo(Combo3 combo) throws SQLException {
        String insertCombo = "INSERT INTO tblCombo3 (comboName, price, description, active) VALUES (?, ?, ?, TRUE)";
        String insertItem = "INSERT INTO tblComboItem3 (comboId, dishId, quantity) VALUES (?, ?, 1)";
        try (Connection connection = DatabaseConfig3.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int comboId;
                try (PreparedStatement statement = connection.prepareStatement(insertCombo, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, combo.getComboName().trim());
                    statement.setBigDecimal(2, combo.getPrice());
                    statement.setString(3, combo.getDescription());
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Không thể tạo combo");
                    }
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Không nhận được mã combo vừa tạo");
                        }
                        comboId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(insertItem)) {
                    for (Dish3 dish : combo.getDishes()) {
                        statement.setInt(1, comboId);
                        statement.setInt(2, dish.getId());
                        statement.addBatch();
                    }
                    if (!combo.getDishes().isEmpty()) {
                        statement.executeBatch();
                    }
                }
                connection.commit();
                return comboId;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<Combo3> findAll() throws SQLException {
        String sql = "SELECT c.id, c.comboName, c.price, c.description FROM tblCombo3 c ORDER BY c.createdAt DESC";
        List<Combo3> combos = new ArrayList<>();
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                combos.add(mapCombo(resultSet, connection));
            }
        }
        return combos;
    }

    @Override
    public Optional<Combo3> findById(int comboId) throws SQLException {
        String sql = "SELECT c.id, c.comboName, c.price, c.description FROM tblCombo3 c WHERE c.id = ?";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, comboId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapCombo(resultSet, connection)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean updateCombo(Combo3 combo) throws SQLException {
        String updateCombo = "UPDATE tblCombo3 SET comboName = ?, price = ?, description = ? WHERE id = ?";
        String deleteItems = "DELETE FROM tblComboItem3 WHERE comboId = ?";
        String insertItem = "INSERT INTO tblComboItem3 (comboId, dishId, quantity) VALUES (?, ?, 1)";

        try (Connection connection = DatabaseConfig3.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement update = connection.prepareStatement(updateCombo)) {
                    update.setString(1, combo.getComboName().trim());
                    update.setBigDecimal(2, combo.getPrice());
                    update.setString(3, combo.getDescription());
                    update.setInt(4, combo.getId());
                    if (update.executeUpdate() != 1) {
                        throw new SQLException("Không thể cập nhật combo");
                    }
                }

                try (PreparedStatement delete = connection.prepareStatement(deleteItems)) {
                    delete.setInt(1, combo.getId());
                    delete.executeUpdate();
                }

                if (!combo.getDishes().isEmpty()) {
                    try (PreparedStatement insert = connection.prepareStatement(insertItem)) {
                        for (Dish3 dish : combo.getDishes()) {
                            insert.setInt(1, combo.getId());
                            insert.setInt(2, dish.getId());
                            insert.addBatch();
                        }
                        insert.executeBatch();
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean deleteCombo(int comboId) throws SQLException {
        String sql = "DELETE FROM tblCombo3 WHERE id = ?";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, comboId);
            return statement.executeUpdate() == 1;
        }
    }

    private Combo3 mapCombo(ResultSet resultSet, Connection connection) throws SQLException {
        Combo3 combo = new Combo3();
        combo.setId(resultSet.getInt("id"));
        combo.setComboName(resultSet.getString("comboName"));
        combo.setPrice(resultSet.getBigDecimal("price"));
        combo.setDescription(resultSet.getString("description"));

        String sql = "SELECT d.id, d.dishName, d.price, d.description, d.category, d.imageUrl, d.available " +
                "FROM tblComboItem3 ci JOIN tblDish3 d ON d.id = ci.dishId WHERE ci.comboId = ? ORDER BY d.dishName";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, combo.getId());
            try (ResultSet dishSet = statement.executeQuery()) {
                List<Dish3> dishes = new ArrayList<>();
                while (dishSet.next()) {
                    dishes.add(new Dish3(
                            dishSet.getInt("id"),
                            dishSet.getString("dishName"),
                            dishSet.getBigDecimal("price"),
                            dishSet.getString("description"),
                            dishSet.getString("category"),
                            dishSet.getString("imageUrl"),
                            dishSet.getBoolean("available")
                    ));
                }
                combo.setDishes(dishes);
            }
        }
        return combo;
    }
}
