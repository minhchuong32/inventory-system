// Script Base.html: SideBar
function updateTime() {
    const el = document.getElementById("current-time");
    if (el) el.textContent = new Date().toLocaleString("vi-VN");
}
updateTime();
setInterval(updateTime, 1000);
document
    .getElementById("sidebarToggle")
    ?.addEventListener("click", () =>
        document.querySelector(".sidebar").classList.toggle("open"),
    );
setTimeout(() => {
    document.querySelectorAll(".alert-custom").forEach((el) => {
        el.style.transition = "opacity 0.5s";
        el.style.opacity = "0";
        setTimeout(() => el.remove(), 500);
    });
}, 4000);
// End Script Base.html