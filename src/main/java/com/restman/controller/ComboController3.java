package com.restman.controller;

import com.restman.dao.ComboDAO3;
import com.restman.dao.ComboDAOImpl3;
import com.restman.dao.DishDAO3;
import com.restman.dao.DishDAOImpl3;
import com.restman.model.Combo3;
import com.restman.model.Dish3;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ComboController3 {
    private final ComboDAO3 comboDAO;
    private final DishDAO3 dishDAO;

    public ComboController3() {
        this(new ComboDAOImpl3(), new DishDAOImpl3());
    }

    public ComboController3(ComboDAO3 comboDAO, DishDAO3 dishDAO) {
        this.comboDAO = comboDAO;
        this.dishDAO = dishDAO;
    }

    public List<Dish3> searchDish(String keyword) throws SQLException {
        return dishDAO.searchDish(keyword);
    }

    public List<Combo3> getAllCombos() throws SQLException {
        return comboDAO.findAll();
    }

    public Optional<Combo3> getComboById(int comboId) throws SQLException {
        return comboDAO.findById(comboId);
    }

    public boolean validateCombo(Combo3 combo) {
        if (combo == null || combo.getDishes() == null || combo.getDishes().size() < 2) {
            return false;
        }
        if (combo.getComboName() == null || combo.getComboName().isBlank()) {
            return false;
        }
        if (combo.getPrice() == null || combo.getPrice().signum() <= 0) {
            return false;
        }
        return true;
    }

    public int createCombo(Combo3 combo) throws SQLException {
        if (!validateCombo(combo)) {
            return -1;
        }
        return comboDAO.createCombo(combo);
    }

    public boolean updateCombo(Combo3 combo) throws SQLException {
        if (!validateCombo(combo)) {
            return false;
        }
        return comboDAO.updateCombo(combo);
    }

    public boolean deleteCombo(int comboId) throws SQLException {
        return comboDAO.deleteCombo(comboId);
    }
}
