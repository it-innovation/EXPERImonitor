var BASE_URL = "/ECC/experiments";
$(document).ready(function() {
    $(document).foundation();

    $('#url_get').text(BASE_URL);
    $('#url_post').text(BASE_URL);

    refreshExperiments();

    $('#url_get_refresh').click(function(e) {
        e.preventDefault();
        refreshExperiments();
    });

    $('#url_post_create').click(function(e) {
        e.preventDefault();
        $.ajax({
            type: 'POST',
            dataType: 'json',
            contentType: 'application/json',
            url: BASE_URL,
            data: JSON.stringify({"name": $("#new_experiment_name").val()}),
            success: function(data) {
                console.log(data);
                refreshExperiments();
            }
        });
    });

    $('#url_update_update').click(function(e) {
        e.preventDefault();
        $.ajax({
            type: 'PATCH',
            url: BASE_URL + "/" + $('#list_exp_update option:selected').val(),
            data: $('#updated_experiment_name').val(),
            success: function(data) {
                console.log(data);
                refreshExperiments();
            }
        });
    });

    $('#url_delete_delete').click(function(e) {
        e.preventDefault();
        $.ajax({
            type: 'DELETE',
            url: BASE_URL + "/" + $('#list_exp_delete option:selected').val(),
            success: function(data) {
                console.log(data);
                refreshExperiments();
            }
        });
    });

    $('#list_exp_update').change(function() {
        var expSelected = $('#list_exp_update option:selected');
        $('#url_update').text(BASE_URL + "/" + expSelected.val());
    });

    $('#list_exp_delete').change(function() {
        var expSelected = $('#list_exp_delete option:selected');
        $('#url_delete').text(BASE_URL + "/" + expSelected.val());
    });
});

function refreshExperiments() {
    $('#experiments_list').empty();
    $('#list_exp_update').empty();
    $('#list_exp_delete').empty();
    $.getJSON(BASE_URL, function(data) {
        console.log(data);
        $.each(data, function(counter, exp) {
            $('#experiments_list').append('<p class="list_item">' + (counter + 1) + ". " + exp.name + ' [' + exp.id + ']</p>');
            $('#list_exp_update').append('<option value="' + exp.id + '">' + exp.name + '</option>');
            $('#list_exp_delete').append('<option value="' + exp.id + '">' + exp.name + '</option>');
            $('#url_update').text(BASE_URL + "/" + $('#list_exp_delete option:selected').val());
            $('#url_delete').text(BASE_URL + "/" + $('#list_exp_delete option:selected').val());
        });
    });
}