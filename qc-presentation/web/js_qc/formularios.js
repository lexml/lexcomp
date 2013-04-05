        
        var xml;
	var categoria;
	var localidade;
	var autoridade;
        var autoridadeFilha;
	var tipoDocumento;
	var numero;
        var evento
        var versao;
	var dataAssinatura;
        var dataEvento
        var dataVigente;
        
        

	$(function() {                
        
            //Omite elementos que somente serão exibidos após alguma ação específica		
            $('select[id="AutoridadesFilhas"]').hide();
            $('select[id="ComponenteDocumento"]').hide();
            $('#trComponenteDocumento').hide();
            
            
            $('#trVersao').hide();
            $('#tdDataVersao').hide();
            
            $('#trEvento').hide();
            $('#tdDataEvento').hide();            
            
            $('#trComissaoEspecial').hide();
            
            //Date picker
            $( "#dataVigente" ).datepicker({ dateFormat: 'yy-mm-dd' });
            $( "#dataAssinatura" ).datepicker({ dateFormat: 'yy-mm-dd' });
            $( "#dataEvento" ).datepicker({ dateFormat: 'yy-mm-dd' });
            
            
            $.ajax({
                    type: 'GET',
                    url: 'xml/configuracao.xml',
                    dataType: 'xml',
                    success: function(xmlDoc) {		

                          xml = xmlDoc;			

                          //Monta combo de Categorias
                          $(xmlDoc).find('Categoria').each(function() { 
                                    var id = $(this).attr('xmlid');
                                    var nome = $(this).find('Nome').text();
                                    $('<option value="'+id+'"></option>').html(nome).appendTo('select[id="Categorias"]');			
                          });

                           //Monta combo de Localidades
                          $(xmlDoc).find('Localidade').each(function() { 
                                    var id = $(this).attr('xmlid');
                                    var nome = $(this).find('Nome').text();
                                    $('<option value="'+id+'"></option>').html(nome).appendTo('select[id="Localidades"]');			
                          });

                    }
              });
              
              
            $("a#addComponente").click(function(event) {
                event.preventDefault(); 
                 //$('#trComissaoEspecial').hide();

                var $tr    = $(this).closest('.tr_clone');
                var $clone = $tr.clone();
                $clone.find(':text').val('');
                $tr.after($clone);
            });
        });
               
	//-- Métodos reset ds combos
        
        //Limpa combo de Versão
	function limpaEvento(){				
                $('select[id="Evento"]').hide();
                $('select[id="Evento"]').find('option').remove().end().append('<option value="">-- Selecione --</option>');
                $('#trEvento').hide();
                $('#tdDataEvento').hide();                
	}       
        
        //Limpa combo de Versão
	function limpaVersao(){				
                $('select[id="versao"]').hide();
                $('select[id="versao"]').find('option').remove().end().append('<option value="">-- Selecione --</option>'); 
                $('#trVersao').hide();
                $('#tdDataVersao').hide();
	}
        
	//Limpa combo de Localidade
	function limpaLocalidades(){				
                $('select[id="Localidades"]').find('option').remove().end().append('<option value="">-- Selecione --</option>');                
	}
        
	//Limpa combo de Autoridades
	function limpaAutoridades(){		
		$('select[id="Autoridades"]').find('option').remove().end().append('<option value="">-- Selecione --</option>');
	}
        
        //Limpa combo de Autoridades Filhas
	function limpaAutoridadesFilhas(){		
                $('select[id="AutoridadesFilhas"]').hide();
                $('#trComissaoEspecial').hide();
		$('select[id="AutoridadesFilhas"]').find('option').remove().end().append('<option value="">-- Selecione --</option>');
	}
        
	//Limpa combo de Tipo de Documento
	function limpaTipoDocumento(){		
		$('select[id="TiposDocumento"]').find('option').remove().end().append('<option value="">-- Selecione --</option>');
	}
        
        //Limpa combo de Componente de Documento
	function limpaComponenteDocumento(){
                $('select[id="ComponenteDocumento"]').hide();
                $('#trComponenteDocumento').hide();
                $('select[id="ComponenteDocumento"]').find('option').remove().end().append('<option value="">-- Selecione --</option>');               
	} 
        
        
         //Limpa combo de Componente de Documento
	function limpaComponenteColegiado(){                               
                $('select[id="SiglaColegiado"]').find('option').remove().end().append('<option value="">-- Selecione --</option>');               
	} 
        
         
        
	//Monta combo de autoridades dada a localidade escolhida
	function dadaLocalidadeRecuperaAutoridades(){		
	limpaAutoridades();
        
		var arrayAutoridade = new Array();               
		
		 //Monta combo de Autoridades
			  $(xml).find('Correlacao').each(function() {
					
                                var attCategoria             =   $(this).attr('categoria');
                                var attLocalidade            =   $(this).attr('localidade');					
                                var attAutoridade            =   $(this).attr('autoridade');                                            

                                    if(attCategoria == categoria && attLocalidade == localidade){
                                        arrayAutoridade.push(attAutoridade);
                                    }
                          });     
                          
			  autoridadesUnicas = $.unique(arrayAutoridade);
                                                  
			  //Autoridades
                          for(var i=0;i<autoridadesUnicas.length;i++){		
				 
                                var novaAutoridade;
                                
                              
                                if(autoridadesUnicas[i].indexOf(',') != -1){                                     
                                     //Autoridade de nível
                                     novaAutoridade = autoridadesUnicas[i].substring(0,autoridadesUnicas[i].indexOf(','));
                                 }else{  
                                     //Autoridade simples
                                     novaAutoridade = autoridadesUnicas[i];
                                 }   
                                 
                                 
                                 var autoridadeGet = getAutoridade(novaAutoridade);
                                 
                                 $('<option value="'+autoridadeGet.id+'"></option>').html(autoridadeGet.nome).appendTo('select[id="Autoridades"]');
			  }
                          return false;			
			 
	}
		
	
        
        
        //Monta combo Localidades dada a categoria
        function dadaCategoriaRecuperaLocalidades(){
            limpaLocalidades();
            
            //AUX - Localidades de cada correlação de acordo com a categoria atual
		var arrayLocalidade = new Array();
		
		 //Monta combo de Tipos de Documento
			  $(xml).find('Correlacao').each(function() { 
					
                                var attCategoria             =   $(this).attr('categoria');
                                var attLocalidade            =   $(this).attr('localidade'); 

                                if(attCategoria == categoria){                                           
                                    arrayLocalidade.push(attLocalidade);                               		
                                }		
			  });
                          
                          localidadesUnicas = $.unique(arrayLocalidade);                          
                          
			  for(var i=0;i<localidadesUnicas.length;i++){		
				 $('<option value="'+localidadesUnicas[i]+'"></option>').html(getLocalidade(localidadesUnicas[i])).appendTo('select[id="Localidades"]');
			  }         
        }
        
        
        
        //Monta combo de Autoridades Filhas, caso existam
        function dadaAutoridadePaiRecuperaAutoridadeFilha(){
                
              limpaAutoridadesFilhas();
              $('select[id="AutoridadesFilhas"]').show();

		var arrayAutoridadesFilha = new Array();
                var autoridadesFilhas;         
		
                    $(xml).find('Correlacao').each(function() {                                      

                          var attCategoria             =   $(this).attr('categoria');
                          var attLocalidade            =   $(this).attr('localidade');					
                          var attAutoridade            =   $(this).attr('autoridade'); 


                              if(attCategoria == categoria && attLocalidade == localidade){  
                                  if(attAutoridade.substring(0, attAutoridade.indexOf(',')) == autoridade){
                                      autoridadesFilhas = attAutoridade.substring(attAutoridade.indexOf(',')+1, attAutoridade.length);                                                     
                                      arrayAutoridadesFilha = autoridadesFilhas.split(',');
                                  }                         
                              }		

                    });  

                    //Autoridades filhas 
                    if(arrayAutoridadesFilha.length>0){
                        $('select[id="AutoridadesFilhas"]').show();
                        for(i=0;arrayAutoridadesFilha.length;i++){
                               var novaautoridade =  getAutoridade(arrayAutoridadesFilha[i]);
                               $('<option value="'+novaautoridade.id+'"></option>').html(novaautoridade.nome).appendTo('select[id="AutoridadesFilhas"]');
                        }
                    }else{
                        $('select[id="AutoridadesFilhas"]').hide();                        
                    }
                        
                  
        }  
        
   
        
        //Dado documento, recupera a versão ou o evento
        function dadoDocumentoRecuperaVersaoEvento(){
           
            limpaVersao();
            limpaEvento();
            
            $('#trVersao').show();
            $('#trEvento').show();
		
		var arrayVersao = new Array();
                var arrayEvento = new Array();
                
                var versaoAtual = {};
               
                $(xml).find('Correlacao').each(function() { 
					
                            var attCategoria             =   $(this).attr('categoria');
                            var attLocalidade            =   $(this).attr('localidade');
                            var attAutoridade            =   $(this).attr('autoridade');
                            var attTiposDocumento        =   $(this).attr('tipoDocumento');
                            var attVersao                =   $(this).attr('versao');
                            var attEvento                =   $(this).attr('evento');
                            var attComponenteColegiado   =   $(this).attr('componenteColegiado');
                            					
                                if(attCategoria == categoria && attLocalidade == localidade){
                                
                                   //Eventos
                                   if(attAutoridade.substring(0, attAutoridade.indexOf(',')) == autoridade){
                                        if(attEvento && attEvento!=""){                                            
                                            arrayEvento = attEvento.split(',');
                                        }
                                    }
                                    
                                    //Versão
                                    if(attAutoridade == autoridade){                                         
                                       arrayVersao = attVersao.split(',');
                                    }                                    
                                    
                                }
                                
			  });	
                          
                          //Versões                          
                          if(arrayVersao.length>0){
                            $('select[id="versao"]').show();
                             limpaEvento();
                            for(var i=0;i<arrayVersao.length;i++){
                                 versaoAtual = getVersao(arrayVersao[i]);                                
                                 $('<option value="'+versaoAtual.id+'"></option>').html(versaoAtual.nome).appendTo('select[id="versao"]');                                
                            }
                            
                           
                            
                          }else{
                              $('select[id="versao"]').hide();
                              $('#trVersao').hide();                              
                          }
                          
                          
                          //Eventos                          
                          if(arrayEvento.length>0){
                            $('select[id="Evento"]').show();
                            limpaVersao();
                            for(var i=0;i<arrayEvento.length;i++){
                                eventoAtual = getEvento(arrayEvento[i]);                                                                 
                                $('<option value="'+eventoAtual.id+'"></option>').html(eventoAtual.nome).appendTo('select[id="Evento"]');                                
                            }                           
                          }else{
                              $('select[id="Evento"]').hide();
                              $('#trEvento').hide();                              
                          }
            
        }
        
       
        
        //Monta combo de componentes de documento
	function dadoTipoDocumentoRecuperaComponenteDocumento(){      

	limpaComponenteDocumento();  
        

                var arrayComponenteDocumento = new Array();
                var arrayTiposDocumento = new Array();
               
                $(xml).find('Correlacao').each(function() { 
					
                            var attCategoria             =   $(this).attr('categoria');
                            var attLocalidade            =   $(this).attr('localidade');
                            var attAutoridade            =   $(this).attr('autoridade');
                            var attTiposDocumento        =   $(this).attr('tipoDocumento');
                            var attComponenteDocumento   =   $(this).attr('componenteDocumento');                            
					
                                if(attCategoria == categoria && attLocalidade == localidade){
                                    
                                    if(attAutoridade == autoridade){                                        
                                        arrayTiposDocumento = attTiposDocumento.split(',');                                        
                                        if($.inArray(tipoDocumento, arrayTiposDocumento) && attComponenteDocumento){
                                            arrayComponenteDocumento = attComponenteDocumento.split(',');                                             
                                        }                                        
                                    }else{ 
                                        arrayTiposDocumento = attTiposDocumento.split(',');
                                        if(attAutoridade.substring(0, attAutoridade.indexOf(',')) == autoridade){                               
                                            if($.inArray(tipoDocumento, arrayTiposDocumento) && attComponenteDocumento){
                                                arrayComponenteDocumento = attComponenteDocumento.split(',');
                                            }
                                        }
                                    }
                                }
                               
			  });	
                          
                         componentesDocumentosUnicos = $.unique(arrayComponenteDocumento);                          
                         
                         //Componentes
                          if(componentesDocumentosUnicos.length>0){
                              $('#trComponenteDocumento').show();
                              $('select[id="ComponenteDocumento"]').show();
                                for(var i=0; i<componentesDocumentosUnicos.length;i++){
                                    var tipoDocumentoAtual = getTipoDocumento(componentesDocumentosUnicos[i]);
                                    
                                    if (tipoDocumentoAtual) {
                                        var labelCombo = tipoDocumentoAtual.siglaDocumento + " - " + tipoDocumentoAtual.nome;
                                        var valorCombo = tipoDocumentoAtual.siglaDocumento;
                                        $('<option value="'+valorCombo+'"></option>').html(labelCombo).appendTo('select[id="ComponenteDocumento"]');
                                    }
                                }
                                
                          }else{
                                 $('select[id="ComponenteDocumento"]').hide();
                                 $('#trComponenteDocumento').hide();
                          }
                          return true;
                                            			 
	}


		
	//Monta combo de Tipo de Documento dada a Autoridade escolhida
	function dadaAutoridadeRecuperaTipoDocumento(){
		
	limpaTipoDocumento();	

		var arrayTiposDocumento = new Array();
               
                $(xml).find('Correlacao').each(function() { 
					
                            var attCategoria             =   $(this).attr('categoria');
                            var attLocalidade            =   $(this).attr('localidade');
                            var attAutoridade            =   $(this).attr('autoridade');
                            var attTiposDocumento        =   $(this).attr('tipoDocumento');                           
                            
					
                                if(attCategoria == categoria && attLocalidade == localidade){
                                    
                                    if(attAutoridade == autoridade){
                                        arrayTiposDocumento = attTiposDocumento.split(',');                                    
                                    }else{                                   
                                        if(attAutoridade.substring(0, attAutoridade.indexOf(',')) == autoridade){                               
                                            arrayTiposDocumento = attTiposDocumento.split(',');
                                        }
                                    }
                                }
                                
			  });	
                          
                         tiposDocumentosUnicos = $.unique(arrayTiposDocumento);                                
                         
                         //Tipos de Documentos
                         for(var i=0;i<arrayTiposDocumento.length;i++){
                             tipoDocumentoAtual = getTipoDocumento(arrayTiposDocumento[i]);
                             
                             if (tipoDocumentoAtual) {
                                $('<option value="'+tipoDocumentoAtual.id.substring(tipoDocumentoAtual.id, tipoDocumentoAtual.id.lengh)+'"></option>').html(tipoDocumentoAtual.nome).appendTo('select[id="TiposDocumento"]');								
                             }
                            
                           
                 }                       
                         
                         
                         
                                            			 
	}
        
        
        
        
        
        function dadoComponenteDocumentoRecuperaSiglaColegiado(){
            
            
            limpaComponenteColegiado(); 	

		var arraySiglaColegiado = new Array();
               
                $(xml).find('Correlacao').each(function() { 
					
                            var attCategoria             =   $(this).attr('categoria');
                            var attLocalidade            =   $(this).attr('localidade');
                            var attAutoridade            =   $(this).attr('autoridade');
                            var attTiposDocumento        =   $(this).attr('tipoDocumento');
                            var attComponenteColegiado   =   $(this).attr('componenteColegiado');
                            
					
                                if(attCategoria == categoria && attLocalidade == localidade){
                                    
                                    
                                    
                                    
                                    if(attComponenteColegiado && attAutoridade == autoridade){
                                        arraySiglaColegiado = attComponenteColegiado.split(',');
                                    }else{                                   
                                        if(attAutoridade.substring(0, attAutoridade.indexOf(',')) == autoridade){                               
                                            arraySiglaColegiado = attComponenteColegiado.split(',');
                                        }
                                    }
                                }
                                
			  });	
                          
                          
                          
                         arraySiglaColegiadoUnicas = $.unique(arraySiglaColegiado);    
                         
                         
                         
                         //Componente colegiados 
                         for(var i=0;i<arraySiglaColegiadoUnicas.length;i++){
                             var novaAutoridade = getAutoridade(arraySiglaColegiadoUnicas[i]);                             
                             
                             if(novaAutoridade && novaAutoridade.siglaColegiado != ""){
                                $('<option value="'+novaAutoridade.id+'"></option>').html(novaAutoridade.siglaColegiado).appendTo('select[id="SiglaColegiado"]');								
                             }
                         }
            
        }
                
                
      
                //---- Ações dos combos
                function acaoVersao(valor){
                    versao = getVersao(valor);                    
                    if(versao && versao.requerData == 'sim'){                        
                        $('#tdDataVersao').show();
                    }else{
                        $('#tdDataVersao').hide();
                    }
                }
                
               
                function acaoEvento(valor){
                    evento = getEvento(valor);
                    if(evento && evento.requerData == 'sim'){                        
                        $('#tdDataEvento').show();
                    }else{
                        $('#tdDataEvento').hide();
                    }
                }
                
                function acaoCategorias(valor){
			categoria=valor;
                        limpaLocalidades();
                        limpaAutoridades();
                        limpaAutoridadesFilhas();
                        limpaTipoDocumento();
                        limpaComponenteDocumento();
                        limpaVersao();  
                        limpaEvento();
                        dadaCategoriaRecuperaLocalidades();
		}
		
		function acaoLocalidades(valor){
			localidade=valor;
			dadaLocalidadeRecuperaAutoridades();
		}
		
		function acaoAutoridades(valor){
			autoridade = valor;
                        limpaAutoridadesFilhas();
                        limpaComponenteDocumento();                        
                        dadaAutoridadeRecuperaTipoDocumento();
                       
		}
                
                
                function acaoAutoridadesFilhas(valor){
			autoridadeFilha = valor;
                        if(valor == 'camara.deputados;comissao.especial'){                           
                            $('#trComissaoEspecial').show();
                        }else{
                            $('#trComissaoEspecial').hide();
                        }
                        
		}
            		
		function acaoTiposDocumento(valor){                    
			tipoDocumento = valor;
                        limpaComponenteColegiado();
                        dadoTipoDocumentoRecuperaComponenteDocumento();                        
                        dadoDocumentoRecuperaVersaoEvento();
		}
                
                
                
                function acaoComponenteDocumento(valor){
                         dadoComponenteDocumentoRecuperaSiglaColegiado();
                }
	
        
                //--- Métodos GET
		function getAutoridade(urn){
                    
                   var autoridade;                   
                   var id;
                   var nome;
                   var siglaColegiado;
                   
			$(xml).find('Autoridade').each(function(){
				var id = $(this).attr('xmlid');		
					if(id == urn){                                            
						 
                                                nome  = $(this).find('Nome').text();
                                                siglaColegiado = $(this).find('SiglaColegiado').text();                                                
                                                autoridade = {id:id,nome: nome,siglaColegiado:siglaColegiado};
					}		
			});	
                        
                        //alert(autoridade.siglaColegiado);
                        
			return autoridade;                   
                    
                }
                
                


		
		function getTipoDocumento(urn){
			var tipoDocumento;
                        var id;
                        var nome;
			var siglaDocumento;
			
			$(xml).find('TipoDocumento').each(function(){
				var id = $(this).attr('xmlid');		
					if(id == urn){
						nome  = $(this).find('Nome').text();
                                                siglaDocumento = $(this).find('SiglaDocumento').text();
                                                tipoDocumento = {id:id,nome: nome,siglaDocumento:siglaDocumento};
					}		
			});	
			return tipoDocumento;
		}
                
                
                function getLocalidade(urn){
			var nome;			
			$(xml).find('Localidade').each(function(){
				var id = $(this).attr('xmlid');		
					if(id == urn){                                            
						nome  = $(this).find('Nome').text();
					}		
			});	
			return nome;
		}
                
                
                function getVersao(urn){
                    
                   var versao;                   
                   var id;
                   var nome;
                   var requerData;
                   
			$(xml).find('Versao').each(function(){
				var id = $(this).attr('xmlid');		
					if(id == urn){                                            
						 
                                                nome  = $(this).find('Nome').text();
                                                requerData = $(this).attr('requerData');
                                                
                                                versao = {id:id,nome: nome,requerData:requerData};
					}		
			});	
			return versao;                   
                    
                }
                
                
                
                
                function getEvento(urn){                    
                   var evento;                   
                   var id;
                   var nome;
                   var requerData;
                   
			$(xml).find('Evento').each(function(){
				var id = $(this).attr('xmlid');		
					if(id == urn){                                            
						 
                                                nome  = $(this).find('Nome').text();
                                                requerData = $(this).attr('requerData');                                                
                                                evento = {id:id,nome: nome,requerData:requerData};
					}		
			});	
			return evento;                   
                    
                }
