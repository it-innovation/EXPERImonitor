// AngularJS JavaScript
var eedApp = angular.module('eedApp', [
    'ngRoute',
    'appControllers'
]);

eedApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider.
    when('/participants', {
        templateUrl: 'partials/participants.html',
        controller: 'ParticipantController'
    }).
    when('/participants/qoe', {
        templateUrl: 'partials/details.html',
        controller: 'DetailsController'
    }).
    otherwise({
        templateUrl: 'partials/home.html',
        controller: 'MainController'
    });
}]); 

// Foundation JavaScript
$(document).foundation();

// jQuery JavaScript
$(document).ready(function () {
    
});