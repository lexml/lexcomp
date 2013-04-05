var quadro = {};
var urnAtual;
var textoAtualModal;
var colunaAtual;
var urnTextoNovo = 'URNTEXTONOVO';

            		
//Gera um nome de URN Temporário, para os novos textos
function uniqval(len) {
    var i, chars='3WzE6hTYbUqI42O8PaASJwLZ5XCVyGBNMeHrtRuopKsdfgjklx7cvFnm10iD9', str='';
			
    if (!len)
        len = 15;
			
    for(i=0; i < len; i++)
        str += chars.substr((Math.floor(Math.random() * (chars.length + 1))), 1);
    return str;
}

function getHeaderColuna (coluna, boundary) {
    
    //alert(coluna.colunaPrincipal);
    
     var checkedC = "";
    if (coluna.colunaPrincipal) {
        checkedC = "checked";
    }
    
    var titulo = coluna.titulo ? coluna.titulo : 'Nova Coluna';
    var header = null;
    
    if (boundary) {
        header = '<th data-header="' + coluna.id + '" id="th-' + coluna.id
        + '" class="dragtable-drag-boundary">';
    
    } else {
        header = '<th data-header="' + coluna.id + '" id="th-' + coluna.id + '">';
    }
    
    header += '<div style="text-align: center; width: 10px; float: left; font-size: 1.6em; margin-left: 5px;">\
                    <a href="" id="removeColumn" col-id="' + coluna.id + '" style="color: white; text-decoration: none; font-weight: bold;">x</a>\
               </div>\
               <div class="dragtable-drag-handle">&nbsp;</div>';
    //header+='<input type="radio" name="principal"  onclick="javascript:definirColunaPrincipal(\'' + coluna.id + '\');" class="checkColunaPrincipal"  ' + checkedC + '  title="Definir como coluna principal." value="' + coluna.id + '">&nbsp;';
    header += '<!--img src="images/edit_c.png" width="15"/--><span id="editme1" class="col_title" col-id="' + coluna.id + '" style="color:#666; text-align:center;" title="Clique sobre o nome da coluna, para poder editá-lo.">' + titulo + '</span>';
    header+='<span style="text-decoration: none;  font-weight: normal;  font-size: 12px;  color: #666;"><br><input type="radio" name="principal"  onclick="javascript:definirColunaPrincipal(\'' + coluna.id + '\');" class="checkColunaPrincipal"  ' + checkedC + '  title="Definir como coluna principal." value="' + coluna.id + '">&nbsp;Definir como coluna principal</span>';
    
    header += '<a href="" id="addColumn" col-id="' + coluna.id + '"><span style="font-size: 1.8em; float: right; margin-right: 6px;">+</span></a>';
    header += '</th>';
    return header; 
}

function getStrColuna (coluna, strTexto) {
    
    return '<td style="vertical-align: top;">\
                <div id="drop" class="recebeDrag">\
                  <div id="' + coluna.id + '" class="novacoluna">\
                    \<a href="" id="addItem">Adicionar Texto</a>\
                    <ul id="listaTextos" class="listaTextos">\
                    ' + strTexto + '\
                    </ul>\
                  </div>\
                </div>\
            </td>';
}
            
function htmlTexto(idDiv, urn, titulo, incluidoVisualizacao){
			
    urnAtual = urn;
    
    var checked = "checked";
    if (!incluidoVisualizacao) {
        checked = "";
    }
    				
    var html = '<li>\
                        <div class="window" id="' + idDiv + '" urn="' + urn + '">\
                            <h2 class="tituloTexto">' + titulo + '</h2>\
                            <a href="" class="removeItem">Remover</a> - \
                            <a href="" class="editText">Editar texto</a> - \
                            <a href="" class="editURN">Editar URN</a> - \
                            <a href="http://www.lexml.gov.br/urn/' + urn + '" class="linkLexml" target="_blank">LexML</a><br><br>\
                            <input type="checkbox" title="Incluir na visualização" name="visualizacao" value="1" onclick="incluiTextoVisualizacao(this, \'' + urn + '\')" class="checkIncluiTexto" ' + checked + '> Incluir na visualização\
                        </div>\
                    </li>';
				
    return html;
}			

//Monta quadro comparativo
function montaQuadro() {

    $("#trHeader").empty();
    $("#trColunas").empty();
    
    //Seta nome do quadro
    $('#nomeQuadro').html(quadro.titulo);
    
    //Realiza iterações para montagem dos headers das colunas
    $.each(quadro.colunas, function (index, coluna) {
        
        $("#trHeader").append(getHeaderColuna(coluna));
    });
    
    //Realiza iterações para montagem das colunas e textos
    for (coluna in quadro.colunas) {
						
        strTexto = "";
						
        for (texto in quadro.colunas[coluna].textos) {
							
            var txt = quadro.colunas[coluna].textos[texto];
							
            var novaURN = txt.urnIdDIV;
            
            strTexto += htmlTexto(novaURN, txt.urn, txt.titulo, txt.incluidoVisualizacao);
            
            ajustaBodyW(250); 
        }
						
        $("#trColunas").append(getStrColuna(quadro.colunas[coluna], strTexto));
            
        $('#listaTextos-' + quadro.colunas[coluna].id).sortable({                        
            cursor: "move",
            revert: true                        
        });
            
            
        for (texto in quadro.colunas[coluna].textos) {
							
            txt = quadro.colunas[coluna].textos[texto];
            novaURN = txt.urnIdDIV;                       
            addEndpoints(novaURN, [[1, 0.2, 1, 0.5],[0, 0.2, 1, 0.5]]);
        }
						
    }
    //Refaz as conexões dinamicamente            
    for (conexao in quadro.conexoes) {
        
        con = quadro.conexoes[conexao];
        console.log(con.sourceId + ' - ' + con.targetId);      
      
        
        jsPlumb.connect({
            source:con.sourceId, 
            target:con.targetId
        });
    }
                
}
			
function carregaQuadro(id) {
					
    url = "/api/qc/";
                
    if (id) {
        url += id;
    }
                
    $.get(url, function(data) {
        						
        quadro = data;
        
        if (!quadro.conexoes) {
            quadro.conexoes = [];
        }
        
        montaQuadro();
        configuraQuadro();
    });
                			
}

function incluiTextoVisualizacao(source, urn) {
    var texto = getTextoByURN(quadro, urn);
    
    if (source.checked) {
        texto.incluidoVisualizacao = true;
    } else {
        texto.incluidoVisualizacao = false;
    }
    
    saveQuadro();
}



function definirColunaPrincipal(idColuna){
    
    $.each(quadro.colunas, function (ic,coluna) {
        
        
                 
                    if (coluna.id == idColuna) {   
                        
                        coluna.colunaPrincipal = true; 
                        console.log(coluna.id + " --> " + idColuna);
                    }else{
                        coluna.colunaPrincipal = false;
                    }
                   
                });
    
    
    
    saveQuadro();
    
}




function strip (html) {
    var tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.innerText || tmp.textContent;
}

//Configura Quadro Comparativo
function configuraQuadro() {
    //Salva títulos alterados   
    $(".col_title").editable(function(value, settings) { 
        
        if (value) {
        
            colId = $(this).attr("col-id");

            $.each(quadro.colunas, function (index, col) {

                if (col.id == colId) {
                    col.titulo = value;
                    saveQuadro();
                    return;
                }
            });
        }
        
        return(value);
    });
    
    $("#nomeQuadro").editable(function(value, settings) {
        
        return tituloQuadroEdited(this, value);
        
    }, { onblur: function(value) {
            this.reset(value);
            $(this).html(value);
            return tituloQuadroEdited(this, value);
            //return (value); }
    }});
        
    $("#qcTable").dragtable({
        stop: function (e, ui) {
            
            var order = $('#qcTable').dragtable('order');
            $.each(order, function(index, item) {
                
                // ordenando colunas...
                $.each(quadro.colunas, function (ic, coluna) {
                   
                    if (coluna.id == item) {
                        coluna.order = index;
                    }
                   
                });
                
                quadro.colunas.sort(function (a, b) {
                    return a.order - b.order;
                });
            });
            
            $(".window").each(function() {
                jsPlumb.show($(this), true);
            });
            saveQuadro();
            jsPlumb.repaintEverything();
        },
        start: function (e, ui) {
            $(".window").each(function() {
                jsPlumb.hide($(this), true);
            });	
            
            $("._jsPlumb_endpoint").hide();
        }
    });

    //Configura Drag and Drop dos textos da coluna
    $('.listaTextos').sortable({
        cursor: "move",
        revert: false,
        scroll:false,
        stop: function(e, ui) {
            
            $(".window").each(function(index, item) {
                
                var div_texto = $(this);
                jsPlumb.show($(this), true);
                
                // ordenando textos...
                $.each(quadro.colunas, function (ic, coluna) {
                   
                    if (coluna.textos) {
                   
                        $.each(coluna.textos, function (it, texto) {
                           
                            if (texto.urnIdDIV == div_texto.attr("id")) {
                                texto.order = index;
                            }
                        });

                        coluna.textos.sort(function (a, b) {
                            return a.order - b.order;
                        });
                   
                    }
                });
            });
            
            saveQuadro();
            jsPlumb.repaintEverything();
        },
        start: function( e, ui ) {
            $(".window").each(function() {
                jsPlumb.hide($(this), true);
            });						                       
        }
    });

    //Adiciona Caixa de Texto
    $('a#addItem').unbind('click');
    $('a#addItem').click(function(evt){
        evt.preventDefault();
        
        //Recupera ID Atual da coluna
        var idColuna = $(this).parent().get(0).id;
        colunaAtual = idColuna;
        textoAtualModal = null;
        
        //Abre a Modal, passando o ID temporário do TEXTO
        formModalOpen(urnTextoNovo, function() {
            
            if(textoAtualModal) {
                //Cria novo texto para a coluna
                var novoTextoVazio = htmlTexto(urnTextoNovo, urnTextoNovo, "Novo texto", true);

                //Adiciona o novo texto na coluna em questão
                $('#' + idColuna + ' ul').append(novoTextoVazio);
            }

            //Ajusta a altura do Body
            ajustaBodyH(110);
            configuraQuadro();
            saveQuadro();
        });
        
    });
    
    
       
    //Importar texto
    $('.importarTexto').unbind('click');
    $('.importarTexto').click(function(event){
        event.preventDefault();
        alert('Realiza importação de texto...');        
    });
    
    
    
    $("#sbLegislacao").click(function(event) {           
        event.preventDefault();        
        //urnAtualModal = "urnXXXX";
        
        $.ajax({  
            url:'/api/urn?' + $('#formLegislacao').serialize(),
            type:'GET',               
            dataType: 'json',
            contentType: "application/json; charset=utf-8",               
            success:function(texto) {
                
                if (textoAtualModal) {
                    var textoEditado = getTextoByURN(quadro, textoAtualModal.urn);
                    texto.preambulo = textoEditado.preambulo;
                    texto.articulacao = textoEditado.articulacao;
                    texto.articulacaoXML = textoEditado.articulacaoXML;
                    texto.fecho = textoEditado.fecho;
                    texto.order = textoEditado.order;
                    texto.documento = textoEditado.documento;
                    replaceTexto(quadro, textoAtualModal.urn, texto);
                    textoAtualModal = texto;
                    
                } else {
                    
                    texto.incluidoVisualizacao = true;
                    textoAtualModal = texto;

                    $.each(quadro.colunas, function (icol, coluna) {
                        //console.log(coluna.id + " --> " + colId);
                        if (coluna.id == colunaAtual) {

                            if(!quadro.colunas[icol].textos) {
                                quadro.colunas[icol].textos = [];
                            }

                            quadro.colunas[icol].textos.push(texto);

                        }
                    });
                }
                
                saveQuadro();
                $("#modalForm").dialog("close");

            },
            error:function(res){
                alert("Bad thing happend! " + res.statusText);
            }
        }); 
         
    }); 

    //Adiciona Coluna
    $('a#addColumn').unbind('click');
    $('a#addColumn').click(function (evt) {
        evt.preventDefault();
        
        ajustaBodyW(250);       
        
        i = 1;
        var novoId = quadro.id + '-' + (quadro.colunas.length + i);
        
        while ($("#th-" + novoId).length > 0) {
            i++;
            novoId = quadro.id + '-' + (quadro.colunas.length + i);
        }
        
        colId = $(this).attr("col-id");
        var coluna = {
            id: novoId, 
            titulo: 'Nova Coluna'
        };
        
        $("#th-" + colId).after(getHeaderColuna(coluna));
        $("#" + colId).closest("td").after(getStrColuna(coluna, ""));
        
        quadro.colunas.push(coluna);
        saveQuadro();
        jsPlumb.repaintEverything();
        configuraQuadro();
    });
    
    // Remove Coluna
    $('a#removeColumn').unbind('click');
    $('a#removeColumn').click(function (evt) {
        evt.preventDefault();
        
        if (quadro.colunas.length <= 1) {
            return;
        }
        
        var colId = $(this).attr("col-id");
        var col = null;
        
        $.each(quadro.colunas, function (index, c) {
           
            if (c.id == colId) {
                col = c;
            }
        });
        
        showConfirmDialog("Tem certeza que deseja excluir a coluna '"
            + col.titulo + "'?", removeColumn, {
                'colId': colId
            });
        
    });
    
    //Janela Modal (Formulário de criação de URNs)
    $('.editURN').unbind('click');
    $('.editURN').click(function(evt){
        evt.preventDefault();        
        var urn =  $(this).parent().attr("urn");
        textoAtualModal = getTextoByURN(quadro, urn);
        formModalOpen(urn);
    });	    
    
    $('.gerarURN').unbind('click');
    $('.gerarURN').click(function(evt){
        evt.preventDefault();								   
        //$(this).parent().parent().remove();
        alert('Gerar URN');
    });	
    
    
    
    $('.removeItem').unbind('click');
    $('.removeItem').click(function(evt){        
        evt.preventDefault();
        
        var titulo = $(this).siblings(".tituloTexto").html();
        
        showConfirmDialog("Tem certeza que deseja excluir o texto '" + titulo + "'?",
            removeText, {
                'texto': $(this).parent()
            });
    });
    
 
    
    
    // edita conteudo dos textos
    $('a.editText').unbind('click');
    $('a.editText').click(function(event) {
        event.preventDefault();
            
        $("#preambulo-textarea").val(null);
        $("#texto-textarea").val(null);
        $("#fecho-textarea").val(null);
            
        //nicEditors.findEditor("preambulo-textarea").setContent(null);
        //nicEditors.findEditor("texto-textarea").setContent(null);
        //nicEditors.findEditor("fecho-textarea").setContent(null);
            
        var urn = $(this).parent().attr("urn");
        var titulo = $(this).siblings(".tituloTexto").html();
        var texto, coluna = null;
            
        $.each(quadro.colunas, function (ic, col) {
                   
            if (col.textos) {

                $.each(col.textos, function (it, txt) {

                    if (txt.urn == urn) {
                        texto = txt;
                        coluna = col;
                        return;
                    }
                });

            }
        });
        
        if ($("#dialog-edit-text").attr("title") != "") {
            $("#dialog-edit-text").dialog('option', 'title', "Editar texto - " + titulo);
        
        } else {
            $("#dialog-edit-text").attr("title", "Editar texto - " + titulo);
        }
        
        while(urn.match(";")) {
            urn = urn.replace(";", "__");
        }
        
        //urn = urn.replace(/:/g, "_");
            
        $("#dialog-edit-text").dialog({
            resizable: false,
            modal: true,
            width: 1000,
            height: 650,
            buttons: {
                "Salvar": function() {
                    var dialog = $(this);
                        
                    //texto.preambulo = nicEditors.findEditor("preambulo-textarea").getContent();
                    //texto.articulacao = nicEditors.findEditor("texto-textarea").getContent();
                    //texto.fecho = nicEditors.findEditor("fecho-textarea").getContent();
                        
                    texto.preambulo = $("#preambulo-textarea").val();
                    texto.articulacao = $("#texto-textarea").val();
                    texto.fecho = $("#fecho-textarea").val();
                            
                    $.ajax({
                        url: '/api/texto/qc/' + quadro.id + '/col/' + coluna.id + "/",
                        type: 'POST',
                        data: JSON.stringify(texto),
                        dataType: 'json',
                        contentType: "application/json; charset=utf-8",
                        success:function(res){
                            dialog.dialog("close");
                        },
                        error:function(res){
                            //showAlertDialog("Falha ao salvar texto: " + res.statusText);
                            dialog.dialog("close");
                        }
                    });
                },
                "Cancelar": function() {
                    $( this ).dialog( "close" );
                }
            },
            open: function (evt, ui) {
                
                $("#texto-textarea").attr('readonly','readonly');
                $("#texto-textarea").val('Buscando texto...');
                $.ajax({
                    url: '/api/texto/' + urn + '/qc/' + quadro.id,
                    type:'GET',
                    contentType: "application/json; charset=utf-8",
                    success:function(res){
                        $("#preambulo-textarea").val(res.preambulo);
                        $("#texto-textarea").val(strip(res.articulacao));
                        $("#texto-textarea").removeAttr('readonly');
                        $("#fecho-textarea").val(res.fecho);
                        
                        
                    //nicEditors.findEditor("preambulo-textarea").setContent(res.preambulo);
                    //nicEditors.findEditor("texto-textarea").setContent(res.articulacao);
                    //nicEditors.findEditor("fecho-textarea").setContent(res.fecho);
                    },
                    error:function(res){
                        showConfirmDialog("Não foi possível recuperar texto remotamente.");
                        $("#texto-textarea").removeAttr('readonly');
                        $("#texto-textarea").val('');
                        
                        
                        //alert("Bad thing happend! " + res.statusText);
                    }
                });
            }
        });
            
    });
    
}

function tituloQuadroEdited(src, value) {
        
    if (value) {
        quadro.titulo = value;
        saveQuadro();
    }
    return(value);
}

function showConnectionDialog(text, editCallback, deleteCallback, params) {
    $("#confirm-text").html(text);
    $("#dialog-confirm").dialog({
        resizable: false,
        modal: true,
        width: 400, 
        buttons: {
           
           "Editar correlação": function() {
                $( this ).dialog( "close" );
                
                if (editCallback) {
                    
                    editCallback(params);
                }
                
            },
            "Deletar": function() {
                $( this ).dialog( "close" );
                
                if (deleteCallback) {
                    
                    deleteCallback(params);
                }
                
            },
            "Cancelar": function() {
                $( this ).dialog( "close" );
            }
        }
    });
}

function showAlertDialog(text, okCallback, params) {
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
            }
        }
    });
}

function removeColumn(params) {
    var colId = params.colId;
    $("#th-" + colId).remove();
    $("#" + colId).closest("td").remove();

    conexoes = [];
    colunas = [];

    $.each(quadro.colunas, function (icol, coluna) {
        //console.log(coluna.id + " --> " + colId);
        if (coluna.id == colId) {

            if (coluna.textos) {

                $.each(coluna.textos, function (it, texto) {

                    var urn = texto.urnIdDIV;
                    jsPlumb.removeAllEndpoints(urn);

                    $.each(quadro.conexoes, function (icon, conexao) {

                        if (conexao.sourceId != urn && conexao.targetId != urn) {
                            //alert(urn + " ---- " + conexao.targetId);
                            conexoes.push(conexao);
                        }
                    });

                });

            } else {
                conexoes = quadro.conexoes;
            }

        } else {
            colunas.push(coluna);
        }
    });

    quadro.conexoes = conexoes;
    quadro.colunas = colunas;

    //jsPlumb.repaintEverything();
    saveQuadro();
    jsPlumb.reset();
    montaQuadro();
    configuraQuadro();
}

function removeText(params) {
    
    var text = params.texto;
    var textId = text.attr("id");
    text.parent().remove();
    
    var conexoes = [];
    var textos = [];
    
    $.each(quadro.colunas, function (icol, coluna) {
        
        textos = [];
        
        if (coluna.textos) {

            $.each(coluna.textos, function (it, texto) {

                var urn = texto.urnIdDIV;
                
                if (urn != textId) {
                    textos.push(texto);
                }
                
            });
            
            if (coluna.textos.length != textos.length) {
                coluna.textos = textos;
            }

        }
    });
    
    $.each(quadro.conexoes, function (i, conexao) {

        if (conexao.sourceId != textId && conexao.targetId != textId) {
            //alert(urn + " ---- " + conexao.targetId);
            conexoes.push(conexao);
        }
    });

    quadro.conexoes = conexoes;

    //jsPlumb.repaintEverything();
    saveQuadro();
    jsPlumb.reset();
    montaQuadro();
    configuraQuadro();
}
            
//JSPLUMB -- Ready --
jsPlumb.ready(function() {
		
    // your jsPlumb related init code goes here
    jsPlumb.importDefaults({
        Connector : "Straight",
        // default drag options
        DragOptions : {
            cursor: 'pointer', 
            zIndex:2000
        },
        // default to blue at one end and green at the other
        EndpointStyles : [{
            fillStyle:'green'
        }, {
            fillStyle:'green'
        }],
                    
        Endpoints : [ [ "Dot", {
            radius:10
        } ], [ "Dot", {
            radius:10
        } ]],

        PaintStyle:{
            lineWidth:4,
            strokeStyle:"green",
            joinstyle:"round",
            outlineColor:"green",
            outlineWidth:2,
            fillStyle:'green'
        },
        
        ConnectorZIndex:5,
        Anchors: [[1, 0.2, 1, 0.5], [0, 0.2, 1, 0.5]]				
    });    
				
				
    //Função para adição e remoção das conexões ativas
    function atualizaConexoes(conexao, acao){
        
        var conexoes = [];
        
        if(conexao && acao != ""){

            if (acao == "adicionar") {
                quadro.conexoes.push(conexao);
            }

            if (acao == "remover") {						
                
                $.each(quadro.conexoes, function (index, conn) {
                    //alert(conexao.sourceId + " === " + conn.sourceId);
                    //alert(conexao.targetId + " === " + conn.targetId);
                   
                    if (conexao.sourceId != conn.sourceId
                        || conexao.targetId != conn.targetId) {
                       
                        conexoes.push(conn);
                    }
                });
                
                quadro.conexoes = conexoes;
            }
            
            saveQuadro();
        }
    }
		
    //Listerner de conexões
				
    //Deleta que for clicada
    jsPlumb.bind("click", function(conn, originalEvent) {
        
        showConnectionDialog("Escolha uma opção abaixo: ",
            function () { 
                
               var urn1 = $("#"+conn.sourceId).attr('urn');
               var urn2 = $("#"+conn.targetId).attr('urn');               
               
               var textoTmp1 = getTextoByURN(quadro, urn1);
               var textoTmp2 = getTextoByURN(quadro, urn2);
               
               if(textoTmp1 && textoTmp1.documentoParseado && textoTmp2 && textoTmp2.documentoParseado) {               
                     
                        urn1 = urn1.split(";").join("__");
                        urn2 = urn2.split(";").join("__");

                        var url = "correlacao.html?qcid=" + quadro.id + "&urn1=" + urn1 + "&urn2=" + urn2;
                        window.location= url;
               }else{
                   alert("Para editar correlações, se faz necessária a inserção dos textos. \n\nClique em Editar Texto para inserir o texto desejado.");
               }
               
            },
            function () { 
                jsPlumb.detach(conn); 
                atualizaConexoes(conn, 'remover');
            });
    });   
				
    //Ao conectar adiciona a nova conexão a lista de conexões existentes;
    jsPlumb.bind("connectionDragStop", function(connection) {                    
					
        var origem = $("#"+connection.sourceId);
        var colunaOrigem = origem.parent().parent().parent().get(0).id;

        var destino = $("#"+connection.targetId);
        var colunaDestino = destino.parent().parent().parent().get(0).id;
					
        if(colunaOrigem != colunaDestino){
						
            var novaConexao = {
                sourceId: connection.sourceId, 
                targetId: connection.targetId
            };
            atualizaConexoes(novaConexao, 'adicionar');
            console.log ("###  Conexão: De: " + connection.sourceId + " para: " + connection.targetId);					
						
        }else{
            jsPlumb.detach(connection);
            alert("Textos da mesma coluna não podem ser relacionados entre si...");
        }
    });
    
    //Rollover do menu
    $("img.rollover").hover(
        function() {
            this.src = this.src.replace("_off", "_on");
        },
        function() {
            this.src = this.src.replace("_on", "_off");
        });
    
                
});
            
// the definition of source endpoints (the small blue ones)
var sourceEndpoint = {
    paintStyle:{
        fillStyle:"green"
    },
    isSource:true,
    isTarget:true,
    maxConnections:-1
},
            
// the definition of target endpoints (will appear when the user drags a connection) 
targetEndpoint = {
    endpoint:"Dot",                                 
    paintStyle:{
        fillStyle:"#558822",
        radius:7
    },
    hoverPaintStyle: {
        lineWidth:5,
        strokeStyle:"#2e2aF8"
    },
    maxConnections:-1,
    dropOptions: {
        hoverClass:"hover", 
        activeClass:"active"
    },
    isTarget:true,                  
    overlays:[
    [ "Label", { 
        location:[0.5, -0.5],
        //label:"Drop",
        cssClass:"endpointTargetLabel"
    }
    ]
    ]
};
            
var allSourceEndpoints = [], allTargetEndpoints = [];
function addEndpoints(toId, sourceAnchors, targetAnchors) {
				
    for (var i = 0; i < sourceAnchors.length; i++) {
        var sourceUUID = toId + sourceAnchors[i];
        allSourceEndpoints.push(jsPlumb.addEndpoint(toId,
            sourceEndpoint, {
                anchor:sourceAnchors[i], 
                uuid:sourceUUID
            }));                                               
    }
                
    if (targetAnchors) {
        for (var j = 0; j < targetAnchors.length; j++) {
            var targetUUID = toId + targetAnchors[j];
            allTargetEndpoints.push(jsPlumb.addEndpoint(toId,
                targetEndpoint, {
                    anchor:targetAnchors[j], 
                    uuid:targetUUID
                }));                                               
        }
    }
}; 
					
$(document).ready(function(){          
    idQuadro = getURLParameter("qcid");
    carregaQuadro(idQuadro);
});