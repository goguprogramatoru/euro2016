$( document ).ready(function() {

    $('.tblPivot').DataTable(
        {
            paging: false,
            info: true,
            "sScrollX": "100%",
            "sScrollXInner": "100%",
            keys: true,
            "fixedColumns": true,
            "scrollY": 500
        }
    );

    $('.tbl').DataTable(
        {
            paging: false,
            info: true,
            "sScrollX": "100%",
            "sScrollXInner": "100%",
            "scrollY": 500
        }
    );


    $('.tblMyScores').DataTable(
        {
            paging: false,
            info: true,
            "sScrollX": "100%",
            "sScrollXInner": "100%",
            "scrollY": 500,
            "order": [[ 0, "desc" ]]
        }
    );



    $( ".team-select" ).click(function(event) {
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