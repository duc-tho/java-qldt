document.addEventListener("change", (event) => {
    const table = document.getElementById("crud-table");
    if (!table || !table.contains(event.target)) {
        return;
    }

    const selectAll = event.target.closest("#crud-select-all");
    if (selectAll) {
        getRowCheckboxes(table).forEach((checkbox) => {
            checkbox.checked = selectAll.checked;
        });
    }

    if (selectAll || event.target.matches('[name="ids"]')) {
        syncBatchDeleteButton(table);
    }
});

document.addEventListener("htmx:afterSwap", (event) => {
    syncBatchDeleteButton(event.target);
});

function getRowCheckboxes(table) {
    return Array.from(table.querySelectorAll('tbody [name="ids"]'));
}

function syncBatchDeleteButton(table) {
    const batchDeleteButton = table.querySelector("[data-batch-delete-url]");
    if (batchDeleteButton) {
        batchDeleteButton.disabled = !getRowCheckboxes(table).some((checkbox) => checkbox.checked);
    }
}
