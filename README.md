# 📦 Hệ thống Quản lý Nhập – Xuất – Tồn Kho

> **Inventory Management System ** — Spring Boot 3 · Spring Data JPA · Spring Security · SQL Server · Thymeleaf

---

## 📋 Mục lục

- [Mô tả dự án](#-mô-tả-dự-án)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Kiến trúc OOP](#-kiến-trúc-oop)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Cơ sở dữ liệu](#-cơ-sở-dữ-liệu)
- [Chức năng hệ thống](#-chức-năng-hệ-thống)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Hướng dẫn cài đặt](#-hướng-dẫn-cài-đặt)
- [Tài khoản demo](#-tài-khoản-demo)
- [Giao diện](#-giao-diện)
- [API nội bộ](#-api-nội-bộ)

---

## 🎯 Mô tả dự án

Hệ thống quản lý nhập – xuất – tồn kho là ứng dụng web giúp doanh nghiệp vừa và nhỏ theo dõi hàng hóa trong kho một cách chính xác và hiệu quả. Phiên bản được thiết kế hoàn toàn theo nguyên lý **OOP** (kế thừa, đóng gói, đa hình) với kiến trúc phân lớp rõ ràng.

### Vấn đề giải quyết
- ❌ Quản lý kho bằng Excel thủ công → dễ nhầm lẫn, khó tra cứu
- ❌ Không có cảnh báo khi hàng sắp hết
- ❌ Không kiểm soát được ai thao tác gì trên hệ thống
- ❌ Không lưu lịch sử biến động tồn kho

### Giải pháp
- ✅ Ứng dụng web real-time, nhiều người dùng đồng thời
- ✅ Cảnh báo tự động sản phẩm sắp hết / hết hàng
- ✅ Audit trail đầy đủ (ai tạo/sửa/xóa gì, lúc nào)
- ✅ Lịch sử biến động tồn kho từng sản phẩm

---

## 🛠 Công nghệ sử dụng

| Công nghệ | Phiên bản | Mục đích |
|---|---|---|
| Java | 17 | Ngôn ngữ lập trình |
| Spring Boot | 3.2.x | Framework backend |
| Spring Data JPA | 3.2.x | ORM / Database access |
| Spring Security | 6.x | Xác thực & phân quyền |
| Hibernate | 6.3.x | JPA implementation |
| SQL Server | 2019+ | Cơ sở dữ liệu |
| Thymeleaf | 3.1.x | Template engine |
| Bootstrap | 5.3 | CSS framework |
| Chart.js | 4.x | Biểu đồ thống kê |
| Lombok | 1.18.x | Giảm boilerplate code |
| Maven | 3.8+ | Build tool |

---

## 🏗 Kiến trúc OOP

### Phân lớp (Layered Architecture)
```
Controller  →  Service (Interface/Impl)  →  Repository  →  Database
                    ↕
              Entity (Domain Model)
```

### Base Classes
```
BaseEntity (abstract)
├── audit fields: createdAt, updatedAt, createdBy, updatedBy
├── soft delete: deleted = false
├── @SuperBuilder — kế thừa builder sang subclass
└── isValid(), softDelete(), restore()

AbstractOrder (abstract) extends BaseEntity
├── code, orderDate, status, paymentStatus
├── totalAmount, discountAmount, finalAmount
├── complete(), cancel() — business logic chuyển trạng thái
└── generateCode(seq) — template method, subclass tự định nghĩa
    ├── ImportOrder → PN######
    └── ExportOrder → PX######
```

### Nguyên lý áp dụng
- **Single Responsibility**: mỗi class một trách nhiệm duy nhất
- **Open/Closed**: thêm entity mới chỉ cần extend `BaseEntity`
- **Liskov Substitution**: `ImportOrder` và `ExportOrder` dùng được ở mọi nơi cần `AbstractOrder`
- **Interface Segregation**: mỗi Service interface chỉ khai báo method cần thiết
- **Dependency Inversion**: Controller phụ thuộc interface, không phụ thuộc implementation

---

## 📁 Cấu trúc dự án

```
src/main/java/com/inventory/
├── InventoryApplication.java
├── config/
│   └── SecurityConfig.java          # Spring Security, BCrypt, RBAC
├── controller/
│   ├── DashboardController.java
│   ├── ProductController.java
│   ├── SupplierController.java
│   ├── CustomerController.java
│   ├── ImportController.java
│   ├── ExportController.java
│   └── ReportController.java
├── dto/
│   └── DashboardStatsDto.java       # Data Transfer Object cho dashboard
├── entity/
│   ├── base/
│   │   ├── BaseEntity.java          # Abstract: audit + soft delete
│   │   └── AbstractOrder.java       # Abstract: order status machine
│   ├── Category.java                # Danh mục (hỗ trợ cha/con)
│   ├── Unit.java                    # Đơn vị tính
│   ├── Warehouse.java               # Kho hàng
│   ├── Supplier.java                # Nhà cung cấp
│   ├── Customer.java                # Khách hàng
│   ├── Product.java                 # Sản phẩm
│   ├── ImportOrder.java             # Phiếu nhập kho
│   ├── ImportDetail.java            # Chi tiết phiếu nhập
│   ├── ExportOrder.java             # Phiếu xuất kho
│   ├── ExportDetail.java            # Chi tiết phiếu xuất
│   ├── StockMovement.java           # Lịch sử biến động tồn kho
│   └── AppUser.java                 # Tài khoản người dùng
├── enums/
│   ├── OrderStatus.java             # PENDING | COMPLETED | CANCELLED
│   ├── PaymentStatus.java           # UNPAID | PARTIAL | PAID
│   ├── StockStatus.java             # IN_STOCK | LOW_STOCK | OUT_OF_STOCK
│   ├── MovementType.java            # IN | OUT | ADJUST | TRANSFER
│   └── UserRole.java                # ADMIN | MANAGER | STAFF
├── exception/
│   ├── BusinessException.java
│   ├── InsufficientStockException.java
│   ├── DuplicateCodeException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java  # @ControllerAdvice
├── repository/                      # Spring Data JPA repositories
│   ├── ProductRepository.java       # + JOIN FETCH queries
│   ├── ImportOrderRepository.java   # + findByIdWithDetails
│   ├── ExportOrderRepository.java
│   └── ... (8 repositories total)
└── service/
    ├── ProductService.java          # Interface
    ├── ImportService.java
    ├── ExportService.java
    ├── SupplierService.java
    ├── CustomerService.java
    ├── StockService.java
    ├── ReportService.java
    └── impl/
        ├── ProductServiceImpl.java  # Implementation
        ├── ImportServiceImpl.java
        ├── ExportServiceImpl.java
        ├── SupplierServiceImpl.java
        ├── CustomerServiceImpl.java
        ├── StockServiceImpl.java
        └── ReportServiceImpl.java

src/main/resources/
├── application.properties
├── static/css/style.css
└── templates/
    ├── layout/base.html             # Layout chung (sidebar, topbar)
    ├── login.html
    ├── dashboard.html
    ├── product/   (list, form, stock-history)
    ├── supplier/  (list, form)
    ├── customer/  (list, form)
    ├── import/    (list, form, detail)
    ├── export/    (list, form, detail)
    └── report/    (index)
```

---

## 🗄 Cơ sở dữ liệu

### Database: `inventory_db` (SQL Server)

```
app_users        — Tài khoản hệ thống (username, password BCrypt, role)
units            — Đơn vị tính (Cái, Hộp, Ram, Thùng...)
warehouses       — Kho hàng
categories       — Danh mục sản phẩm (hỗ trợ parent/child)
suppliers        — Nhà cung cấp (+ hạn mức nợ, ngân hàng)
customers        — Khách hàng (+ loại KH, tổng mua, loyalty)
products         — Sản phẩm (+ barcode, weight, kho, đơn vị)
import_orders    — Phiếu nhập kho
import_details   — Chi tiết phiếu nhập (totalPrice COMPUTED)
export_orders    — Phiếu xuất kho
export_details   — Chi tiết phiếu xuất (+ discount_percent, COMPUTED)
stock_movements  — Lịch sử biến động tồn kho
```

**Đặc điểm DB:**
- Mọi bảng có cột `deleted BIT` → soft delete
- Mọi bảng có `created_at`, `updated_at`, `created_by`, `updated_by` → audit trail
- `totalPrice` và `total_price` là computed column trong SQL Server
- 3 views: `v_inventory_summary`, `v_monthly_import`, `v_monthly_export`

---

## ✅ Chức năng hệ thống

### 🔐 Xác thực & Phân quyền
| Role | Quyền hạn |
|---|---|
| **ADMIN** | Toàn quyền + quản lý tài khoản người dùng |
| **MANAGER** | Quản lý nghiệp vụ đầy đủ (CRUD + xác nhận phiếu) |
| **STAFF** | Xem danh sách, tạo phiếu, không xác nhận |

### 📊 Dashboard
- KPI: tổng sản phẩm, nhà cung cấp, khách hàng, giá trị tồn kho
- Thống kê phiếu nhập/xuất tháng hiện tại (số lượng + tổng tiền)
- Cảnh báo sản phẩm sắp hết (`LOW_STOCK`) và hết hàng (`OUT_OF_STOCK`)
- Biểu đồ cột số phiếu nhập/xuất 12 tháng (Chart.js)
- Thao tác nhanh: tạo phiếu nhập, xuất, thêm sản phẩm, khách hàng

### 📦 Quản lý Sản phẩm
- Danh sách với phân trang (10/trang) và tìm kiếm theo mã/tên
- Thêm mới / chỉnh sửa: gắn danh mục, NCC, đơn vị tính, kho, barcode
- Xóa mềm (soft delete) — không mất dữ liệu lịch sử
- Hiển thị trạng thái tồn kho với màu sắc: 🟢 Đủ / 🟡 Sắp hết / 🔴 Hết
- Xem lịch sử biến động tồn kho từng sản phẩm

### 🏭 Quản lý Nhà cung cấp
- CRUD đầy đủ với thông tin tài chính: MST, ngân hàng, hạn mức nợ
- Cảnh báo trực quan khi nhà cung cấp vượt hạn mức nợ

### 👥 Quản lý Khách hàng 
- Phân loại: Bán lẻ (RETAIL) / Bán sỉ (WHOLESALE) / VIP
- Tự động tính hạng loyalty dựa trên tổng mua hàng:
  - < 5M → Thường | 5–20M → Bạc | 20–100M → Vàng | > 100M → VIP
- Theo dõi tổng giá trị đã mua

### 📥 Phiếu nhập kho
- Tạo phiếu với nhiều dòng sản phẩm (thêm/xóa động bằng JS)
- Gắn nhà cung cấp và số hóa đơn
- Tự động gợi ý giá nhập từ dữ liệu sản phẩm
- **Xác nhận nhập kho** → tự động cộng tồn kho + ghi `StockMovement`
- Hủy phiếu (chỉ khi còn PENDING)
- In phiếu qua trình duyệt (CSS print)

### 📤 Phiếu xuất kho
- Tạo phiếu gắn khách hàng và địa chỉ giao hàng
- Hiển thị tồn kho hiện tại khi chọn sản phẩm
- Hỗ trợ **chiết khấu theo từng dòng** (`discountPercent`) 
- **Kiểm tra tồn kho** trước khi xác nhận — từ chối nếu không đủ hàng
- **Xác nhận xuất kho** → tự động trừ tồn + ghi `StockMovement`

### 📈 Báo cáo & Thống kê
- Biểu đồ cột: số phiếu nhập/xuất theo 12 tháng (chọn năm)
- Biểu đồ tròn: phân bổ tình trạng tồn kho
- Bảng chi tiết tồn kho với giá trị tồn (qty × costPrice)
- In báo cáo qua trình duyệt

---

## 💻 Yêu cầu hệ thống

| Phần mềm | Phiên bản tối thiểu |
|---|---|
| JDK | 17+ |
| Maven | 3.8+ |
| SQL Server | 2019+ |
| SQL Server Management Studio | 19+ (khuyến nghị) |
| IntelliJ IDEA hoặc STS4 | Bất kỳ phiên bản hiện đại |

---

## 🚀 Hướng dẫn cài đặt

### Bước 1 — Tạo Database

Mở **SQL Server Management Studio**, kết nối vào server, mở file `db.sql` và chạy:

```sql
-- Script tự động tạo database inventory_db
-- và chèn dữ liệu mẫu
```

### Bước 2 — Cấu hình `application.properties`

```properties
# Thay thông tin kết nối SQL Server
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=inventory_db;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=YourPassword123
```

> **Nếu dùng SQL Server Express**, thay URL thành:
> `jdbc:sqlserver://localhost\SQLEXPRESS:1433;databaseName=inventory_db;...`

### Bước 3 — Chạy ứng dụng

```bash
# Terminal
cd inventory-system
mvn clean install
mvn spring-boot:run
```

Hoặc trong IDE: chạy `InventoryApplication.java`

### Bước 4 — Truy cập

```
http://localhost:8080
```

---

## 🔑 Tài khoản demo

Chạy lệnh SQL sau để thiết lập mật khẩu (BCrypt hash của `admin123`):

```sql
UPDATE app_users
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username IN ('admin', 'manager', 'staff1');
```

| Tài khoản | Mật khẩu | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN — toàn quyền |
| `manager` | `admin123` | MANAGER — quản lý nghiệp vụ |
| `staff1` | `admin123` | STAFF — nhập liệu cơ bản |

---

## 🎨 Giao diện

- **Màu chủ đạo**: Navy `#1a237e` + Trắng
- **Sidebar**: Cố định bên trái, highlight trang hiện tại
- **Responsive**: Bootstrap 5 — hoạt động trên màn hình ≥ 768px
- **Biểu đồ**: Chart.js 4.x (bar chart + doughnut chart)
- **Flash message**: Tự động ẩn sau 4 giây
- **Print support**: Ẩn sidebar/nút khi in phiếu

---

## 🔌 API nội bộ (AJAX)

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/products/api/{id}` | Lấy thông tin sản phẩm cho form |
| GET | `/imports/api/product/{id}` | Lấy giá nhập gợi ý |
| GET | `/exports/api/product/{id}` | Lấy giá bán + tồn kho hiện tại |

---

## 🐛 Xử lý lỗi thường gặp

**Lỗi kết nối SQL Server**
```
Login failed for user 'sa'
```
→ Bật SQL Server Authentication trong SSMS: chuột phải server → Properties → Security → chọn **SQL Server and Windows Authentication mode**

**Port 1433 không kết nối**
```
TCP/IP connection to host failed
```
→ SQL Server Configuration Manager → SQL Server Network Configuration → Protocols → TCP/IP → **Enable** → Restart service

**Lombok không hoạt động (IntelliJ)**
```
Cannot find symbol: method builder()
```
→ File → Settings → Plugins → cài **Lombok** → bật **Enable annotation processing**

**Port 8080 bị chiếm**
→ Đổi `server.port=8081` trong `application.properties`
