import $ from 'jquery';

/**
 * categorizing.jsp, displayCategories.jsp can only be added one per page.
 * Needs rework to make it work with multiple instances
 */

var uuids = [];
export function initAdd(uuid, postUrl) {
    $(function() {
        var submitBtn = document.getElementById('categorySubmit');
        if (submitBtn) {
            submitBtn.addEventListener('click', function() {addCategory(uuid, postUrl)});
        }
    });
};

export function initDisplay(uuid, fetchedUuids, postUrl) {
    $(function() {
        uuids = fetchedUuids;
        var deleteCategories = document.getElementsByClassName('deleteCategory');
        for (var i = 0; i < deleteCategories.length; i++) {
            deleteCategories[i].addEventListener('click', function(e) {
                deleteCategory(uuid, e.currentTarget.id, postUrl);
            });
        }
    });
}

function addCategory(uuid, postUrl) {
    var categoryUuid = $('#categorytoadd').val();

    var isAlreadyExist = uuids.indexOf(categoryUuid) >= 0;
    if (categoryUuid === '' || isAlreadyExist) {
        return false;
    }

    uuids.push(categoryUuid);
    var payload = {
        'j:defaultCategory': uuids,
        'jcrMethodToCall': 'put',
        'jcr:mixinTypes': 'jmix:categorized'
    };
    $.ajaxSetup({traditional: true, cache: false});
    $.post(postUrl, payload, function() {addCategoryCallback(uuid, categoryUuid, postUrl)}, 'json');
}

function addCategoryCallback(uuid, categoryUuid, postUrl) {
    var addedItems = $('.nocategorizeditem' + uuid);
    var separator = ', ';
    if (addedItems.length > 0 && addedItems.is(':visible')) {
        addedItems.hide();
        separator = '';
    }

    var catVal = $('#category').val();
    var catDiv = $('<div id="category' + categoryUuid + '" style="display:inline"></div>');
    var catDisplay = $('<span class="categorizeditem">' + catVal + '</span>');
    var catLinkDelete = $('<a class="delete deleteCategory" id="' + categoryUuid + '" href="#"></a>');
    catLinkDelete.on('click', function() {deleteCategory(uuid, categoryUuid, postUrl)});

    $('#jahia-categories-' + uuid).append(catDiv);
    catDiv.append(separator)
    catDiv.append(catDisplay);
    catDiv.append(catLinkDelete);

    $('#category').val('');
    $('#categorySubmit').hide();
}

function deleteCategory(uuid, categoryUuid, postUrl) {
    var uuidIndex = uuids.indexOf(categoryUuid);
    if (uuidIndex >= 0) {
        uuids.splice(uuidIndex, 1);
    }

    $.ajaxSetup({traditional: true, cache: false});
    if (uuids.length === 0) {
        $.post(postUrl,
            {'jcrMethodToCall': 'put', 'jcrRemoveMixin': 'jmix:categorized'},
            function() {
                $('#category' + categoryUuid).hide();
                var spanNoYetCat = $('<span class="nocategorieditem' + uuid + '"></span>');
                var noCategoryMsg = $('<fmt:message key="label.categories.noCategory"/>');
                spanNoYetCat.append(noCategoryMsg);
                $('#jahia-categories-' + uuid).append(spanNoYetCat);
            }, 'json');
    } else {
        $.post(postUrl,
            {'j:defaultCategory': uuids, 'jcrMethodToCall': 'put', 'jcr:mixinTypes': 'jmix:categorized'},
            function() {$('#category' + categoryUuid).hide()}, 'json');
    }
    return false;
}
