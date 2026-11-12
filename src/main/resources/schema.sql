CREATE DATABASE IF NOT EXISTS restman3
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE restman3;

CREATE TABLE IF NOT EXISTS tblMembershipCard3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cardNumber VARCHAR(30) NOT NULL UNIQUE,
    cardTier VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    points INT NOT NULL DEFAULT 0,
    issueDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discountRate DECIMAL(5,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblUser3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    passwordHash VARCHAR(255) NOT NULL,
    email VARCHAR(120) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblCustomer3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL UNIQUE,
    firstName VARCHAR(80) NOT NULL,
    lastName VARCHAR(80) NOT NULL,
    customerCode VARCHAR(20) UNIQUE,
    dateOfBirth DATE,
    street VARCHAR(150),
    ward VARCHAR(100),
    district VARCHAR(100),
    city VARCHAR(100),
    membershipCardId INT UNIQUE,
    CONSTRAINT fk_customer_user3 FOREIGN KEY (userId) REFERENCES tblUser3(id),
    CONSTRAINT fk_customer_card3 FOREIGN KEY (membershipCardId) REFERENCES tblMembershipCard3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblManager3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL UNIQUE,
    fullName VARCHAR(160) NOT NULL,
    CONSTRAINT fk_manager_user3 FOREIGN KEY (userId) REFERENCES tblUser3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblSalesStaff3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL UNIQUE,
    fullName VARCHAR(160) NOT NULL,
    CONSTRAINT fk_sales_user3 FOREIGN KEY (userId) REFERENCES tblUser3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblInventoryStaff3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL UNIQUE,
    fullName VARCHAR(160) NOT NULL,
    CONSTRAINT fk_inventory_user3 FOREIGN KEY (userId) REFERENCES tblUser3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblSupplier3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplierName VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(120),
    address VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblIngredient3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    supplierId INT NOT NULL,
    ingredientName VARCHAR(120) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    quantityInStock DECIMAL(12,3) NOT NULL DEFAULT 0,
    CONSTRAINT fk_ingredient_supplier3 FOREIGN KEY (supplierId) REFERENCES tblSupplier3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDish3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    dishName VARCHAR(150) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    description TEXT,
    category VARCHAR(80) NOT NULL,
    imageUrl VARCHAR(500),
    available BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_dish_price3 CHECK (price >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDishIngredient3 (
    dishId INT NOT NULL,
    ingredientId INT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL,
    PRIMARY KEY (dishId, ingredientId),
    CONSTRAINT fk_dishingredient_dish3 FOREIGN KEY (dishId) REFERENCES tblDish3(id),
    CONSTRAINT fk_dishingredient_ingredient3 FOREIGN KEY (ingredientId) REFERENCES tblIngredient3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblCombo3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    comboName VARCHAR(150) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_combo_price3 CHECK (price > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblComboItem3 (
    comboId INT NOT NULL,
    dishId INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    PRIMARY KEY (comboId, dishId),
    CONSTRAINT chk_comboitem_quantity3 CHECK (quantity > 0),
    CONSTRAINT fk_comboitem_combo3 FOREIGN KEY (comboId) REFERENCES tblCombo3(id) ON DELETE CASCADE,
    CONSTRAINT fk_comboitem_dish3 FOREIGN KEY (dishId) REFERENCES tblDish3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblDiningTable3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tableName VARCHAR(40) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT chk_table_capacity3 CHECK (capacity > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblReservation3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customerId INT NOT NULL,
    tableId INT NOT NULL,
    reservationTime DATETIME NOT NULL,
    guestCount INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_reservation_customer3 FOREIGN KEY (customerId) REFERENCES tblCustomer3(id),
    CONSTRAINT fk_reservation_table3 FOREIGN KEY (tableId) REFERENCES tblDiningTable3(id),
    CONSTRAINT chk_reservation_guests3 CHECK (guestCount > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblFoodOrder3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customerId INT,
    salesStaffId INT,
    tableId INT,
    orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    orderStatus VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    totalAmount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    notes VARCHAR(500),
    CONSTRAINT fk_order_customer3 FOREIGN KEY (customerId) REFERENCES tblCustomer3(id),
    CONSTRAINT fk_order_sales3 FOREIGN KEY (salesStaffId) REFERENCES tblSalesStaff3(id),
    CONSTRAINT fk_order_table3 FOREIGN KEY (tableId) REFERENCES tblDiningTable3(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblOrderItem3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderId INT NOT NULL,
    dishId INT,
    comboId INT,
    quantity INT NOT NULL,
    unitPrice DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_orderitem_order3 FOREIGN KEY (orderId) REFERENCES tblFoodOrder3(id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_dish3 FOREIGN KEY (dishId) REFERENCES tblDish3(id),
    CONSTRAINT fk_orderitem_combo3 FOREIGN KEY (comboId) REFERENCES tblCombo3(id),
    CONSTRAINT chk_orderitem_product3 CHECK ((dishId IS NOT NULL AND comboId IS NULL) OR (dishId IS NULL AND comboId IS NOT NULL)),
    CONSTRAINT chk_orderitem_quantity3 CHECK (quantity > 0),
    CONSTRAINT chk_orderitem_price3 CHECK (unitPrice >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tblBill3 (
    id INT AUTO_INCREMENT PRIMARY KEY,
    foodOrderId INT NOT NULL UNIQUE,
    salesStaffId INT NOT NULL,
    customerName VARCHAR(100) NOT NULL DEFAULT 'Khách lẻ',
    customerId INT NULL,
    membershipCardId INT NULL,
    billDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discountAmount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    totalAmount DECIMAL(12,2) NOT NULL,
    paymentMethod VARCHAR(30) NOT NULL,
    CONSTRAINT fk_bill_foodorder3 FOREIGN KEY (foodOrderId) REFERENCES tblFoodOrder3(id),
    CONSTRAINT fk_bill_salesstaff3 FOREIGN KEY (salesStaffId) REFERENCES tblSalesStaff3(id),
    CONSTRAINT fk_bill_customer3 FOREIGN KEY (customerId) REFERENCES tblCustomer3(id),
    CONSTRAINT fk_bill_membership3 FOREIGN KEY (membershipCardId) REFERENCES tblMembershipCard3(id),
    CONSTRAINT chk_bill_amount3 CHECK (totalAmount >= 0)
) ENGINE=InnoDB;

INSERT INTO tblSupplier3 (id, supplierName, phone, email, address) VALUES
(1, 'Nông sản Đà Lạt', '02633881234', 'contact@dalatfarm.example', 'Đà Lạt, Lâm Đồng'),
(2, 'Hải sản Biển Xanh', '02583885678', 'sales@bienxanh.example', 'Nha Trang, Khánh Hòa')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblMembershipCard3 (id, cardNumber, cardTier, points, issueDate, discountRate) VALUES
(1, 'RESTMAN-DEMO-0001', 'Bạc', 120, CURRENT_DATE, 10.00),
(2, 'CARD001', 'Vàng', 350, CURRENT_DATE, 10.00),
(3, 'CARD002', 'Kim cương', 820, CURRENT_DATE, 10.00)
ON DUPLICATE KEY UPDATE cardTier = VALUES(cardTier), discountRate = VALUES(discountRate);

INSERT INTO tblIngredient3 (id, supplierId, ingredientName, unit, quantityInStock) VALUES
(1, 1, 'Thịt bò', 'kg', 30.000),
(2, 1, 'Rau sống', 'kg', 18.000),
(3, 2, 'Tôm sú', 'kg', 12.000),
(4, 1, 'Gạo thơm', 'kg', 45.000),
(5, 2, 'Cá basa', 'kg', 16.000)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblDiningTable3 (id, tableName, capacity, status) VALUES
(1, 'Bàn 01', 2, 'AVAILABLE'),
(2, 'Bàn 02', 4, 'AVAILABLE'),
(3, 'Bàn 03', 6, 'AVAILABLE'),
(4, 'Bàn 04', 4, 'AVAILABLE')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblDish3 (id, dishName, price, description, category, imageUrl, available) VALUES
(1, 'Phở bò đặc biệt', 75000, 'Phở bò truyền thống với thịt bò và nước dùng ninh xương.', 'Món chính', 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=900&q=80', TRUE),
(2, 'Gỏi cuốn tôm thịt', 45000, 'Gỏi cuốn tươi với tôm, thịt, rau sống và nước chấm.', 'Khai vị', 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80', TRUE),
(3, 'Cơm gà nướng', 68000, 'Cơm nóng ăn kèm gà nướng mật ong và rau củ.', 'Món chính', 'https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?auto=format&fit=crop&w=900&q=80', TRUE),
(4, 'Chè sen long nhãn', 30000, 'Chè hạt sen long nhãn thanh mát.', 'Tráng miệng', 'https://images.unsplash.com/photo-1551024601-bec78aea704b?auto=format&fit=crop&w=900&q=80', TRUE),
(5, 'Trà đào cam sả', 35000, 'Trà đào tươi kết hợp cam và sả.', 'Đồ uống', 'https://images.unsplash.com/photo-1546173159-315724a31696?auto=format&fit=crop&w=900&q=80', TRUE),
(6, 'Bún chả Hà Nội', 65000, 'Bún chả thơm nức cùng chả nướng, rau thơm và nước chấm đặc trưng.', 'Món chính', 'https://images.unsplash.com/photo-1559847844-5315695dadae?auto=format&fit=crop&w=900&q=80', TRUE),
(7, 'Cá kho tộ', 98000, 'Cá kho đậm đà, ăn cùng cơm trắng nóng.', 'Món chính', 'https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=900&q=80', TRUE),
(8, 'Salad hoa quả', 52000, 'Salad hoa quả tươi mát, bổ dưỡng.', 'Khai vị', 'https://images.unsplash.com/photo-1546793665-c74683f339c1?auto=format&fit=crop&w=900&q=80', TRUE),
(9, 'Bánh flan caramel', 28000, 'Bánh flan mềm mịn với lớp caramel ngọt.', 'Tráng miệng', 'https://images.unsplash.com/photo-1551024506-0bccd828d307?auto=format&fit=crop&w=900&q=80', TRUE),
(10, 'Cà phê sữa đá', 25000, 'Cà phê sữa đá thơm ngon, phù hợp cho mọi bữa ăn.', 'Đồ uống', 'https://images.unsplash.com/photo-1497636577773-f1231844b336?auto=format&fit=crop&w=900&q=80', TRUE),
(11, 'Nem rán Hà Nội', 58000, 'Nem rán vàng giòn với nhân thịt heo, miến, mộc nhĩ và rau thơm, dùng kèm nước mắm chua ngọt.', 'Khai vị', 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80', TRUE),
(12, 'Súp cua măng tây', 52000, 'Súp nóng nấu từ thịt cua, măng tây non và trứng, vị thanh nhẹ, thích hợp dùng khai vị.', 'Khai vị', 'https://images.unsplash.com/photo-1547592166-23ac45744acd?auto=format&fit=crop&w=900&q=80', TRUE),
(13, 'Khoai tây lắc phô mai', 39000, 'Khoai tây chiên giòn lắc phô mai thơm béo, phục vụ nóng.', 'Khai vị', 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?auto=format&fit=crop&w=900&q=80', TRUE),
(14, 'Gỏi ngó sen tôm thịt', 65000, 'Ngó sen giòn trộn tôm, thịt luộc, rau thơm và nước mắm chua ngọt.', 'Khai vị', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=900&q=80', TRUE),
(15, 'Cơm tấm sườn bì chả', 79000, 'Cơm tấm thơm ăn cùng sườn nướng, bì, chả trứng, đồ chua và nước mắm.', 'Món chính', 'https://images.unsplash.com/photo-1512058564366-18510be2db19?auto=format&fit=crop&w=900&q=80', TRUE),
(16, 'Bò lúc lắc khoai tây', 135000, 'Thịt bò mềm áp chảo với hành tây, ớt chuông và khoai tây chiên.', 'Món chính', 'https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=900&q=80', TRUE),
(17, 'Mì Quảng gà ta', 72000, 'Mì Quảng sợi dai với gà ta, nước dùng đậm vị, đậu phộng rang và bánh tráng.', 'Món chính', 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?auto=format&fit=crop&w=900&q=80', TRUE),
(18, 'Cá hồi áp chảo sốt chanh dây', 168000, 'Phi lê cá hồi áp chảo vừa chín tới, dùng cùng sốt chanh dây và rau củ theo mùa.', 'Món chính', 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?auto=format&fit=crop&w=900&q=80', TRUE),
(19, 'Lẩu Thái hải sản', 299000, 'Nồi lẩu chua cay với tôm, mực, cá, nấm và rau tươi, phù hợp cho nhóm 2–3 người.', 'Lẩu & Nướng', 'https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=900&q=80', TRUE),
(20, 'Lẩu riêu cua bắp bò', 329000, 'Lẩu riêu cua đồng ăn kèm bắp bò, đậu phụ, bún tươi và rau theo mùa.', 'Lẩu & Nướng', 'https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=900&q=80', TRUE),
(21, 'Ba chỉ bò Mỹ nướng sốt tiêu', 159000, 'Ba chỉ bò Mỹ thái lát nướng thơm, dùng với sốt tiêu đen và rau cuốn.', 'Lẩu & Nướng', 'https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&w=900&q=80', TRUE),
(22, 'Sườn non nướng BBQ', 189000, 'Sườn non ướp sốt BBQ, nướng chậm mềm mọng và phủ mè rang.', 'Lẩu & Nướng', 'https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?auto=format&fit=crop&w=900&q=80', TRUE),
(23, 'Nước ép cam tươi', 39000, 'Cam tươi ép nguyên chất, không thêm chất bảo quản, có thể yêu cầu điều chỉnh lượng đá.', 'Đồ uống', 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?auto=format&fit=crop&w=900&q=80', TRUE),
(24, 'Trà chanh sả mật ong', 32000, 'Trà thanh mát pha chanh tươi, sả và mật ong, phục vụ lạnh.', 'Đồ uống', 'https://images.unsplash.com/photo-1513558161293-cdaf765edfd7?auto=format&fit=crop&w=900&q=80', TRUE),
(25, 'Sinh tố bơ sữa', 45000, 'Bơ chín xay cùng sữa tươi tạo vị béo mịn, thơm dịu.', 'Đồ uống', 'https://images.unsplash.com/photo-1505252585461-04db1eb84625?auto=format&fit=crop&w=900&q=80', TRUE),
(26, 'Soda việt quất', 42000, 'Soda mát lạnh kết hợp si-rô việt quất và lát chanh tươi.', 'Đồ uống', 'https://images.unsplash.com/photo-1513558161293-cdaf765edfd7?auto=format&fit=crop&w=900&q=80', TRUE),
(27, 'Chè khúc bạch hạnh nhân', 38000, 'Khúc bạch mềm mát dùng với nhãn, hạnh nhân rang và nước đường thanh.', 'Tráng miệng', 'https://images.unsplash.com/photo-1551024601-bec78aea704b?auto=format&fit=crop&w=900&q=80', TRUE),
(28, 'Panna cotta chanh dây', 42000, 'Panna cotta béo nhẹ phủ sốt chanh dây chua ngọt.', 'Tráng miệng', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=900&q=80', TRUE),
(29, 'Bánh chuối nướng cốt dừa', 35000, 'Bánh chuối nướng thơm mềm, dùng cùng nước cốt dừa béo nhẹ.', 'Tráng miệng', 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?auto=format&fit=crop&w=900&q=80', TRUE),
(30, 'Trái cây tươi theo mùa', 45000, 'Đĩa trái cây tươi được tuyển chọn theo mùa, sơ chế và phục vụ trong ngày.', 'Tráng miệng', 'https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?auto=format&fit=crop&w=900&q=80', TRUE)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblUser3 (id, username, passwordHash, email, phone, role, status) VALUES
(1, 'manager', 'manager123', 'manager@restman.vn', '0901111001', 'MANAGER', 'ACTIVE'),
(2, 'inventory', 'inventory123', 'inventory@restman.vn', '0901111002', 'INVENTORYSTAFF', 'ACTIVE'),
(3, 'sales', 'sales123', 'sales@restman.vn', '0901111003', 'SALESSTAFF', 'ACTIVE'),
(4, 'customer', 'customer123', 'customer@restman.vn', '0901111004', 'CUSTOMER', 'ACTIVE')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblUser3 (username, passwordHash, email, phone, role, status) VALUES
('cardholder01', 'customer123', 'cardholder01@restman.vn', '0901111005', 'CUSTOMER', 'ACTIVE'),
('cardholder02', 'customer123', 'cardholder02@restman.vn', '0901111006', 'CUSTOMER', 'ACTIVE'),
('newcustomer', 'customer123', 'newcustomer@restman.vn', '0901111007', 'CUSTOMER', 'ACTIVE')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblManager3 (id, userId, fullName) VALUES
(1, 1, 'Nguyễn Văn Quản Lý')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblInventoryStaff3 (id, userId, fullName) VALUES
(1, 2, 'Trần Thị Kho')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblSalesStaff3 (id, userId, fullName) VALUES
(1, 3, 'Lê Hoàng Bán Hàng')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblCustomer3 (id, userId, firstName, lastName, customerCode, dateOfBirth, street, ward, district, city, membershipCardId) VALUES
(1, 4, 'Phạm', 'An', 'KH1', '1994-08-17', '12 Lê Lợi', 'Bến Nghé', 'Quận 1', 'TP.HCM', NULL)
ON DUPLICATE KEY UPDATE id = id;

UPDATE tblCustomer3 SET membershipCardId = 1 WHERE id = 1 AND membershipCardId IS NULL;

INSERT INTO tblCustomer3 (userId, firstName, lastName, customerCode, membershipCardId)
SELECT u.id, 'Trần', 'Lan', 'KH-DEMO-2', 2
FROM tblUser3 u WHERE u.username = 'cardholder01'
ON DUPLICATE KEY UPDATE membershipCardId = 2;

INSERT INTO tblCustomer3 (userId, firstName, lastName, customerCode, membershipCardId)
SELECT u.id, 'Lê', 'Minh', 'KH-DEMO-3', 3
FROM tblUser3 u WHERE u.username = 'cardholder02'
ON DUPLICATE KEY UPDATE membershipCardId = 3;

INSERT INTO tblCustomer3 (userId, firstName, lastName, customerCode)
SELECT u.id, 'Nguyễn', 'Bình', 'KH-DEMO-4'
FROM tblUser3 u WHERE u.username = 'newcustomer'
ON DUPLICATE KEY UPDATE userId = userId;

INSERT INTO tblCombo3 (id, comboName, price, description, active) VALUES
(1, 'Combo Gia Đình', 155000, 'Gồm 2 món chính và 1 đồ uống cho cả gia đình.', TRUE),
(2, 'Combo Trưa Năng Động', 125000, 'Combo tiết kiệm cho bữa trưa nhanh gọn.', TRUE),
(3, 'Combo Tiệc Nhóm', 599000, 'Bữa tiệc nướng dành cho nhóm bạn với món khai vị, thịt nướng và đồ uống.', TRUE),
(4, 'Combo Cặp Đôi', 249000, 'Bữa ăn dành cho hai người gồm món chính, món khai vị và tráng miệng.', TRUE),
(5, 'Combo Lẩu Cuối Tuần', 389000, 'Nồi lẩu hải sản dùng chung với món khai vị và đồ uống mát lạnh.', TRUE)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblComboItem3 (comboId, dishId, quantity) VALUES
(1, 1, 1),
(1, 3, 1),
(1, 5, 1),
(2, 6, 1),
(2, 8, 1),
(2, 10, 1),
(3, 14, 1),
(3, 21, 2),
(3, 22, 1),
(3, 24, 2),
(4, 12, 1),
(4, 17, 2),
(4, 28, 2),
(5, 19, 1),
(5, 13, 1),
(5, 23, 2)
ON DUPLICATE KEY UPDATE comboId = comboId;

INSERT INTO tblDishIngredient3 (dishId, ingredientId, quantity) VALUES
(1, 1, 0.180),
(1, 2, 0.050),
(3, 4, 0.200),
(6, 1, 0.150),
(7, 5, 0.250)
ON DUPLICATE KEY UPDATE dishId = dishId;

INSERT INTO tblReservation3 (id, customerId, tableId, reservationTime, guestCount, status) VALUES
(1, 1, 2, TIMESTAMP(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '18:30:00'), 3, 'PENDING')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblFoodOrder3 (id, customerId, salesStaffId, tableId, orderDate, orderStatus, totalAmount, notes) VALUES
(1, 1, NULL, NULL, CURRENT_TIMESTAMP, 'PENDING', 75000, NULL),
(2, NULL, 1, 1, CURRENT_TIMESTAMP, 'CONFIRMED', 136000, 'Đơn mẫu tại bàn')
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblOrderItem3 (id, orderId, dishId, comboId, quantity, unitPrice) VALUES
(1, 1, 1, NULL, 1, 75000),
(2, 2, 3, NULL, 2, 68000)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO tblBill3 (id, foodOrderId, salesStaffId, billDate, totalAmount, paymentMethod) VALUES
(1, 2, 1, CURRENT_TIMESTAMP, 136000, 'CASH')
ON DUPLICATE KEY UPDATE id = id;
