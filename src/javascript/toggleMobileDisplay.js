import Cookies from 'js-cookie';

export function initCookieHandler(cookieName, isEmptyCookie) {
    $(function () {
        if (isEmptyCookie) {
            addCookieHandler('#forceGenericChannel', cookieName, 'generic');
        } else {
            addCookieHandler('#useCurrentChannel', cookieName, null);
        }
    });
}

function addCookieHandler(querySelector, cookieName, cookieValue) {
    document.getElementById(querySelector)
        .addEventListener('click', function () {
            Cookies.set(cookieName, cookieValue);
            location.reload();
        });
}
