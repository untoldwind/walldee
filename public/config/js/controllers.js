/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers', []);

    controllers.controller('Projects', ['$scope', 'projectService', function ($scope, projectService) {
        projectService.findAll().then(function (projects) {
            $scope.projects = projects;
        });
    }]);
});
