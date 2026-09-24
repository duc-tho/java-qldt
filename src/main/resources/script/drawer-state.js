const DRAWER_STATE_KEY = "qldt.drawer.open";

const drawer = document.getElementById("my-drawer-4");

if (drawer) {
    try {
        drawer.checked = localStorage.getItem(DRAWER_STATE_KEY) !== "false";
        drawer.addEventListener("change", () => {
            localStorage.setItem(DRAWER_STATE_KEY, String(drawer.checked));
        });
    } catch {
        // Keep the server-rendered default when browser storage is unavailable.
    }
}
