package com.restman.view;

import com.restman.controller.SearchDishController3;
import com.restman.model.Dish3;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class SearchDishFrm3 extends JFrame {
    private final JTextField txtKeywordInput = new JTextField(24);
    private final JButton btnSearch = new JButton("Tìm kiếm");
    private final JTable tblDishListOutput = new JTable();
    private final JButton btnViewDetail = new JButton("Xem chi tiết");
    private final SearchDishController3 controller = new SearchDishController3();
    private final JFrame parent;
    private List<Dish3> currentDishes = List.of();

    public SearchDishFrm3(JFrame parent) {
        super("Tìm kiếm món ăn");
        this.parent = parent;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(760, 440);
        setLocationRelativeTo(parent);
        tblDishListOutput.setModel(new DefaultTableModel(new Object[]{"Mã món", "Tên món", "Giá (VND)", "Thể loại", "Còn phục vụ"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Từ khóa:"));
        searchPanel.add(txtKeywordInput);
        searchPanel.add(btnSearch);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnViewDetail);
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(tblDishListOutput), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnSearch.addActionListener(event -> search());
        txtKeywordInput.addActionListener(event -> search());
        btnViewDetail.addActionListener(event -> viewDetail());
        search();
    }

    private void search() {
        try {
            currentDishes = controller.searchDish(txtKeywordInput.getText());
            DefaultTableModel model = (DefaultTableModel) tblDishListOutput.getModel();
            model.setRowCount(0);
            for (Dish3 dish : currentDishes) {
                model.addRow(new Object[]{dish.getId(), dish.getDishName(), dish.getPrice(),
                        dish.getCategory(), dish.isAvailable() ? "Có" : "Không"});
            }
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void viewDetail() {
        int selectedRow = tblDishListOutput.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDishes.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món ăn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Dish3 selectedDish = currentDishes.get(tblDishListOutput.convertRowIndexToModel(selectedRow));
        try {
            controller.getDishDetail(selectedDish.getId())
                    .ifPresentOrElse(dish -> new DishDetailFrm3(this, dish).setVisible(true),
                            () -> JOptionPane.showMessageDialog(this, "Không tìm thấy món ăn.", "Thông báo", JOptionPane.WARNING_MESSAGE));
        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    private void showDatabaseError(SQLException exception) {
        JOptionPane.showMessageDialog(this, "Không thể truy cập cơ sở dữ liệu: " + exception.getMessage(),
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
