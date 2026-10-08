function registerPageBack() {
  Alpine.data("pageBack", () => ({
    hasHistory: window.history.length > 1,

    goBack() {
      if (this.hasHistory) {
        window.history.back();
        return;
      }

      window.location.assign("/");
    },
  }));
}

if (window.Alpine) {
  registerPageBack();
} else {
  document.addEventListener("alpine:init", registerPageBack);
}
