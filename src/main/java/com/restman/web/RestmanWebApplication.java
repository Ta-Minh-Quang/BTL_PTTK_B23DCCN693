package com.restman.web;

import com.restman.config.DatabaseConfig3;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestmanWebApplication {
    public static void main(String[] args) {
        initializeDatabase();
        SpringApplication.run(RestmanWebApplication.class, args);
    }

    private static void initializeDatabase() {
        try (Connection connection = DatabaseConfig3.getServerConnection();
             InputStream inputStream = RestmanWebApplication.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (inputStream == null) {
                throw new IllegalStateException("Không tìm thấy schema.sql");
            }
            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            boolean compatibilityChecked = false;
            for (String command : sql.split(";")) {
                if (command.isBlank()) continue;
                String normalized = command.trim().toUpperCase(java.util.Locale.ROOT);
                if (!compatibilityChecked && normalized.startsWith("INSERT INTO")) {
                    migrateExistingSchema(connection);
                    compatibilityChecked = true;
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute(command.trim());
                }
            }
        } catch (SQLException | IOException exception) {
            throw new IllegalStateException("Không thể khởi tạo CSDL RestMan từ schema.sql", exception);
        }
    }

    private static void migrateExistingSchema(Connection connection) throws SQLException {
        if (!columnExists(connection, "tblUser3", "role")) {
            execute(connection, "ALTER TABLE tblUser3 ADD COLUMN role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER'");
            execute(connection, "UPDATE tblUser3 u SET role=CASE "
                    + "WHEN EXISTS (SELECT 1 FROM tblManager3 m WHERE m.userId=u.id) THEN 'MANAGER' "
                    + "WHEN EXISTS (SELECT 1 FROM tblSalesStaff3 s WHERE s.userId=u.id) THEN 'SALESSTAFF' "
                    + "WHEN EXISTS (SELECT 1 FROM tblInventoryStaff3 i WHERE i.userId=u.id) THEN 'INVENTORYSTAFF' "
                    + "ELSE 'CUSTOMER' END");
        }
        if (!columnExists(connection, "tblCustomer3", "customerCode")) {
            execute(connection, "ALTER TABLE tblCustomer3 ADD COLUMN customerCode VARCHAR(20) UNIQUE");
        }
        execute(connection, "UPDATE tblCustomer3 SET customerCode=CONCAT('KH',id) "
                + "WHERE customerCode IS NULL OR customerCode=''");
        if (!columnExists(connection, "tblMembershipCard3", "cardTier")) {
            execute(connection, "ALTER TABLE tblMembershipCard3 ADD COLUMN cardTier "
                    + "VARCHAR(30) NOT NULL DEFAULT 'STANDARD'");
        }
        if (!columnExists(connection, "tblMembershipCard3", "issueDate")) {
            if (columnExists(connection, "tblMembershipCard3", "issuedAt")) {
                execute(connection, "ALTER TABLE tblMembershipCard3 CHANGE COLUMN issuedAt "
                        + "issueDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
            } else {
                execute(connection, "ALTER TABLE tblMembershipCard3 ADD COLUMN issueDate "
                        + "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
        }
        if (columnExists(connection, "tblIngredient3", "stockQuantity")
                && !columnExists(connection, "tblIngredient3", "quantityInStock")) {
            execute(connection, "ALTER TABLE tblIngredient3 CHANGE COLUMN stockQuantity "
                    + "quantityInStock DECIMAL(12,3) NOT NULL DEFAULT 0");
        }
        if (!columnExists(connection, "tblIngredient3", "quantityInStock")) {
            execute(connection, "ALTER TABLE tblIngredient3 ADD COLUMN quantityInStock "
                    + "DECIMAL(12,3) NOT NULL DEFAULT 0");
        }
        if (!columnExists(connection, "tblFoodOrder3", "tableId")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 ADD COLUMN tableId INT NULL");
        }
        if (columnExists(connection, "tblFoodOrder3", "orderedAt")
                && !columnExists(connection, "tblFoodOrder3", "orderDate")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 CHANGE COLUMN orderedAt "
                    + "orderDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }
        if (!columnExists(connection, "tblFoodOrder3", "orderDate")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 ADD COLUMN orderDate "
                    + "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }
        if (columnExists(connection, "tblFoodOrder3", "status")
                && !columnExists(connection, "tblFoodOrder3", "orderStatus")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 CHANGE COLUMN status "
                    + "orderStatus VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
        }
        if (!columnExists(connection, "tblFoodOrder3", "orderStatus")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 ADD COLUMN orderStatus "
                    + "VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
        }
        if (!columnExists(connection, "tblFoodOrder3", "totalAmount")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 ADD COLUMN totalAmount "
                    + "DECIMAL(12,2) NOT NULL DEFAULT 0.00");
        }
        if (!columnExists(connection, "tblFoodOrder3", "notes")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 ADD COLUMN notes VARCHAR(500)");
        }
        if (!constraintExists(connection, "tblFoodOrder3", "fk_order_table3")) {
            execute(connection, "ALTER TABLE tblFoodOrder3 ADD CONSTRAINT fk_order_table3 "
                    + "FOREIGN KEY (tableId) REFERENCES tblDiningTable3(id)");
        }
        execute(connection, "UPDATE tblFoodOrder3 o SET totalAmount=COALESCE("
                + "(SELECT SUM(i.quantity*i.unitPrice) FROM tblOrderItem3 i WHERE i.orderId=o.id),0) "
                + "WHERE o.totalAmount=0");
        execute(connection, "UPDATE tblFoodOrder3 SET orderStatus='PENDING' WHERE orderStatus='NEW'");

        if (columnExists(connection, "tblBill3", "orderId")
                && !columnExists(connection, "tblBill3", "foodOrderId")) {
            execute(connection, "ALTER TABLE tblBill3 CHANGE COLUMN orderId foodOrderId INT NOT NULL");
        }
        if (!columnExists(connection, "tblBill3", "foodOrderId")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN foodOrderId INT NULL");
        }
        if (columnExists(connection, "tblBill3", "issuedAt")
                && !columnExists(connection, "tblBill3", "billDate")) {
            execute(connection, "ALTER TABLE tblBill3 CHANGE COLUMN issuedAt "
                    + "billDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }
        if (!columnExists(connection, "tblBill3", "billDate")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN billDate DATETIME "
                    + "NOT NULL DEFAULT CURRENT_TIMESTAMP");
        }
        if (!columnExists(connection, "tblBill3", "totalAmount")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN totalAmount DECIMAL(12,2) NOT NULL DEFAULT 0.00");
        }
        if (!columnExists(connection, "tblBill3", "paymentMethod")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN paymentMethod VARCHAR(30) NOT NULL DEFAULT 'CASH'");
        }
        if (!columnExists(connection, "tblBill3", "salesStaffId")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN salesStaffId INT NULL");
        }
        if (!columnExists(connection, "tblBill3", "customerName")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN customerName VARCHAR(100) NULL");
        }
        if (!columnExists(connection, "tblBill3", "customerId")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN customerId INT NULL");
        }
        if (!columnExists(connection, "tblBill3", "membershipCardId")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN membershipCardId INT NULL");
        }
        if (!columnExists(connection, "tblBill3", "discountAmount")) {
            execute(connection, "ALTER TABLE tblBill3 ADD COLUMN discountAmount "
                    + "DECIMAL(12,2) NOT NULL DEFAULT 0.00");
        }
        if (columnExists(connection, "tblBill3", "total")) {
            execute(connection, "UPDATE tblBill3 SET totalAmount=total WHERE totalAmount=0");
            execute(connection, "ALTER TABLE tblBill3 MODIFY subtotal DECIMAL(12,2) NOT NULL DEFAULT 0.00");
            execute(connection, "ALTER TABLE tblBill3 MODIFY discount DECIMAL(12,2) NOT NULL DEFAULT 0.00");
            execute(connection, "ALTER TABLE tblBill3 MODIFY total DECIMAL(12,2) NOT NULL DEFAULT 0.00");
        }
        execute(connection, "UPDATE tblBill3 b LEFT JOIN tblFoodOrder3 o ON o.id=b.foodOrderId "
                + "SET b.salesStaffId=COALESCE(b.salesStaffId,o.salesStaffId,"
                + "(SELECT MIN(id) FROM tblSalesStaff3)) WHERE b.salesStaffId IS NULL");
        execute(connection, "UPDATE tblBill3 b LEFT JOIN tblFoodOrder3 o ON o.id=b.foodOrderId "
                + "LEFT JOIN tblCustomer3 c ON c.id=o.customerId "
                + "LEFT JOIN tblUser3 u ON u.id=c.userId "
                + "SET b.customerId=COALESCE(b.customerId,o.customerId), "
                + "b.customerName=LEFT(COALESCE(NULLIF(b.customerName,''), "
                + "NULLIF(TRIM(CONCAT_WS(' ',c.firstName,c.lastName)),''),u.username,'Khách lẻ'),100), "
                + "b.discountAmount=COALESCE(b.discountAmount,0) "
                + "WHERE b.customerId IS NULL OR b.customerName IS NULL OR b.customerName='' "
                + "OR b.discountAmount IS NULL");
        if (columnIsNullable(connection, "tblBill3", "customerName")) {
            execute(connection, "ALTER TABLE tblBill3 MODIFY customerName VARCHAR(100) NOT NULL DEFAULT 'Khách lẻ'");
        }
        if (!constraintExists(connection, "tblBill3", "fk_bill_foodorder3")) {
            execute(connection, "ALTER TABLE tblBill3 ADD CONSTRAINT fk_bill_foodorder3 "
                    + "FOREIGN KEY (foodOrderId) REFERENCES tblFoodOrder3(id)");
        }
        if (!constraintExists(connection, "tblBill3", "fk_bill_salesstaff3")) {
            execute(connection, "ALTER TABLE tblBill3 ADD CONSTRAINT fk_bill_salesstaff3 "
                    + "FOREIGN KEY (salesStaffId) REFERENCES tblSalesStaff3(id)");
        }
        if (!constraintExists(connection, "tblBill3", "fk_bill_customer3")) {
            execute(connection, "ALTER TABLE tblBill3 ADD CONSTRAINT fk_bill_customer3 "
                    + "FOREIGN KEY (customerId) REFERENCES tblCustomer3(id)");
        }
        if (!constraintExists(connection, "tblBill3", "fk_bill_membership3")) {
            execute(connection, "ALTER TABLE tblBill3 ADD CONSTRAINT fk_bill_membership3 "
                    + "FOREIGN KEY (membershipCardId) REFERENCES tblMembershipCard3(id)");
        }
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema=DATABASE() AND table_name=? AND column_name=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private static boolean columnIsNullable(Connection connection, String table, String column) throws SQLException {
        String sql = "SELECT is_nullable FROM information_schema.columns "
                + "WHERE table_schema=DATABASE() AND table_name=? AND column_name=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && "YES".equalsIgnoreCase(result.getString(1));
            }
        }
    }

    private static boolean constraintExists(Connection connection, String table, String constraint) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.table_constraints "
                + "WHERE table_schema=DATABASE() AND table_name=? AND constraint_name=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, constraint);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) > 0;
            }
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
