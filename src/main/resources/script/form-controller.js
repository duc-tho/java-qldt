import { createFormValidator } from "./form-validation.js";

function registerFormValidation() {
    Alpine.data("formValidation", () => ({
        validator: null,

        init() {
            this.validator = createFormValidator(this.$el);
        },

        async submit() {
            if (await this.validator.revalidate()) {
                this.$el.dispatchEvent(new CustomEvent("validated-submit", { bubbles: true }));
            }
        },

        destroy() {
            this.validator?.destroy();
        },
    }));
}

if (window.Alpine) {
    registerFormValidation();
} else {
    document.addEventListener("alpine:init", registerFormValidation);
}
