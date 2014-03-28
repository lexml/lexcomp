//Ajusta a largura do Body
function ajustaBodyW(pixels){   
    pixels = parseInt(pixels);
    var bodyAtual = $('body').css('width');        
    bodyAtual = parseInt(bodyAtual.substring(0,(bodyAtual.length - 2)));
    bodyAtual = bodyAtual + pixels;
    $('body').css('width', bodyAtual);
}


//Ajusta o a altura do Body
function ajustaBodyH(pixels){   
    pixels = parseInt(pixels);
    var bodyAtual = $('body').css('height');        
    bodyAtual = parseInt(bodyAtual.substring(0,(bodyAtual.length - 2)));
    bodyAtual = bodyAtual + pixels;
    $('body').css('height', bodyAtual);
}

//Parser da DIV de Texto que recebe a URN como nome, por padrão
function parse_divURN (urn) {
    var pontos = urn.replace (/:/g, "_");
    var pontov = pontos.replace (/;/g, "_");
    var traco = pontov.replace (/-/g, "_");
    var ponto =  traco.replace (/\./, "_");
    var arroba =  ponto.replace (/@/g, "_");
    var novaURN = arroba;				
    return novaURN;
}

function atualizaIdConexoes(urnAntiga, urnNova) {
    
    $.each(quadro.conexoes, function (index, conn) {
        //alert(conexao.sourceId + " === " + conn.sourceId);
        //alert(conexao.targetId + " === " + conn.targetId);

        if (conn.sourceId == urnAntiga) {
            conn.sourceId = urnNova;
        }
            
        if (conn.targetId == urnAntiga) {
            conn.targetId = urnNova;
        }
    });
    
    saveQuadro();
}

function atualizaIdCorrelacao(qcId, urnAntiga, urnNova, callback) {
    
    //substitui ; por __ porque tudo o que está após o caracter ; desaparece 
    while(urnAntiga.match(";")) {
        urnAntiga = urnAntiga.replace(";", "__");
    }
    
    $.ajax({
        url: '/api/correlacao/updateid/' + qcId + '/' + urnAntiga,
        type: 'POST',
        data: urnNova,
        dataType: "html",
        contentType: "application/json; charset=utf-8"
        
    }).done(function() {
        
    }).fail(function(){
        //alert("Bad thing happend! " + res.statusText);
    }).always(function() {
        if (callback) {
            callback();
        }
    });
}

function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '='
        + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g,'%20')) || null;
}

function saveQuadro() {
    $.ajax({
        url: '/api/qc/',
        type:'POST',
        data: JSON.stringify(quadro),
        dataType: 'json',
        contentType: "application/json; charset=utf-8",
        success:function(res){
            
        },
        error:function(res){
            //alert("Bad thing happend! " + res.statusText);
        }
    });
}

function getTiposComentario(callback) {
    $.ajax({
        type: 'GET',
        url: 'xml/tipo-comentario.xml',
        dataType: 'xml',
        success: function(res) {

            if (callback) {
                callback(res);
            } 
        }
    });
}

function getTextoByURN(quadro, urn) {
    
    var texto;
    
    $.each(quadro.colunas, function (ic, col) {
                   
        if (col.textos) {

            $.each(col.textos, function (it, txt) {

                if (txt.urn == urn) {
                    texto = txt;
                }
            });

        }
    });
    
    return texto;
}

function replaceTexto(quadro, urn, texto) {
    
    $.each(quadro.colunas, function (ic, col) {
                   
        if (col.textos) {

            $.each(col.textos, function (it, txt) {

                if (txt.urn == urn) {
                    
                    col.textos[it] = texto;
                }
            });

        }
    });
    
}

function removeFromArray(arr) {
    var what, a = arguments, L = a.length, ax;
    while (L > 1 && arr.length) {
        what = a[--L];
        while ((ax= arr.indexOf(what)) !== -1) {
            arr.splice(ax, 1);
        }
    }
    return arr;
}

//Padrão
function showConfirmDialog(text, okCallback, params) {
    $("#confirm-text").html(text);
    $("#dialog-confirm").dialog({
        resizable: false,
        modal: true,
        width: 400, 
        buttons: {
            "OK": function() {
                $( this ).dialog( "close" );
                
                if (okCallback) {
                    
                    okCallback(params);
                }
                
            },
            "Cancelar": function() {
                $( this ).dialog( "close" );
            }
        }	
    });
}

function showAlertDialog(text, okCallback, params) {
    $("#alert-text").html(text);
    $("#dialog-alert").dialog({
        resizable: false,
        modal: true,
        width: 400,
        buttons: {
            "OK": function() {
                $(this).dialog("close");

                if (okCallback) {

                    okCallback(params);
                }
            }
        }
    });
}



