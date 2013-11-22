/*global define */

'use strict';

define(['angular'], function(angular) {

    /* Directives */

    angular.module('myApp.directives', []).
        directive('appVersion', ['version', function(version) {
            return function(scope, elm, attrs) {
                elm.text(version);
            };
        }]).
        directive('activeLink', ['$location', function(location) {
            return {
                restrict: 'A',
                link: function(scope, element, attrs, controller) {
                    var path = attrs.href;
                    path = path.substring(1);
                    scope.location = location;
                    scope.$watch('location.path()', function(newPath) {
                        if (path === newPath) {
                            element.addClass('active');
                        } else {
                            element.removeClass('active');
                        }
                    });
                }
            }
        }]);
});