/*global define */

'use strict';

define(['angular'], function (angular) {

    /* Services */

    var services = angular.module('walldee.services', []);

    services.value('version', '0.1');

    services.factory("projectService", ['$resource', function ($resource) {
        var projectResource = $resource('/projects/:projectId', {projectId:'@id'}, {
            'update': {method:'PUT'}
        });

        return {
            findAll: function () {
                return projectResource.query().$promise;
            }
        };
    }]);

    services.factory('teamService', ['$resource', function ($resource) {
        var teamsResource = $resource('/teams', {}, {
            'get': {
                method: 'GET',
                isArray: true
            }});

        return {
            findAll: function () {
                return teamsResource.get().$promise;
            }
        }
    }]);
});