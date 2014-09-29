
appControllers.directive('siteHeader', function () {
    return {
        restrict: 'E',
        template: '<a href="" class="button tiny radius">{{back}}</a>',
        scope: {
            back: '@back'
        },
        link: function(scope, element, attrs) {
            $(element[0]).on('click', function() {
                history.back();
                scope.$apply();
            });
        }
    };
});