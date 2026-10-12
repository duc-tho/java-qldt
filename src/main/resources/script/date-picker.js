import flatpickr from "flatpickr";
import { Vietnamese } from "flatpickr/dist/l10n/vn.js";
import "flatpickr/dist/flatpickr.min.css";

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-date-picker]").forEach((input) => {
    flatpickr(input, {
      enableTime: true,
      time_24hr: true,
      dateFormat: "d/m/Y H:i",
      locale: Vietnamese,
      allowInput: false,
      disableMobile: true,
      clickOpens: input.dataset.datePickerMode === "edit",
    });
  });
});
