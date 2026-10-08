function registerCrudTable() {
  Alpine.data("crudTable", () => ({
    selectedIds: [],

    get rowIds() {
      return Array.from(this.$root.querySelectorAll('tbody [name="ids"]')).map(
        (checkbox) => checkbox.value,
      );
    },

    get allSelected() {
      return (
        this.rowIds.length > 0 &&
        this.rowIds.every((id) => this.selectedIds.includes(id))
      );
    },

    toggleAll(event) {
      this.selectedIds = event.target.checked ? this.rowIds : [];
    },

    submitQuery(page, sort, dir) {
      const form = this.$refs.queryForm;

      if (page !== undefined) {
        form.elements.page.value = page;
      }
      if (sort !== undefined) {
        form.elements.sort.value = sort;
      }
      if (dir !== undefined) {
        form.elements.dir.value = dir;
      }

      form.submit();
    },
  }));
}

if (window.Alpine) {
  registerCrudTable();
} else {
  document.addEventListener("alpine:init", registerCrudTable);
}
