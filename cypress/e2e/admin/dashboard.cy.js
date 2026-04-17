describe('Dashboard (Bảng điều khiển)', () => {
  beforeEach(() => {
    cy.visit('/auth/login');
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');
    cy.get('#btnLogin').click();
    cy.url().should('include', '/dashboard');
  });

  it('Hiển thị đầy đủ các thẻ thống kê tổng quan', () => {
    // 4 thẻ stats chính
    cy.get('.stats-grid .stat-card').should('have.length', 4);
    
    cy.get('.stats-grid').contains('Tổng sản phẩm');
    cy.get('.stats-grid').contains('Nhà cung cấp');
    cy.get('.stats-grid').contains('Khách hàng');
    cy.get('.stats-grid').contains('Giá trị tồn kho');
  });

  it('Hiển thị các thẻ mini stat (Phiếu nhập xuất tháng này)', () => {
    cy.get('.mini-stat-card').should('have.length', 4);
    
    cy.get('.mini-stat-card').contains('Phiếu nhập tháng này');
    cy.get('.mini-stat-card').contains('Phiếu xuất tháng này');
    cy.get('.mini-stat-card').contains('Sản phẩm sắp hết');
    cy.get('.mini-stat-card').contains('Sản phẩm hết hàng');
  });

  it('Điều hướng Thao tác nhanh (Quick Actions) hoạt động đúng', () => {
    // Click Tạo phiếu nhập
    cy.get('a[href="/imports/new"]').contains('Tạo phiếu nhập').click();
    cy.url().should('include', '/imports/new');
    cy.go('back');

    // Click Tạo phiếu xuất
    cy.get('a[href="/exports/new"]').contains('Tạo phiếu xuất').click();
    cy.url().should('include', '/exports/new');
    cy.go('back');

    // Click Báo cáo
    cy.get('a[href="/reports"]').contains('Báo cáo').click();
    cy.url().should('include', '/reports');
  });
});
