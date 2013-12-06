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
                        $timeout(function () {
                            element[0].focus();
                            moveCursorToEnd(element[0]);
                        });
                    }
                });
                element.bind('blur', function () {
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
    });

    directives.directive('confirmDelete', ['$document', function ($document) {
        return {
            restrict: 'A',
            scope: {
                title: '=confirmDelete',
                deleteAction: '=onDelete'
            },
            template: '<button class="btn btn-danger dropdown-toggle" ng-click="opened = !opened; $event.stopPropagation()">Delete <span class="caret"></span></button>' +
                '<ul class="dropdown-menu" role="menu">' +
                '<li><a ng-click="deleteAction()">Delete {{ title }}</a></li>' +
                '</ul>',
            link: function (scope, element, attrs) {
                element.addClass('btn-group');
                scope.opened = false;
                scope.$watch('opened', function (opened) {
                    if (opened)
                        element.addClass('open');
                    else
                        element.removeClass('open');
                });
                $document.bind('click', function () {
                    scope.$apply(scope.opened = false);
                })
            }
        };
    }]);

    directives.directive('popupButton', ['$document', function ($document) {
        return {
            restrict: 'A',
            controller: function ($scope) {
                $scope.opened = false;

                $scope.toggle = function () {
                    $scope.opened = !$scope.opened;
                }
            },
            link: function (scope, element, attrs) {
                element.addClass('btn-group');
                scope.$watch('opened', function (opened) {
                    if (opened)
                        element.addClass('open');
                    else
                        element.removeClass('open');
                });
                $document.bind('click', function () {
                    scope.$apply(scope.opened = false);
                })
            }
        }
    }]);

    directives.directive('showModal', ['$document', function ($document) {
        return {
            restrict: 'A',
            scope: {
                opened: '=showModal'
            },
            link: function (scope, element, attrs) {
                element.addClass('modal');

                scope.$watch('opened', function (opened) {
                    var body = angular.element($document[0].body);
                    console.log(body);
                    if (opened) {
                        body.addClass('modal-open');
                        element.addClass('in');
                        element.css('display', 'block');
                    } else {
                        body.removeClass('modal-open');
                        element.removeClass('in');
                        element.css('display', 'none');
                    }
                });
            }
        };
    }]);

    directives.directive('keepAspect', ['$document', function ($document) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                scope.getWidth = function() {
                    return element.prop('offsetWidth');
                };

                scope.onResize = function() {
                    scope.$apply();
                }

                scope.$watch(scope.getWidth, function(w) {
                    if ( w > 0 ) {
                        var h = w * 10.0 / 16.0;
                        element.css('height', h + "px");
                    }
                });

                angular.element(window).bind('resize', scope.onResize);
                scope.$on('$destroy', function() {
                    angular.element(window).off('resize', scope.onResize);
                });
            }
        };
    }]);
});