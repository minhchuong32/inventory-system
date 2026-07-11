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
            System.out.println(">> Bắt đầu quá trình Seeding Database...");

            // ======================================================
            // 0. XÓA TOÀN BỘ DỮ LIỆU CŨ THEO THỨ TỰ (TỪ CON LÊN CHA)
            // ======================================================
            String deleteDataSql = """
                    -- 1. Xóa các bảng giao dịch và chi tiết (Leaf tables)
                    DELETE FROM stock_movements;
                    DELETE FROM export_details;
                    DELETE FROM export_orders;
                    DELETE FROM import_details;
                    DELETE FROM import_orders;
                    
                    -- 2. Xóa sản phẩm
                    DELETE FROM products;
                    
                    -- 3. Xóa các danh mục từ điển và đối tác
                    DELETE FROM customers;
                    DELETE FROM suppliers;
                    DELETE FROM warehouses;
                    DELETE FROM units;
                    
                    -- 4. Xử lý xóa categories (bảng này có tự tham chiếu parent_category_id)
                    UPDATE categories SET parent_category_id = NULL;
                    DELETE FROM categories;
                    
                    -- 5. Xóa người dùng hệ thống
                    DELETE FROM app_users;
                    """;

            try {
                System.out.println(">> Đang dọn dẹp cơ sở dữ liệu cũ...");
                jdbcTemplate.execute(deleteDataSql);
                System.out.println(">> Đã dọn dẹp xong dữ liệu cũ.");
            } catch (Exception e) {
                System.err.println(">> CẢNH BÁO: Lỗi khi dọn dẹp dữ liệu cũ (Có thể do bảng chưa tồn tại): " + e.getMessage());
            }

            // ======================================================
            // 1. THÊM NGƯỜI DÙNG VỚI MẬT KHẨU ĐƯỢC BĂM (HASH) ĐỘNG
            // ======================================================
            System.out.println(">> Bắt đầu nạp dữ liệu mẫu mới...");
            
            String insertUserSql = """
                        INSERT INTO app_users
                        (username, password, full_name, email, phone, role, status,
                         created_at, created_by, updated_at, updated_by, deleted, refresh_token, last_login)
                        VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP, 'system', 0, NULL, NULL)
                    """;

            jdbcTemplate.update(insertUserSql, "admin", passwordEncoder.encode("123456"), "Quản trị viên",
                    "admin@ims.vn", "0901000001", "ADMIN", 1);
            jdbcTemplate.update(insertUserSql, "manager", passwordEncoder.encode("123456"), "Nguyễn Quản Lý",
                    "manager@ims.vn", "0901000002", "MANAGER", 1);
            jdbcTemplate.update(insertUserSql, "staff1", passwordEncoder.encode("123456"), "Trần Nhân Viên",
                    "staff1@ims.vn", "0901000003", "STAFF", 1);

            System.out.println(">> Đã thêm danh sách Users kèm mã băm mật khẩu thành công.");

            // ======================================================
            // 2. THÊM DỮ LIỆU CÁC BẢNG CÒN LẠI
            // ======================================================
            String insertOtherData = """
                        -- 1. Units
                        INSERT INTO units (name, abbreviation, description, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('Cái',   'Cái',   'Đơn vị tính theo từng cái', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Hộp',   'Hộp',   'Đơn vị tính theo hộp',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Ram',   'Ram',   'Xấp giấy 500 tờ',           CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Thùng', 'Thùng', 'Thùng chứa nhiều sản phẩm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Kg',    'kg',    'Đơn vị khối lượng kilogram',CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Lít',   'L',     'Đơn vị thể tích lít',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Mét',   'm',     'Đơn vị chiều dài mét',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Bộ',    'Bộ',    'Bộ gồm nhiều phụ kiện',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 2. Warehouses
                        INSERT INTO warehouses (code, name, address, capacity, manager_name, phone, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('KHO001', 'Kho Chính Hà Nội', '123 Cầu Giấy, Hà Nội', 5000, 'Nguyễn Văn A', '0912001001', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('KHO002', 'Kho Phụ TP.HCM',   '456 Bình Thạnh, TP.HCM', 3000, 'Trần Thị B',   '0912001002', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('KHO003', 'Kho Đà Nẵng',      '789 Hải Châu, Đà Nẵng',  2000, 'Lê Văn C',     '0912001003', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 3. Categories
                        INSERT INTO categories (name, description, parent_category_id, sort_order, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('Điện tử & Công nghệ',  'Thiết bị điện tử, linh kiện',   NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Văn phòng phẩm',       'Dụng cụ và thiết bị văn phòng', NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Thiết bị mạng',        'Router, switch, cáp mạng',      NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Thực phẩm & Đồ uống',  'Thực phẩm đóng gói, đồ uống', NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Gia dụng & Nội thất',  'Đồ gia dụng, nội thất',       NULL, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        INSERT INTO categories (name, description, parent_category_id, sort_order, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('Laptop & Máy tính',    'Máy tính xách tay, bàn', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Chuột & Bàn phím',     'Thiết bị nhập liệu',      1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Màn hình',             'Màn hình máy tính',        1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Giấy in',              'Các loại giấy in',         2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('Bút & Mực',            'Bút viết, mực in',         2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 4. Suppliers
                        INSERT INTO suppliers (code, name, contact_person, phone, email, address, tax_code, bank_account, bank_name, credit_limit, current_debt, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('NCC0001', 'Cty TNHH Công nghệ ABC', 'Nguyễn Văn A', '0901234567', 'abc@techvn.com', '15 Nguyễn Huệ, TP.HCM', '0301234567', NULL, NULL, 500000000, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('NCC0002', 'Cty CP Phân phối XYZ',   'Trần Thị B',   '0912345678', 'xyz@phanboi.vn', '234 Lê Lợi, Hà Nội',    '0109876543', NULL, NULL, 300000000, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('NCC0003', 'Nhà phân phối Việt Hưng','Lê Văn C',     '0923456789', 'viethung@sup.vn','89 Trần Phú, Đà Nẵng',  '0400012345', NULL, NULL, 200000000, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('NCC0004', 'Cty TM Sao Mai',         'Phạm Thị D',   '0934567890', 'saomai@trade.vn','56 Hoàng Diệu, TP.HCM', '0310234567', NULL, NULL, 150000000, 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('NCC0005', 'Cty Điện máy Bắc Kỳ',    'Hoàng Văn E',  '0945678901', 'bky@dienmay.vn', '102 Cầu Giấy, Hà Nội',  '0105432109', NULL, NULL, 100000000, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 5. Customers
                        INSERT INTO customers (code, name, phone, email, address, tax_code, customer_type, total_purchase, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('KH0001', 'Cty TNHH Phát Đạt',     '0901111222', 'phatdat@company.vn',  '45 Lê Lợi, Q.1, TP.HCM',      NULL, 'WHOLESALE', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('KH0002', 'Trường THPT Nguyễn Du', '0912222333', 'nguyendu@school.edu', '123 Nguyễn Du, Q.1, TP.HCM',  NULL, 'RETAIL',    0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('KH0003', 'VP Đại diện MNO',       '0923333444', 'mno@corp.vn',         '78 Pasteur, Q.3, TP.HCM',      NULL, 'WHOLESALE', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('KH0004', 'Nguyễn Văn An',         '0934444555', NULL,                  'Hà Nội',                        NULL, 'RETAIL',    0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('KH0005', 'Cty ABC Logistics',     '0945555666', 'abc@logistics.vn',    '300 Nguyễn Thái Học, Hà Nội', NULL, 'VIP',       0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('KH0006', 'Siêu thị Thành Công',   '0956666777', 'thanhcong@mart.vn',   '55 Đinh Tiên Hoàng, TP.HCM',  NULL, 'WHOLESALE', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 6. Products
                        INSERT INTO products (code, name, barcode, category_id, supplier_id, unit_id, warehouse_id, cost_price, sell_price, quantity, min_quantity, max_quantity, weight, description, image_url, status, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('SP00001', 'Laptop Dell Inspiron 15', '8901234567890', 6, 1, 1, 1,  12000000, 15500000,  8,  3, 50, 1.850, 'Intel Core i5', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00002', 'Chuột Logitech M185',     '8901234567891', 7, 1, 1, 1,    180000,   290000, 45, 10,200, 0.090, 'Kết nối USB nano', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00003', 'Bàn phím cơ Keychron K2', '8901234567892', 7, 2, 1, 1,    750000,  1150000,  3,  5, 50, 0.680, 'Switch Brown', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00004', 'Màn hình Dell 24" FHD',   '8901234567893', 8, 1, 1, 1,   3500000,  4800000,  5,  2, 20, 3.900, '1920x1080', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00005', 'Giấy in A4 Double A',     '8901234567894', 9, 3, 3, 2,     48000,    62000,150, 20,500, 2.300, '500 tờ/ram', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00006', 'Bộ bút bi Thiên Long',    '8901234567895',10, 3, 2, 2,     18000,    28000, 80, 15,300, 0.120, '12 cây/hộp', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00007', 'Router Wi-Fi TP-Link',    '8901234567896', 3, 4, 1, 1,    650000,   890000, 12,  4, 60, 0.350, 'Wi-Fi 6', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00008', 'Switch Mạng 8 Cổng',      '8901234567897', 3, 4, 1, 1,    280000,   420000,  2,  5, 30, 0.500, '10/100Mbps', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00009', 'Nước suối Aquafina',      '8901234567898', 4, 5, 4, 2,     65000,    90000, 40,  8,100, 12.000,'24 chai/thùng', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00010', 'Ghế xoay văn phòng',      '8901234567899', 5, 2, 1, 3,    850000,  1250000,  6,  2, 20, 12.500,'Lưng lưới', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00011', 'Máy in HP LaserJet',      '8901234567900', 1, 1, 1, 1,   2800000,  3900000,  4,  2, 15, 4.200, 'In laser', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('SP00012', 'Tai nghe Sony',           '8901234567901', 1, 1, 1, 1,    650000,   950000, 15,  5, 80, 0.147, 'Bluetooth 5.2', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 7. Import Orders
                        INSERT INTO import_orders (code, order_date, expected_date, received_date, supplier_id, invoice_number, total_amount, discount_amount, tax_amount, final_amount, status, payment_status, note, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('PN000001', '2024-11-05', NULL, NULL, 1, 'INV-ABC-001', 98700000, 0, 0, 98700000, 'COMPLETED', 'PAID',   'Nhập hàng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PN000002', '2024-11-18', NULL, NULL, 3, 'INV-VH-001',  16410000, 0, 0, 16410000, 'COMPLETED', 'PAID',   'Nhập VPP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PN000003', '2024-12-02', NULL, NULL, 2, 'INV-XYZ-001',  6450000, 0, 0,  6450000, 'COMPLETED', 'PAID',   'Nhập mạng', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PN000004', '2024-12-10', NULL, NULL, 4, 'INV-SM-001',   3340000, 0, 0,  3340000, 'COMPLETED', 'PARTIAL','Nhập router', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PN000005', '2025-01-03', NULL, NULL, 1, NULL,          15500000, 0, 0, 15500000, 'PENDING',   'UNPAID', 'Chờ XN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PN000006', '2025-02-15', NULL, NULL, 2, 'INV-XYZ-002',  9600000, 0, 0,  9600000, 'COMPLETED', 'PAID',   'Nhập thêm', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PN000007', '2025-03-01', NULL, NULL, 3, 'INV-VH-002',   7200000, 0, 0,  7200000, 'COMPLETED', 'PAID',   'Nhập tháng 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 8. Import Details
                        INSERT INTO import_details (import_order_id, product_id, quantity, unit_price, expiry_date, batch_number, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        (1, 1, 5, 12000000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (1, 2, 20, 180000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (1, 4, 3, 3500000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (2, 5, 150, 48000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (2, 6, 100, 18000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (3, 3, 5, 750000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (3, 8, 8, 280000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (4, 7, 3, 650000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (4, 9, 20, 65000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (5, 1, 1, 12000000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (5, 4, 1, 3500000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (6, 3, 8, 750000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (6, 12, 10, 650000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (7, 5, 100, 48000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (7, 6, 50, 18000, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 9. Export Orders
                        INSERT INTO export_orders (code, order_date, expected_delivery, actual_delivery, customer_id, delivery_address, total_amount, discount_amount, tax_amount, final_amount, status, payment_status, note, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        ('PX000001', '2024-11-12', NULL, NULL, 1, '45 Lê Lợi, TP.HCM',      20680000, 0, 0, 20680000, 'COMPLETED', 'PAID',   'Xuất khách lớn', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PX000002', '2024-11-25', NULL, NULL, 2, '123 Nguyễn Du, TP.HCM',   5960000, 0, 0,  5960000, 'COMPLETED', 'PAID',   'Xuất trường', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PX000003', '2024-12-08', NULL, NULL, 3, '78 Pasteur, TP.HCM',      7200000, 0, 0,  7200000, 'COMPLETED', 'PAID',   'Xuất VP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PX000004', '2024-12-20', NULL, NULL, 4, 'Hà Nội',                   580000, 0, 0,   580000, 'COMPLETED', 'PAID',   'Bán lẻ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PX000005', '2025-01-05', NULL, NULL, 5, '300 Nguyễn Thái Học',     3450000, 0, 0,  3450000, 'PENDING',   'UNPAID',  'Chờ XN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PX000006', '2025-02-20', NULL, NULL, 6, '55 Đinh Tiên Hoàng',      4750000, 475000,0, 4275000, 'COMPLETED', 'PAID', 'Xuất siêu thị', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        ('PX000007', '2025-03-10', NULL, NULL, 1, '45 Lê Lợi, TP.HCM',       9500000, 0, 0,  9500000, 'PENDING',   'UNPAID',  'Đơn tháng 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 10. Export Details
                        INSERT INTO export_details (export_order_id, product_id, quantity, unit_price, discount_percent, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        (1, 1, 1, 15500000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (1, 4, 1, 4800000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (1, 2, 2, 290000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (2, 5, 80, 62000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (2, 6, 50, 28000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (3, 7, 2, 890000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (3, 8, 3, 420000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (3, 10, 3, 1250000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (4, 2, 1, 290000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (4, 6, 5, 28000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (5, 9, 30, 90000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (5, 5, 20, 62000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (6, 5, 50, 62000, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (6, 6, 25, 28000, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (6, 2, 5, 290000, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (7, 11, 2, 3900000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0), (7, 12, 5, 950000, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);

                        -- 11. Stock Movements
                        INSERT INTO stock_movements (product_id, warehouse_id, movement_type, quantity, before_quantity, after_quantity, reference_code, reference_type, note, created_at, updated_at, created_by, updated_by, deleted) VALUES
                        (1, 1, 'IN',  5, 3,  8, 'PN000001', 'IMPORT', 'Nhập phiếu 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (2, 1, 'IN', 20, 25, 45,'PN000001', 'IMPORT', 'Nhập phiếu 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (4, 1, 'IN',  3, 2,  5, 'PN000001', 'IMPORT', 'Nhập phiếu 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (5, 2, 'IN',150, 0, 150,'PN000002', 'IMPORT', 'Nhập phiếu 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (6, 2, 'IN',100, 0, 100,'PN000002', 'IMPORT', 'Nhập phiếu 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (1, 1, 'OUT', 1, 8,  7, 'PX000001', 'EXPORT', 'Xuất phiếu 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (4, 1, 'OUT', 1, 5,  4, 'PX000001', 'EXPORT', 'Xuất phiếu 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (2, 1, 'OUT', 2, 45, 43,'PX000001', 'EXPORT', 'Xuất phiếu 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (5, 2, 'OUT',80, 150,70,'PX000002', 'EXPORT', 'Xuất phiếu 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0),
                        (6, 2, 'OUT',50, 100,50,'PX000002', 'EXPORT', 'Xuất phiếu 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0);
                    """;

            jdbcTemplate.execute(insertOtherData);
            System.out.println(">> Đã nạp thành công toàn bộ dữ liệu mẫu vào cơ sở dữ liệu!");
        };
    }
}