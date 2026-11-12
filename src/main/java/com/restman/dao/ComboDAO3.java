package com.restman.dao;

import com.restman.model.Combo3;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ComboDAO3 {
    int createCombo(Combo3 combo) throws SQLException;
    List<Combo3> findAll() throws SQLException;
    Optional<Combo3> findById(int comboId) throws SQLException;
    boolean updateCombo(Combo3 combo) throws SQLException;
    boolean deleteCombo(int comboId) throws SQLException;
}
