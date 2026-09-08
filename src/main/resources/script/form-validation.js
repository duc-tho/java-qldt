import JustValidate from "just-validate";

export function createFormValidator(formElement) {
    const validator = new JustValidate(formElement, {
        errorFieldCssClass: "input-error",
        errorLabelCssClass: "text-error text-sm mt-1 block",
        focusInvalidField: true,
    });

    formElement.querySelectorAll("[validate-data]").forEach((input) => {
        validator.addField(input, JSON.parse(input.getAttribute("validate-data")), {
            errorsContainer: `#${input.id}-error`,
        });
    });

    return validator;
}
