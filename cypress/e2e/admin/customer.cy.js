describe('Quản lý Khách hàng', () => {
  beforeEach(() => {
    cy.visit('/auth/login');
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');
    cy.get('#btnLogin').click();
    cy.url().should('include', '/dashboard');
  });

  it('Thêm khách hàng mới thành công', () => {
    cy.visit('/customers');
    
    // Bấm nút thêm mới
    cy.get('a[href="/customers/new"]').first().click();
    cy.url().should('include', '/customers/new');

    const customerName = 'Khách hàng Test ' + Date.now();
    const phone = '09' + Math.floor(10000000 + Math.random() * 90000000);

    // Điền thông tin
    cy.get('input[name="name"]').clear().type(customerName);
    cy.get('input[name="phone"]').clear().type(phone);
    cy.get('input[name="email"]').clear().type('test' + Date.now() + '@example.com');
    cy.get('input[name="address"]').clear().type('Địa chỉ Test');
    
    // Loại khách hàng (chọn option 2: WHOLESALE)
    cy.get('select[name="customerType"]').select('WHOLESALE');

    // Lưu
    cy.get('button[type="submit"]').contains('Lưu').click();

    // Verify
    cy.location('pathname').should('eq', '/customers');
    cy.get('table').should('contain', customerName).and('contain', phone);
  });

  it('Validation - Thiếu tên khách hàng', () => {
    cy.visit('/customers/new');
    
    const phone = '0912345678';
    cy.get('input[name="phone"]').clear().type(phone);
    
    // Lưu
    cy.get('button[type="submit"]').click();

    // Verify vẫn ở trang form và có lỗi
    cy.get('.is-invalid').should('exist');
    cy.get('.invalid-feedback').should('be.visible');
  });

  it('Tìm kiếm khách hàng', () => {
    cy.visit('/customers');
    
    // Giả sử có "Khách hàng Test"
    cy.get('input[name="search"]').clear().type('Siêu{enter}');
    // Sử dụng backticks (`) và encodeURIComponent để tự động chuyển 'Siêu' thành 'Si%C3%AAu'
cy.url().should('include', `search=${encodeURIComponent('Siêu')}`);
  });
});
