/*global define */

'use strict';

define(['angular'], function (angular) {

    /* Services */

    var services = angular.module('walldee.services', []);

    services.value('version', '0.1');

    var stdMethods = {
        'create': {
            method: 'POST',
            transformResponse: function (data, headers) {
                var location = headers('Location');
                var idx = location.lastIndexOf('/');

                return {location: location, id: location.substring(idx + 1)};
            }
        },
        'update': {method: 'PUT'}
    };

    services.factory('projectResource', ['$resource', function ($resource) {
        return $resource('/projects/:projectId', {projectId: '@id'}, stdMethods);
    }]);

    services.factory('statusMonitorResource', ['$resource', function ($resource) {
        return $resource('/projects/:projectId/statusMonitors/:statusMonitorId', {projectId: '@projectId', statusMonitorId: '@id'}, {
            'update': {method: 'PUT'}
        });
    }]);

    services.factory('statusMonitorValuesResource', ['$resource', function ($resource) {
        return $resource('/projects/:projectId/statusMonitors/:statusMonitorId/values', {projectId: '@projectId', statusMonitorId: '@id'});
    }]);

    services.factory('teamResource', ['$resource', function ($resource) {
        return $resource('/teams/:teamId', {teamId: '@id'}, stdMethods);
    }]);

    services.factory('displayResource', ['$resource', function ($resource) {
        return $resource('/displays/:displayId', {displayId: '@id'}, {
            'create': {
                method: 'POST',
                transformResponse: function (data, headers) {
                    var location = headers('Location');

                    var idx = location.lastIndexOf('/');

                    return {location: location, id: location.substring(idx + 1)};
                }
            },
            'update': {method: 'PUT'}
        });
    }]);

    services.factory('displayItemResource', ['$resource', function ($resource) {
        return $resource('/displays/:displayId/items/:displayItemId', {displayId: '@displayId', displayItemId: '@id'}, stdMethods);
    }]);
});