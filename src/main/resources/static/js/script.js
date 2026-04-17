function updateTime() {
  const el = document.getElementById("current-time");
  if (el) el.textContent = new Date().toLocaleString("vi-VN");
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function notificationThemeClass(item) {
  if (item?.badgeClass === "badge-low") return "low-stock";
  if (item?.badgeClass === "badge-pending") return "pending";
  if (item?.badgeClass === "badge-completed") return "completed";
  if (item?.badgeClass === "badge-cancelled") return "cancelled";
  return "pending";
}

async function fetchLowStockAlerts() {
  const listEl = document.getElementById("stockAlertList");
  const countEl = document.getElementById("stockAlertCount");
  const summaryEl = document.getElementById("stockAlertSummary");
  if (!listEl || !countEl || !summaryEl) return;

  try {
    const response = await fetch("/api/alerts", {
      headers: { Accept: "application/json" },
      credentials: "same-origin",
    });

    if (!response.ok) throw new Error("Failed to load alerts");

    const alerts = await response.json();
    countEl.hidden = alerts.length === 0;
    countEl.textContent = String(alerts.length);
    summaryEl.textContent =
      alerts.length === 0
        ? "Hiện chưa có thông báo mới."
        : `${alerts.length} thông báo đang chờ xem`;

    if (alerts.length === 0) {
      listEl.innerHTML =
        '<div class="stock-alert-empty"><i class="bi bi-check-circle-fill me-1"></i>Không có thông báo mới</div>';
      return;
    }

    listEl.innerHTML = alerts
      .map(
        (item) => `
            <a class="notification-item ${escapeHtml(notificationThemeClass(item))}" href="${escapeHtml(item.href || "#")}">
                <div class="notification-icon"><i class="bi ${escapeHtml(item.icon || "bi-bell-fill")}"></i></div>
                <div class="notification-body">
                    <div class="notification-title">${escapeHtml(item.title)}</div>
                    <div class="notification-message">${escapeHtml(item.message)}</div>
                    <div class="notification-meta">${escapeHtml(item.code)}${item.occurredAtText ? ` • ${escapeHtml(item.occurredAtText)}` : ""}</div>
                </div>
                <div class="notification-side">
                    <span class="notification-badge ${escapeHtml(item.badgeClass || "badge-pending")}">${escapeHtml(item.statusLabel || "")}</span>
                    ${item.quantity != null ? `<div class="notification-qty">${escapeHtml(item.quantity)}</div><div class="notification-threshold">min: ${escapeHtml(item.minQuantity)}</div>` : ""}
                </div>
            </a>
        `,
      )
      .join("");
  } catch (error) {
    countEl.hidden = true;
    summaryEl.textContent = "Không tải được thông báo.";
    listEl.innerHTML =
      '<div class="stock-alert-empty" style="background:#fff3e0;border-color:#ffd59e;color:#8a4b00;">Không thể tải dữ liệu thông báo.</div>';
  }
}

document.addEventListener("DOMContentLoaded", () => {
  updateTime();
  setInterval(updateTime, 1000);

  document.getElementById("sidebarToggle")?.addEventListener("click", () => {
    document.querySelector(".sidebar")?.classList.toggle("open");
  });

  const loadAlerts = () => fetchLowStockAlerts();
  loadAlerts();

  document
    .getElementById("stockAlertButton")
    ?.addEventListener("click", loadAlerts);
  setInterval(loadAlerts, 60000);

  document.querySelectorAll(".clickable-row[data-href]").forEach((row) => {
    row.addEventListener("click", (event) => {
      if (event.target.closest("a, button, input, textarea, select, label")) {
        return;
      }
      const href = row.getAttribute("data-href");
      if (href) window.location.href = href;
    });

    row.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        const href = row.getAttribute("data-href");
        if (href) window.location.href = href;
      }
    });
  });

  setTimeout(() => {
    document.querySelectorAll(".alert-custom").forEach((el) => {
      el.style.transition = "opacity 0.5s";
      el.style.opacity = "0";
      setTimeout(() => el.remove(), 500);
    });
  }, 4000);
});
