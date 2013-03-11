var longPolling = function () {
    function applyUpdates(displayUpdate) {
        console.log(displayUpdate);
        $.each(displayUpdate.removeWidgets, function (idx, removedWidgetId) {
            $("#" + removedWidgetId).remove();
        });
        $.each(displayUpdate.changedWidgets, function (idx, update) {
            var selector = "#" + update.id;
            if ($(selector).length == 0) {
                $("#displayItems").append("<div id=\"" + update.id + "\"></div>");
            }
            $(selector).attr("class", "widget");
            $(selector).addClass("outer");
            $(selector).attr("etag", update.etag);
            $(selector).css("left", update.posx + "px");
            $(selector).css("top", update.posy + "px");
            $(selector).css("width", update.width + "px");
            $(selector).css("height", update.height + "px");
            $(selector).html("<div class=\"inner " + update.style + "\">" + update.content + "</div>");
        });
        animations.start();
    }

    function getUpdates(url) {
        var state = {};
        $("div.widget").each(function () {
            state[$(this).attr("id")] = $(this).attr("etag");
        });
        $.ajax({
            url: url,
            data: JSON.stringify(state),
            dataType: "json",
            contentType: "application/json",
            type: "POST",
            success: function (result) {
                applyUpdates(result);
                getUpdates(url);
            },
            error: function (request, error, exception) {
                getUpdates(url);
            }
        });
    }

    return {
        start: function (url) {
            window.setTimeout("location.reload(true);", 4 * 3600 * 1000);
            getUpdates(url);
        }
    }
}();

window['longPolling'] = longPolling;