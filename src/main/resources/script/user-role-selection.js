function registerUserRoleSelection() {
  Alpine.data("userRoleSelection", () => ({
    init() {
      this.select();
    },

    select(checkbox) {
      const checkboxes = [...this.$root.querySelectorAll('input[name="roleIds"]')];
      const roleName = (input) => input.closest('[data-role-name]')?.dataset.roleName;
      if (checkbox?.checked && roleName(checkbox) === "STUDENT") {
        checkboxes.filter((input) => input !== checkbox).forEach((input) => {
          input.checked = false;
        });
      }

      const selected = checkboxes.filter((input) => input.checked);
      const selectedRole = selected.length === 1 ? roleName(selected[0]) : null;

      checkboxes.forEach((input) => {
        const role = roleName(input);
        const allowedPair =
          (selectedRole === "ADMIN" && ["LECTURER", "HEAD_OF_FACULTY"].includes(role)) ||
          (role === "ADMIN" && ["LECTURER", "HEAD_OF_FACULTY"].includes(selectedRole));
        const canSwitchToStudent =
          role === "STUDENT" && ["LECTURER", "HEAD_OF_FACULTY"].includes(selectedRole);

        input.disabled = !input.checked && selected.length > 0 && !allowedPair && !canSwitchToStudent;
      });
    },
  }));
}

if (window.Alpine) {
  registerUserRoleSelection();
} else {
  document.addEventListener("alpine:init", registerUserRoleSelection);
}
