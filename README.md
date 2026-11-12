# Đề tài số 22 — Hệ thống quản lý nhà hàng RestMan

> **Học phần:** Phân tích Thiết kế Hệ thống Thông tin  
> **Sinh viên thực hiện:** Tạ Minh Quang  
> **Mã sinh viên:** `B23DCCN693`  
> **Quy tắc định danh:** Các lớp và bảng dữ liệu của đề tài sử dụng hậu tố `3`.

RestMan là hệ thống quản lý nhà hàng có giao diện Web và Desktop. Hệ thống hỗ trợ các nghiệp vụ thực đơn, đặt bàn, đặt món, bán hàng, thanh toán, quản lý kho và thống kê.

## Công nghệ và kiến trúc

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 (Spring Boot 3 hỗ trợ Java 17 trở lên) |
| Web UI | Spring Boot 3.3.5, Thymeleaf, Bootstrap 5 |
| Desktop UI | Java Swing |
| Cơ sở dữ liệu | MySQL 8.0, 17 bảng quan hệ |
| Kết nối dữ liệu | JDBC, `PreparedStatement`, transaction cho các nghiệp vụ nhiều bước |

Ứng dụng được tổ chức theo luồng **Boundary (Web/ Swing UI) → Controller → DAO → Entity/Model**. Các bảng và lớp nghiệp vụ tuân theo quy ước hậu tố `3`, ví dụ `tblDish3`, `tblFoodOrder3`, `tblBill3` và `Dish3`.

## Chức năng theo tác nhân

### Đăng nhập và đăng ký

- Đăng nhập tại `/login`, có các tài khoản demo theo bốn vai trò.
- Đăng ký tài khoản khách hàng tại `/register`.
- Lưu thông tin người dùng đăng nhập trong `HttpSession` và điều hướng theo vai trò.

### Khách hàng — `Customer`

- Tìm món theo từ khóa và thể loại; xem thông tin chi tiết món ăn.
- Đặt bàn trực tuyến và xem các bàn còn trống.
- Đặt món trực tuyến với giỏ hàng nhiều món và tổng tiền tự tính.

### Nhân viên quản lý — `Manager`

- Thêm, sửa và xóa món ăn.
- Tạo và quản lý combo; mỗi combo yêu cầu tối thiểu hai món và được lưu trong transaction.
- Xem thống kê doanh thu, tồn kho, khách hàng, nhà cung cấp và chi tiết đơn hàng.

### Nhân viên bán hàng — `SalesStaff`

- Xác nhận đặt bàn và đơn đặt món trực tuyến.
- Lập đơn gọi món tại bàn với nhiều món.
- Thanh toán, lưu hóa đơn và nhân viên thu ngân, xem lịch sử hóa đơn.
- Cấp thẻ thành viên cho khách hàng.

### Nhân viên kho — `InventoryStaff`

- Thêm và chỉnh sửa nhà cung cấp.
- Nhập nguyên liệu và cập nhật số lượng tồn kho.

## Tài khoản demo

Dữ liệu đăng nhập được khởi tạo bởi `schema.sql`. Chỉ sử dụng các tài khoản này cho môi trường demo/local.

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Manager | `manager` | `manager123` |
| SalesStaff | `sales` | `sales123` |
| InventoryStaff | `inventory` | `inventory123` |
| Customer | `customer` | `customer123` |

## Cấu trúc dự án

```text
restman-backend-B23DCCN693/
├── docs/
│   └── images/                  # Ảnh báo cáo, UML và ảnh giao diện
├── src/
│   └── main/
│       ├── java/com/restman/
│       │   ├── config/          # Cấu hình kết nối MySQL
│       │   ├── controller/      # Xử lý nghiệp vụ
│       │   ├── dao/             # Truy vấn JDBC
│       │   ├── model/           # Entity/Model
│       │   ├── view/            # Các form Java Swing
│       │   └── web/             # Spring Boot application và Web Controller
│       └── resources/
│           ├── templates/       # Giao diện Thymeleaf
│           ├── application.properties
│           └── schema.sql       # 17 bảng và dữ liệu mẫu
├── pom.xml
└── README.md
```

## Cài đặt và chạy

### Yêu cầu

- JDK 21 (Spring Boot 3 yêu cầu tối thiểu Java 17).
- MySQL Server 8.0 và MySQL Workbench.
- VS Code với Java/Maven extensions hoặc IntelliJ IDEA có hỗ trợ Maven project.

### 1. Khởi tạo cơ sở dữ liệu

1. Mở `src/main/resources/schema.sql` trong MySQL Workbench.
2. Kết nối MySQL và chạy toàn bộ script. Script tạo database `restman3`, 17 bảng liên quan và dữ liệu demo cho người dùng, món ăn, combo, bàn, nhà cung cấp và nguyên liệu.

### 2. Cấu hình kết nối MySQL

Kiểm tra các khóa `db.url`, `db.username`, `db.password` trong `src/main/resources/application.properties` và cập nhật theo cấu hình MySQL trên máy. Có thể ghi đè bằng biến môi trường:

```text
RESTMAN_DB_URL=jdbc:mysql://localhost:3306/restman3?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true
RESTMAN_DB_USER=<mysql-username>
RESTMAN_DB_PASSWORD=<mysql-password>
```

Không đưa mật khẩu cá nhân hoặc thông tin kết nối của môi trường triển khai vào Git.

### 3. Chạy giao diện Web

1. Mở thư mục gốc dự án trong VS Code hoặc IntelliJ và chờ IDE import các dependency Maven từ `pom.xml`.
2. Chạy lớp `com.restman.web.RestmanWebApplication` bằng lệnh **Run Java** của IDE.
3. Mở [http://localhost:8080](http://localhost:8080).

Không bắt buộc cài Maven CLI riêng nếu IDE đã cấu hình Maven integration và có thể tải các dependency của dự án. Cơ sở dữ liệu MySQL cần chạy và được cấu hình trước khi khởi động Web app.

### 4. Chạy giao diện Desktop

Trong IDE, mở `src/main/java/com/restman/view/CustomerMainFrm3.java` và chạy `CustomerMainFrm3.main()` dưới dạng Java Application. Desktop UI cũng sử dụng cấu hình kết nối trong `application.properties`; cần bảo đảm file này nằm trên classpath và MySQL đang chạy.

