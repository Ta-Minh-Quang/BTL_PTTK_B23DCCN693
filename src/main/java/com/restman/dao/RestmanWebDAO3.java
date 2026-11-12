package com.restman.dao;

import com.restman.config.DatabaseConfig3;
import com.restman.model.Dish3;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestmanWebDAO3 {
    public List<Dish3> searchDishes(String keyword, String category) throws SQLException {
        String sql = "SELECT id, dishName, price, description, category, imageUrl, available FROM tblDish3 "
                + "WHERE LOWER(dishName) LIKE LOWER(?) AND (? = '' OR category = ?) ORDER BY dishName";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + (keyword == null ? "" : keyword.trim()) + "%");
            String categoryFilter = category == null ? "" : category.trim();
            statement.setString(2, categoryFilter);
            statement.setString(3, categoryFilter);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Dish3> dishes = new ArrayList<>();
                while (resultSet.next()) {
                    dishes.add(new Dish3(resultSet.getInt("id"), resultSet.getString("dishName"),
                            resultSet.getBigDecimal("price"), resultSet.getString("description"),
                            resultSet.getString("category"), resultSet.getString("imageUrl"),
                            resultSet.getBoolean("available")));
                }
                return dishes;
            }
        }
    }

    public List<String> getDishCategories() throws SQLException {
        return queryStrings("SELECT DISTINCT category FROM tblDish3 ORDER BY category", null, "category");
    }

    public void saveDish(Dish3 dish) throws SQLException {
        String sql = dish.getId() > 0
                ? "UPDATE tblDish3 SET dishName=?, price=?, description=?, category=?, imageUrl=?, available=? WHERE id=?"
                : "INSERT INTO tblDish3 (dishName, price, description, category, imageUrl, available) VALUES (?,?,?,?,?,?)";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindDish(statement, dish);
            if (dish.getId() > 0) {
                statement.setInt(7, dish.getId());
            }
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Không tìm thấy món ăn để cập nhật.");
            }
        }
    }

    public boolean deleteDish(int id) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM tblDish3 WHERE id=?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Map<String, Object>> getAvailableTables(LocalDateTime time) throws SQLException {
        String sql = "SELECT t.id, t.tableName, t.capacity FROM tblDiningTable3 t WHERE t.status='AVAILABLE' "
                + "AND NOT EXISTS (SELECT 1 FROM tblReservation3 r WHERE r.tableId=t.id "
                + "AND r.status IN ('PENDING','CONFIRMED') AND r.reservationTime BETWEEN ? AND ?) ORDER BY t.tableName";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            Timestamp start = Timestamp.valueOf(time.minusHours(2));
            Timestamp end = Timestamp.valueOf(time.plusHours(2));
            statement.setTimestamp(1, start);
            statement.setTimestamp(2, end);
            return readRows(statement);
        }
    }

    public List<Map<String, Object>> getDiningTables() throws SQLException {
        return query("SELECT id, tableName, capacity, status FROM tblDiningTable3 ORDER BY tableName", null);
    }

    public int getCustomerId(int userId) throws SQLException {
        return selectInt("SELECT id FROM tblCustomer3 WHERE userId=?", userId);
    }

    public int getSalesStaffId(int userId) throws SQLException {
        return selectInt("SELECT id FROM tblSalesStaff3 WHERE userId=?", userId);
    }

    public void createReservation(int customerId, int tableId, LocalDateTime time, int guests) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String check = "SELECT capacity FROM tblDiningTable3 WHERE id=? AND status='AVAILABLE' "
                        + "AND NOT EXISTS (SELECT 1 FROM tblReservation3 WHERE tableId=? "
                        + "AND status IN ('PENDING','CONFIRMED') AND reservationTime BETWEEN ? AND ? ) FOR UPDATE";
                try (PreparedStatement statement = connection.prepareStatement(check)) {
                    statement.setInt(1, tableId);
                    statement.setInt(2, tableId);
                    statement.setTimestamp(3, Timestamp.valueOf(time.minusHours(2)));
                    statement.setTimestamp(4, Timestamp.valueOf(time.plusHours(2)));
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || result.getInt("capacity") < guests) {
                            throw new SQLException("Bàn không còn trống hoặc không đủ chỗ.");
                        }
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO tblReservation3 (customerId, tableId, reservationTime, guestCount, status) VALUES (?,?,?,?, 'PENDING')")) {
                    statement.setInt(1, customerId);
                    statement.setInt(2, tableId);
                    statement.setTimestamp(3, Timestamp.valueOf(time));
                    statement.setInt(4, guests);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Map<String, Object>> getCartItems(Map<Integer, Integer> cart) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        if (cart == null || cart.isEmpty()) return items;
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT dishName, category, price, available FROM tblDish3 WHERE id=?")) {
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                if (entry.getValue() == null || entry.getValue() < 1) {
                    throw new SQLException("Số lượng trong giỏ hàng không hợp lệ.");
                }
                statement.setInt(1, entry.getKey());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) throw new SQLException("Một món trong giỏ hàng không còn tồn tại.");
                    BigDecimal price = result.getBigDecimal("price");
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("dishId", entry.getKey());
                    item.put("dishName", result.getString("dishName"));
                    item.put("category", result.getString("category"));
                    item.put("unitPrice", price);
                    item.put("quantity", entry.getValue());
                    item.put("lineTotal", price.multiply(BigDecimal.valueOf(entry.getValue())));
                    item.put("available", result.getBoolean("available"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public int createFoodOrder(Integer customerId, Integer salesStaffId, Integer tableId,
                               Map<Integer, Integer> cart, String notes, String orderStatus) throws SQLException {
        if (cart == null || cart.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 món ăn");
        if (!"PENDING".equals(orderStatus) && !"CONFIRMED".equals(orderStatus)) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ.");
        }
        try (Connection connection = DatabaseConfig3.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (tableId != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT id FROM tblDiningTable3 WHERE id=? FOR UPDATE")) {
                        statement.setInt(1, tableId);
                        try (ResultSet result = statement.executeQuery()) {
                            if (!result.next()) throw new SQLException("Không tìm thấy bàn được chọn.");
                        }
                    }
                }

                BigDecimal totalAmount = BigDecimal.ZERO;
                Map<Integer, BigDecimal> unitPrices = new LinkedHashMap<>();
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT price FROM tblDish3 WHERE id=? AND available=TRUE FOR UPDATE")) {
                    for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                        if (entry.getValue() == null || entry.getValue() < 1) {
                            throw new IllegalArgumentException("Số lượng món phải lớn hơn 0.");
                        }
                        statement.setInt(1, entry.getKey());
                        try (ResultSet result = statement.executeQuery()) {
                            if (!result.next()) throw new SQLException("Một món đã chọn không còn được phục vụ.");
                            BigDecimal price = result.getBigDecimal("price");
                            unitPrices.put(entry.getKey(), price);
                            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(entry.getValue())));
                        }
                    }
                }

                int orderId;
                String orderSql = "INSERT INTO tblFoodOrder3 "
                        + "(customerId, salesStaffId, tableId, orderDate, orderStatus, totalAmount, notes) "
                        + "VALUES (?,?,?,NOW(),?,?,?)";
                try (PreparedStatement statement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    if (customerId == null) statement.setNull(1, java.sql.Types.INTEGER);
                    else statement.setInt(1, customerId);
                    if (salesStaffId == null) statement.setNull(2, java.sql.Types.INTEGER);
                    else statement.setInt(2, salesStaffId);
                    if (tableId == null) statement.setNull(3, java.sql.Types.INTEGER);
                    else statement.setInt(3, tableId);
                    statement.setString(4, orderStatus);
                    statement.setBigDecimal(5, totalAmount);
                    statement.setString(6, notes == null || notes.isBlank() ? null : notes.trim());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không nhận được mã đơn hàng.");
                        orderId = keys.getInt(1);
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO tblOrderItem3 (orderId, dishId, quantity, unitPrice) VALUES (?,?,?,?)")) {
                    for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                        statement.setInt(1, orderId);
                        statement.setInt(2, entry.getKey());
                        statement.setInt(3, entry.getValue());
                        statement.setBigDecimal(4, unitPrices.get(entry.getKey()));
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                if (tableId != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE tblDiningTable3 SET status='OCCUPIED' WHERE id=?")) {
                        statement.setInt(1, tableId);
                        if (statement.executeUpdate() != 1) throw new SQLException("Không thể cập nhật trạng thái bàn.");
                    }
                }
                connection.commit();
                return orderId;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Map<String, Object>> getPendingReservations() throws SQLException {
        return query("SELECT r.id, r.reservationTime, r.guestCount, r.status, t.tableName, "
                + "CONCAT(c.firstName,' ',c.lastName) customerName FROM tblReservation3 r "
                + "JOIN tblDiningTable3 t ON t.id=r.tableId JOIN tblCustomer3 c ON c.id=r.customerId "
                + "WHERE r.status='PENDING' ORDER BY r.reservationTime", null);
    }

    public List<Map<String, Object>> getPendingOrders() throws SQLException {
        return query("SELECT o.id, o.orderDate, o.orderStatus, o.totalAmount, COALESCE(CONCAT(c.firstName,' ',c.lastName),'Khách tại bàn') customerName, "
                + "COALESCE(t.tableName,'Đơn trực tuyến') tableName, items.items "
                + "FROM tblFoodOrder3 o LEFT JOIN tblCustomer3 c ON c.id=o.customerId "
                + "LEFT JOIN tblDiningTable3 t ON t.id=o.tableId JOIN (SELECT i.orderId, "
                + "GROUP_CONCAT(CONCAT(d.dishName,' x',i.quantity) SEPARATOR ', ') items "
                + "FROM tblOrderItem3 i JOIN tblDish3 d ON d.id=i.dishId GROUP BY i.orderId) items ON items.orderId=o.id "
                + "WHERE o.orderStatus='PENDING' ORDER BY o.orderDate", null);
    }

    public boolean updateStatus(String tableName, int id, String fromStatus, String toStatus) throws SQLException {
        if (!"tblReservation3".equals(tableName) && !"tblFoodOrder3".equals(tableName)) {
            throw new IllegalArgumentException("Bảng trạng thái không hợp lệ.");
        }
        String statusColumn = "tblReservation3".equals(tableName) ? "status" : "orderStatus";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE " + tableName + " SET " + statusColumn + "=? WHERE id=? AND " + statusColumn + "=?")) {
            statement.setString(1, toStatus);
            statement.setInt(2, id);
            statement.setString(3, fromStatus);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Map<String, Object>> getOrdersForPayment() throws SQLException {
        return query("SELECT o.id, o.orderDate, COALESCE(t.tableName,'Đơn trực tuyến') tableName, "
                + "o.totalAmount total, itemSummary.items FROM tblFoodOrder3 o LEFT JOIN tblDiningTable3 t ON t.id=o.tableId "
                + "JOIN (SELECT i.orderId, GROUP_CONCAT(CONCAT(d.dishName,' x',i.quantity) SEPARATOR ', ') items "
                + "FROM tblOrderItem3 i JOIN tblDish3 d ON d.id=i.dishId GROUP BY i.orderId) itemSummary "
                + "ON itemSummary.orderId=o.id "
                + "WHERE o.orderStatus='CONFIRMED' AND NOT EXISTS (SELECT 1 FROM tblBill3 b WHERE b.foodOrderId=o.id) "
                + "ORDER BY o.orderDate", null);
    }

    public Map<String, Object> getOrderPaymentDetails(int orderId) throws SQLException {
        List<Map<String, Object>> orders = query("SELECT o.id, o.orderDate, o.totalAmount, o.notes, o.customerId, "
                + "COALESCE(NULLIF(TRIM(CONCAT_WS(' ',c.firstName,c.lastName)),''),u.username,'Khách lẻ') customerName, "
                + "COALESCE(t.tableName,'Đơn trực tuyến') tableName FROM tblFoodOrder3 o "
                + "LEFT JOIN tblCustomer3 c ON c.id=o.customerId "
                + "LEFT JOIN tblUser3 u ON u.id=c.userId "
                + "LEFT JOIN tblDiningTable3 t ON t.id=o.tableId "
                + "WHERE o.id=? AND o.orderStatus='CONFIRMED' "
                + "AND NOT EXISTS (SELECT 1 FROM tblBill3 b WHERE b.foodOrderId=o.id)", orderId);
        if (orders.isEmpty()) return null;
        Map<String, Object> order = orders.get(0);
        order.put("items", query("SELECT d.dishName, i.quantity, i.unitPrice, "
                + "(i.quantity*i.unitPrice) lineTotal FROM tblOrderItem3 i "
                + "JOIN tblDish3 d ON d.id=i.dishId WHERE i.orderId=? ORDER BY i.id", orderId));
        return order;
    }

    public List<Map<String, Object>> getBillHistory() throws SQLException {
        return query("SELECT b.id billId, b.billDate, b.foodOrderId, b.customerName, "
                + "b.customerId, b.membershipCardId, mc.cardNumber, b.discountAmount, b.totalAmount, "
                + "CASE b.paymentMethod WHEN 'CASH' THEN 'Tiền mặt' "
                + "WHEN 'VIETQR' THEN 'Chuyển khoản / VietQR' WHEN 'CARD' THEN 'Thẻ' "
                + "ELSE b.paymentMethod END paymentMethod, "
                + "COALESCE(NULLIF(s.fullName,''),u.username,'Không xác định') cashierName "
                + "FROM tblBill3 b JOIN tblFoodOrder3 o ON o.id=b.foodOrderId "
                + "LEFT JOIN tblMembershipCard3 mc ON mc.id=b.membershipCardId "
                + "LEFT JOIN tblSalesStaff3 s ON s.id=b.salesStaffId "
                + "LEFT JOIN tblUser3 u ON u.id=s.userId ORDER BY b.billDate DESC, b.id DESC", null);
    }

    public Map<String, Object> findMembershipCard(String lookup) throws SQLException {
        if (lookup == null || lookup.isBlank()) return null;
        try (Connection connection = DatabaseConfig3.getConnection()) {
            return findMembershipCard(connection, lookup.trim(), false);
        }
    }

    public Map<String, Object> createBill(int orderId, int salesStaffId, String paymentMethod,
                                          String customerName, String membershipLookup) throws SQLException {
        if (paymentMethod == null || !List.of("CASH", "VIETQR", "CARD").contains(paymentMethod)) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }
        try (Connection connection = DatabaseConfig3.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal originalAmount;
                Integer tableId;
                Integer orderCustomerId;
                String orderCustomerName;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT o.totalAmount, o.tableId, o.customerId, "
                                + "COALESCE(NULLIF(TRIM(CONCAT_WS(' ',c.firstName,c.lastName)),''),"
                                + "u.username,'Khách lẻ') customerName "
                                + "FROM tblFoodOrder3 o LEFT JOIN tblCustomer3 c ON c.id=o.customerId "
                                + "LEFT JOIN tblUser3 u ON u.id=c.userId "
                                + "WHERE o.id=? AND o.orderStatus='CONFIRMED' FOR UPDATE")) {
                    statement.setInt(1, orderId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || result.getBigDecimal("totalAmount") == null) {
                            throw new SQLException("Không tìm thấy đơn hàng cần thanh toán.");
                        }
                        originalAmount = result.getBigDecimal("totalAmount");
                        int value = result.getInt("tableId");
                        tableId = result.wasNull() ? null : value;
                        int customerValue = result.getInt("customerId");
                        orderCustomerId = result.wasNull() ? null : customerValue;
                        orderCustomerName = result.getString("customerName");
                    }
                }
                String billedCustomerName = orderCustomerId == null
                        ? normalizeCustomerName(customerName) : normalizeCustomerName(orderCustomerName);
                Integer billedCustomerId = orderCustomerId;
                Integer membershipCardId = null;
                BigDecimal discountAmount = BigDecimal.ZERO.setScale(2);
                int pointsEarned = 0;
                if (membershipLookup != null && !membershipLookup.isBlank()) {
                    Map<String, Object> membershipCard = findMembershipCard(connection, membershipLookup.trim(), true);
                    if (membershipCard == null) {
                        throw new IllegalArgumentException("Không tìm thấy thẻ thành viên hợp lệ.");
                    }
                    membershipCardId = (Integer) membershipCard.get("membershipCardId");
                    billedCustomerId = (Integer) membershipCard.get("customerId");
                    billedCustomerName = normalizeCustomerName((String) membershipCard.get("customerName"));
                    discountAmount = originalAmount.multiply(new BigDecimal("0.10"))
                            .setScale(2, RoundingMode.HALF_UP);
                }
                BigDecimal totalAmount = originalAmount.subtract(discountAmount);
                if (membershipCardId != null) {
                    pointsEarned = totalAmount.divideToIntegralValue(new BigDecimal("10000")).intValueExact();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO tblBill3 (foodOrderId, salesStaffId, billDate, customerName, customerId, "
                                + "membershipCardId, discountAmount, totalAmount, paymentMethod) "
                                + "VALUES (?,?,NOW(),?,?,?,?,?,?)")) {
                    statement.setInt(1, orderId);
                    statement.setInt(2, salesStaffId);
                    statement.setString(3, billedCustomerName);
                    if (billedCustomerId == null) statement.setNull(4, Types.INTEGER);
                    else statement.setInt(4, billedCustomerId);
                    if (membershipCardId == null) statement.setNull(5, Types.INTEGER);
                    else statement.setInt(5, membershipCardId);
                    statement.setBigDecimal(6, discountAmount);
                    statement.setBigDecimal(7, totalAmount);
                    statement.setString(8, paymentMethod);
                    if (statement.executeUpdate() != 1) throw new SQLException("Không thể lưu hóa đơn.");
                }
                if (membershipCardId != null && pointsEarned > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE tblMembershipCard3 SET points=points+? WHERE id=?")) {
                        statement.setInt(1, pointsEarned);
                        statement.setInt(2, membershipCardId);
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Không thể cộng điểm thành viên.");
                        }
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE tblFoodOrder3 SET orderStatus='PAID' WHERE id=?")) {
                    statement.setInt(1, orderId);
                    if (statement.executeUpdate() != 1) throw new SQLException("Không thể cập nhật trạng thái đơn hàng.");
                }
                if (tableId != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE tblDiningTable3 t SET status='AVAILABLE' WHERE t.id=? "
                                    + "AND NOT EXISTS (SELECT 1 FROM tblFoodOrder3 o WHERE o.tableId=t.id "
                                    + "AND o.id<>? AND o.orderStatus IN ('PENDING','CONFIRMED'))")) {
                        statement.setInt(1, tableId);
                        statement.setInt(2, orderId);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                Map<String, Object> paymentResult = new LinkedHashMap<>();
                paymentResult.put("customerName", billedCustomerName);
                paymentResult.put("membershipCardId", membershipCardId);
                paymentResult.put("discountAmount", discountAmount);
                paymentResult.put("totalAmount", totalAmount);
                paymentResult.put("pointsEarned", pointsEarned);
                return paymentResult;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private Map<String, Object> findMembershipCard(Connection connection, String lookup,
                                                   boolean lock) throws SQLException {
        String selection = "SELECT mc.id membershipCardId, mc.cardNumber, mc.cardTier, mc.points, "
                + "c.id customerId, COALESCE(NULLIF(TRIM(CONCAT_WS(' ',c.firstName,c.lastName)),''),"
                + "u.username) customerName FROM tblMembershipCard3 mc "
                + "JOIN tblCustomer3 c ON c.membershipCardId=mc.id "
                + "JOIN tblUser3 u ON u.id=c.userId WHERE ";
        String lockClause = lock ? " FOR UPDATE" : "";
        Map<String, Object> card = readMembershipCard(connection,
                selection + "mc.cardNumber=? LIMIT 1" + lockClause, lookup);
        if (card != null) return card;
        return readMembershipCard(connection, selection + "u.phone=? ORDER BY mc.id LIMIT 1" + lockClause, lookup);
    }

    private Map<String, Object> readMembershipCard(Connection connection, String sql,
                                                   String lookup) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lookup);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return null;
                Map<String, Object> card = new LinkedHashMap<>();
                card.put("membershipCardId", result.getInt("membershipCardId"));
                card.put("cardNumber", result.getString("cardNumber"));
                card.put("cardTier", result.getString("cardTier"));
                card.put("points", result.getInt("points"));
                card.put("customerId", result.getInt("customerId"));
                card.put("customerName", result.getString("customerName"));
                return card;
            }
        }
    }

    private String normalizeCustomerName(String customerName) {
        String normalized = customerName == null || customerName.isBlank() ? "Khách lẻ" : customerName.trim();
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Tên khách hàng không được vượt quá 100 ký tự.");
        }
        return normalized;
    }

    public List<Map<String, Object>> getSuppliers() throws SQLException {
        return query("SELECT id, supplierName, phone, email, address FROM tblSupplier3 ORDER BY supplierName", null);
    }

    public void saveSupplier(Integer id, String name, String phone, String email, String address) throws SQLException {
        String sql = id == null ? "INSERT INTO tblSupplier3 (supplierName,phone,email,address) VALUES (?,?,?,?)"
                : "UPDATE tblSupplier3 SET supplierName=?,phone=?,email=?,address=? WHERE id=?";
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name.trim());
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.setString(4, address);
            if (id != null) statement.setInt(5, id);
            if (statement.executeUpdate() != 1) throw new SQLException("Không tìm thấy nhà cung cấp.");
        }
    }

    public List<Map<String, Object>> getIngredients() throws SQLException {
        return query("SELECT i.id, i.ingredientName, i.unit, i.quantityInStock, i.supplierId, s.supplierName "
                + "FROM tblIngredient3 i JOIN tblSupplier3 s ON s.id=i.supplierId ORDER BY i.ingredientName", null);
    }

    public void restockIngredient(int id, BigDecimal quantity) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE tblIngredient3 SET quantityInStock=quantityInStock+? WHERE id=?")) {
            statement.setBigDecimal(1, quantity);
            statement.setInt(2, id);
            if (statement.executeUpdate() != 1) throw new SQLException("Không tìm thấy nguyên liệu.");
        }
    }

    public List<Map<String, Object>> getCustomers() throws SQLException {
        return query("SELECT c.id, CONCAT(c.firstName,' ',c.lastName) customerName, u.phone, u.email, "
                + "c.membershipCardId FROM tblCustomer3 c JOIN tblUser3 u ON u.id=c.userId ORDER BY customerName", null);
    }

    public List<Map<String, Object>> getMembershipCards() throws SQLException {
        return query("SELECT mc.id, mc.cardNumber, mc.cardTier, mc.points, mc.issueDate, "
                + "TRIM(CONCAT_WS(' ',c.firstName,c.lastName)) customerName, u.phone "
                + "FROM tblMembershipCard3 mc JOIN tblCustomer3 c ON c.membershipCardId=mc.id "
                + "JOIN tblUser3 u ON u.id=c.userId ORDER BY mc.id DESC", null);
    }

    public List<Map<String, Object>> getStatistics() throws SQLException {
        return query("SELECT 'Doanh thu' label, COALESCE(SUM(totalAmount),0) value FROM tblBill3 "
                + "UNION ALL SELECT 'Số món ăn', COUNT(*) FROM tblDish3 "
                + "UNION ALL SELECT 'Tồn kho nguyên liệu', COALESCE(SUM(quantityInStock),0) FROM tblIngredient3 "
                + "UNION ALL SELECT 'Khách hàng', COUNT(*) FROM tblCustomer3 "
                + "UNION ALL SELECT 'Nhà cung cấp', COUNT(*) FROM tblSupplier3", null);
    }

    public Map<String, Object> getOrderStatisticsSummary() throws SQLException {
        List<Map<String, Object>> rows = query("SELECT "
                + "(SELECT COUNT(*) FROM tblFoodOrder3) totalOrders, "
                + "(SELECT COALESCE(SUM(b.totalAmount),0) FROM tblBill3 b "
                + "JOIN tblFoodOrder3 o ON o.id=b.foodOrderId WHERE o.orderStatus='PAID') totalRevenue, "
                + "(SELECT COUNT(*) FROM tblFoodOrder3 WHERE orderStatus='PENDING') pendingOrders, "
                + "(SELECT COUNT(*) FROM tblFoodOrder3 WHERE orderStatus='PAID') paidOrders", null);
        return rows.get(0);
    }

    public List<Map<String, Object>> getOrderStatistics(String requestedStatus) throws SQLException {
        String status = requestedStatus != null && List.of("ALL", "PAID", "PENDING").contains(requestedStatus)
                ? requestedStatus : "ALL";
        String sql = "SELECT o.id orderId, o.orderDate, "
                + "COALESCE(NULLIF(TRIM(CONCAT_WS(' ',c.firstName,c.lastName)),''),u.username,'Khách tại bàn') customerName, "
                + "o.totalAmount, o.orderStatus, b.id billId, b.billDate, "
                + "CASE b.paymentMethod WHEN 'CASH' THEN 'Tiền mặt' "
                + "WHEN 'VIETQR' THEN 'Chuyển khoản / VietQR' WHEN 'CARD' THEN 'Thẻ' "
                + "ELSE b.paymentMethod END paymentMethod, "
                + "i.id itemId, d.dishName, i.quantity, i.unitPrice, (i.quantity*i.unitPrice) lineTotal "
                + "FROM tblFoodOrder3 o "
                + "LEFT JOIN tblCustomer3 c ON c.id=o.customerId "
                + "LEFT JOIN tblUser3 u ON u.id=c.userId "
                + "LEFT JOIN tblBill3 b ON b.foodOrderId=o.id "
                + "LEFT JOIN tblOrderItem3 i ON i.orderId=o.id "
                + "LEFT JOIN tblDish3 d ON d.id=i.dishId "
                + "WHERE (?='ALL' OR (?='PAID' AND o.orderStatus='PAID') "
                + "OR (?='PENDING' AND o.orderStatus='PENDING')) "
                + "ORDER BY o.orderDate DESC,o.id DESC,i.id";
        Map<Integer, Map<String, Object>> ordersById = new LinkedHashMap<>();
        Map<Integer, List<Map<String, Object>>> itemsByOrderId = new LinkedHashMap<>();
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, status);
            statement.setString(3, status);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int orderId = result.getInt("orderId");
                    if (!ordersById.containsKey(orderId)) {
                        Map<String, Object> order = new LinkedHashMap<>();
                        order.put("orderId", orderId);
                        order.put("orderDate", result.getTimestamp("orderDate"));
                        order.put("customerName", result.getString("customerName"));
                        order.put("totalAmount", result.getBigDecimal("totalAmount"));
                        order.put("orderStatus", result.getString("orderStatus"));
                        order.put("billId", result.getObject("billId"));
                        order.put("billDate", result.getTimestamp("billDate"));
                        order.put("paymentMethod", result.getString("paymentMethod"));
                        ordersById.put(orderId, order);
                        itemsByOrderId.put(orderId, new ArrayList<>());
                    }
                    if (result.getObject("itemId") != null) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("dishName", result.getString("dishName"));
                        item.put("quantity", result.getInt("quantity"));
                        item.put("unitPrice", result.getBigDecimal("unitPrice"));
                        item.put("lineTotal", result.getBigDecimal("lineTotal"));
                        itemsByOrderId.get(orderId).add(item);
                    }
                }
            }
        }
        for (Map.Entry<Integer, Map<String, Object>> entry : ordersById.entrySet()) {
            entry.getValue().put("items", itemsByOrderId.get(entry.getKey()));
        }
        return new ArrayList<>(ordersById.values());
    }

    public Map<String, Object> createMembershipCard(String fullName, String phone, String email,
                                                    String cardTier) throws SQLException {
        String customerName = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        String normalizedPhone = phone == null ? "" : phone.trim();
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedTier = cardTier == null ? "" : cardTier.trim().toUpperCase(java.util.Locale.ROOT);
        if (customerName.isEmpty() || customerName.length() > 160) {
            throw new IllegalArgumentException("Họ và tên khách hàng phải có từ 1 đến 160 ký tự.");
        }
        if (normalizedPhone.isEmpty() || normalizedPhone.length() > 20) {
            throw new IllegalArgumentException("Số điện thoại phải có từ 1 đến 20 ký tự.");
        }
        if (!normalizedEmail.isEmpty() && normalizedEmail.length() > 120) {
            throw new IllegalArgumentException("Email không được vượt quá 120 ký tự.");
        }
        if (!List.of("STANDARD", "VIP").contains(normalizedTier)) {
            throw new IllegalArgumentException("Hạng thẻ không hợp lệ.");
        }

        int lastSpace = customerName.lastIndexOf(' ');
        String firstName = lastSpace < 0 ? customerName : customerName.substring(0, lastSpace);
        String lastName = lastSpace < 0 ? customerName : customerName.substring(lastSpace + 1);
        if (firstName.length() > 80 || lastName.length() > 80) {
            throw new IllegalArgumentException("Họ và tên không được vượt quá 80 ký tự mỗi phần.");
        }

        try (Connection connection = DatabaseConfig3.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer customerId = null;
                Integer linkedCardId = null;
                Integer existingUserId = null;
                String existingRole = null;
                String findCustomer = "SELECT c.id customerId,c.userId,c.membershipCardId "
                        + "FROM tblCustomer3 c JOIN tblUser3 u ON u.id=c.userId "
                        + "WHERE u.phone=? ORDER BY c.id LIMIT 1 FOR UPDATE";
                try (PreparedStatement statement = connection.prepareStatement(findCustomer)) {
                    statement.setString(1, normalizedPhone);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            customerId = result.getInt("customerId");
                            existingUserId = result.getInt("userId");
                            linkedCardId = (Integer) result.getObject("membershipCardId");
                        }
                    }
                }

                if (customerId != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE tblCustomer3 SET firstName=?,lastName=? WHERE id=?")) {
                        statement.setString(1, firstName);
                        statement.setString(2, lastName);
                        statement.setInt(3, customerId);
                        statement.executeUpdate();
                    }
                    if (linkedCardId != null) {
                        throw new IllegalArgumentException("Khách hàng này đã sở hữu thẻ thành viên!");
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT id,role FROM tblUser3 WHERE phone=? ORDER BY id LIMIT 1 FOR UPDATE")) {
                        statement.setString(1, normalizedPhone);
                        try (ResultSet result = statement.executeQuery()) {
                            if (result.next()) {
                                existingUserId = result.getInt("id");
                                existingRole = result.getString("role");
                            }
                        }
                    }
                    if (existingUserId != null && !"CUSTOMER".equalsIgnoreCase(existingRole)) {
                        throw new IllegalArgumentException("Số điện thoại này đang thuộc tài khoản nhân viên.");
                    }
                    if (existingUserId == null) {
                        if (!normalizedEmail.isEmpty() && valueExists(connection,
                                "SELECT id FROM tblUser3 WHERE email=?", normalizedEmail)) {
                            throw new IllegalArgumentException("Email đã được sử dụng.");
                        }
                        String username = uniqueCustomerUsername(connection, normalizedPhone);
                        try (PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO tblUser3 (username,passwordHash,email,phone,role,status) "
                                        + "VALUES (?,?,?,?,'CUSTOMER','ACTIVE')", Statement.RETURN_GENERATED_KEYS)) {
                            statement.setString(1, username);
                            statement.setString(2, "123456");
                            if (normalizedEmail.isEmpty()) statement.setNull(3, Types.VARCHAR);
                            else statement.setString(3, normalizedEmail);
                            statement.setString(4, normalizedPhone);
                            statement.executeUpdate();
                            try (ResultSet keys = statement.getGeneratedKeys()) {
                                if (!keys.next()) throw new SQLException("Không nhận được mã tài khoản khách hàng.");
                                existingUserId = keys.getInt(1);
                            }
                        }
                    }
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO tblCustomer3 (userId,firstName,lastName,customerCode) "
                                    + "VALUES (?,?,?,NULL)", Statement.RETURN_GENERATED_KEYS)) {
                        statement.setInt(1, existingUserId);
                        statement.setString(2, firstName);
                        statement.setString(3, lastName);
                        statement.executeUpdate();
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("Không nhận được mã hồ sơ khách hàng.");
                            customerId = keys.getInt(1);
                        }
                    }
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE tblCustomer3 SET customerCode=CONCAT('KH',id) WHERE id=?")) {
                        statement.setInt(1, customerId);
                        statement.executeUpdate();
                    }
                }

                int cardId;
                String temporaryCode = "TMP-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO tblMembershipCard3 (cardNumber,cardTier,points,issueDate,discountRate) "
                                + "VALUES (?,?,0,NOW(),10.00)", Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, temporaryCode);
                    statement.setString(2, normalizedTier);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không nhận được mã thẻ thành viên.");
                        cardId = keys.getInt(1);
                    }
                }
                String cardCode = String.format(java.util.Locale.ROOT, "CARD%08d", cardId);
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE tblMembershipCard3 SET cardNumber=? WHERE id=?")) {
                    statement.setString(1, cardCode);
                    statement.setInt(2, cardId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE tblCustomer3 SET membershipCardId=? WHERE id=? AND membershipCardId IS NULL")) {
                    statement.setInt(1, cardId);
                    statement.setInt(2, customerId);
                    if (statement.executeUpdate() != 1) {
                        throw new IllegalArgumentException("Khách hàng này đã sở hữu thẻ thành viên!");
                    }
                }
                connection.commit();
                Map<String, Object> createdCard = new LinkedHashMap<>();
                createdCard.put("customerName", customerName);
                createdCard.put("cardNumber", cardCode);
                createdCard.put("cardTier", normalizedTier);
                return createdCard;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private String uniqueCustomerUsername(Connection connection, String phone) throws SQLException {
        String username = phone;
        int suffix = 2;
        while (valueExists(connection, "SELECT id FROM tblUser3 WHERE username=?", username)) {
            String suffixText = "_" + suffix++;
            username = phone.substring(0, Math.min(phone.length(), Math.max(1, 60 - suffixText.length()))) + suffixText;
        }
        return username;
    }

    private boolean valueExists(Connection connection, String sql, String value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public int count(String tableName) throws SQLException {
        List<String> tables = List.of("tblDish3", "tblCombo3", "tblCustomer3", "tblSupplier3", "tblIngredient3");
        if (!tables.contains(tableName)) throw new IllegalArgumentException("Bảng thống kê không hợp lệ.");
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private void bindDish(PreparedStatement statement, Dish3 dish) throws SQLException {
        statement.setString(1, dish.getDishName().trim());
        statement.setBigDecimal(2, dish.getPrice());
        statement.setString(3, dish.getDescription());
        statement.setString(4, dish.getCategory().trim());
        statement.setString(5, dish.getImageUrl());
        statement.setBoolean(6, dish.isAvailable());
    }

    private int selectInt(String sql, int parameter) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("Không tìm thấy thông tin khách hàng.");
                return resultSet.getInt(1);
            }
        }
    }

    private List<String> queryStrings(String sql, Integer parameter, String column) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) statement.setInt(1, parameter);
            try (ResultSet result = statement.executeQuery()) {
                List<String> values = new ArrayList<>();
                while (result.next()) values.add(result.getString(column));
                return values;
            }
        }
    }

    private List<Map<String, Object>> query(String sql, Integer parameter) throws SQLException {
        try (Connection connection = DatabaseConfig3.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) statement.setInt(1, parameter);
            return readRows(statement);
        }
    }

    private List<Map<String, Object>> readRows(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            int columns = result.getMetaData().getColumnCount();
            while (result.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columns; i++) row.put(result.getMetaData().getColumnLabel(i), result.getObject(i));
                rows.add(row);
            }
            return rows;
        }
    }

    private void rollback(Connection connection, Exception exception) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            exception.addSuppressed(rollbackException);
        }
    }
}
