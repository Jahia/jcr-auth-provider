(function() {
    'use strict';

    angular.module('JahiaOAuthApp').controller('JCROAuthProviderController', JCROAuthController);

    JCROAuthController.$inject = ['$routeParams', 'settingsService', 'helperService', 'i18nService'];

    function JCROAuthController($routeParams, settingsService, helperService, i18nService) {
        var vm = this;

        // Variables
        vm.isActivate = false;
        vm.connectorProperties = [];
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
            if (vm.isActivate && !mandatoryPropertyAreMapped) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.error.mandatoryPropertiesNotMapped'));
                return false;
            }

            settingsService.setMapperMapping({
                connectorServiceName: $routeParams.connectorServiceName,
                mapperServiceName: 'jcrOAuthProvider',
                nodeType: 'joant:jcrOAuthSettings',
                isActivate: vm.isActivate,
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
                    connector: vm.selectedPropertyFromConnector
                });
                vm.selectedPropertyFromConnector = '';
            }
        }

        function removeMapping(index) {
            vm.mapping.splice(index, 1);
        }

        function getConnectorI18n(value) {
            return i18nService.message($routeParams.connectorServiceName + '.label.' + value);
        }

        function getMapperI18n(value) {
            return i18nService.message('jcrOAuthProvider.label.' + value.replace(':', '_'));
        }

        function toggleCard() {
            vm.expandedCard = !vm.expandedCard;
        }

        // Private functions under this line
        function _init() {
            i18nService.addKey(jcroai18n);

            settingsService.getMapperMapping({
                connectorServiceName: $routeParams.connectorServiceName,
                mapperServiceName: 'jcrOAuthProvider'
            }).success(function (data) {
                if (!angular.equals(data, {})) {
                    vm.isActivate = data.isActivate;
                    vm.mapping = data.mapping;
                }
            }).error(function(data) {
                helperService.errorToast(i18nService.message('joant_jcrOAuthView.message.label') + ' ' + data.error);
            });

            settingsService.getConnectorProperties({
                connectorServiceName: $routeParams.connectorServiceName
            }).success(function(data) {
                vm.connectorProperties = data.connectorProperties;
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