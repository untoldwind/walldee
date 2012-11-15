
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
		success: function(result) {
			$(ref).html(result);
		},
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
