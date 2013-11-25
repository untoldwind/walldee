/*global define */

'use strict';

define(['angular'], function(angular) {

    /* Directives */

    angular.module('walldee.directives', []).
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
                            element.parent().addClass('active');
                        } else {
                            element.parent().removeClass('active');
                        }
                    });
                }
            }
        }]);
});