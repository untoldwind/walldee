/*global define */

'use strict';

define(['angular'], function (angular) {
    var controllers = angular.module('walldee.controllers.teams', []);

    controllers.controller('Teams', ['$scope', '$route', '$location', 'teamResource',
        function ($scope, $route, $location, teamResource) {
            function loadTeams(selectedTeamId) {
                teamResource.query().$promise.then(function (teams) {
                    $scope.select(null);
                    $scope.teams = teams;

                    angular.forEach(teams, function (team) {
                        if (team.id == selectedTeamId) {
                            $scope.select(team);
                        }
                    });
                });
            }

            loadTeams($route.current.params.teamId);

            $scope.select = function (team) {
                $scope.selectedTeam = team;
                $location.search('teamId', $scope.selectedTeam != null ? $scope.selectedTeam.id : null);
            };

            $scope.isSelected = function (team) {
                if (team == null)
                    return $scope.selectedTeam != null && $scope.selectedTeam.id == null;
                else
                    return $scope.selectedTeam != null && team.id == $scope.selectedTeam.id;
            };

            $scope.newTeam = function () {
                $scope.selectedTeam = { name: '' };
                $location.search('teamId', null);
            };

            $scope.createTeam = function () {
                teamResource.create($scope.selectedTeam).$promise.then(function (response) {
                    loadTeams(response.id);
                });
            };

            $scope.deleteTeam = function () {
                $scope.selectedTeam.$delete().then(function () {
                    loadTeams(null);
                });
            };

            $scope.changeTeamName = function (name) {
                $scope.selectedTeam.name = name;
                $scope.selectedTeam.$update().then(null, function () {
                    $scope.selectedTeam.$get();
                });
            };
        }]);

    controllers.controller('Team', ['$scope', '$route', '$location',
        function ($scope, $route, $location) {
            $scope.$watch('selectedTeam', function (team) {
                $scope.team = team;
            });

            $scope.isNoTeam = function () {
                return $scope.team == null;
            };

            $scope.isTeamEdit = function () {
                return $scope.team != null && $scope.team.id != null;
            };

            $scope.isTeamCreate = function () {
                return $scope.team != null && $scope.team.id == null;
            };
        }]);
});
