describe('Quản lý User (Nhân viên)', () => {
  beforeEach(() => {
    cy.visit('/auth/login');
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');
    cy.get('#btnLogin').click();
    cy.url().should('include', '/dashboard');
  });

  it('Thêm nhân viên mới thành công', () => {
    cy.visit('/users');
    
    // Nút Tạo tài khoản
    cy.get('a[href="/users/new"]').first().click();
    cy.url().should('include', '/users/new');

    const username = 'staff' + Date.now().toString().slice(-6);

    // Form
    cy.get('input[name="username"]').clear().type(username);
    cy.get('input[name="fullName"]').clear().type('Nhân viên Test');
    cy.get('input[name="email"]').clear().type(username + '@example.com');
    cy.get('input[name="phone"]').clear().type('0987654321');
    
    cy.get('select[name="role"]').select('STAFF');

    // Password
    cy.get('input[name="password"]').clear().type('staff123');
    cy.get('input[name="confirmPassword"]').clear().type('staff123');

    // Submit
    cy.get('button[type="submit"]').contains('Tạo tài khoản').click();

    // Verify redirect về list
    cy.location('pathname').should('eq', '/users');
    cy.get('table').should('contain', username);
  });

  it('Validation - Thiếu thông tin bắt buộc', () => {
    cy.visit('/users/new');
    
    // Cố tình không điền trường required (username ...) và click Submit
    cy.get('button[type="submit"]').click();

    // HTML5 Validation: Vẫn ở trang tạo
    cy.url().should('include', '/users/new');
  });
});
