$(document).ready(function() {

    // load popup reset
    $.ajax({
        url: "extra/resetEccPopup.html",
        success: function(data) {
            $('body').append(data);

            $(document).foundation();

            // Enables "Restart ECC" button at the bottom
            $("#restartEccButton").click(function(e) {
                e.preventDefault();
                $('#resetEccWarningModal').foundation('reveal', 'open');
            });
            $("#resetEccWarningModalProceed").click(function(e) {
                e.preventDefault();
                $('#resetEccWarningModal').foundation('reveal', 'open');

                $.getJSON(BASE_URL + "/configuration/do/reset", function(resetResult) {
                    if (resetResult === true) {
                        window.location.replace(BASE_URL + "/index.html");
                    } else {
                        $('#configStatus').attr('class', 'right alert-color');
                        $('#configStatus').text('Service reset failed');
                    }
                });

            });
            $("#resetEccWarningModalCancel").click(function(e) {
                e.preventDefault();
                $('#resetEccWarningModal').foundation('reveal', 'close');
            });
        },
        dataType: 'html'
    });

});