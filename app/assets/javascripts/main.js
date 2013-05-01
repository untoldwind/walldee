
log = function() {
    if(typeof console.log !== "undefined" && console.log !== null)
        console.log.apply(console, arguments);
};

$(document).on("click", ".options dt, .delete dt", function(e) {
    e.preventDefault();
    if($(e.target).parent().hasClass("opened")) {
        $(e.target).parent().removeClass("opened");
    } else {
        $(e.target).parent().addClass("opened");
        $(document).one("click", function() {
            $(e.target).parent().removeClass("opened");
        })
    }
    return false;
});

$(document).on("click", ".delete dd a", function(e) {
	e.preventDefault();
    var href = e.target.href;
    var ref = e.target.attributes["ref"].value;
    $.ajax({
		url: href,
		type: "DELETE",
        dataType: "script",
		error: function(request, error, exception) {
			alert(exception);
		}
	});
});

$(document).on("click", ".edit-switch", function(e) {
    e.preventDefault();
    var ref = e.target.attributes["ref"].value;
    var editRef = e.target.attributes["editRef"].value;
    $(ref).hide();
    $(editRef).show();
});

$(document).on("click", "button.create", function(e) {
    e.preventDefault();
    var action = e.target.attributes["action"].value;
    var ref = e.target.attributes["ref"].value;
    var dataRef = e.target.attributes["dataRef"].value;
    var data = $(dataRef).find("*").serialize();

    $.ajax({
        url: action,
        data: data,
        type: "POST",
        success: function(result) {
            $(ref).html(result);
        },
        error: function(request, error, exception) {
            alert(exception);
        }
    });
});

$(document).on("click", "button.update", function(e) {
    e.preventDefault();
    var action = e.target.attributes["action"].value;
    var ref = e.target.attributes["ref"].value;
    var dataRef = e.target.attributes["dataRef"].value;
    var data = $(dataRef).find("*").serialize();

    $.ajax({
        url: action,
        data: data,
        type: "PUT",
        success: function(result) {
            $(ref).html(result);
        },
        error: function(request, error, exception) {
            alert(exception);
        }
    });
});

var utils = (function() {
    /*
     * date formatting is adapted from
     * http://jacwright.com/projects/javascript/date_format
     */
    function _formatDate(date, format) {
        var returnStr = '';
        for (var i = 0; i < format.length; i++) {
            var curChar = format.charAt(i);
            if (i != 0 && format.charAt(i - 1) == '\\') {
                returnStr += curChar;
            }
            else if (replaceChars[curChar]) {
                returnStr += replaceChars[curChar](date, this);
            } else if (curChar != '\\') {
                returnStr += curChar;
            }
        }
        return returnStr;
    }

    var replaceChars = {
        // Day
        d: function(date) { return (date.getDate() < 10 ? '0' : '') + date.getDate(); },
        j: function(date) { return date.getDate(); },
        N: function(date) { var _d = date.getDay(); return _d ? _d : 7; },
        S: function(date) { return (date.getDate() % 10 == 1 && date.getDate() != 11 ? 'st' : (date.getDate() % 10 == 2 && date.getDate() != 12 ? 'nd' : (date.getDate() % 10 == 3 && date.getDate() != 13 ? 'rd' : 'th'))); },
        w: function(date) { return date.getDay(); },
        z: function(date) { var d = new Date(date.getFullYear(), 0, 1); return Math.ceil((date - d) / 86400000); }, // Fixed now
        // Week
        W: function(date) { var d = new Date(date.getFullYear(), 0, 1); return Math.ceil((((date - d) / 86400000) + d.getDay() + 1) / 7); }, // Fixed now
        // Month
        M: function(date) { return (date.getMonth() < 9 ? '0' : '') + (date.getMonth() + 1); },
        n: function(date) { return date.getMonth() + 1; },
        t: function(date) { var d = date; return new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate() }, // Fixed now, gets #days of date
        // Year
        L: function(date) { var year = date.getFullYear(); return (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)); },  // Fixed now
        o: function(date) { var d = new Date(date.valueOf()); d.setDate(d.getDate() - ((date.getDay() + 6) % 7) + 3); return d.getFullYear();}, //Fixed now
        Y: function(date) { return date.getFullYear(); },
        y: function(date) { return ('' + date.getFullYear()).substr(2); },
        // Time
        a: function(date) { return date.getHours() < 12 ? 'am' : 'pm'; },
        A: function(date) { return date.getHours() < 12 ? 'AM' : 'PM'; },
        B: function(date) { return Math.floor((((date.getUTCHours() + 1) % 24) + date.getUTCMinutes() / 60 + date.getUTCSeconds() / 3600) * 1000 / 24); }, // Fixed now
        g: function(date) { return date.getHours() % 12 || 12; },
        G: function(date) { return date.getHours(); },
        h: function(date) { return ((date.getHours() % 12 || 12) < 10 ? '0' : '') + (date.getHours() % 12 || 12); },
        H: function(date) { return (date.getHours() < 10 ? '0' : '') + date.getHours(); },
        m: function(date) { return (date.getMinutes() < 10 ? '0' : '') + date.getMinutes(); },
        s: function(date) { return (date.getSeconds() < 10 ? '0' : '') + date.getSeconds(); },
        u: function(date) { var m = date.getMilliseconds(); return (m < 10 ? '00' : (m < 100 ? '0' : '')) + m; },
        // Timezone
        e: function(date) { return 'Not Yet Supported'; },
        I: function(date) { return 'Not Yet Supported'; },
        O: function(date) { return (-date.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(date.getTimezoneOffset() / 60) < 10 ? '0' : '') + (Math.abs(date.getTimezoneOffset() / 60)) + '00'; },
        P: function(date) { return (-date.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(date.getTimezoneOffset() / 60) < 10 ? '0' : '') + (Math.abs(date.getTimezoneOffset() / 60)) + ':00'; }, // Fixed now
        T: function(date) { var m = date.getMonth(); date.setMonth(0); var result = date.toTimeString().replace(/^.+ \(?([^\)]+)\)?$/, '$1'); date.setMonth(m); return result;},
        Z: function(date) { return -date.getTimezoneOffset() * 60; },
        // Full Date/Time
        U: function(date) { return date.getTime() / 1000; }
    }

    return {
        formatDate: function(date, format) {
            return _formatDate(date, format);
        }
    }
})();

window['utils'] = utils;


