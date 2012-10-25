
log = function() {
    if(typeof console.log !== "undefined" && console.log !== null)
        console.log.apply(console, arguments)
}

$(document).on("click", ".options dt, .delete dt", function(e) {
    e.preventDefault();
    if($(e.target).parent().hasClass("opened")) {
        $(e.target).parent().removeClass("opened")
    } else {
        $(e.target).parent().addClass("opened")
        $(document).one("click", function() {
            $(e.target).parent().removeClass("opened")
        })
    }
    false
})

$(document).on("click", ".delete dd a", function(e) {
	e.preventDefault();
	$.ajax({
		url: e.target.href,
		type: "DELETE",
		success: function(result) {
			$(e.target).parent().parent().parent().remove()
		},
		error: function(request, error, exception) {
			alert(exception)
		}
	})
})