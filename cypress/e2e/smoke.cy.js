describe("Inventory System smoke test", () => {
  it("loads the login page", () => {
    cy.request("/auth/login").then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.include("loginForm");
      expect(response.body).to.include("username");
      expect(response.body).to.include("password");
    });
  });
});
