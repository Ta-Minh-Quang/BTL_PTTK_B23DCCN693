package com.restman.controller;

import com.restman.dao.DishDAO3;
import com.restman.dao.DishDAOImpl3;
import com.restman.model.Dish3;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SearchDishController3 {
    private final DishDAO3 dishDAO;

    public SearchDishController3() {
        this(new DishDAOImpl3());
    }

    public SearchDishController3(DishDAO3 dishDAO) {
        this.dishDAO = dishDAO;
    }

    public List<Dish3> searchDish(String keyword) throws SQLException {
        return dishDAO.searchDish(keyword);
    }

    public Optional<Dish3> getDishDetail(int dishId) throws SQLException {
        return dishDAO.findById(dishId);
    }
}
