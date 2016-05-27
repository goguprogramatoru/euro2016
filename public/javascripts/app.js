$( document ).ready(function() {

    $('.tbl').DataTable(
        {
            paging: false,
            info: true,
            "scrollX": false,
            "scrollY": 500
        }
    );

    $( ".team-select" ).click(function() {
        var team = event.target.id.replace("btnimg","").replace("btn","");
        $( "#in_team").val(
            team
        );
        $("#frm").submit()
    });

    $('#confirm-delete').on('show.bs.modal', function(e) {
        $(this).find('.btn-ok').attr('href', $(e.relatedTarget).data('href'));
    });

    $(function() {
        $('.addGameForm').submit(function() {
            // DO STUFF
            var t1 = $('#in_team1').val()
            var t2 = $('#in_team2').val()
            if(t1==t2) {
                $('#in_team1_group').addClass("has-error")
                $('#in_team2_group').addClass("has-error")
                $('#error_message').removeClass("hidden")
                return false;
            }
            else {
                return true;
            }
        });
    });

});