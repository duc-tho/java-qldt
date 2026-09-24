import { createFormValidator } from "./form-validation.js";

function registerFormValidation() {
    Alpine.data("formValidation", () => ({
        validator: null,

        init() {
            this.validator = createFormValidator(this.$el);
        },

        async submit() {
            if (await this.validator.revalidate()) {
                HTMLFormElement.prototype.submit.call(this.$el);
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
