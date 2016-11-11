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

function isSelected(key, dimensionFilterValues) {
    if (isEmpty(dimensionFilterValues)) {
        return false;
    }

    for (var a = 0; a < dimensionFilterValues.length; a++) {
        if (dimensionFilterValues[a].path === key) {
            return true;
        }
    }
    return false;
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

function onlyUnique(value, index, self) {
    return self.indexOf(value) === index;
}


function parentSegments(path) {
    var parts = path.split('.');
    var str = '';
    for (var a = 0; a < parts.length - 1; a++) {
        str += parts[a] + '.'
    }
    str = str.substring(0, str.length - 1); // chop off last dot

    return str;
}

function arrayMove(arr, fromIndex, toIndex) {
    var element = arr[fromIndex];
    arr.splice(fromIndex, 1);
    arr.splice(toIndex, 0, element);
}


angular.module('biIndexApp')
    .controller('IndexController', ['$scope', '$http', function($scope, $http) {
        'use strict';

        $scope.rows = [];
        $scope.columns = [];
        $scope.filterDimension = {};
        $scope.filterValues = [];

        $scope.resultData = 'Ingen fråga har körts än!';
        $scope.includeEmpty = false;
        $scope.swapAxis = false;

        $scope.loadDimensions = function() {
            $http({
                method: 'GET',
                url: '/api/mdx/dimensions'
            }).then(function successCallback(response) {

                var dims = response.data;

                // Traverse tree, add path attribute for all leafs
                for (var a = 0; a < dims.dimensions.children.length; a++) {
                    enrich(dims.dimensions.children[a], '');
                }
                for (var a = 0; a < dims.measures.children.length; a++) {
                    enrich(dims.measures.children[a], 'Measures.');
                }


                $scope.dimensions = dims.dimensions.children;
                $scope.measures = [{
                    name: 'Measures',
                    children: dims.measures.children,
                    filterValues: []      // Cannot filter on measures...
                }];
            });
        };

        $scope.available = function(dimension) {
            var index = $scope.columns.indexOf(dimension.path);
            if (index > -1) {
                return false;
            }
            index = $scope.rows.indexOf(dimension.path);
            if (index > -1) {
                return false;
            }
            return true;
        }

        $scope.addColumn = function(dimension) {
            var dim = {
                path: dimension.path,
                filterValues: []
            };

            $scope.columns.push(dim);
            $scope.doQuery();
        };
        $scope.addRow = function(dimension) {
            var dim = {
                path: dimension.path,
                filterValues: []
            };
            $scope.rows.push(dim);
            $scope.doQuery();
        };

        $scope.removeColumn = function(dimension) {
            var index = $scope.columns.indexOf(dimension);
            if (index > -1) {
                $scope.columns.splice(index, 1);
            }
            $scope.doQuery();
        };
        $scope.removeRow = function(dimension) {
            var index = $scope.rows.indexOf(dimension);
            if (index > -1) {
                $scope.rows.splice(index, 1);
            }
            $scope.doQuery();
        };
        $scope.removeFilter = function(dimension) {
            $scope.doQuery();
        };

        $scope.moveUp = function(dimension, type) {
              if (type == 'ROW') {
                  var index = $scope.rows.indexOf(dimension);
                  if (index - 1 > -1) {
                      arrayMove($scope.rows, index, index - 1);
                      $scope.doQuery();
                  }
              }
              if (type == 'COLUMN') {
                var index = $scope.columns.indexOf(dimension);
                if (index - 1 > -1) {
                    arrayMove($scope.columns, index, index - 1);
                    $scope.doQuery();
                }
            }

        };

        $scope.moveDown = function(dimension, type) {
            if (type == 'ROW') {
                var index = $scope.rows.indexOf(dimension);
                if (index + 1 < $scope.rows.length) {
                    arrayMove($scope.rows, index, index + 1);
                    $scope.doQuery();
                }
            }
            if (type == 'COLUMN') {
                var index = $scope.columns.indexOf(dimension);
                if (index + 1 < $scope.columns.length) {
                    arrayMove($scope.columns, index, index + 1);
                    $scope.doQuery();
                }
            }
        };

        $scope.swapAxes = function() {
            var tmp = angular.copy($scope.rows);
            $scope.rows = $scope.columns;
            $scope.columns = tmp;
            $scope.doQuery();
        };


        $scope.clear = function() {
            $scope.columns = [];
            $scope.rows = [];
            $scope.filters = [];
        };


        $scope.addFilter = function(filterObject) {
            var valueDimension = {
                path: filterObject.path
            };

            // If filter dimension already exists, remove. Otherwise, add.
            var index = -1;
            for (var a = 0; a < $scope.filterDimension.filterValues.length; a++) {
               var filterValue = $scope.filterDimension.filterValues[a];
                if (filterObject.path === filterValue.path) {
                    index = a;
                    break;
                }
            }

            if (index == -1) {
                $scope.filterDimension.filterValues.push(valueDimension);
            } else {
                // Remove instead...
                $scope.filterDimension.filterValues.splice(index, 1);
            }

            $scope.doQuery();
        };

        $scope.openFilter = function(dimension, type) { // type == ROW | COLUMN

            $scope.filterDimension = dimension;
            $scope.filterValues = [];

            $http({
                method: 'POST',
                url: '/api/mdx/dimension/values',
                data: dimension.path
            }).success(function(data) {
                $scope.filterName = dimension.path;

                var backendValues = data.filter( onlyUnique );

                for (var a = 0; a < backendValues.length; a++) {
                    var val = {
                        title: backendValues[a],
                        path: parentSegments(dimension.path) + '.' + backendValues[a],
                        selected: isSelected(parentSegments(dimension.path) + '.' + backendValues[a], dimension.filterValues)
                    };
                    $scope.filterValues.push(val);
                }
                $("#filterModal").modal('show');
            });

        };

        $scope.doQuery = function() {

            // Make sure we have at least one row and one column
            if ($scope.rows == 0 || $scope.columns == 0) {
                $scope.resultData = 'Måste finnas minst en rad och en kolumn.';
                return;
            }

            // Construct fugly queryModel
            var queryModel = {
                rows: [],
                columns: [],
                includeEmpty: $scope.includeEmpty,
                swapAxis: $scope.swapAxis
            };


            for (var a = 0; a < $scope.columns.length; a++) {
                var col = $scope.columns[a].path;
                var parts = col.split(".");

                var obj = {
                    operator: "CHILDREN",
                    parts: parts,
                    filterValues : []
                };

                for (var b = 0; b < $scope.columns[a].filterValues.length; b++) {
                    obj.filterValues.push({
                        parts: $scope.columns[a].filterValues[b].path.split('.')
                    });
                }

                queryModel.columns.push(obj);
            }

            for (var a = 0; a < $scope.rows.length; a++) {
                var row = $scope.rows[a].path;
                var parts = row.split(".");

                var obj = {
                    operator: "CHILDREN",
                    "parts": parts,
                    filterValues : []
                };
                for (var b = 0; b < $scope.rows[a].filterValues.length; b++) {
                    obj.filterValues.push({
                        parts: $scope.rows[a].filterValues[b].path.split('.')
                    });
                }
                queryModel.rows.push(obj);
            }

            $scope.resultData = 'Exekverar fråga...';

            $http({
                method: 'POST',
                url: '/api/model',
                data: queryModel,
                headers: ['accept:text/plain']
            }).success(function(data) {
                $scope.resultData = '\n' + data.trim().replace(/�/g, ' ');
            }).error(function(data, status) {
                $scope.resultData = 'Fel: ' + data + ' HTTP: ' + status;
            });
        };

        $scope.loadDimensions();

    }]);
