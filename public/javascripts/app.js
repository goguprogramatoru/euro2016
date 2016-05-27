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

});