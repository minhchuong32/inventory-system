# Hệ thống Quản lý Nhập - Xuất - Tồn Kho

Ứng dụng web quản lý kho hàng cho doanh nghiệp nhỏ và vừa, xây dựng bằng Spring Boot, Spring Security, JPA, Thymeleaf và SQL Server.

## Giới thiệu nhanh

Hệ thống hỗ trợ quản lý sản phẩm, nhà cung cấp, khách hàng, phiếu nhập, phiếu xuất, báo cáo và lịch sử biến động tồn kho. Dữ liệu mẫu sẽ được nạp tự động khi chạy ứng dụng lần đầu.

## Tính năng chính

- Đăng nhập và phân quyền theo vai trò `ADMIN`, `MANAGER`, `STAFF`.
- Quản lý sản phẩm, danh mục, đơn vị tính, kho hàng.
- Quản lý nhà cung cấp, khách hàng và hạn mức công nợ.
- Tạo phiếu nhập và phiếu xuất nhiều dòng.
- Tự động cộng/trừ tồn kho khi xác nhận phiếu.
- Theo dõi lịch sử nhập - xuất - điều chỉnh tồn kho.
- Xem dashboard và báo cáo thống kê.

## Yêu cầu cài đặt

- JDK 17 trở lên.
- Maven 3.8+ hoặc dùng Maven Wrapper có sẵn trong dự án.
- SQL Server 2019 trở lên.
- Trình duyệt hiện đại như Chrome, Edge hoặc Firefox.

## Cấu hình cơ sở dữ liệu

Ứng dụng đang cấu hình mặc định kết nối tới SQL Server tại:

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=inventory_db;encrypt=false;trustServerCertificate=true
```

Trước khi chạy, hãy tạo sẵn database `inventory_db` trên SQL Server và chỉnh lại username/password trong `src/main/resources/application.properties` nếu máy bạn dùng thông tin đăng nhập khác.

## Cách chạy ứng dụng

### Cách 1: Dùng Maven Wrapper trên Windows

```bat
cd Inventory-System
mvnw.cmd clean spring-boot:run
```

### Cách 2: Dùng Maven trên mọi nền tảng

```bash
cd Inventory-System
mvn clean spring-boot:run
```

### Cách 3: Chạy trực tiếp trong IDE

Mở file `src/main/java/com/system/inventorysystem/InventorySystemApplication.java` và chạy hàm `main`.

Sau khi khởi động xong, truy cập:

```text
http://localhost:8080
```

## Dữ liệu mẫu và tài khoản mặc định

Project có sẵn `DataSeederConfig` để nạp dữ liệu mẫu tự động khi bảng `app_users` còn trống.

Tài khoản mặc định:

| Tài khoản | Mật khẩu   | Vai trò       |
| --------- | ---------- | ------------- |
| `admin`   | `admin123` | Quản trị viên |
| `manager` | `admin123` | Quản lý       |
| `staff1`  | `admin123` | Nhân viên     |

Nếu bạn đã có dữ liệu cũ trong database, hệ thống sẽ bỏ qua bước nạp lại dữ liệu mẫu để tránh ghi đè ngoài ý muốn.

## Hướng dẫn sử dụng nhanh

1. Đăng nhập bằng một tài khoản phù hợp với vai trò của bạn.
2. Dùng thanh menu bên trái để chuyển giữa Dashboard, Sản phẩm, Nhà cung cấp, Khách hàng, Nhập kho, Xuất kho, Báo cáo và Người dùng.
3. Thêm hoặc sửa dữ liệu ở từng màn hình theo nhu cầu.
4. Khi tạo phiếu nhập hoặc phiếu xuất, hãy kiểm tra lại sản phẩm, số lượng và thông tin liên quan trước khi xác nhận.
5. Sau khi xác nhận phiếu, hệ thống sẽ tự cập nhật tồn kho và lưu lịch sử biến động.

## Quyền theo vai trò

- `ADMIN`: toàn quyền, bao gồm quản lý người dùng.
- `MANAGER`: quản lý nghiệp vụ kho, nhập, xuất, báo cáo.
- `STAFF`: thao tác nhập liệu và xem dữ liệu theo phân quyền.

## Cấu trúc thư mục chính

```text
src/main/java/com/system/inventorysystem/
├── controller
├── service
├── repository
├── entity
├── dto
├── config
├── exception
├── factory
├── observer
├── strategy
└── util

src/main/resources/
├── application.properties
├── static/
└── templates/
```

## Tài liệu kỹ thuật

### Kiến trúc tổng quan

Project được tổ chức theo mô hình lớp quen thuộc của Spring Boot:

- `controller`: xử lý request từ giao diện web, điều hướng trang và nhận dữ liệu người dùng nhập vào.
- `service`: chứa nghiệp vụ chính như nhập hàng, xuất hàng, tính tồn kho, kiểm tra quyền và xử lý trạng thái đơn.
- `repository`: làm việc với JPA để truy vấn và lưu dữ liệu xuống SQL Server.
- `entity`: mô tả các bảng dữ liệu của hệ thống.
- `config`: cấu hình bảo mật, JWT, upload file và các thiết lập ứng dụng khác.
- `factory`, `strategy`, `observer`: các phần minh họa pattern được dùng trong một số luồng xử lý của hệ thống.
- `exception`: gom các ngoại lệ nghiệp vụ và handler để trả lỗi thống nhất.

### Các màn hình và chức năng chính

Ứng dụng hiện có các nhóm controller chính như sau:

- `LoginController`, `AuthController`, `AccessDeniedController`: xử lý đăng nhập và phân quyền.
- `DashboardController`, `ReportController`, `HealthController`: hiển thị dashboard, báo cáo và kiểm tra trạng thái hệ thống.
- `ProductController`, `CategoryController`, `UnitController`, `WareHouseController`: quản lý dữ liệu danh mục và kho.
- `SupplierController`, `CustomerController`, `UserController`: quản lý đối tác và người dùng.
- `ImportController`, `ExportController`, `StockAlertController`: xử lý nhập kho, xuất kho và cảnh báo tồn kho.

### Luồng xử lý nghiệp vụ

Một luồng điển hình trong hệ thống thường diễn ra như sau:

1. Người dùng đăng nhập và được xác thực bởi Spring Security.
2. Request đi vào controller tương ứng với màn hình đang sử dụng.
3. Controller gọi service để xử lý kiểm tra dữ liệu, quyền truy cập và trạng thái nghiệp vụ.
4. Service làm việc với repository để đọc hoặc ghi dữ liệu xuống database.
5. Kết quả được trả về giao diện Thymeleaf hoặc trả về thông báo lỗi nếu có vấn đề.

### Các thành phần hỗ trợ

- JWT được cấu hình trong `application.properties` để hỗ trợ xác thực.
- Upload file được đặt trong thư mục `uploads`.
- `spring.jpa.hibernate.ddl-auto=update` giúp tự đồng bộ bảng khi thay đổi entity.
- `DataSeederConfig` sẽ nạp dữ liệu mẫu khi bảng người dùng trống.

## Mô hình cơ sở dữ liệu

Các entity chính của hệ thống gồm:

- `AppUser`: tài khoản đăng nhập và vai trò.
- `Product`: sản phẩm quản lý trong kho.
- `Category`: nhóm sản phẩm.
- `Unit`: đơn vị tính.
- `Supplier`: nhà cung cấp.
- `Customer`: khách hàng.
- `WareHouse`: kho hàng.
- `ImportOrder`, `ImportDetail`: phiếu nhập và chi tiết phiếu nhập.
- `ExportOrder`, `ExportDetail`: phiếu xuất và chi tiết phiếu xuất.
- `StockMovement`: lịch sử biến động tồn kho.

Quan hệ dữ liệu được tổ chức theo hướng thực tế của hệ thống kho:

- Một `Category` có thể chứa nhiều `Product`.
- Một `Unit` có thể được dùng cho nhiều `Product`.
- Một `Supplier` và một `Customer` có thể gắn với nhiều giao dịch nhập hoặc xuất.
- Một `ImportOrder` hoặc `ExportOrder` thường bao gồm nhiều dòng chi tiết.
- `StockMovement` ghi lại mọi thay đổi số lượng để phục vụ tra cứu và báo cáo.

## Triển khai Docker

Project có sẵn `docker-compose.yml` để chạy toàn bộ hệ thống bằng container.

### Các service trong Docker Compose

- `inventory-sqlserver`: container SQL Server 2022, ánh xạ cổng `1434` trên máy host ra `1433` trong container.
- `inventory-app`: ứng dụng Spring Boot, ánh xạ cổng `8080`.
- `cypress`: service tùy chọn để chạy kiểm thử end-to-end.

### Cách chạy

```bash
docker compose up --build
```

Sau khi các service khởi động xong, mở ứng dụng tại:

```text
http://localhost:8080
```

### Lưu ý khi triển khai Docker

- Nếu chạy bằng Docker, hãy đảm bảo cấu hình datasource của ứng dụng trỏ tới hostname `inventory-sqlserver`.
- Nên thay đổi mật khẩu SQL Server trước khi dùng trong môi trường thật.
- Nếu cần test E2E, có thể chạy thêm service `cypress` sau khi ứng dụng đã sẵn sàng.

## Tuyến truy cập chính

Phần giao diện và API của hệ thống hiện đang tổ chức theo các nhóm đường dẫn sau:

- `GET /auth/login`: trang đăng nhập.
- `GET /dashboard`: trang tổng quan sau khi đăng nhập.
- `GET /products` và `GET /products/api/*`: quản lý sản phẩm và dữ liệu liên quan.
- `GET /suppliers`: quản lý nhà cung cấp.
- `GET /customers`: quản lý khách hàng.
- `GET /imports`: quản lý phiếu nhập.
- `GET /exports`: quản lý phiếu xuất.
- `GET /users`: quản lý người dùng.
- `GET /reports`: xem báo cáo.
- `GET /health`: kiểm tra trạng thái ứng dụng.
- `GET /access-denied`: trang báo lỗi khi không đủ quyền.

Với các API thao tác dữ liệu, dự án đang dùng các endpoint như `api/list`, `api/{id}`, `api/create`, `api/{id}/delete`, `api/{id}/adjust-stock`, `api/{id}/complete` và `api/{id}/cancel` tùy theo từng module.

## Kiểm thử nhanh

Nếu muốn xác nhận hệ thống đang chạy đúng, bạn có thể kiểm tra theo thứ tự sau:

1. Khởi động SQL Server và ứng dụng.
2. Mở trang đăng nhập tại `/auth/login`.
3. Đăng nhập bằng tài khoản mặc định như `admin` hoặc `manager`.
4. Vào `Dashboard` để kiểm tra dữ liệu tổng quan.
5. Thử tạo mới một sản phẩm, nhà cung cấp hoặc khách hàng.
6. Tạo phiếu nhập hoặc phiếu xuất và xác nhận để kiểm tra số lượng tồn kho thay đổi đúng.
7. Mở `reports` và `health` để kiểm tra báo cáo và trạng thái hệ thống.

Nếu chạy bằng Cypress trong Docker, có thể dùng bộ test E2E trong thư mục `cypress/` để kiểm tra luồng đăng nhập và các màn hình nghiệp vụ chính.

## Lưu ý khi chạy lần đầu

- Nếu không kết nối được database, hãy kiểm tra lại SQL Server đang bật và cổng `1433` có mở hay không.
- Nếu muốn đổi cổng ứng dụng, sửa `server.port` trong `application.properties`.
- Nếu muốn nạp lại dữ liệu mẫu, hãy xóa dữ liệu cũ trong database trước khi khởi động lại ứng dụng.

## Ghi chú cho người dùng

- File cấu hình kết nối database nằm tại `src/main/resources/application.properties`.
- Class chạy chính của ứng dụng là `InventorySystemApplication`.
- Dữ liệu mẫu được tạo tự động, nên bạn không cần file SQL riêng để khởi tạo nếu database trống.

## Hỗ trợ mở rộng

### Sơ đồ luồng nghiệp vụ

#### 1. Luồng đăng nhập và phân quyền

1. Người dùng mở trang đăng nhập tại `/auth/login`.
2. Hệ thống kiểm tra thông tin đăng nhập qua Spring Security và JWT.
3. Nếu hợp lệ, người dùng được chuyển tới `/dashboard`.
4. Nếu không đủ quyền, hệ thống chuyển tới trang `/access-denied`.

#### 2. Luồng quản lý dữ liệu danh mục

1. Người dùng mở các màn hình như sản phẩm, nhà cung cấp, khách hàng hoặc người dùng.
2. Controller nhận request và gọi service để xử lý nghiệp vụ.
3. Service kiểm tra dữ liệu đầu vào, trạng thái hợp lệ và quyền thao tác.
4. Repository lưu thay đổi xuống database SQL Server.
5. Giao diện cập nhật lại danh sách hoặc thông báo kết quả cho người dùng.

#### 3. Luồng nhập kho và xuất kho

1. Người dùng tạo phiếu nhập hoặc phiếu xuất mới.
2. Hệ thống cho phép thêm nhiều dòng chi tiết vào phiếu.
3. Khi xác nhận phiếu, service kiểm tra số lượng, sản phẩm và trạng thái liên quan.
4. Nếu hợp lệ, hệ thống cập nhật tồn kho và ghi lịch sử biến động.
5. Nếu phiếu bị hủy, trạng thái được cập nhật tương ứng để đảm bảo dữ liệu nhất quán.

### Mô tả API

Các endpoint chính của hệ thống hiện được nhóm theo chức năng như sau:

| Nhóm      | Endpoint tiêu biểu                                                                                                            | Mục đích                                   |
| --------- | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| Auth      | `GET /auth/login`, `POST /api/auth/login`                                                                                     | Hiển thị trang đăng nhập và xử lý xác thực |
| Dashboard | `GET /dashboard`                                                                                                              | Xem tổng quan hệ thống                     |
| Product   | `GET /products`, `GET /products/api/list`, `POST /products/api/create`, `PUT /products/api/{id}`, `DELETE /products/api/{id}` | Quản lý sản phẩm                           |
| Supplier  | `GET /suppliers`, `POST /suppliers/save`, `POST /suppliers/{id}/delete`                                                       | Quản lý nhà cung cấp                       |
| Customer  | `GET /customers`, `POST /customers/save`, `POST /customers/{id}/delete`                                                       | Quản lý khách hàng                         |
| Import    | `GET /imports`, `POST /imports/save`, `POST /imports/{id}/complete`, `POST /imports/{id}/cancel`                              | Quản lý phiếu nhập                         |
| Export    | `GET /exports`, `POST /exports/save`, `POST /exports/{id}/complete`, `POST /exports/{id}/cancel`                              | Quản lý phiếu xuất                         |
| User      | `GET /users`, `POST /users/save`, `POST /users/{id}/toggle`, `POST /users/{id}/delete`                                        | Quản lý người dùng                         |
| Report    | `GET /reports`                                                                                                                | Xem báo cáo                                |
| Health    | `GET /health`                                                                                                                 | Kiểm tra trạng thái ứng dụng               |

Các API bổ trợ như `api/active`, `api/low-stock`, `api/count-active` và `api/product/{id}` được dùng cho phần hiển thị dữ liệu động trên giao diện.

### Kiểm thử theo chức năng

#### Kiểm thử đăng nhập

- Mở `/auth/login` và đăng nhập bằng tài khoản mẫu `admin`, `manager` hoặc `staff1`.
- Kiểm tra người dùng được điều hướng về dashboard sau khi xác thực thành công.
- Thử truy cập một trang yêu cầu quyền cao hơn để xác nhận màn hình từ chối truy cập hoạt động đúng.

#### Kiểm thử danh mục và dữ liệu cơ bản

- Tạo mới, sửa và xóa một sản phẩm.
- Tạo mới và cập nhật nhà cung cấp.
- Tạo mới và cập nhật khách hàng.
- Tạo mới người dùng và thử bật hoặc tắt trạng thái hoạt động.

#### Kiểm thử nhập kho và xuất kho

- Tạo phiếu nhập với nhiều dòng chi tiết và xác nhận phiếu.
- Tạo phiếu xuất với số lượng phù hợp với tồn kho hiện tại.
- Kiểm tra số lượng tồn kho thay đổi đúng sau khi hoàn tất phiếu.
- Thử hủy phiếu để xác nhận trạng thái được cập nhật chính xác.

#### Kiểm thử báo cáo và trạng thái hệ thống

- Mở trang báo cáo để kiểm tra dữ liệu tổng hợp.
- Gọi `/health` để xác minh ứng dụng đang hoạt động.
- Nếu chạy bằng Docker, kiểm tra thêm service `cypress` để xác nhận luồng E2E.
