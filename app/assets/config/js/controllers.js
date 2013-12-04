/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers', []);

    controllers.controller('Projects', ['$scope', '$route', '$location', 'projectResource',
        function ($scope, $route, $location, projectResource) {

            function loadProjects(selectedProjectId) {
                projectResource.query().$promise.then(function (projects) {
                    $scope.select(null);
                    $scope.projects = projects;

                    angular.forEach(projects, function (project) {
                        if (project.id == selectedProjectId) {
                            $scope.select(project);
                        }
                    });
                });
            }

            loadProjects($route.current.params.projectId);

            $scope.select = function (project) {
                $scope.selectedProject = project;
                $location.search('projectId', $scope.selectedProject != null ? $scope.selectedProject.id : null);
            };

            $scope.isSelected = function (project) {
                if (project == null)
                    return $scope.selectedProject != null && $scope.selectedProject.id == null;
                else
                    return $scope.selectedProject != null && project.id == $scope.selectedProject.id;
            };

            $scope.newProject = function () {
                $scope.selectedProject = { name: '' };
                $location.search('projectId', null);
            };

            $scope.createProject = function () {
                projectResource.create($scope.selectedProject).$promise.then(function (response) {
                    loadProjects(response.id);
                });
            };

            $scope.deleteProject = function () {
                $scope.selectedProject.$delete().then(function () {
                    loadProjects(null);
                });
            };

            $scope.changeProjectName = function (name) {
                $scope.selectedProject.name = name;
                $scope.selectedProject.$update().then(null, function () {
                    $scope.selectedProject.$get();
                });
            };
        }]);

    controllers.controller('Project', ['$scope', '$route', '$location', 'statusMonitorResource',
        function ($scope, $route, $location, statusMonitorResource) {
            $scope.$watch('selectedProject', function (project) {
                $scope.project = project;
                $scope.selectedStatusMonitor = null;
                $scope.currentStatusMonitorType = $route.current.params.statusMonitorType != null ? $route.current.params.statusMonitorType : "Jenkins";
                if (project != null && project.id != null) {
                    $scope.statusMonitors = statusMonitorResource.query({projectId: project.id});
                }
            });

            $scope.isNoProject = function () {
                return $scope.project == null;
            };

            $scope.isProjectEdit = function () {
                return $scope.project != null && $scope.project.id != null;
            };

            $scope.isProjectCreate = function () {
                return $scope.project != null && $scope.project.id == null;
            };

            $scope.selectStatusMonitorType = function (statusMonitorType) {
                $scope.currentStatusMonitorType = statusMonitorType;
                $scope.selectedStatusMonitor = null;
                $location.search('statusMonitorType', statusMonitorType != null ? statusMonitorType : null);
            };

            $scope.changeStatusMonitorName = function (name) {
                $scope.selectedStatusMonitor.name = name;
                $scope.saveCurrentStatusMonitor();
            };

            $scope.saveCurrentStatusMonitor = function () {
                $scope.statusMonitorChanged = false;
                $scope.selectedStatusMonitor.$update().then(null, function () {
                    $scope.selectedStatusMonitor.$get();
                });
            };

            $scope.isCurrentStatusMonitorType = function (statusMonitor) {
                return statusMonitor.type == $scope.currentStatusMonitorType;
            };

            $scope.selectStatusMonitor = function (statusMonitor) {
                $location.path('/project/' + $scope.project.id + '/statusMonitor/' + statusMonitor.id);
            };
        }]);

    controllers.controller('StatusMonitor', ['$scope', '$route', 'projectResource', 'statusMonitorResource',
        function ($scope, $route, projectResource, statusMonitorResource) {
            $scope.project = projectResource.get($route.current.params);
            $scope.statusMonitor = statusMonitorResource.get($route.current.params);

            console.log($scope.project);
        }
    ]);

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
