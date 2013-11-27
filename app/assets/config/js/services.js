/*global define */

'use strict';

define(['angular'], function (angular) {

    /* Services */

    var services = angular.module('walldee.services', []);

    services.value('version', '0.1');

    services.factory('projectResource', ['$resource', function ($resource) {
        return $resource('/projects/:projectId', {projectId: '@id'}, {
            'update': {method: 'PUT'}
        });
    }]);

    services.factory('statusMonitorResource', ['$resource', function ($resource) {
        return $resource('/projects/:projectId/statusMonitors/:statusMonitorId', {projectId: '@projectId', statusMonitorId: '@id'}, {
            'update': {method: 'PUT'}
        });
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