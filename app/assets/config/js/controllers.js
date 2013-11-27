/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers', []);

    controllers.controller('Projects', ['$scope', '$route', '$location',  'projectResource', 'statusMonitorResource',
        function ($scope, $route, $location, projectResource, statusMonitorResource) {
            $scope.selectedProject = null;
            $scope.statusMonitors = [];

            projectResource.query().$promise.then(function (projects) {
                $scope.projects = projects;
                var paramProjectId = $route.current.params.projectId;

                angular.forEach(projects, function(project) {
                    if ( project.id == paramProjectId ) {
                        $scope.select(project);
                    }
                });
            });

            $scope.select = function (project) {
                $scope.selectedProject = project;
                $scope.statusMonitors = statusMonitorResource.query({projectId: project.id})
                $scope.currentStatusMonitors = "Jenkins";
                $scope.selectedStatusMonitor = null;
                $location.search('projectId', $scope.selectedProject.id);
            };

            $scope.newProject = function () {

            };

            $scope.changeProjectName = function (name) {
                $scope.selectedProject.name = name;
                $scope.selectedProject.$update().then(null, function () {
                    $scope.selectedProject.$get()
                });
            };

            $scope.isCurrentStatusMonitor = function(statusMonitor) {
                return statusMonitor.type == $scope.currentStatusMonitors;
            };

            $scope.selectStatusMonitor = function(statusMonitor) {
                $scope.selectedStatusMonitor = statusMonitor;
            };
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
