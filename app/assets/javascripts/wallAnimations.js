var animations = (function() {
    function animateElements() {
        $(".running").fadeTo(500, 0.3).fadeTo(500, 1.0, animateElements);
    }

    return {
        start: function() {
            animateElements();
        }
    }
})();

window['animations'] = animations;
