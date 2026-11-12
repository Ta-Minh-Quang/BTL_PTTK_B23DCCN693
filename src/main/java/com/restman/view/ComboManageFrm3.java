package com.restman.view;

import com.restman.controller.ComboController3;
import com.restman.model.Combo3;
import com.restman.model.Dish3;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ComboManageFrm3 extends JFrame {
    private final JTextField txtComboName = new JTextField(22);
    private final JTextField txtComboPrice = new JTextField(14);
    private final JTextArea txtComboDescription = new JTextArea(4, 30);
    private final DefaultListModel<Dish3> selectedDishesModel = new DefaultListModel<>();
    private final JList<Dish3> selectedDishesList = new JList<>(selectedDishesModel);
    private final JButton btnFindDish = new JButton("Tìm và thêm món");
    private final JButton btnRemoveDish = new JButton("Bỏ món đang chọn");
    private final JButton btnCreateCombo = new JButton("Xác nhận tạo combo");
    private final ComboController3 controller = new ComboController3();
    private final JFrame parent;

    public ComboManageFrm3(JFrame parent) {
        super("Quản lý combo");
        this.parent = parent;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 540);
        setLocationRelativeTo(parent);

        JPanel fields = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Tên combo:"));
        fields.add(txtComboName);
        fields.add(new JLabel("Giá (VND):"));
        fields.add(txtComboPrice);
        fields.add(new JLabel("Mô tả:"));
        fields.add(new JScrollPane(txtComboDescription));
        fields.add(new JLabel("Danh sách món đã chọn:"));
        fields.add(new JScrollPane(selectedDishesList));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnFindDish);
        actions.add(btnRemoveDish);
        actions.add(btnCreateCombo);
        add(fields, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        btnFindDish.addActionListener(event -> findAndAddDish());
        btnRemoveDish.addActionListener(event -> removeDish());
        btnCreateCombo.addActionListener(event -> createCombo());
    }

    private void findAndAddDish() {
        String keyword = JOptionPane.showInputDialog(this, "Nhập tên món cần tìm:", "Tìm món", JOptionPane.QUESTION_MESSAGE);
        if (keyword == null) {
            return;
        }
        try {
            List<Dish3> results = controller.searchDish(keyword);
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy món phù hợp.", "Kết quả", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Dish3 selected = (Dish3) JOptionPane.showInputDialog(this, "Chọn món để thêm vào combo:",
                    "Kết quả tìm kiếm", JOptionPane.QUESTION_MESSAGE, null, results.toArray(), results.get(0));
            boolean alreadySelected = false;
            if (selected != null) {
                for (int index = 0; index < selectedDishesModel.size(); index++) {
                    if (selectedDishesModel.get(index).getId() == selected.getId()) {
                        alreadySelected = true;
                        break;
                    }
                }
            }
            if (selected != null && !alreadySelected) {
                selectedDishesModel.addElement(selected);
            }
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void removeDish() {
        int index = selectedDishesList.getSelectedIndex();
        if (index >= 0) {
            selectedDishesModel.remove(index);
        }
    }

    private void createCombo() {
        BigDecimal price;
        try {
            price = new BigDecimal(txtComboPrice.getText().trim());
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Giá combo phải là một số hợp lệ.", "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Dish3> dishes = new ArrayList<>();
        for (int index = 0; index < selectedDishesModel.size(); index++) {
            dishes.add(selectedDishesModel.get(index));
        }
        Combo3 combo = new Combo3(0, txtComboName.getText().trim(), price,
                txtComboDescription.getText().trim(), dishes);
        try {
            int comboId = controller.createCombo(combo);
            if (comboId > 0) {
                JOptionPane.showMessageDialog(this, "Tạo combo thành công. Mã combo: " + comboId,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void showDatabaseError(SQLException exception) {
        JOptionPane.showMessageDialog(this, "Không thể truy cập cơ sở dữ liệu: " + exception.getMessage(),
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
