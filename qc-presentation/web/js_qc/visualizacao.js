// parametros criados em menu.js
var qcid = getURLParameter('qcid');
var porcentagem = getURLParameter('porcentagem');


$(document).ready(function() {

    function adjustTable(tbl) {
        //var tbl = document.getElementById(tableId);
        var row = tbl.rows[0];
        var maxWidth = $("body").width() / ($(row).find("td").length);
        /*for ( var i = 0; i < row.childNodes.length; i++ ) {
         var width = row.childNodes[i].offsetWidth;
         if(width && width > maxWidth) { maxWidth = width; } 
         }*/
        for (var i = 0; i < row.childNodes.length; i++) {
            if (row.childNodes[i].style) {
                row.childNodes[i].style.width = maxWidth + "px";
            }
        }

        $(".css-vis-texto-formatado").width(maxWidth);
        $(".css-vis-diff-vazio").width(maxWidth / 2);
        $(".css-vis-diff").width(maxWidth);
        $(".css-vis-empty-right").width(maxWidth / 2);

    }


    getVisualizacao(qcid, porcentagem, function(tableVisualizacao) {
        //var table = $("div").html(tableVisualizacao).find("table:first");

        $("#htmlVisualizacaoServidor").html(tableVisualizacao);
        $('#htmlVisualizacaoServidor').show();

        adjustTable($(".css-vis-table").get(0));

        //Encerra loading
        $('#loading').hide();
        $('#fade').hide();

    }, function(res) { // fail
        //Encerra loading
        $('#loading').hide();
        $('#fade').hide();
        showAlertDialog(res.responseText, function () {
            $(location).attr('href', "/?qcid=" + qcid);
        });
        
    });

});

function getVisualizacao(qcid, porcentagem, callback, failCallback) {

    $.ajax({
        url: '/api/visualizacao/' + qcid + '/' + porcentagem,
        type: 'GET',
        contentType: "application/json; charset=utf-8"

    }).done(function(res) {
        if (callback) {
            callback(res);
        }

    }).fail(function(res) {
        if (failCallback) {
            failCallback(res);
        }
    });

}
