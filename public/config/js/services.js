/*global define */

'use strict';

define(['angular'], function (angular) {

    /* Services */

    var services = angular.module('walldee.services', []);

    services.value('version', '0.1');

    services.factory("projectService", ['$resource', function ($resource) {
        var projectsResource = $resource('/projects', {}, {
            'get': {
                method: 'GET',
                isArray: true
            }});

        return {
            findAll: function () {
                return projectsResource.get().$promise;
            }
        };
    }]);
});