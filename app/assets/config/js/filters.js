/*global define */

'use strict';

define(['angular'], function (angular) {

    /* Filters */

    var filters = angular.module('walldee.filters', []);

    filters.filter('interpolate', ['version', function (version) {
        return function (text) {
            return String(text).replace(/\%VERSION\%/mg, version);
        }
    }]);

    filters.filter('requestInfo', function() {
        return function (requestInfo) {
            if ( requestInfo == undefined )
                return "";

            var result = requestInfo.method + " " + requestInfo.url + "\n";

            angular.forEach(requestInfo.headers, function(header) {
                result += header.name + ": " + header.value + "\n";
            });

            return result;
        }
    });

    filters.filter('failureInfo', function() {
        return function (failure) {
            if ( failure == undefined )
                return "";

            return failure.errorClass + "\n" + failure.message;
        }
    });
});