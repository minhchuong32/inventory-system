package com.system.inventorysystem.config; // Nhớ đổi lại package cho khớp với project của bạn

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner seedDatabase(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultPasswordHash = passwordEncoder.encode("admin123");

            // Kiểm tra xem bảng app_users đã có dữ liệu chưa.
            // Bước này rất quan trọng để tránh việc mỗi lần container restart lại bị lỗi
            // báo trùng lặp (Duplicate Key).
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM app_users", Integer.class);
            String updateAuthType = "UPDATE app_users SET auth_type = 'NORMAL' WHERE auth_type IS NULL";
            jdbcTemplate.update(updateAuthType);

            jdbcTemplate.update("UPDATE app_users SET password = ? WHERE username = ?", defaultPasswordHash, "admin");
            jdbcTemplate.update("UPDATE app_users SET password = ? WHERE username = ?", defaultPasswordHash, "manager");
            jdbcTemplate.update("UPDATE app_users SET password = ? WHERE username = ?", defaultPasswordHash, "staff1");

            if (count != null && count == 0) {
                System.out.println(">> Database đang trống. Bắt đầu nạp dữ liệu mẫu...");

                // ======================================================
                // 1. THÊM NGƯỜI DÙNG VỚI MẬT KHẨU ĐƯỢC BĂM (HASH) ĐỘNG
                // ======================================================
                String insertUserSql = """
                            INSERT INTO app_users
                            (username, password, full_name, email, phone, role, status,
                             created_at, created_by, updated_at, updated_by, deleted, refresh_token, last_login, auth_type)
                            VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), 'system', GETDATE(), 'system', 0, NULL, NULL, 'NORMAL')
                        """;

                // Thuật toán passwordEncoder.encode() sẽ tự động băm mật khẩu "admin123" thành
                // mã
                // BCrypt
                jdbcTemplate.update(insertUserSql, "admin", defaultPasswordHash, "Quản trị viên",
                        "admin@ims.vn", "0901000001", "ADMIN", 1);
                jdbcTemplate.update(insertUserSql, "manager", defaultPasswordHash, "Nguyễn Quản Lý",
                        "manager@ims.vn", "0901000002", "MANAGER", 1);
                jdbcTemplate.update(insertUserSql, "staff1", defaultPasswordHash, "Trần Nhân Viên",
                        "staff1@ims.vn", "0901000003", "STAFF", 1);

                System.out.println(">> Đã thêm danh sách Users kèm mã băm mật khẩu thành công.");

                // ======================================================
                // 2. THÊM DỮ LIỆU CÁC BẢNG CÒN LẠI TỪ FILE DB.SQL CỦA BẠN
                // ======================================================
                // Sử dụng Text Block (""") của Java để dán trực tiếp câu lệnh SQL mà không cần
                // nối chuỗi
                String insertOtherData = """
                            -- 1. Units
                                                INSERT INTO units (name, abbreviation, description, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                (N'Cái',   N'Cái',   N'Đơn vị tính theo từng cái', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Hộp',   N'Hộp',   N'Đơn vị tính theo hộp',      GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Ram',   N'Ram',   N'Xấp giấy 500 tờ',           GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Thùng', N'Thùng', N'Thùng chứa nhiều sản phẩm', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Kg',    N'kg',    N'Đơn vị khối lượng kilogram',GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Lít',   N'L',     N'Đơn vị thể tích lít',       GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Mét',   N'm',     N'Đơn vị chiều dài mét',      GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Bộ',    N'Bộ',    N'Bộ gồm nhiều phụ kiện',     GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 2. Warehouses
                                                INSERT INTO warehouses (code, name, address, capacity, manager_name, phone, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                ('KHO001', N'Kho Chính Hà Nội', N'123 Cầu Giấy, Hà Nội', 5000, N'Nguyễn Văn A', '0912001001', 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('KHO002', N'Kho Phụ TP.HCM',   N'456 Bình Thạnh, TP.HCM', 3000, N'Trần Thị B',   '0912001002', 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('KHO003', N'Kho Đà Nẵng',      N'789 Hải Châu, Đà Nẵng',  2000, N'Lê Văn C',     '0912001003', 1, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 3. Categories
                                                INSERT INTO categories (name, description, parent_category_id, sort_order, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                (N'Điện tử & Công nghệ',  N'Thiết bị điện tử, linh kiện',   NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Văn phòng phẩm',       N'Dụng cụ và thiết bị văn phòng', NULL, 2, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Thiết bị mạng',        N'Router, switch, cáp mạng',      NULL, 3, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Thực phẩm & Đồ uống',  N'Thực phẩm đóng gói, đồ uống', NULL, 4, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Gia dụng & Nội thất',  N'Đồ gia dụng, nội thất',       NULL, 5, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                INSERT INTO categories (name, description, parent_category_id, sort_order, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                (N'Laptop & Máy tính',    N'Máy tính xách tay, bàn', 1, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Chuột & Bàn phím',     N'Thiết bị nhập liệu',      1, 2, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Màn hình',             N'Màn hình máy tính',        1, 3, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Giấy in',              N'Các loại giấy in',         2, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (N'Bút & Mực',            N'Bút viết, mực in',         2, 2, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 4. Suppliers
                                                INSERT INTO suppliers (code, name, contact_person, phone, email, address, tax_code, bank_account, bank_name, credit_limit, current_debt, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                ('NCC0001', N'Cty TNHH Công nghệ ABC', N'Nguyễn Văn A', '0901234567', 'abc@techvn.com', N'15 Nguyễn Huệ, TP.HCM', '0301234567', NULL, NULL, 500000000, 0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('NCC0002', N'Cty CP Phân phối XYZ',   N'Trần Thị B',   '0912345678', 'xyz@phanboi.vn', N'234 Lê Lợi, Hà Nội',    '0109876543', NULL, NULL, 300000000, 0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('NCC0003', N'Nhà phân phối Việt Hưng',N'Lê Văn C',     '0923456789', 'viethung@sup.vn',N'89 Trần Phú, Đà Nẵng',  '0400012345', NULL, NULL, 200000000, 0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('NCC0004', N'Cty TM Sao Mai',         N'Phạm Thị D',   '0934567890', 'saomai@trade.vn',N'56 Hoàng Diệu, TP.HCM', '0310234567', NULL, NULL, 150000000, 0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('NCC0005', N'Cty Điện máy Bắc Kỳ',    N'Hoàng Văn E',  '0945678901', 'bky@dienmay.vn', N'102 Cầu Giấy, Hà Nội',  '0105432109', NULL, NULL, 100000000, 0, 0, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 5. Customers
                                                INSERT INTO customers (code, name, phone, email, address, tax_code, customer_type, total_purchase, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                ('KH0001', N'Cty TNHH Phát Đạt',     '0901111222', 'phatdat@company.vn',  N'45 Lê Lợi, Q.1, TP.HCM',      NULL, 'WHOLESALE', 0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('KH0002', N'Trường THPT Nguyễn Du', '0912222333', 'nguyendu@school.edu', N'123 Nguyễn Du, Q.1, TP.HCM',  NULL, 'RETAIL',    0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('KH0003', N'VP Đại diện MNO',       '0923333444', 'mno@corp.vn',         N'78 Pasteur, Q.3, TP.HCM',      NULL, 'WHOLESALE', 0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('KH0004', N'Nguyễn Văn An',         '0934444555', NULL,                  N'Hà Nội',                        NULL, 'RETAIL',    0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('KH0005', N'Cty ABC Logistics',     '0945555666', 'abc@logistics.vn',    N'300 Nguyễn Thái Học, Hà Nội', NULL, 'VIP',       0, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('KH0006', N'Siêu thị Thành Công',   '0956666777', 'thanhcong@mart.vn',   N'55 Đinh Tiên Hoàng, TP.HCM',  NULL, 'WHOLESALE', 0, 1, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 6. Products
                                                INSERT INTO products (code, name, barcode, category_id, supplier_id, unit_id, warehouse_id, cost_price, sell_price, quantity, min_quantity, max_quantity, weight, description, image_url, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                ('SP00001', N'Laptop Dell Inspiron 15', '8901234567890', 6, 1, 1, 1,  12000000, 15500000,  8,  3, 50, 1.850, N'Intel Core i5', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00002', N'Chuột Logitech M185',     '8901234567891', 7, 1, 1, 1,    180000,   290000, 45, 10,200, 0.090, N'Kết nối USB nano', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00003', N'Bàn phím cơ Keychron K2', '8901234567892', 7, 2, 1, 1,    750000,  1150000,  3,  5, 50, 0.680, N'Switch Brown', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00004', N'Màn hình Dell 24" FHD',   '8901234567893', 8, 1, 1, 1,   3500000,  4800000,  5,  2, 20, 3.900, N'1920x1080', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00005', N'Giấy in A4 Double A',     '8901234567894', 9, 3, 3, 2,     48000,    62000,150, 20,500, 2.300, N'500 tờ/ram', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00006', N'Bộ bút bi Thiên Long',    '8901234567895',10, 3, 2, 2,     18000,    28000, 80, 15,300, 0.120, N'12 cây/hộp', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00007', N'Router Wi-Fi TP-Link',    '8901234567896', 3, 4, 1, 1,    650000,   890000, 12,  4, 60, 0.350, N'Wi-Fi 6', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00008', N'Switch Mạng 8 Cổng',      '8901234567897', 3, 4, 1, 1,    280000,   420000,  2,  5, 30, 0.500, N'10/100Mbps', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00009', N'Nước suối Aquafina',      '8901234567898', 4, 5, 4, 2,     65000,    90000, 40,  8,100, 12.000,N'24 chai/thùng', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00010', N'Ghế xoay văn phòng',      '8901234567899', 5, 2, 1, 3,    850000,  1250000,  6,  2, 20, 12.500,N'Lưng lưới', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00011', N'Máy in HP LaserJet',      '8901234567900', 1, 1, 1, 1,   2800000,  3900000,  4,  2, 15, 4.200, N'In laser', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('SP00012', N'Tai nghe Sony',           '8901234567901', 1, 1, 1, 1,    650000,   950000, 15,  5, 80, 0.147, N'Bluetooth 5.2', NULL, 1, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 7. Import Orders
                                                INSERT INTO import_orders (code, order_date, expected_date, received_date, supplier_id, invoice_number, total_amount, discount_amount, tax_amount, final_amount, status, payment_status, note, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                ('PN000001', '2024-11-05', NULL, NULL, 1, 'INV-ABC-001', 98700000, 0, 0, 98700000, 'COMPLETED', 'PAID',   N'Nhập hàng', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PN000002', '2024-11-18', NULL, NULL, 3, 'INV-VH-001',  16410000, 0, 0, 16410000, 'COMPLETED', 'PAID',   N'Nhập VPP', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PN000003', '2024-12-02', NULL, NULL, 2, 'INV-XYZ-001',  6450000, 0, 0,  6450000, 'COMPLETED', 'PAID',   N'Nhập mạng', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PN000004', '2024-12-10', NULL, NULL, 4, 'INV-SM-001',   3340000, 0, 0,  3340000, 'COMPLETED', 'PARTIAL',N'Nhập router', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PN000005', '2025-01-03', NULL, NULL, 1, NULL,          15500000, 0, 0, 15500000, 'PENDING',   'UNPAID', N'Chờ XN', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PN000006', '2025-02-15', NULL, NULL, 2, 'INV-XYZ-002',  9600000, 0, 0,  9600000, 'COMPLETED', 'PAID',   N'Nhập thêm', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PN000007', '2025-03-01', NULL, NULL, 3, 'INV-VH-002',   7200000, 0, 0,  7200000, 'COMPLETED', 'PAID',   N'Nhập tháng 3', GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 8. Import Details
                                                INSERT INTO import_details (import_order_id, product_id, quantity, unit_price, expiry_date, batch_number, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                (1, 1, 5, 12000000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (1, 2, 20, 180000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (1, 4, 3, 3500000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (2, 5, 150, 48000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (2, 6, 100, 18000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (3, 3, 5, 750000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (3, 8, 8, 280000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (4, 7, 3, 650000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (4, 9, 20, 65000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (5, 1, 1, 12000000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (5, 4, 1, 3500000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (6, 3, 8, 750000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (6, 12, 10, 650000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (7, 5, 100, 48000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0), (7, 6, 50, 18000, NULL, NULL, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 9. Export Orders
                                                INSERT INTO export_orders (code, order_date, expected_delivery, actual_delivery, customer_id, delivery_address, total_amount, discount_amount, tax_amount, final_amount, status, payment_status, note, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                ('PX000001', '2024-11-12', NULL, NULL, 1, N'45 Lê Lợi, TP.HCM',      20680000, 0, 0, 20680000, 'COMPLETED', 'PAID',    N'Xuất khách lớn', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PX000002', '2024-11-25', NULL, NULL, 2, N'123 Nguyễn Du, TP.HCM',   5960000, 0, 0,  5960000, 'COMPLETED', 'PAID',    N'Xuất trường', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PX000003', '2024-12-08', NULL, NULL, 3, N'78 Pasteur, TP.HCM',      7200000, 0, 0,  7200000, 'COMPLETED', 'PAID',    N'Xuất VP', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PX000004', '2024-12-20', NULL, NULL, 4, N'Hà Nội',                   580000, 0, 0,   580000, 'COMPLETED', 'PAID',    N'Bán lẻ', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PX000005', '2025-01-05', NULL, NULL, 5, N'300 Nguyễn Thái Học',     3450000, 0, 0,  3450000, 'PENDING',   'UNPAID',  N'Chờ XN', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PX000006', '2025-02-20', NULL, NULL, 6, N'55 Đinh Tiên Hoàng',      4750000, 475000,0, 4275000, 'COMPLETED', 'PAID', N'Xuất siêu thị', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                ('PX000007', '2025-03-10', NULL, NULL, 1, N'45 Lê Lợi, TP.HCM',       9500000, 0, 0,  9500000, 'PENDING',   'UNPAID',  N'Đơn tháng 3', GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 10. Export Details
                                                INSERT INTO export_details (export_order_id, product_id, quantity, unit_price, discount_percent, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                (1, 1, 1, 15500000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (1, 4, 1, 4800000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (1, 2, 2, 290000, 0, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (2, 5, 80, 62000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (2, 6, 50, 28000, 0, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (3, 7, 2, 890000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (3, 8, 3, 420000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (3, 10, 3, 1250000, 0, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (4, 2, 1, 290000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (4, 6, 5, 28000, 0, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (5, 9, 30, 90000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (5, 5, 20, 62000, 0, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (6, 5, 50, 62000, 10, GETDATE(), GETDATE(), 'system', 'system', 0), (6, 6, 25, 28000, 10, GETDATE(), GETDATE(), 'system', 'system', 0), (6, 2, 5, 290000, 10, GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (7, 11, 2, 3900000, 0, GETDATE(), GETDATE(), 'system', 'system', 0), (7, 12, 5, 950000, 0, GETDATE(), GETDATE(), 'system', 'system', 0);

                                                -- 11. Stock Movements
                                                INSERT INTO stock_movements (product_id, warehouse_id, movement_type, quantity, before_quantity, after_quantity, reference_code, reference_type, note, created_at, updated_at, created_by, updated_by, deleted) VALUES
                                                (1, 1, 'IN',  5, 3,  8, 'PN000001', 'IMPORT', N'Nhập phiếu 1', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (2, 1, 'IN', 20, 25, 45,'PN000001', 'IMPORT', N'Nhập phiếu 1', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (4, 1, 'IN',  3, 2,  5, 'PN000001', 'IMPORT', N'Nhập phiếu 1', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (5, 2, 'IN',150, 0, 150,'PN000002', 'IMPORT', N'Nhập phiếu 2', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (6, 2, 'IN',100, 0, 100,'PN000002', 'IMPORT', N'Nhập phiếu 2', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (1, 1, 'OUT', 1, 8,  7, 'PX000001', 'EXPORT', N'Xuất phiếu 1', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (4, 1, 'OUT', 1, 5,  4, 'PX000001', 'EXPORT', N'Xuất phiếu 1', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (2, 1, 'OUT', 2, 45, 43,'PX000001', 'EXPORT', N'Xuất phiếu 1', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (5, 2, 'OUT',80, 150,70,'PX000002', 'EXPORT', N'Xuất phiếu 2', GETDATE(), GETDATE(), 'system', 'system', 0),
                                                (6, 2, 'OUT',50, 100,50,'PX000002', 'EXPORT', N'Xuất phiếu 2', GETDATE(), GETDATE(), 'system', 'system', 0);

                        """;

                jdbcTemplate.execute(insertOtherData);
                System.out.println(">> Đã nạp thành công toàn bộ dữ liệu mẫu vào cơ sở dữ liệu!");
            } else {
                System.out.println(">> Dữ liệu đã tồn tại, bỏ qua bước khởi tạo (Seeding).");
            }
        };
    }
}