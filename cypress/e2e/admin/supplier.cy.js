describe('Quản lý Nhà cung cấp', () => {
  beforeEach(() => {
    cy.visit('/auth/login');
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');
    cy.get('#btnLogin').click();
    cy.url().should('include', '/dashboard');
  });

  it('Thêm Nhà cung cấp mới thành công', () => {
    cy.visit('/suppliers');
    
    // Nút Thêm mới (dùng thuộc tính href)
    cy.get('a[href="/suppliers/new"]').first().click();
    cy.url().should('include', '/suppliers/new');

    const supplierName = 'Nhà Cung Cấp ' + Date.now();
    const phone = '09' + Math.floor(10000000 + Math.random() * 90000000);

    // Thông tin cơ bản
    cy.get('input[name="name"]').clear().type(supplierName);
    cy.get('input[name="contactPerson"]').clear().type('Nguyễn Văn B');
    cy.get('input[name="phone"]').clear().type(phone);
    cy.get('input[name="email"]').clear().type('supplier' + Date.now() + '@example.com');
    cy.get('input[name="taxCode"]').clear().type('03' + Math.floor(10000000 + Math.random() * 90000000));
    cy.get('input[name="address"]').clear().type('Khu công nghiệp VSIP');

    // Thông tin tài chính
    cy.get('input[name="bankAccount"]').clear().type('1234567890');
    cy.get('input[name="bankName"]').clear().type('Vietcombank');
    
    // Loại NCC
    cy.get('select[name="supplierType"]').select('REGULAR');

    // Lưu
    cy.get('button[type="submit"]').contains('Lưu nhà cung cấp').click();

    // Verify
    cy.location('pathname').should('eq', '/suppliers');
    cy.get('input[name="search"]').clear().type(supplierName + '{enter}');
    cy.get('table').should('contain', phone);
  });

  it('Validation - Thiếu tên nhà cung cấp', () => {
    cy.visit('/suppliers/new');
    
    cy.get('button[type="submit"]').click();

    // Verify lỗi hiển thị
    cy.get('.is-invalid').should('exist');
    cy.get('.invalid-feedback').should('be.visible');
  });

  it('Tìm kiếm Nhà cung cấp', () => {
    cy.visit('/suppliers');
    
    cy.get('input[name="search"]').clear().type('Cty TM Sao Mai{enter}');
    cy.url().should('include', `search=${encodeURIComponent('Cty TM Sao Mai').replace(/%20/g, '+')}`);
    cy.get('table').should('contain', 'Cty TM Sao Mai');
  });
});
