        
        var xml;     
        

	$(function() {                
        
            
            
            
            $.ajax({
                    type: 'GET',
                    url: 'xml/tipo-comentario.xml',
                    dataType: 'xml',
                    success: function(xmlDoc) {		

                          xml = xmlDoc;			

                          //Monta combo de Tipos
                          $(xmlDoc).find('Tipo').each(function() { 
                                    var id = $(this).attr('xmlid');
                                    var nome = $(this).find('Nome').text();
                                    $('<option value="'+id+'"></option>').html(nome).appendTo('select[id="tiposComentario"]');			
                          });

                          

                    }
              });
              
        });