var editableFileDisplay = {
    downloadFile: function (href) {
        var link = document.createElement('a');
        link.download = href.substr(href.lastIndexOf('/') + 1);
        link.href = href;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
};
