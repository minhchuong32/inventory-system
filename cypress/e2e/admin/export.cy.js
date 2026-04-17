describe('Quản lý Phiếu Xuất Kho (Export)', () => {
  beforeEach(() => {
    cy.visit('/auth/login');
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');
    cy.get('#btnLogin').click();
    cy.url().should('include', '/dashboard');
  });

  it('Tạo phiếu xuất kho mới thành công (cần có Khách hàng và Sản phẩm có tồn kho)', () => {
    cy.visit('/exports');
    
    // Nút tạo phiếu xuất
    cy.get('a[href="/exports/new"]').first().click();
    cy.url().should('include', '/exports/new');

    // Chon khách hàng tuỳ chọn (index 1)
    cy.get('select[name="customerId"]').select(1);

    // Điền địa chỉ giao hàng
    cy.get('input[name="deliveryAddress"]').clear().type('Địa chỉ giao hàng Test ' + Date.now());

    // Chọn sản phẩm đầu tiên
    cy.get('select[name="productIds"]').select(1);

    // Điền số lượng xuất
    cy.get('input[name="quantities"]').clear().type('5');
    
    // Đơn giá bán (mặc định js đã load lên, ta có thể sửa thêm)
    cy.get('input[name="unitPrices"]').clear().type('30000');

    // Lưu phiếu xuất
    cy.get('button[type="submit"]').contains('Lưu phiếu xuất').click();

    // Verify redirect về danh sách phiếu xuất
    cy.location('pathname').should('eq', '/exports');
    cy.get('table').should('exist');
  });

  it('Validation - Không duyệt nếu chưa chọn sản phẩm', () => {
    cy.visit('/exports/new');

    // Xóa hết dòng
    cy.get('button.btn-delete').first().click();

    // Lưu ngay sẽ bị Alert cản lại
    cy.get('button[type="submit"]').contains('Lưu phiếu xuất').click();

    cy.on('window:alert', (text) => {
      expect(text).to.contains('Vui lòng thêm ít nhất 1 sản phẩm');
    });

    cy.url().should('include', '/exports/new');
  });
});
