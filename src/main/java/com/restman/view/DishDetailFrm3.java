package com.restman.view;

import com.restman.model.Dish3;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URI;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DishDetailFrm3 extends JFrame {
    private final JButton btnBack = new JButton("Quay lại");

    public DishDetailFrm3(JFrame parent, Dish3 dish) {
        super("Chi tiết món ăn");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 420);
        setLocationRelativeTo(parent);

        JPanel details = new JPanel(new GridLayout(0, 1, 4, 4));
        details.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        details.add(new JLabel("Tên món: " + dish.getDishName()));
        details.add(new JLabel("Giá: " + dish.getPrice() + " VND"));
        details.add(new JLabel("Thể loại: " + dish.getCategory()));
        details.add(new JLabel("Tình trạng: " + (dish.isAvailable() ? "Đang phục vụ" : "Tạm hết")));
        JTextArea description = new JTextArea(dish.getDescription() == null ? "" : dish.getDescription());
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        details.add(new JScrollPane(description));
        JLabel imageLabel = new JLabel("Chưa có hình ảnh", JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(200, 150));
        if (dish.getImageUrl() != null && !dish.getImageUrl().isBlank()) {
            try {
                imageLabel.setText("");
                imageLabel.setIcon(new ImageIcon(URI.create(dish.getImageUrl()).toURL()));
            } catch (MalformedURLException | IllegalArgumentException exception) {
                imageLabel.setText("Đường dẫn hình ảnh không hợp lệ");
            }
        }
        add(details, BorderLayout.NORTH);
        add(imageLabel, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.add(btnBack);
        add(bottom, BorderLayout.SOUTH);
        btnBack.addActionListener(event -> dispose());
    }
}
