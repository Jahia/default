import $ from 'jquery';
import 'jquery-validation'

export function initComment(isLoggedIn) {
    console.log('getting called from my own script');

    const rules = {'jcr:title': 'required'};
    if (!isLoggedIn) {
        rules.pseudo = 'required';
    }
    console.log('rules: ' + JSON.stringify(rules));

    $(document).ready(() => {
        $("#newCommentForm").validate({rules});
    });
}
