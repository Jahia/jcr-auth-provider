angular.module('JahiaOAuth')
    .controller('jcrOAuthDataMapperController',
        ['$scope', '$routeParams', 'settingsService', 'helperService', 'i18nService',
        function($scope, $routeParams, settingsService, helperService, i18nService) {
        $scope.isActivate = false;
        $scope.connectorProperties = {};
        $scope.mapperProperties = {};
        $scope.mapping = [];
        $scope.selectedPropertyFromConnector = '';
        $scope.selectedPropertyFromMapper = '';

        settingsService.getMapperMapping({
            serviceName: $routeParams.connectorServiceName,
            mapperServiceName: 'jcrOAuthDataMapper'
        }).success(function (data) {
            if (!angular.equals(data, {})) {
                $scope.isActivate = data.isActivate;
                $scope.mapping = data.mapping;
            }
        }).error(function(data) {
            helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
        });

        settingsService.getConnectorProperties({
            serviceName: $routeParams.connectorServiceName
        }).success(function(data) {
            $scope.connectorProperties = data;
        }).error(function(data) {
            helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
        });

        settingsService.getMapperProperties({
            mapperServiceName: 'jcrOAuthDataMapper'
        }).success(function(data) {
            $scope.mapperProperties = data;
        }).error(function(data) {
            helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
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
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.error.mandatoryPropertiesNotMapped'));
                return false;
            }

            settingsService.setMapperMapping({
                serviceName: $routeParams.connectorServiceName,
                mapperServiceName: 'jcrOAuthDataMapper',
                nodeType: 'joant:jcrOAuthSettings',
                isActivate: $scope.isActivate,
                mapping: $scope.mapping
            }).success(function() {
                helperService.successToast(i18nService.message('joant_jcrOAuthView.message.success.mappingSaved'));
            }).error(function(data) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
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
        };

        $scope.getConnectorI18n = function(value) {
            return i18nService.message($routeParams.connectorServiceName + '.label.' + value);
        };

        $scope.getMapperI18n = function(value) {
            return i18nService.message('jcrOAuthDataMapper.label.' + value.replace(':', '_'));
        }
    }]);