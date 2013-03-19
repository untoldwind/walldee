var animations = (function () {
    log = function () {
        if (typeof console.log !== "undefined" && console.log !== null)
            console.log.apply(console, arguments);
    };

    var animationCycle = -1;
    var animations = {};
    var nextCycleQueued = false;

    function applyAnimations() {
        animationCycle += 1;
        animationCycle = animationCycle % animations.length;

        var animation = animations[animationCycle];

        log("Cylce: " + animationCycle);
        log("Animation: ");
        log(animation);
        log(animation.widgetIds);
        log(animation.widgetIds.length);

        if ( animation.widgetIds.length > 0) {
            var selector = $.map(animation.widgetIds, function (elem, i) {
                return "#displayItem-" + elem
            }).join(",");

            log(selector);
            log(animation.effect);
            log(animation.params)

            $(selector).toggle(animation.effect, animation.options, animation.duration);
        }

        if ( !nextCycleQueued && animation.delay > 0 ) {
            nextCycleQueued = true;
            window.setTimeout("animations.nextCycle()", animation.delay);
        } else {
            applyAnimations();
        }
    }

    function animateElements() {
        $(".bigText").textfill({debug: false, maxFontPixels: 0});

        $(".running").fadeTo(500, 0.3).fadeTo(500, 1.0, animateElements);
    }

    return {
        start: function (config) {
            log("Start with config");
            log(config);
            log(typeof config.animations);
            animateElements();

            if (typeof config.animations !== "undefined" && config.animations !== null) {
                animations = config.animations;
                applyAnimations();
            }
        },

        nextCycle: function () {
            nextCycleQueued = false;
            applyAnimations();
        }
    }
})();

window['animations'] = animations;
