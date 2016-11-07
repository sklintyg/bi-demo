/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//Register module
angular.module('biIndexApp', [

]);

function guid() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000)
            .toString(16)
            .substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
        s4() + '-' + s4() + s4() + s4();
}

function isDefined(value) {
    return value !== null && typeof value !== 'undefined';
}
function isEmpty(value) {
    return value === null || typeof value === 'undefined' || value === '';
}
function returnJoinedArrayOrNull(value) {
    return value !== null && value !== undefined ? value.join(', ') : null;
}
function valueOrNull(value) {
    return value !== null && value !== undefined ? value : null;
}

function enrich(node, hierarchy) {
    if (typeof node.children !== 'undefined' && node.children.length > 0) {
        for (var a = 0; a < node.children.length; a++) {
            enrich(node.children[a], hierarchy + node.name + '.');
        }
    } else {
        node.path = hierarchy + node.name;
    }
}

angular.module('biIndexApp')
    .controller('IndexController', ['$scope', '$http', function($scope, $http) {
        'use strict';

        $scope.rows = [];
        $scope.columns = [];
        $scope.resultData = 'No query executed yet!';

        $scope.loadDimensions = function() {
            $http({
                method: 'GET',
                url: '/api/mdx/dimensions'
            }).then(function successCallback(response) {

                var dims = response.data;
                for (var a = 0; a < dims.dimensions.children.length; a++) {
                    enrich(dims.dimensions.children[a], '');
                }
                for (var a = 0; a < dims.measures.children.length; a++) {
                    enrich(dims.measures.children[a], '');
                }
                // Traverse tree, add path attribute for all leafs

                $scope.dimensions = dims;
            });
        };

        $scope.addColumn = function(dimension) {
            console.log('Add col: ' + dimension.path);
            $scope.columns.push(dimension.path);
        };
        $scope.addRow = function(dimension) {
            console.log('Add row: ' + dimension.path);
            $scope.rows.push(dimension.path);
        };

        $scope.doQuery = function() {

            // Construct fugly queryModel
            var queryModel = {
                rows: [],
                columns: []
            };

            for (var a = 0; a < $scope.columns.length; a++) {
                var col = $scope.columns[a];
                var parts = col.split(".");

                // Ugly, rebuild parts without first part...
                //var finalParts = [];
                //for (var b = 1; b < parts.length; b++) {
                //    finalParts.push(parts[b]);
                //}

                var obj = {
                    operator: "CHILDREN",
                    "parts": parts
                };
                queryModel.columns.push(obj);
            }

            for (var a = 0; a < $scope.rows.length; a++) {
                var row = $scope.rows[a];
                var parts = row.split(".");

                // Ugly, rebuild parts without first part...
                //var finalParts = [];
                //for (var b = 1; b < parts.length; b++) {
                //    finalParts.push(parts[b]);
                //}

                var obj = {
                    operator: "CHILDREN",
                    "parts": parts
                };
                queryModel.rows.push(obj);
            }

            $http({
                method: 'POST',
                url: '/api/model',
                data: queryModel,
                headers: ['accept:text/plain']
            }).then(function successCallback(response) {
                $scope.resultData = '\n' + response.data.trim();
            });
        };

        $scope.loadDimensions();

    }]);
