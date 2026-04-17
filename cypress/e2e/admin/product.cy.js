describe('Quản lý Sản phẩm / Linh kiện', () => {
  // Chạy trước mỗi test: tự động đăng nhập vào quyền admin
  beforeEach(() => {
    cy.visit('/auth/login');
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');
    cy.get('#btnLogin').click();
    cy.url().should('include', '/dashboard');
  });

  it('Thêm linh kiện mới: Nhập đầy đủ thông tin thành công', () => {
    cy.visit('/products');
    
    // Bấm nút hình dấu cộng (Thêm mới sản phẩm)
    cy.get('a.btn-primary-custom[href="/products/new"]').click();
    cy.url().should('include', '/products/new');

    // 1. Nhập Tên linh kiện (clear trước khi nhập để tránh rác)
    const productName = 'IC 555 Timer Precision ' + Date.now();
    cy.get('input[name="name"]').clear().type(productName);
    
    // 2. Nhập Loại (Category) bằng cách chọn trực tiếp index số 1 thay vì tìm value mông lung
    cy.get('select[name="category"]').select(1);

    // 3. Nhập Đơn vị tính
    cy.get('select[name="unit"]').select(1);

    // 4. Giá cả (clear số 0 mặc định của ô input để tránh sinh ra "05000")
    cy.get('input[name="costPrice"]').clear().type('5000');
    cy.get('input[name="sellPrice"]').clear().type('15000');

    // 5. Tồn kho và Số lượng tối thiểu
    cy.get('input[name="quantity"]').clear().type('500');
    cy.get('input[name="minQuantity"]').clear().type('50');
    cy.get('input[name="maxQuantity"]').clear().type('2000');

    // 6. Bấm Lưu
    cy.get('button[type="submit"]').contains('Thêm mới').click();

    // 7. Đảm bảo form xử lý thành công và không phọt ra báo lỗi màu đỏ (Validate backend)
    //cy.get('.text-danger').should('not.exist');

    // 8. Chờ trình duyệt THỰC SỰ chuyển hướng (Redirect) về trang danh sách /products 
    // chứ không phải đang mắc kẹt ở lại /products/new hoặc /products/save
    cy.location('pathname').should('eq', '/products');

    // 9. Kiểm tra dòng thông báo màu xanh "Lưu sản phẩm thành công!"
    cy.contains('Lưu sản phẩm thành công!').should('be.visible');
    
    // Đảm bảo record có tồn tại trong table
    cy.get('table').should('contain', productName);
  });

  it('Tìm kiếm và Lọc: Tìm theo tên linh kiện và lọc trạng thái', () => {
    cy.visit('/products');
    
    // Tìm kiếm theo tên linh kiện 
    cy.get('input[name="search"]').clear().type('IC 555{enter}');
    cy.url().should('include', 'search=IC+555');
    
    // Dropdown 1 (Danh mục) - Chọn vị trí số 1
    cy.get('select').eq(0).select(1);
    
    // Dropdown 2 (Trạng thái) - Chọn Tình trạng " ACTIVE"
    cy.get('select').eq(1).select('ACTIVE');
    cy.get('select').eq(1).should('have.value', 'ACTIVE');
  });
});
