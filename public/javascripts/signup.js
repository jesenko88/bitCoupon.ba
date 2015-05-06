// app.js
    // create angular app
    var validationApp = angular.module('validationApp', []);

    // create angular controller
    validationApp.controller('signupController', function($scope) {

        // function to submit the form after all validation has occurred            
        $scope.submitForm = function(isValid) {

            // check to make sure the form is completely valid
            if (isValid) {
                alert('our form is amazing');
            }

        };

    });