import $ from 'jquery';
import 'jquery-validation';

(function initAddComment() {
    console.log('getting called from my own script');

    // Retrieve jsp param through DOM element
    const isLoggedIn = document.getElementById('addComment')?.dataset.loggedIn === 'true';
    console.log(`isLoggedIn: ${isLoggedIn}`);

    const rules = {'jcr:title': 'required'};
    if (!isLoggedIn) {
        rules.pseudo = 'required';
    }
    console.log('rules: ' + JSON.stringify(rules));

    $(document).ready(() => {
        $("#newCommentForm").validate({rules});
    });
})();
