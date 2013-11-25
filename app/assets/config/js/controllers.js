/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers', []);

    controllers.controller('Projects', ['$scope', 'projectService', function ($scope, projectService) {
        $scope.selectedProject = null;

        $scope.select = function (project) {
            $scope.selectedProject = project;
        };

        $scope.newProject = function() {

        };

        projectService.findAll().then(function (projects) {
            $scope.projects = projects;
        });
    }]);

    controllers.controller('Teams', ['$scope', 'teamService', function ($scope, teamService) {
        $scope.selectedTeam = null;

        $scope.select = function (team) {
            $scope.selectedTeam = team;
        };

        $scope.newTeam = function () {

        };

        teamService.findAll().then(function (teams) {
            $scope.teams = teams;
        });
    }]);
});
