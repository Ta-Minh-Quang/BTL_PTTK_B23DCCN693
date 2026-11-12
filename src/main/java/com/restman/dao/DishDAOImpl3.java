package com.restman.dao;

import com.restman.config.DatabaseConfig3;
import com.restman.model.Dish3;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DishDAOImpl3 implements DishDAO3 {
    private static final String SELECT_COLUMNS = "SELECT id, dishName, price, description, category, imageUrl, available FROM tblDish3 ";

    @Override
    public List<Dish3> searchDish(String keyword) throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE LOWER(dishName) LIKE LOWER(?) ORDER BY dishName";
        List<Dish3> dishes = new ArrayList<>();
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + (keyword == null ? "" : keyword.trim()) + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    dishes.add(mapDish(resultSet));
                }
            }
        }
        return dishes;
    }

    @Override
    public Optional<Dish3> findById(int dishId) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COLUMNS + "WHERE id = ?")) {
            statement.setInt(1, dishId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapDish(resultSet)) : Optional.empty();
            }
        }
    }

    private Dish3 mapDish(ResultSet resultSet) throws SQLException {
        return new Dish3(resultSet.getInt("id"), resultSet.getString("dishName"),
                resultSet.getBigDecimal("price"), resultSet.getString("description"),
                resultSet.getString("category"), resultSet.getString("imageUrl"),
                resultSet.getBoolean("available"));
    }
}
