(function() {
    'use strict';

    angular.module('JahiaOAuthApp').controller('JCROAuthProviderController', JCROAuthController);

    JCROAuthController.$inject = ['$routeParams', 'settingsService', 'helperService', 'i18nService'];

    function JCROAuthController($routeParams, settingsService, helperService, i18nService) {
        var vm = this;

        // Variables
        vm.enabled = false;
        vm.createUserAtSiteLevel = false;
        vm.connectorProperties = [{name: 'other', valueType: 'string'}];
        vm.mapperProperties = [];
        vm.mapping = [];
        vm.selectedPropertyFromConnector = '';
        vm.selectedPropertyFromMapper = '';
        vm.expandedCard = false;

        // Functions
        vm.saveMapperSettings = saveMapperSettings;
        vm.addMapping = addMapping;
        vm.removeMapping = removeMapping;
        vm.getConnectorI18n = getConnectorI18n;
        vm.getMapperI18n = getMapperI18n;
        vm.toggleCard = toggleCard;
        vm.orderByConnector = orderByConnector;
        vm.orderByMapper = orderByMapper;

        _init();

        function saveMapperSettings() {
            var isMappingComplete = true;
            angular.forEach(vm.mapping, function(mapped) {
                if (!mapped.mapper || !mapped.connector) {
                    isMappingComplete =false;
                }
            });
            if (!isMappingComplete) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.error.incompleteMapping'));
                return false;
            }

            var mandatoryPropertyAreMapped = true;
            angular.forEach(vm.mapperProperties, function(property) {
                if (property.mandatory) {
                    if (_isNotMapped(property.name, 'mapper')) {
                        mandatoryPropertyAreMapped = false
                    }
                }
            });
            if (vm.enabled && !mandatoryPropertyAreMapped) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.error.mandatoryPropertiesNotMapped'));
                return false;
            }

            settingsService.setMapperMapping({
                connectorServiceName: $routeParams.connectorServiceName,
                mapperServiceName: 'jcrOAuthProvider',
                properties: {
                    enabled: vm.enabled,
                    createUserAtSiteLevel: vm.createUserAtSiteLevel
                },
                mapping: vm.mapping
            }).success(function() {
                helperService.successToast(i18nService.message('joant_jcrOAuthView.message.success.mappingSaved'));
            }).error(function(data) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
            });
        }

        function addMapping() {
            if (vm.selectedPropertyFromConnector) {
                vm.mapping.push({
                    connector: angular.copy(vm.selectedPropertyFromConnector),
                    editable: vm.selectedPropertyFromConnector.name === 'other'
                });
                vm.selectedPropertyFromConnector = '';
            }
        }

        function removeMapping(index) {
            vm.mapping.splice(index, 1);
        }

        function getConnectorI18n(value) {
            return value === 'other' ? i18nService.message('jcrOAuthProvider.label.other') : i18nService.message($routeParams.connectorServiceName + '.label.' + value) ;
        }

        function getMapperI18n(value) {
            return i18nService.message('jcrOAuthProvider.label.' + value.replace(':', '_'));
        }

        function toggleCard() {
            vm.expandedCard = !vm.expandedCard;
        }

        function orderByConnector(property) {
            return getConnectorI18n(property.name);
        }

        function orderByMapper(property) {
            return getMapperI18n(property.name);
        }

        // Private functions under this line
        function _init() {
            i18nService.addKey(jcroai18n);

            settingsService.getMapperMapping({
                connectorServiceName: $routeParams.connectorServiceName,
                mapperServiceName: 'jcrOAuthProvider',
                properties: {'createUserAtSiteLevel': true}
            }).success(function (data) {
                if (!angular.equals(data, {})) {
                    vm.enabled = data.enabled;
                    vm.createUserAtSiteLevel = data.createUserAtSiteLevel,
                    vm.mapping = data.mapping;
                }
            }).error(function(data) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
            });

            settingsService.getConnectorProperties({
                connectorServiceName: $routeParams.connectorServiceName
            }).success(function(data) {
                vm.connectorProperties = vm.connectorProperties.concat(data.connectorProperties);
            }).error(function(data) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
            });

            settingsService.getMapperProperties({
                mapperServiceName: 'jcrOAuthProvider'
            }).success(function(data) {
                vm.mapperProperties = data.mapperProperties;
            }).error(function(data) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
            });
        }

        function _isNotMapped(field, key) {
            var isNotMapped = true;
            angular.forEach(vm.mapping, function(entry) {
                if (entry[key] && entry[key].name == field) {
                    isNotMapped = false;
                }
            });
            return isNotMapped;
        }
    }
})();