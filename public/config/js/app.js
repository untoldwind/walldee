/*global define, angular */

'use strict';

// Declare here that angular is the US version - other locales can be easily substituted.

define('angular', ['webjars!angular-locale_en-us.js', 'webjars!angular-route.js', 'webjars!ui-bootstrap.js'], function() {
	return angular;
});

requirejs.config({
    shim: {
        'webjars!angular-route.js': ['webjars!angular.js']
    }
});

require([ 'angular', './controllers', './directives', './filters', './services'], function (angular, controllers) {

// Declare app level module which depends on filters, and services

    angular.module('myApp', ['ngRoute', 'ui.bootstrap', 'myApp.filters', 'myApp.services', 'myApp.directives']).
        config(['$routeProvider', function ($routeProvider) {
            $routeProvider.when('/projects', {templateUrl: '/assets/config/partials/partial1.html', controller: controllers.MyCtrl1});
            $routeProvider.when('/teams', {templateUrl: '/assets/config/partials/partial2.html', controller: controllers.MyCtrl2});
            $routeProvider.otherwise({redirectTo: '/view1'});
        }]);

    angular.bootstrap(document, ['myApp']);
});
