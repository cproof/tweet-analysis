var baseUrl = "http://localhost:8080";

angular.module('sentiment', [])
        .controller('sentimentSearchController', function ($scope, $http) {
            $scope.query = "";
            $scope.currentlySearching = false; //disable form when performing a query

            $scope.fetch = function () {
                $scope.sentQuery = $scope.query;
                $scope.currentlySearching = true;
                
                $http.get(baseUrl + "/search?q=" + $scope.query)
                        .success(function (response) {
                            $scope.currentlySearching = false;
                            $scope.sentiment = response.sentiment;
                        });
            };
            
            $scope.impression = function() {
                if ($scope.sentiment > 0.8) {
                    return 'strongly positive';
                }
                else if ($scope.sentiment > 0.6) {
                    return 'positive';
                }
                else if ($scope.sentiment > 0.4) {
                    return 'neutral';
                }
                else if ($scope.sentiment > 0.2) {
                    return 'negative';
                }
                else {
                    return 'strongly negative';
                }
            };
        });