/*global define */

'use strict';

define(['angular'], function (angular) {

    function moveCursorToEnd(el) {
        if (typeof el.selectionStart == "number") {
            el.selectionStart = el.selectionEnd = el.value.length;
        } else if (typeof el.createTextRange != "undefined") {
            el.focus();
            var range = el.createTextRange();
            range.collapse(false);
            range.select();
        }
    }

    /* Directives */

    var directives = angular.module('walldee.directives', []);

    directives.directive('appVersion', ['version', function (version) {
        return function (scope, elm, attrs) {
            elm.text(version);
        };
    }]);

    directives.directive('activeLink', ['$location', function (location) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs, controller) {
                var path = attrs.href;
                path = path.substring(1);
                scope.location = location;
                scope.$watch('location.path()', function (newPath) {
                    if (path === newPath) {
                        element.parent().addClass('active');
                    } else {
                        element.parent().removeClass('active');
                    }
                });
            }
        }
    }]);

    directives.directive('setFocus', ['$timeout', '$parse', function ($timeout, $parse) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                var model = $parse(attrs.setFocus);
                scope.$watch(model, function (value) {
                    if (value) {
                        $timeout(function() {
                            element[0].focus();
                            moveCursorToEnd(element[0]);
                        });
                    }
                });
                element.bind('blur', function() {
                    scope.$apply(model.assign(scope, false));
                });
            }
        };
    }]);

    directives.directive('inPlaceEdit', function () {
        return {
            restrict: 'A',
            scope: {
                editText: "=inPlaceEdit",
                textChanged: "=inPlaceOnChange"
            },
            template: '<form ng-submit="textChanged(editText); formVisible=false" style="display: inline-block">' +
                '<span ng-hide="formVisible">{{ editText }} ' +
                '<button type="button" class="btn btn-xs btn-default" ng-click="formVisible=true">' +
                '<span class="glyphicon glyphicon-pencil"></span></button></span>' +
                '<span ng-show="formVisible">' +
                '<input type="text" ng-model="editText" set-focus="formVisible">' +
                '</span>' +
                '</form>'
        };
    })
});