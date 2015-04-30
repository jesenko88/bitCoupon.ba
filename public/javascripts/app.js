// create angular app
var validationApp = angular.module('movieApp', []);

// create angular controller
validationApp.controller('movieController', function($scope) {

    // function to submit the form after all validation has occurred            
    $scope.submitForm = function(isValid) {

        // check to make sure the form is completely valid
        if (isValid) { 
        	return true;           
        }

    };

});