 //Id do quadro selecionado pelo usuário para Exclusão, não remover, pois este exerce controle sobre o redirecionamento da tela caso o usuário exclua na janela o quadro no qual esteja trabalhando.
var idQuadroExclusao;

 function excluirQuadro(idQuadro,row){    
         
    url = "/api/qc/";
    if (idQuadro) {
            url += idQuadro;
            idQuadroExclusao = idQuadro;
    }
    
    
    showConfirmDialog("Deseja realmente excluir este Quadro Comparativo?",
            function () {
                //window.location.href = "/";              
                
                
                   $.ajax({
                        url: url,
                        dataType: "html",
                        type:'DELETE'        
                        
                    }).done(function() {
                        tabelaQuadros.fnDeleteRow(document.getElementById(row));
                    }).fail(function(){
                        alert("Erro ao tentar excluir quadro!");        
                    }).always(function() {
                        
                    });
            })
    
 
    }

function listaQuadros(abreJanela){
    
         $.ajax({
            url: '/api/qc/list',
            type:'GET',
            contentType: "application/json; charset=utf-8",
            success:function(res){
                //console.log(res);
                $("#qcOpenTable").empty();
                $("#qcOpenTable").append(
                    '<thead>\
                    <tr id="qcOpenTrHeader">\
                        <th>Título</th>\
                        <th>Data de criação</th>\
                        <th>Data de modificação</th>\
                        <th>&nbsp;</th>\
                    </tr>\
                </thead>');
                
                              
                $.each(res, function (index, quadro) {
                    
                    if (quadro.id && quadro.dataModificacao) {
                        
                        var idTr = 'qcOpenTrColunas' + index;
                        
                        $("#qcOpenTable").append(
                            '<tr id="'+idTr+'">\
                            <td><a href="/?qcid=' + quadro.id + '">' + quadro.titulo + '</a></td>\
                            <td>' + moment(quadro.id, "YYYYMMDDHHmmssSSS").format("DD/MM/YYYY HH:mm:ss") + '</td>\
                            <td>' + moment(quadro.dataModificacao, "YYYYMMDDHHmmssSSS").format("DD/MM/YYYY HH:mm:ss") + '</td>\
                            <td><a href="#" onclick="javascript:excluirQuadro(\'' + quadro.id + '\',\'' + idTr + '\');"><img src="images/remove_c.png" width="15" title="Excluir Quadro Comparativo"></a></td>\
                        </tr>');
                    }
                })

                tabelaQuadros = $("#qcOpenTable").dataTable({
                    oLanguage: {
                        sLengthMenu: "Mostrar _MENU_ registros por página",
                        sZeroRecords: "Nenhum registro encontrado",
                        sInfo: "Mostrando _START_ a _END_ de _TOTAL_ registros",
                        sInfoEmpty: "Mostrando 0 a 0 de 0 registros",
                        sInfoFiltered: "(filtrado de um total de _MAX_ registros)",
                        oPaginate: {
                            sFirst: "Primeira",
                            sLast: "Última",
                            sPrevious: "Anterior",
                            sNext: "Próxima"
                        },
                        sSearch: "Buscar"
                    },
                    bDestroy: true
                });

                if(abreJanela){                
                    $("#dialog-open").dialog({
                        resizable: false,
                        modal: true,
                        width: 700, 
                        buttons: {
                            "Cancelar": function() {
                                $( this ).dialog( "close" );
                            }
                        },
                        //Caso o usuário tenha excluído o quadro no qual está trabalhando, é feito redirecionamento para a raíz para evitar erros e que ele trabalhe no quadro que já não existe mais.
                        close: function(event, ui) { if(quadro.id == idQuadroExclusao){window.location='/';}; } 
                    });                
                }
            },
            error:function(res){
            //alert("Bad thing happend! " + res.statusText);
            }
        });
    
}

function print_menu() {
    
    document.write(
    '<div id="menu">\
        <img src="images/iniciobarra.png" border="0" />\
        <a id="newQuadro" href=""><img src="images/novoQuadro_off.png" border="0" class="xrollover"/></a>\
        <a id="openQuadro" href="javascript:void(0);"><img src="images/abrirQuadro_off.png" border="0" class="xrollover"/></a>\
        <img src="images/separadorclaro.png" border="0" />\
        <a href="/" id="plano"><img src="images/plano_off.png" border="0" class="rollover"/></a>\
        <a id="linkVisualizacao" href="#"><img src="images/visualizacao_off.png" border="0" style="height:53px;" class="rollover"/></a>\
        <img src="images/separadorclaro.png" border="0" />\
        <div id="menuCorrelacoes">\
        <a id="linkConfiguracao" href="#"><img src="images/config_rel.png" border="0" style="height:53px;" class="rollover"/></a>\
        </div>\
    	<div id="divLogo" class="logo"><img src="images/icone_lexcomp_50x.png"></div>\
    </div>');
    
    document.write('<div id="dialog-open" title="Abrir quadro comparativo" style="display: none;">\
                        <table id="qcOpenTable" class="display dataTable" style="width: 680px; margin: 20px 0; clear: both;">\
                        </table>\
                    </div>');
    
    //formulário da visualização
    document.write('<div id="dialog-visualizacao" title="Visualização" style="display: none;">\
            <form action="" id="formVisualizacao" method="POST">\
                <table>\
                    <tr>\
                    	<td>Percentual mínimo de semelhança para fazer não considerar diferença completa:</td>\
                    </tr>\
		            <tr>\
			            <td><div id="sliderVisualizacao"></div></td>\
			        </tr>\
		    		<tr>\
    					<td><input type="text" id="visualizacao-porcentagem" disabled="true" style="border: 0; color: #f6931f; font-weight: bold;"/></td>\
			        </tr>\
                    <tr>\
                        <td><input type="button" class="btnAcao" value="Processar" id="sbVisualizacao"></td>\
                    </tr>\
                </table>\
            </form>\
        </div>');
    
    if (!getURLParameter) {
        document.write('<script type="text/javascript" src="js_qc/qc_misc.js"></script>');
    }
    
    var qcid = getURLParameter("qcid");
    if (qcid) {
    	
    	/*
    	 * Configura o destino de Plano
    	 */
    	
        $("#plano").attr("href", "/?qcid=" + qcid);

        
        /*
         * Configuração da página de visualização
         */ 
        
        $("a#linkVisualizacao").click(function(event) {
            event.preventDefault();         
            $( "#dialog-visualizacao" ).dialog({
                 modal:true,draggable:false,width:600, height:360
            });
        });
        
        //campo slider da visualização
        $("#sliderVisualizacao").slider({
           value:80,
           min: 1,
           max: 99,
           step: 1,
           slide: function( event, ui ) {
               $("#visualizacao-porcentagem").val( ui.value + "%" );
           }
       });
       $("#visualizacao-porcentagem").val("80%");
       
      //botão da visualização que abre a visualização em si
       $("#sbVisualizacao").click(function(event) { 
           event.preventDefault();
           $(location).attr('href',"/visualizacao.html?qcid=" + qcid + "&porcentagem="+$("#sliderVisualizacao").slider("value"));
       }); 
       
    }
    
    //Salva o Planejamento do Quadro Comparativo
    /*$("a#saveQuadro").click(function(event) {
        event.preventDefault(); 

        saveQuadro();
    });*/

    $("a#newQuadro").click(function(event) {
        event.preventDefault();

        showConfirmDialog("Deseja criar um quadro novo? (as alterações não salvas do quadro atual serão descartadas.)",
            function () {
                window.location.href = "/";
            });
    });

    $("a#openQuadro").click(function(event) {
        event.preventDefault(); 
        
        listaQuadros(true);


    });

    $("a#linkConfiguracao").click(function(event) {
        event.preventDefault();         
        
        $( "#dialog-configuracao" ).dialog({
             modal:true,draggable:false,width:600, height:150,
             buttons: {
                 "Processar": function () {
                     
                     $.ajax({
                        url: '/api/correlacao/processar/' + qcid + '/' + urn1 + '/' + urn2 + '/' + $("#slider").slider("value"),
                        type: 'GET',
                        contentType: "application/json; charset=utf-8",
                        success: function() {
                            $("#dialog-configuracao").dialog("close");
                            //location.reload();
                            getRelacoes(qcid, urn1, urn2);
                        },
                        error: function(res) {
                            showAlertDialog("Erro ao processar correlações.");
                            console.error(res);
                        }

                    });
                    
                    $(this).dialog("close");
                 }
             }
        });
    });
    
    
    
   
}