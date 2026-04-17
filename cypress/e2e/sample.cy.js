describe('Kịch bản kiểm thử trang chủ Inventory System', () => {
  it('Truy cập trang chủ thành công', () => {
    // Bước 1: Mở trình duyệt web và đi tới đường link gốc (baseUrl)
    cy.visit('/auth/login');
    
    // Bước 2: Kiểm tra một thành phần cơ bản (chắc chắn có thẻ HTML <body> thì web mới chạy)
    cy.get('body').should('be.visible');
    
    // MẸO: Nếu web bạn có chữ "Đăng nhập" hay danh sách gì đó, có thể test như sau:
    // cy.contains('Đăng nhập').should('be.visible');
  });
});
