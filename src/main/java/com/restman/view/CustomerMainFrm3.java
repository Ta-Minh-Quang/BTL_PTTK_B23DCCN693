package com.restman.view;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class CustomerMainFrm3 extends JFrame {
    public CustomerMainFrm3() {
        super("RestMan - Quản lý nhà hàng");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 260);
        setLocationRelativeTo(null);

        JLabel heading = new JLabel("HỆ THỐNG QUẢN LÝ NHÀ HÀNG RESTMAN", SwingConstants.CENTER);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        JButton searchDishButton = new JButton("Tìm kiếm món ăn");
        JButton comboButton = new JButton("Quản lý combo");
        JPanel actions = new JPanel();
        actions.add(searchDishButton);
        actions.add(comboButton);
        add(heading, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        searchDishButton.addActionListener(event -> new SearchDishFrm3(this).setVisible(true));
        comboButton.addActionListener(event -> new ComboManageFrm3(this).setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerMainFrm3().setVisible(true));
    }
}
