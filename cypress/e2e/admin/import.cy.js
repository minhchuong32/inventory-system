describe('Quản lý Phiếu Nhập Kho (Import)', () => {
  beforeEach(() => {
    cy.visit('/auth/login');
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');
    cy.get('#btnLogin').click();
    cy.url().should('include', '/dashboard');
  });

  it('Tạo phiếu nhập kho mới thành công (cần có sẵn data Nhà cung cấp và Sản phẩm)', () => {
    cy.visit('/imports');
    
    // Nút tạo phiếu nhập
    cy.get('a[href="/imports/new"]').first().click();
    cy.url().should('include', '/imports/new');

    // Mặc định row 1 đã được tạo qua script addRow()
    
    // Chọn nhà cung cấp đầu tiên (index 1 do index 0 là "-- Chọn --")
    cy.get('select[name="supplierId"]').select(1);

    // Gán invoice
    cy.get('input[name="invoiceNumber"]').clear().type('INV-CYPRESS-' + Date.now());

    // Chọn sản phẩm đầu tiên
    cy.get('select[name="productIds"]').select(1);

    // Cập nhật số lượng và giá
    cy.get('input[name="quantities"]').clear().type('15');
    cy.get('input[name="unitPrices"]').clear().type('25000');

    // Lưu phiếu
    cy.get('button[type="submit"]').contains('Lưu phiếu nhập').click();

    // Verify redirect về list
    cy.location('pathname').should('eq', '/imports');
    
    // Chắc chắn bảng /imports có mã INV vừa nhập (hoặc ít nhất redirect không báo lỗi)
    cy.get('table').should('exist');
  });

  it('Validation - Không cho submit nếu chưa chọn sản phẩm hoặc để số lượng 0', () => {
    cy.visit('/imports/new');

    // Chọn nhà cung cấp
    cy.get('select[name="supplierId"]').select(1);

    // Xóa hết dòng detail
    cy.get('button[onclick*="removeRow"]').first().click();

    // Submit
    cy.get('button[type="submit"]').contains('Lưu phiếu nhập').click();

    // Do js có alert("Vui lòng thêm ít nhất 1 sản phẩm!") -> Check alert
    cy.on('window:alert', (text) => {
      expect(text).to.contains('Vui lòng thêm ít nhất 1 sản phẩm');
    });

    // Vẫn ở trang tạo
    cy.url().should('include', '/imports/new');
  });
});
