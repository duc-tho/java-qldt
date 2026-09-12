function registerCrudDeleteModal() {
    Alpine.data("crudDeleteModal", () => ({
        name: "",

        init() {
            this.$refs.confirm.addEventListener("htmx:afterRequest", (event) => {
                if (event.detail.successful) {
                    this.$refs.dialog.close();
                }
            });
        },

        open({ url, name, ids = [], template }) {
            let requestUrl = url;

            if (ids.length > 0) {
                const query = new URLSearchParams();
                ids.forEach((id) => query.append("ids", id));
                requestUrl += "?" + query.toString();
                this.name = template.replace("{0}", ids.length);
            } else {
                this.name = name || "";
            }

            this.$refs.confirm.setAttribute("hx-delete", requestUrl);

            const table = document.getElementById("crud-table");
            if (table) {
                this.$refs.confirm.setAttribute("hx-target", "#crud-table");
                this.$refs.confirm.setAttribute("hx-swap", "outerHTML");
                this.$refs.confirm.setAttribute(
                    "hx-include",
                    "[name='search'], [name='sort'], [name='dir'], [name='size'], [name^='filters[']",
                );
            } else {
                this.$refs.confirm.removeAttribute("hx-target");
                this.$refs.confirm.removeAttribute("hx-swap");
                this.$refs.confirm.removeAttribute("hx-include");
            }

            window.htmx.process(this.$refs.confirm);
            this.$refs.dialog.showModal();
        },
    }));
}

if (window.Alpine) {
    registerCrudDeleteModal();
} else {
    document.addEventListener("alpine:init", registerCrudDeleteModal);
}
