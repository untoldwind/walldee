/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers.projects', []);

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

            $scope.isCurrentStatusMonitorType = function (statusMonitor) {
                return statusMonitor.type == $scope.currentStatusMonitorType;
            };

            $scope.selectStatusMonitor = function (statusMonitor) {
                $location.path('/projects/' + $scope.project.id + '/statusMonitors/' + statusMonitor.id);
            };
        }]);

    controllers.controller('StatusMonitor', ['$scope', '$route', 'projectResource', 'statusMonitorResource',
        function ($scope, $route, projectResource, statusMonitorResource) {
            $scope.project = projectResource.get({projectId: $route.current.params.projectId});
            if ($route.current.params.statusMonitorId == null) {
                $scope.statusMonitor = {projectId: $route.current.params.projectId, type: $route.current.params.statusMonitorType};
            } else {
                $scope.statusMonitor = statusMonitorResource.get({projectId: $route.current.params.projectId, statusMonitorId: $route.current.params.statusMonitorId});
            }

            $scope.setView = function (view) {
                $scope.view = view;
            }

            $scope.setView('config')
        }
    ]);

    controllers.controller('StatusMonitorConfig', ['$scope',
        function ($scope) {
            $scope.statusMonitorChanged = false

            $scope.saveStatusMonitor = function () {
                $scope.statusMonitor.$update().then(function (response) {
                    $scope.statusMonitorChanged = false
                });
            };

        }
    ]);

    controllers.controller('StatusMonitorHistory', ['$scope', 'statusMonitorValuesResource',
        function ($scope, statusMonitorValuesResource) {
            $scope.refresh = function () {
                $scope.statusValues = statusMonitorValuesResource.query({projectId: $scope.statusMonitor.projectId, statusMonitorId: $scope.statusMonitor.id});
            };

            $scope.refresh();
        }
    ]);

    controllers.controller('StatusMonitorTest', ['$scope', 'statusMonitorTestResource',
        function ($scope, statusMonitorTestResource) {

        }
    ]);
});
