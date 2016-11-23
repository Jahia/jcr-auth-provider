angular.module('JahiaOAuth')
    .controller('jcrOAuthDataMapperController', ['$scope', '$mdToast', '$routeParams', 'settingsService', function($scope, $mdToast, $routeParams, settingsService) {
        $scope.isActivate = false;
        $scope.connectorFields = ['firstName', 'lastName', 'positions', 'location'];
        $scope.mapperFields = ['firstName', 'lastName', 'positions', 'location'];
        $scope.mappedFields = [];
        $scope.selectedFieldFromConnector = '';
        $scope.selectedFieldFromMapper = '';

        $scope.toggleMapper = function() {
            console.log($scope.isActivate);
            settingsService.toggleMapper({ connectorNodeName: $routeParams.connectorNodeName, nodeName: 'jcrOAuthDataMapper', activate: $scope.isActivate })
        };

        $scope.addMapping = function() {
            $scope.mappedFields.push({connector: $scope.selectedFieldFromConnector, mapper: $scope.selectedFieldFromMapper});

            $scope.selectedFieldFromConnector = '';
            $scope.selectedFieldFromMapper = '';
        };

        $scope.removeMapping = function(index) {
            $scope.mappedFields.splice(index, 1);
        };

        $scope.isNotMapped = function(field, key) {
            var isNotMapped = true;
            angular.forEach($scope.mappedFields, function(entry) {
                if (entry[key] == field) {
                    isNotMapped = false;
                }
            });
            return isNotMapped;
        }
    }]);