package com.restman.dao;

import com.restman.model.Dish3;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DishDAO3 {
    List<Dish3> searchDish(String keyword) throws SQLException;
    Optional<Dish3> findById(int dishId) throws SQLException;
}
