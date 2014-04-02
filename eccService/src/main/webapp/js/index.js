var BASE_URL = "/ECC";
$(document).ready(function() {
    $(document).foundation();

    // check if the service is configured
    $.ajax({
        type: 'GET',
        url: BASE_URL + "/configuration/ifconfigured",
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(textStatus);
            console.log(errorThrown);
            $('#configStatus').attr('class', 'alert-color');
            $('#configStatus').text("error (" + errorThrown + ")");
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

    $("#fetchProjectConfigByNameForm").submit(function(e) {
        e.preventDefault();
        $("#fetchProjectConfigByNameButton").trigger('click');
    });
    $("#fetchProjectConfigByNameButton").click(function(e) {
        e.preventDefault();

        var projectName = $("#projectName").val();

        // fetch configuration for the project by name
        $.ajax({
            type: 'GET',
            url: BASE_URL + "/configuration/project/" + projectName,
            error: function(jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
            },
            success: function(data) {
                console.log(data);
                $("#activeProjectConfigForm").css('display', 'block');
                $("#activeProjectConfigTitle").css('display', 'block');
                $("#activeProjectConfigurationName").html("for project <strong>" + data.projectName + "</strong>");
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
});