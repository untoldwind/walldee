/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers', []);

    controllers.controller('Projects', ['$scope', 'projectService', function ($scope, projectService) {
        $scope.selectedProject = null;
        $scope.newProjectName = null;

        $scope.select = function (project) {
            $scope.selectedProject = project;
            $scope.newProjectName = null;
        };

        $scope.newProject = function () {

        };

        $scope.editProjectName = function () {
            $scope.newProjectName = $scope.selectedProject.name;
        };

        $scope.changeProjectName = function (name) {
            $scope.selectedProject.name = name;
            $scope.selectedProject.$update().then(null, function () {
                $scope.selectedProject.$get()
            });
            $scope.newProjectName = null;
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
