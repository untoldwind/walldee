/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers.displays', []);

    controllers.controller('Displays', ['$scope', '$route', '$location', 'displayResource',
        function ($scope, $route, $location, displayResource) {
            function loadDisplays(selectedDisplayId) {
                displayResource.query().$promise.then(function (displays) {
                    $scope.select(null);
                    $scope.displays = displays;

                    angular.forEach(displays, function (display) {
                        if (display.id == selectedDisplayId) {
                            $scope.select(display);
                        }
                    });
                });
            }

            loadDisplays($route.current.params.displayId);

            $scope.select = function (display) {
                $scope.selectedDisplay = display;
                $location.search('displayId', $scope.selectedDisplay != null ? $scope.selectedDisplay.id : null);
            };

            $scope.isSelected = function (display) {
                if (display == null)
                    return $scope.selectedDisplay != null && $scope.selectedDisplay.id == null;
                else
                    return $scope.selectedDisplay != null && display.id == $scope.selectedDisplay.id;
            };

            $scope.newDisplay = function () {
                $scope.selectedDisplay = { name: '' };
                $location.search('displayId', null);
            };
        }]);

    controllers.controller('Display', ['$scope', '$route', '$location', 'displayItemResource',
        function ($scope, $route, $location, displayItemResource) {
            $scope.$watch('selectedDisplay', function (display) {
                $scope.display = display;
                if (display != null && display.id != null) {
                    $scope.displayItems = displayItemResource.query({displayId: display.id});
                    $scope.displayInfo = {
                        width: 100,
                        height: 100
                    }
                    $scope.displayItems.$promise.then(function (displayitems) {
                        angular.forEach(displayitems, function (displayItem) {
                            if ( displayItem.posx + displayItem.width > $scope.displayInfo.width ) {
                                $scope.displayInfo.width = displayItem.posx + displayItem.width;
                            }
                            if ( displayItem.posy + displayItem.height > $scope.displayInfo.height ) {
                                $scope.displayInfo.height = displayItem.posy + displayItem.height;
                            }
                        });
                    })
                }
            });

            $scope.isNoDisplay = function () {
                return $scope.display == null;
            };

            $scope.isDisplayEdit = function () {
                return $scope.display != null && $scope.display.id != null;
            };

            $scope.isDisplayCreate = function () {
                return $scope.display != null && $scope.display.id == null;
            };

            $scope.itemPosition = function(displayItem) {
                return {
                    position: 'absolute',
                    left: (displayItem.posx * 100.0 / $scope.displayInfo.width ) + "%",
                    top: (displayItem.posy * 100.0 / $scope.displayInfo.height ) + "%",
                    width: (displayItem.width * 100.0 / $scope.displayInfo.width ) + "%",
                    height: (displayItem.height * 100.0 / $scope.displayInfo.height ) + "%"
                };
            }
        }]);
});
