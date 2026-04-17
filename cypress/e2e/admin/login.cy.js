describe('Quản trị viên (Admin) Login Test', () => {
  it('Đăng nhập THÀNH CÔNG với đúng thông tin Admin', () => {
    // 1. Đi tới trang đăng nhập
    cy.visit('/auth/login');

    // 2. Định vị các ô input dựa theo ID (như trong file login.html) và gõ phím
    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('admin123');

    // 3. Nhấn nút Đăng Nhập
    cy.get('#btnLogin').click();

    // 4. Kiểm tra việc chuyển hướng. Hệ thống sẽ tự động retry chờ tối đa 4 giây để fetch API trả về.
    // Dựa theo file login.html, nếu fetch OK, nó sẽ redirect tới /dashboard
    cy.url().should('include', '/dashboard');
  });

  it('Đăng nhập THẤT BẠI khi mật khẩu sai', () => {
    cy.visit('/auth/login');

    cy.get('#username').clear().type('admin');
    cy.get('#password').clear().type('matkhau_taolao');
    cy.get('#btnLogin').click();

    // Kiểm tra phải có popup lỗi đỏ hiện lên ở trên và URL không bị đổi 
    cy.get('#error-msg').should('be.visible');
cy.get('#error-text-content')
  .should('be.visible')
  .and('contain', 'sai');
    cy.url().should('include', '/auth/login');
  });
});
