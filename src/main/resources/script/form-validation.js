import JustValidate from "just-validate";

export function createFormValidator(formElement) {
  const validator = new JustValidate(formElement, {
    errorFieldCssClass: "input-error",
    errorLabelCssClass: "text-error text-sm mt-1 block",
    focusInvalidField: true,
  });

  formElement.querySelectorAll("[validate-data]").forEach((input) => {
    const rules = JSON.parse(input.getAttribute("validate-data"));

    if (rules.length === 0) {
      return;
    }

    const matchSelector = input.dataset.validateMatch;
    const matchInput = matchSelector ? formElement.querySelector(matchSelector) : null;
    if (matchInput) {
      rules.push({
        validator: (value) => value === matchInput.value,
        errorMessage: input.dataset.validateMatchMessage,
      });
    }

    validator.addField(input, rules, {
      errorsContainer: `#${input.id}-error`,
    });

    if (formElement.dataset.liveValidation !== undefined) {
      validator.addListener("input", input, () => {
        void validator.revalidateField(input);
      });

      if (matchInput) {
        validator.addListener("input", matchInput, () => {
          void validator.revalidateField(input);
        });
      }
    }
  });

  return validator;
}
