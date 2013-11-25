/*global define */

'use strict';

define(['angular'], function(angular) {

    /* Filters */

    angular.module('walldee.filters', []).
        filter('interpolate', ['version', function(version) {
            return function(text) {
                return String(text).replace(/\%VERSION\%/mg, version);
            }
        }]);

});