var BASE_URL = "/ECC";
$(document).ready(function() {
    $(document).foundation();

    // check if the service is initialised successfully.
    $.ajax({
        type: 'GET',
        url: BASE_URL + "/configuration/ifinitialised",
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(textStatus);
            console.log(errorThrown);
            $('#configStatus').attr('class', 'right alert-color');
            $('#configStatus').text("initialisation error (" + errorThrown + ")");
        },
        success: function(idata) {
            console.log(idata);
            if (idata === false) {
                $('#configStatus').attr('class', 'right alert-color');
                $('#configStatus').text('not initialised');
            } else if (idata === true) {
                $('#configStatus').attr('class', 'right success-color');
                $('#configStatus').text('initialised');

                // check if configuration was set
                $.ajax({
                    type: 'GET',
                    url: BASE_URL + "/configuration/ifconfigured",
                    error: function(jqXHR, textStatus, errorThrown) {
                        console.log(jqXHR);
                        console.log(textStatus);
                        console.log(errorThrown);
                        $('#configStatus').attr('class', 'right alert-color');
                        $('#configStatus').text("configuration error (" + errorThrown + ")");
                    },
                    success: function(cdata) {
                        console.log(cdata);
                        if (cdata === false) {
                            $('#configStatus').attr('class', 'right alert-color');
                            $('#configStatus').text('not configured');
                            window.location.replace(BASE_URL + "/index.html");

                        } else if (cdata === true) {
                            $('#configStatus').attr('class', 'right success-color');
                            $('#configStatus').text('configured');

                            // go to experiment


                        } else {
                            $('#configStatus').attr('class', 'right alert-color');
                            $('#configStatus').text('unknown configured status');
                        }
                    }});

            } else {
                $('#configStatus').attr('class', 'right alert-color');
                $('#configStatus').text('unknown initialisation status');
            }
        }
    });
});