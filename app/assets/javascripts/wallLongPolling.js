var longPolling = function () {
    function applyUpdates(displayUpdate, unit) {
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
            $(selector).css("left", update.posx + unit);
            $(selector).css("top", update.posy + unit);
            $(selector).css("width", update.width + unit);
            $(selector).css("height", update.height + unit);
            $(selector).html("<div class=\"inner " + update.style + "\">" + update.content + "</div>");
        });
        animations.start(displayUpdate.animationCycles, displayUpdate.animationDelay);
    }

    function getUpdates(url, unit) {
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
                applyUpdates(result, unit);
                getUpdates(url, unit);
            },
            error: function (request, error, exception) {
                getUpdates(url, unit);
            }
        });
    }

    return {
        start: function (url, unit) {
            window.setTimeout("location.reload(true);", 4 * 3600 * 1000);
            getUpdates(url, unit);
        }
    }
}();

window['longPolling'] = longPolling;