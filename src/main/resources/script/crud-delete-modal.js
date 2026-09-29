function registerCrudDeleteModal() {
  Alpine.data("crudDeleteModal", () => ({
    ids: [],
    name: "",

    open({ url, name, ids = [], template }) {
      this.ids = ids;
      this.name =
        ids.length > 0 ? template.replace("{0}", ids.length) : name || "";
      this.$refs.form.action = url;
      this.$refs.dialog.showModal();
    },
  }));
}

if (window.Alpine) {
  registerCrudDeleteModal();
} else {
  document.addEventListener("alpine:init", registerCrudDeleteModal);
}
