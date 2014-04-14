var BASE_URL = "/ECC";
$(document).ready(function() {
    $(document).foundation();

    $("#localConfigurationRadioInput").data("link", BASE_URL + "/configuration/local");
    $("#localConfigurationRadioInput").change(function(e) {
        displayFetchedConfiguration($(this).data("link"));
    });
    $("#localConfigurationRadioInput").trigger('change');

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

                            // display available configurations
                            $.getJSON(BASE_URL + "/configuration/projects", function(data) {
                                console.log(data);
                                if (data.size > 1) {
                                    console.log("No remote projects found");
                                } else {
                                    for (counter in data) {
                                        var remoteConfigurationRadioInput = $('<input type="radio" name="configurationSet" value="rp' +
                                                counter + '" id="rp' + counter + '"><label for="rp' + counter + '">' + data[counter] + '</label><br>').appendTo("#listofRemoteConfigurations");
                                        remoteConfigurationRadioInput.data("link", BASE_URL + "/configuration/projects/" + data[counter]);
                                        remoteConfigurationRadioInput.change(function(e) {
                                            displayFetchedConfiguration($(this).data("link"));
                                        });
                                    }
                                }

                            });

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
    /*

     $("#fetchProjectConfigByNameForm").submit(function(e) {
     e.preventDefault();
     $("#fetchProjectConfigByNameButton").trigger('click');
     });


     $("#activeProjectConfigForm").submit(function(e) {
     e.preventDefault();
     $("#setActiveConfiguration").trigger('click');
     });
     $("#setActiveConfiguration").click(function(e) {
     e.preventDefault();

     var newConfiguration = $("#activeProjectConfigForm").serializeJSON();
     if (newConfiguration.rabbitConfig.useSsl === "false") {
     newConfiguration.rabbitConfig.useSsl = false;
     } else {
     newConfiguration.rabbitConfig.useSsl = true;
     }

     console.log(newConfiguration);

     $.ajax({
     type: 'POST',
     dataType: 'json',
     contentType: 'application/json',
     url: BASE_URL + "/configuration",
     data: JSON.stringify(newConfiguration),
     error: function(jqXHR, textStatus, errorThrown) {
     console.log(jqXHR);
     console.log(textStatus);
     console.log(errorThrown);
     },
     success: function(data) {
     console.log(data);
     if (data === false) {
     $('#configStatus').attr('class', 'alert-color');
     $('#configStatus').text('not configured');
     } else if (data === true) {
     $('#configStatus').attr('class', 'success-color');
     $('#configStatus').text('configured');
     } else {
     $('#configStatus').attr('class', 'alert-color');
     $('#configStatus').text('unknown status');
     }
     }
     });

     });

     */
});

function displayFetchedConfiguration(URL) {
    $.ajax({
        type: 'GET',
        url: URL,
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(textStatus);
            console.log(errorThrown);
        },
        success: function(data) {
            console.log(data);
            $("#activeProjectConfigForm").removeClass('hide');
            $("#projectInstructions").remove();

            $("#config_projectName").val(data.projectName);
            $("#config_monitorid").val(data.rabbitConfig.monitorId);
            $("#config_rabbitip").val(data.rabbitConfig.ip);
            $("#config_rabbitport").val(data.rabbitConfig.port);
            $("#config_username").val(data.rabbitConfig.userName);
            $("#config_password").val(data.rabbitConfig.userPassword);
            $("#config_keystore").val(data.rabbitConfig.keystore);
            if (data.rabbitConfig.useSsl === false) {
                $("#config_userssl").prop('checked', false);
            } else {
                $("#config_userssl").prop('checked', true);
            }
        }
    });
}