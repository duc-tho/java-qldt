document.body.addEventListener('htmx:configRequest', (event) => {
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');

    if (csrfHeader && csrfToken) {
        event.detail.headers[csrfHeader] = csrfToken;
    }
});