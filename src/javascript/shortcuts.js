import {textSizer} from './utils/textsizer';

export function initShortcuts() {
    $(function () {
        $('#shortcuts').children('ul').hide();
        $('#shortcuts').on('mouseover', function () {
            $(this).children('ul').show();
        }).on('mouseout', function () {
            $(this).children('ul').hide();
        });
    });
    init();
}

export function init() {
    $(function () {
        document.getElementById("print")
            .addEventListener("click", function() {
                window.print();
                return false;
            });

        addTextSizerClickHandler('.shortcuts-typoincrease a', 1);
        addTextSizerClickHandler('.shortcuts-typoreduce a', -1);
    });
}

function addTextSizerClickHandler(selector, increment) {
    document.querySelector(selector)
        .addEventListener('click', function () {
            textSizer('body', increment);
        });
}
