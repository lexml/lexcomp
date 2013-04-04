function getVisualizacao(qcid, callback){
    
    $.ajax({
        url: '/api/visualizacao/' + qcid,
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
