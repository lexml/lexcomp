    
//***** JSPLUMB *****//
jsPlumb.ready(function() {                



    // your jsPlumb related init code goes here
    jsPlumb.importDefaults({
        Connector : "Straight",
        // default drag options
        /*DragOptions : {
                cursor: 'pointer', 
                zIndex:2000
            },*/
        // default to blue at one end and green at the other
        EndpointStyles : [{
            fillStyle:'green'
        }, {
            fillStyle:'green'
        }],

        Endpoints : [ [ "Dot", {
            radius:5
        } ], [ "Dot", {
            radius:5
        } ]],

        PaintStyle:{
            lineWidth:2,
            strokeStyle:"green",
            joinstyle:"round",
            outlineColor:"green",
            outlineWidth:2,
            fillStyle:'green'
        },

        ConnectorZIndex:5,
        Anchors: [[0, 0.2, 1, 0.5],[1, 0.2, 1, 0.5]]				
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
        }
    }

    //Listerner de conexões
    //Deleta que for clicada
    jsPlumb.bind("click", function(conn, originalEvent) {

        showConfirmDialog("Você confirma a exclusão da relação, entre: " 
            + conn.sourceId + " a " + conn.targetId + " ?",
            function () { 
                jsPlumb.detach(conn); 
                atualizaConexoes(conn, 'remover');
            });
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
    }]]
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
}

var relacaoSources;
var relacaoTargets;
var relacoes;
var tiposRelacao;

function initObjs(){

    relacaoSources = [];
    relacaoTargets = [];
    
    //Ao clicar no elemento, trata a questão do relacionamento entre estes
    $(".objDocumento").click(function(event) {
        
        
                
        event.stopPropagation();
        event.preventDefault();
        
        
        
        if($("#divMenuContexto").is(":visible")){            
            $("#divMenuContexto").hide();
            return;
        }
        
        elementoSource = $(this).attr('id');
        
        //Hint de seleção
        if(!$(this).hasClass("selected")) {
            
            addSelectedStyle($(this));
            addSelected($(this));
            
        } else {
            
            removeSelectedStyle($(this));
            removeDeselected($(this));
        }
        
        jsPlumb.detachAllConnections("pendingRelacao");
            
        if ((relacaoSources && relacaoSources.length > 0)
            || (relacaoTargets && relacaoTargets.length > 0)) {
            
            addDivRelacaoPendente();
            
        } else if ($("#pendingRelacao").length > 0) {
            $("#pendingRelacao").remove();
            $(".relacao").remove();
            jsPlumb.reset();
            getRelacoes(qcid, urn1, urn2);
        }
        
    });
    
    //Ao clicar com o botão direito
    $(".objDocumento").bind("contextmenu",function(event){
        $("#menuContexto").menu();
        $("#divMenuContexto").show();
        $("#divMenuContexto").css({top: event.pageY + 5,left: event.pageX + 5}).show();
        event.stopPropagation();
        event.preventDefault();
        return false;
    });
    //Oculta menu, quando usuário clica em qualquer outra parte do documento
    $('body').click(function() {
     $("#divMenuContexto").hide();   
    });
    
    $(window).scroll(function () {
        updateRelationDivPosition({id: "pendingRelacao",
            origem: relacaoSources, alvo: relacaoTargets}, true);
        jsPlumb.repaintEverything();
    });
    
    $(".objDocumento").mouseover(function(event) {
        event.stopPropagation();
        event.preventDefault();
        
        
        if (!$(this).hasClass("selected")) {
            
            var _this = $(this);
            if (_this.closest("#colunaComparacaoA").length != 0) {
                _this.parents("#colunaComparacaoA > .highlighted").css("background-color", "#f7f7f7");
                _this.parents("#colunaComparacaoA > .highlighted").removeClass("highlighted");
                
            
            } else {
                _this.parents("#colunaComparacaoB > .highlighted").css("background-color", "#f7f7f7");
                _this.parents("#colunaComparacaoB > .highlighted").removeClass("highlighted");
                
            }
            
            $(this).css("background-color", "#FFFF99");
            $(this).find(".objDocumento").css("background-color", "#FFFF99").addClass("highlighted");
            _this.addClass("highlighted");
        }
    }); 
    
    $(".objDocumento").mouseout(function(event) {
        event.stopPropagation();
        event.preventDefault();
        if (!$(this).hasClass("selected")) {
            $(this).css("background-color", "#f7f7f7");
            $(this).removeClass("highlighted");
            $(this).find(".highlighted").not(".selected")
            .css("background-color", "#f7f7f7").removeClass("highlighted");
            
        }
        
    });
}

function addDivRelacaoPendente(id) {
    var strDivRelacao = '<div id="pendingRelacao"';
    var relacao;
    
    if (id) {
        strDivRelacao += ' relacao_id="' + id + '"';
        
        $.each(relacoes, function (index, rel) {
            
            if (rel.id == id) {
                relacao = rel;
                return;
            }
        });
        
    }
    
    if (relacao) {
        
        if (relacao.origem) {
            $.each(relacao.origem, function (index, elem) {
                var obj = $("#objA_" + elem);
                addSelectedStyle(obj);
                addSelected(obj);
            });
        }
        
        if (relacao.alvo) {
            $.each(relacao.alvo, function (index, elem) {
                var obj = $("#objB_" + elem);
                addSelectedStyle(obj);
                addSelected(obj);
            });
        }
    }
    
    strDivRelacao += ' style="height: 20px; width: 4.5%; float: left; margin-left: 47.2%; margin-top: 150px; border: dashed; position: fixed;" class="colunaComparacao">\
                            <a href="javascript:void(0);" id="linkCancelaRelacao" style="margin: 5px;"><img src="images/remove_c.png" width="15" align="left" title="Cancelar"></a>\
                            <a href="javascript:void(0);" id="linkConfirmaRelacao" style="margin: 5px;"><img src="images/check_c.png" width="18" align="left" title="Salvar"></a>\
                        </div>';

    if ($("#pendingRelacao").length == 0) {
        var divRelacao = $(strDivRelacao);
        $("#colunaComparacaoA").after(divRelacao);
        
        $("#linkCancelaRelacao").click(function () {
            cancelaRelacao();
            $(".relacao").remove();
            getRelacoes(qcid, urn1, urn2);
        });
        
        $("#linkConfirmaRelacao").click(function () {
            var id = $(this).parent().attr("relacao_id");
            confirmaRelacao(id);
        });
        
        $(".relacao").remove();
        jsPlumb.reset();
    }
    
    updateRelationDivPosition({id: "pendingRelacao", origem: relacaoSources,
        alvo: relacaoTargets}, true);
    
    if (relacaoSources) {
        $.each(relacaoSources, function (index, elem) {

            jsPlumb.connect({ source: elem, target: "pendingRelacao",
                anchor: ["RightMiddle", "LeftMiddle"]});
        });
    }

    if (relacaoTargets) {
        $.each(relacaoTargets, function (index, elem) {

            jsPlumb.connect({ source: "pendingRelacao", target: elem,
                anchor: ["RightMiddle", "LeftMiddle"]});
        });
    }
    
}

function addDivRelacao(relacao) {
    
    var relacaoId = "relacao_" + relacao.id;
    var strDivRelacao = '<div id="' + relacaoId + '" relacao_id="' + relacao.id + '" style="height: 20px; width: 86%; border: solid;" class="colunaComparacao relacao">\
                            <a href="javascript:void(0);" id="linkRemoveRelacao_' + relacao.id + '" class="linkRemoveRelacao" style="margin: 2px;"><img src="images/remove_c.png" width="15" align="left" title="Apagar"></a>&nbsp;\
                            <a href="javascript:void(0);" id="linkEditaComentario_' + relacao.id + '" style="margin: 2px;" class="linkEditaComentario"><img src="images/comentario.png" width="15" align="left" title="Comentários"></a>&nbsp;\
                            <a href="javascript:void(0);" id="linkEditaRelacao_' + relacao.id + '" class="linkEditaRelacao" style="margin: 1px;"><img src="images/edit_c.png" width="15" align="left" title="Editar"></a>\
                            <a href="javascript:void(0);" id="linkEditaTipoRelacao_' + relacao.id + '" class="linkEditaTipoRelacao" style="margin: 0px;"><img src="images/tipo.png" width="15" align="right" title="Tipos" style="top:-14px; position:relative; right:5px;"></a>\
                        </div>';

    var divRelacao = $(strDivRelacao);
    
    $("#divRelacoes").append(divRelacao);
    
    if (relacao) {
        
        // remove elementos raiz
        var novaOrigem = [];
        var novoAlvo = [];
        
        if (relacao.origem) {
            $.each(relacao.origem, function (index, elem) {
                
                if (elem != idRaizDoc["A"]) {
                    novaOrigem.push(elem);
                }
                
            });
            
            relacao.origem = novaOrigem;
        }
        
        if (relacao.alvo) {
            $.each(relacao.alvo, function (index, elem) {
                
                if (elem != idRaizDoc["B"]) {
                    novoAlvo.push(elem);
                }
                
            });
            
            relacao.alvo = novoAlvo;
        }

        if (relacao.origem) {
            $.each(relacao.origem, function (index, elem) {
                
                if (elem == idRaizDoc["A"]) {
                    return;
                }
                
                jsPlumb.connect({ source: "objA_" + elem, target: relacaoId,
                    anchor: ["RightMiddle", "LeftMiddle"]});
            });
        }

        if (relacao.alvo) {
            $.each(relacao.alvo, function (index, elem) {

                if (elem == idRaizDoc["A"]) {
                    return;
                }

                jsPlumb.connect({ source: relacaoId, target: "objB_" + elem,
                    anchor: ["RightMiddle", "LeftMiddle"]});
            });
        }
        
        updateRelationDivPosition({ id: relacaoId, alvo: relacao.alvo,
        origem: relacao.origem});
    }
    
}

function updateRelationDivPosition(relacao, alwaysVisible) {
    
    if ($("#" + relacao.id).length == 0) {
        return;
    }
    
    var meanTop = 0;
    var nElems = 0;
    
    if (relacao.origem) {
        nElems += relacao.origem.length;
    }
    
    if (relacao.alvo) {
        nElems += relacao.alvo.length;
    }
    
    var docViewTop = $(window).scrollTop();
    var docViewBottom = docViewTop + $(window).height()
        - $("#" + relacao.id).height() - 20;

    if (relacao.origem) {
        $.each(relacao.origem, function (index, obj) {
            var id = obj + "";

            if (!id.indexOf("objA_") == 0) {
                id = "objA_" + obj;
            }
            meanTop += $("#" + id).offset().top;
        });
    }
    
    if (relacao.alvo) {
        $.each(relacao.alvo, function (index, obj) {
            var id = obj + "";

            if (!id.indexOf("objB_") == 0) {
                id = "objB_" + obj;
            }
            meanTop += $("#" + id).offset().top;
        });
    }
    
    meanTop = meanTop / nElems;

    if (alwaysVisible) {

        if (meanTop < docViewTop) {
            meanTop = docViewTop + 5;
        }

        if (meanTop > docViewBottom) {
            meanTop = docViewBottom;
        }
    }
    
    $("#" + relacao.id).offset({top: meanTop});
}

function cancelaRelacao() {
    
    $("#pendingRelacao").remove();
    jsPlumb.reset();
    
    var sourcesArray = relacaoSources.slice(0);
    var targetsArray = relacaoTargets.slice(0);
    
    $.each(sourcesArray, function (index, obj) {
       
       var elem = $("#" + obj);
       removeSelectedStyle(elem);
       removeDeselected(elem);
    });
     
    $.each(targetsArray, function (index, obj) {
       var elem = $("#" + obj);
       removeSelectedStyle(elem);
       removeDeselected(elem);
    });
}

function confirmaRelacao(id) {
    
    var relacao = {};
    relacao.id = id;
    relacao.origem = relacaoSources.map(function (x) { return x.replace("objA_", "")});
    relacao.alvo = relacaoTargets.map(function (x) { return x.replace("objB_", "")});
    saveRelacao(qcid, urn1, urn2, relacao, relacaoSaved);
}

function editaRelacao(id) {
    cancelaRelacao();
    $(".relacao").remove();
    addDivRelacaoPendente(id);
}

function comentaRelacao(id){
   
    $( "#dialog-comentario" ).dialog({
        modal:true,draggable:false,width:600, height:360});
    
}

function editaTipoRelacao(src, relacao_id){   
    
    var target_conn = jsPlumb.getConnections({source: src.attr("id")});
    var source_conn = jsPlumb.getConnections({target: src.attr("id")});
    
    var tipos = getTiposCorrelacao(source_conn.length, target_conn.length);
    
    $('select[id="tiposCorrelacao"]').html("");
    $('select[id="tiposCorrelacao"]').append("<option>-- Selecione --</option>");
    
    $.each(tipos, function (key, tipo) {               
         $('<option value="'+ key +'"></option>').html(tipo).appendTo('select[id="tiposCorrelacao"]');                     
    });   
 
    $("#dialog-tiporelacao").dialog({
        modal:true,draggable:false,width:600, height:150});
}

function relacaoSaved(res) {
    cancelaRelacao();
    $(".relacao").remove();
    getRelacoes(qcid, urn1, urn2);
}

function addSelected(elem, column) {
    
    var array;
    if ((column && column == "A")
        || elem.closest("#colunaComparacaoA").length != 0) {
        
        array = relacaoSources;
        column = "A";
        
    } else {
        array = relacaoTargets;
        column = "B";
    }
    
    if (elem.find(".objDocumento").filter("div").length == 0 ) {
            
        removeFromArray(array, elem.attr("id"));
        array.push(elem.attr("id"));

    } else {
        elem.find(".objDocumento").filter("div").each(function() {
            addSelected($(this), column);
        });
    }
}

function addSelectedStyle(obj) {
    obj.addClass("selected");
    obj.css("background-color", "#ffff99");
    obj.find(".objDocumento").addClass("selected");
    obj.find(".objDocumento").css("background-color", "#ffff99");
}

function removeSelectedStyle(obj) {
    obj.removeClass("selected").css("background-color", "#f7f7f7");
    obj.parents(".objDocumento.selected").removeClass("selected")
        .css("background-color", "#f7f7f7");
    obj.find(".objDocumento").removeClass("selected")
        .css("background-color", "#f7f7f7");
}

function removeDeselected(elem, column) {
    
    var array;
    if ((column && column == "A")
        || elem.closest("#colunaComparacaoA").length != 0) {
        
        array = relacaoSources;
        column = "A";
        
    } else {
        array = relacaoTargets;
        column = "B";
    }
    
    if (elem.find(".objDocumento").filter("div").length == 0 ) {
            
        removeFromArray(array, elem.attr("id"));
        
    } else {
        elem.find(".objDocumento").filter("div").each(function() {
            removeDeselected($(this), column);
        });
    }
    
}

var idRaizDoc = {};

function printObjetoSimbolico(obj, rotulo, coluna) {

    var strDiv = '';
    
    if (obj) {
        
        if (!idRaizDoc[coluna]) {
            idRaizDoc[coluna] = obj.id;
        }

        strDiv = '<div>';
        
        if (rotulo) {
            var strRotulo = getStrRotulo(rotulo);

            if (strRotulo) {
                strDiv += strRotulo;
            }
        }

        if (obj.representacao) {
            var novoId = 'obj' + coluna +"_"+ obj.id;
            strDiv = '<div class="objTexto objDocumento" id='+novoId+'>';
            strDiv += obj.representacao;
            strDiv += '<div class="divComentario" onclick="linkEditaComentario();">\
                           <a href="#"><img src="images/comentario.png" width="16"/></a>\
                       </div>';
        }

        if (obj.posicoes) {
                
            $.each(obj.posicoes, function (index, pos) {
                
                var rep = pos.rotulo.representacao;
                var divFilho = null;

                // excluindo agrupadores
                if (rep && !rep.match("((.)?agrupadores(.)?)")) { 
                    divFilho = printObjetoSimbolico(pos.objeto, pos.rotulo, coluna);
                }

                strDiv += divFilho;
            });
            
            var matchDoc = strDiv.match(/class=\"objTexto/g);
            if (matchDoc && matchDoc.length > 0) {
            
            	var strReplacement = '<div class="objDocumento"';
            	
            	if (obj.id) {
                    strReplacement += ' id="' + 'obj' + coluna +"_"+ obj.id + '"';
            	}
            	
                strDiv = strDiv.replace("<div", strReplacement);
            }
        }

    	strDiv += "</div>";    
    }
    
    return strDiv;
}

function getStrRotulo(rotulo) {
    
    var rep = rotulo.representacao;
    
    if (rep && !rep.match("((.)?articulacao(.)?|(.)?texto(.)?)")) { 
        
        var strRotulo = "<span class='objRotulo'>";
        
        if (rotulo.nomeRole) {
            //Tratamento Artigo
            if(rotulo.nomeRole == "art"){                                        
                strRotulo += "  " + "Art. " + rotulo.posicaoRole[0] ;
                if(rotulo.posicaoRole[0]<10){                                        
                    strRotulo += "<u><sup>o</sup></u>";
                }else{
                    strRotulo += ". ";
                }                                       
            
            } else if(rotulo.nomeRole == "cpt") { //Tratamento Inciso
                strRotulo += "<span style='color: darkgray'><i>caput</i></span>";
            
            } else if(rotulo.nomeRole == "inc") { //Tratamento Inciso
                strRotulo += romano(rotulo.posicaoRole[0]) + " -";
            
            } else if(rotulo.nomeRole == "par") { //Tratamento Parágrafo
                
                if (rotulo.classificacao == "unico") {
                    strRotulo += "Parágrafo único. ";
                
                } else {
                    strRotulo += "§ " + rotulo.posicaoRole[0] + "<u style='font-size: 0.8em;'><sup>o</sup></u> ";
                }
                
            } else if(rotulo.nomeRole == "ali") { //Tratamento Alínea 
                strRotulo += alinea(rotulo.posicaoRole[0]) //+ rotulo.nomeRole;                                        
            
            } else if(rotulo.nomeRole == "cap") { //Tratamento Capitulo 
                strRotulo = "<span class='objRotulo agregador'>";
            	strRotulo += "Capítulo " + romano(rotulo.posicaoRole[0]);
            
            } else if(rotulo.nomeRole == "tit") { //Tratamento Titulo 
                strRotulo = "<span class='objRotulo agregador'>";
            	strRotulo += "Título " + romano(rotulo.posicaoRole[0]);
                
            } else if(rotulo.nomeRole == "liv") {
                strRotulo = "<span class='objRotulo agregador'>";
            	strRotulo += "Livro " + romano(rotulo.posicaoRole[0]);
            
            } else if(rotulo.nomeRole == "sec") {
                strRotulo = "<span class='objRotulo agregador'>";
            	strRotulo += "Seção " + romano(rotulo.posicaoRole[0]);
            
            } else if(rotulo.nomeRole == "sub") {
                strRotulo = "<span class='objRotulo agregador'>";
            	strRotulo += "Subseção " + romano(rotulo.posicaoRole[0]);
            
            } else if(rotulo.nomeRole == "prt") {
                strRotulo = "<span class='objRotulo agregador'>";
            	strRotulo += "Parte " + romano(rotulo.posicaoRole[0]);
            }                                   
        
        }

        strRotulo += "&nbsp;</span>"
        return strRotulo;
    }
    
    return null;
}

function printRelacoes (relacoes) {
    
    if (!relacoes || !relacoes.length) {
        return;
    }
    
    $.each(relacoes, function (i, relacao) {
        addDivRelacao(relacao);
    });
    
    $(".linkRemoveRelacao").click(function (evt) {
        
        var thisRelacao = $(this);
        showConfirmDialog("Deseja remover a relação?",
                function () {
                    deleteRelacao(qcid, urn1, urn2, thisRelacao.parent().attr("id").replace("relacao_", ""), relacaoSaved);
                });
        
    });
    
    $(".linkEditaRelacao").click(function () {
        var id = $(this).parent().attr("relacao_id");
        editaRelacao(id);
    });
    
     $(".linkEditaComentario").click(function () {
        var id = $(this).parent().attr("relacao_id");
        comentaRelacao(id);        
    });
    
    
    $(".linkEditaTipoRelacao").click(function () {
        var id = $(this).parent().attr("relacao_id");
        editaTipoRelacao($(this).parent(), id);
    });
    
    // POG
    jsPlumb.repaintEverything();
}

//***** FUNÇÕES AUXILIARES ***//
function romano(valor){

    var N = parseInt(valor);
    var N1 = N;
    var Y = ""
    while (N/1000 >= 1) {
        Y += "M";
        N = N-1000;
    }
    if (N/900 >= 1) {
        Y += "CM";
        N=N-900;
    }
    if (N/500 >= 1) {
        Y += "D";
        N=N-500;
    }
    if (N/400 >= 1) {
        Y += "CD";
        N=N-400;
    }
    while (N/100 >= 1) {
        Y += "C";
        N = N-100;
    }
    if (N/90 >= 1) {
        Y += "XC";
        N=N-90;
    }
    if (N/50 >= 1) {
        Y += "L";
        N=N-50;
    }
    if (N/40 >= 1) {
        Y += "XL";
        N=N-40;
    }
    while (N/10 >= 1) {
        Y += "X";
        N = N-10;
    }
    if (N/9 >= 1) {
        Y += "IX";
        N=N-9;
    }
    if (N/5 >= 1) {
        Y += "V";
        N=N-5;
    }
    if (N/4 >= 1) {
        Y += "IV";
        N=N-4;
    }
    while (N >= 1) {
        Y += "I";
        N = N-1;
    }
    //alert("O numero " + N1 + " em romanos = " + Y);
    return Y;
}

function alinea(numero){

    var letra;
    if(numero == 1  ) letra = 'a)';
    if(numero == 2  ) letra = 'b)';
    if(numero == 3  ) letra = 'c)';
    if(numero == 4  ) letra = 'd)';
    if(numero == 5  ) letra = 'e)';
    if(numero == 6  ) letra = 'f)';
    if(numero == 7  ) letra = 'g)';
    if(numero == 8  ) letra = 'h)';
    if(numero == 9  ) letra = 'i)';
    if(numero == 10 ) letra = 'j)';
    if(numero == 11 ) letra = 'k)';
    if(numero == 12 ) letra = 'l)';
    if(numero == 13 ) letra = 'm)';
    if(numero == 14 ) letra = 'n)';
    if(numero == 15 ) letra = 'o';
    if(numero == 16 ) letra = 'p)';
    if(numero == 17 ) letra = 'q)';
    if(numero == 18 ) letra = 'r)';
    if(numero == 19 ) letra = 's)';
    if(numero == 20 ) letra = 't)';
    if(numero == 21 ) letra = 'u)';
    if(numero == 22 ) letra = 'v)';
    if(numero == 23 ) letra = 'w)';
    if(numero == 24 ) letra = 'x)';
    if(numero == 25 ) letra = 'y)';
    if(numero == 26 ) letra = 'z)';

    return letra;
}
    
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

function getTiposCorrelacao(cardOrigem, cardAlvo){
    
    if (cardOrigem > 1) {
        cardOrigem = "n";
    }
    
    if (cardAlvo > 1) {
        cardAlvo = "n";
    }
    
    var key = cardOrigem + ":" + cardAlvo;
    return tiposRelacao[key];
}







function getCorrelacao(qcid, urn1, urn2, callback){
    
    $.ajax({
        url: '/api/correlacao/' + qcid + '/' + urn1 + '/' + urn2,
        type:'GET',
        contentType: "application/json; charset=utf-8",
        success:function(res){
                    
            //console.log(res);
            callback(res);
        },
        error:function(res){
        //alert("Bad thing happend! " + res.statusText);
        }
    });
    
}

function getRelacoes(qcid, urn1, urn2) {
    
    var strLoading = "<div id='loadingRelacao' style='position: fixed; top: 50%; margin: 10px;'><img src='images/load2.gif'/></div>"
    $("#divRelacoes").append($(strLoading));
    
    $.ajax({
        url: '/api/correlacao/relacao/' + qcid + '/' + urn1 + '/' + urn2,
        type:'GET',
        contentType: "application/json; charset=utf-8",
        success:function(res){
            relacoes = res;
            printRelacoes(res);
            $("#loadingRelacao").remove();
        },
        error:function(res){
            //alert("Bad thing happend! " + res.statusText);
        }
    });
    
}

function saveRelacao(qcid, urn1, urn2, relacao, callback) {
    
    $.ajax({
        url: '/api/correlacao/relacao/' + qcid + '/' + urn1 + '/' + urn2,
        type: 'POST',
        data: JSON.stringify(relacao),
        dataType: 'json',
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

function deleteRelacao(qcid, urn1, urn2, idRelacao, callback) {
    
    $.ajax({
        url: '/api/correlacao/relacao/' + qcid + '/' + urn1 + '/' + urn2 + '/' + idRelacao,
        type: 'DELETE',
        dataType: 'json',
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











