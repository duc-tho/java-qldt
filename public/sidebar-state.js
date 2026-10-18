const sidebarToggle = document.getElementById("my-drawer-4");

try {
  sidebarToggle.checked = localStorage.getItem("sidebar-open") !== "false";
} catch {
  sidebarToggle.checked = true;
}

sidebarToggle.addEventListener("change", () => {
  try {
    localStorage.setItem("sidebar-open", sidebarToggle.checked);
  } catch {
    // Keep the sidebar usable when storage is unavailable.
  }
});
