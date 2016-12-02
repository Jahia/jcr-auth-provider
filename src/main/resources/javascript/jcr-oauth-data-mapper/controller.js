angular.module('JahiaOAuth')
    .controller('jcrOAuthDataMapperController', ['$scope', '$mdToast', '$routeParams', 'settingsService', function($scope, $mdToast, $routeParams, settingsService) {
        $scope.isActivate = false;
        $scope.connectorProperties = {};
        $scope.mapperProperties = {};
        $scope.mapping = [];
        $scope.selectedPropertyFromConnector = '';
        $scope.selectedPropertyFromMapper = '';

        settingsService.getMapperMapping({
            serviceName: $routeParams.connectorServiceName,
            mapperKey: 'jcrOAuthDataMapper'
        }).success(function (data) {
            if (!angular.equals(data, {})) {
                $scope.isActivate = data.isActivate;
                $scope.mapping = data.mapping;
            }
        });

        settingsService.getConnectorProperties({
            serviceName: $routeParams.connectorServiceName
        }).success(function(data) {
            $scope.connectorProperties = data;
        });

        settingsService.getMapperProperties({
            mapperKey: 'jcrOAuthDataMapper'
        }).success(function(data) {
            $scope.mapperProperties = data;
        });

        $scope.saveMapperSettings = function() {
            var mandatoryPropertyAreMapped = true;
            angular.forEach($scope.mapperProperties, function(value, key) {
                if (value.mandatory) {
                    if ($scope.isNotMapped(key, 'mapper')) {
                        mandatoryPropertyAreMapped = false
                    }
                }
            });
            if (!mandatoryPropertyAreMapped) {
                $mdToast.show($mdToast.simple()
                    .textContent('Missing mandatory properties!')
                    .position('bottom right'));
                return false;
            }

            settingsService.setMapperMapping({
                serviceName: $routeParams.connectorServiceName,
                mapperKey: 'jcrOAuthDataMapper',
                nodeType: 'joant:jcrOAuthSettings',
                isActivate: $scope.isActivate,
                mapping: $scope.mapping
            }).success(function(data) {
                $mdToast.show($mdToast.simple().textContent('SUCCESS!'));
            }).error(function(data) {
                $mdToast.show($mdToast.simple()
                    .textContent(data.error)
                    .theme('errorToast')
                    .position('bottom right'));
            });
        };

        $scope.addMapping = function() {
            if ($scope.selectedPropertyFromConnector && $scope.selectedPropertyFromMapper) {
                $scope.mapping.push({
                    connector: $scope.selectedPropertyFromConnector,
                    mapper: $scope.selectedPropertyFromMapper
                });
                $scope.selectedPropertyFromConnector = '';
                $scope.selectedPropertyFromMapper = '';
            }
        };

        $scope.removeMapping = function(index) {
            $scope.mapping.splice(index, 1);
        };

        $scope.isNotMapped = function(field, key) {
            var isNotMapped = true;
            angular.forEach($scope.mapping, function(entry) {
                if (entry[key] == field) {
                    isNotMapped = false;
                }
            });
            return isNotMapped;
        }
    }]);