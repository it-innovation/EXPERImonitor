var BASE_URL = "/" + window.location.href.split('/')[3];

$(document).ready(function() {
    $(document).foundation();

    // disable js cache
    $.ajaxSetup({cache: false});

    $("#localConfigurationRadioInput").data("link", BASE_URL + "/configuration/local");
    $("#localConfigurationRadioInput").change(function(e) {
        displayFetchedConfiguration($(this).data("link"));
    });
    $("#localConfigurationRadioInput").trigger('change');

    $("#closeNewConfigurationErrorModal").click(function(e) {
        e.preventDefault();
        $("#newConfigurationErrorModal").foundation('reveal', 'close');
    });

    $("#closeServicesFailedErrorModal").click(function(e) {
        e.preventDefault();
        $("#servicesFailedErrorModal").foundation('reveal', 'close');
    });

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

                        } else if (cdata === true) {
                            $('#configStatus').attr('class', 'right success-color');
                            $('#configStatus').text('configured');

                            // check if services started
                            $.ajax({
                                type: 'GET',
                                url: BASE_URL + "/configuration/ifservicesstarted",
                                error: function(jqXHR, textStatus, errorThrown) {
                                    console.log(jqXHR);
                                    console.log(textStatus);
                                    console.log(errorThrown);
                                    $('#configStatus').attr('class', 'right alert-color');
                                    $('#configStatus').text("service start error (" + errorThrown + ")");
                                },
                                success: function(ssdata) {
                                    if (ssdata === false) {
                                        $('#configStatus').attr('class', 'right alert-color');
                                        $('#configStatus').text('services failed to start');
                                        $("#servicesFailedErrorModal").foundation('reveal', 'open');
                                    } else if (ssdata === true) {
                                        $('#configStatus').attr('class', 'right success-color');
                                        $('#configStatus').text('service started');
                                        window.location.replace(BASE_URL + "/experiment.html");
                                    } else {
                                        $('#configStatus').attr('class', 'right alert-color');
                                        $('#configStatus').text('unknown service started status');
                                    }
                                }});

                        } else {
                            $('#configStatus').attr('class', 'right alert-color');
                            $('#configStatus').text('unknown configured status');
                        }

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
//                                    var customSearchRadioInput = $('<input type="radio" name="configurationSet" value="rp' +
//                                            counter + '" id="rp' + counter + '"><input type="text" name="searchForWebDavProjectName" id="searchForWebDavProjectName"><br>').appendTo("#listofRemoteConfigurations");
                            }

                        });
                    }});

            } else {
                $('#configStatus').attr('class', 'right alert-color');
                $('#configStatus').text('unknown initialisation status');
            }
        }
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
        if (newConfiguration.remote === "false") {
            newConfiguration.remote = false;
        } else {
            newConfiguration.remote = true;
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
                $("#newConfigurationErrorModal").foundation('reveal', 'open');
            },
            success: function(data) {
                console.log(data);
                if (data === false) {
                    $('#configStatus').attr('class', 'right alert-color');
                    $('#configStatus').text('not configured');
                    $("#newConfigurationErrorModal").foundation('reveal', 'open');
                } else if (data === true) {
                    $('#configStatus').attr('class', 'right success-color');
                    $('#configStatus').text('configured');

                    window.location.replace(BASE_URL + "/experiment.html");
                } else {
                    $('#configStatus').attr('class', 'right alert-color');
                    $('#configStatus').text('unknown status');
                }
            }
        });
    });
});

function displayFetchedConfiguration(URL) {
    $("#activeProjectConfigForm").addClass('hide');
    $("#setActiveConfigurationRow").addClass('hide');
    $("#fetchConfigurationRow").removeClass('hide');
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
            $("#setActiveConfigurationRow").removeClass('hide');
            $("#fetchConfigurationRow").addClass('hide');
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

            $("#config_databaseurl").val(data.databaseConfig.url);
            $("#config_databasename").val(data.databaseConfig.databaseName);
            $("#config_databasetype").val(data.databaseConfig.databaseType);
            $("#config_databaseusername").val(data.databaseConfig.userName);
            $("#config_databasepassword").val(data.databaseConfig.userPassword);
            $("#config_miscsnapshotCount").val(data.miscConfig.snapshotCount);
            $("#config_miscsnapnagiousUrl").val(data.miscConfig.nagiousUrl);
        }
    });
}