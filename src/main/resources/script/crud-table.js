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

    async postAction(url, reason) {
      const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
      const headers = csrfToken && csrfHeader ? { [csrfHeader]: csrfToken } : {};
      const body = reason === undefined ? undefined : new URLSearchParams({ reason });
      const response = await fetch(url, { method: "POST", headers, body });
      if (response.redirected) {
        window.location.assign(response.url);
        return;
      }
      document.open();
      document.write(await response.text());
      document.close();
    },
  }));
}

if (window.Alpine) {
  registerCrudTable();
} else {
  document.addEventListener("alpine:init", registerCrudTable);
}
