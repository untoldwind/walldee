var animations = (function() {
    function animateElements() {
        $(".bigText").textfill({debug: true, maxFontPixels: 0});

        $(".running").fadeTo(500, 0.3).fadeTo(500, 1.0, animateElements);
    }

    return {
        start: function() {
            animateElements();
        }
    }
})();

window['animations'] = animations;
