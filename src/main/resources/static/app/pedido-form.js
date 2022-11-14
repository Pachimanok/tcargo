const formulario = {

	iniciar : function() {
		$("#custodia").iCheck({
			checkboxClass: 'icheckbox_square-red',
	        radioClass: 'iradio_square-red',
		});
		
		$("#seguro").iCheck({
			checkboxClass: 'icheckbox_square-red',
	        radioClass: 'iradio_square-red',
		});
		
		$("#indivisible").iCheck({
			checkboxClass: 'icheckbox_square-red',
	        radioClass: 'iradio_square-red',
		});
		
		$("#valor").iCheck({
			checkboxClass: 'icheckbox_square-red',
	        radioClass: 'iradio_square-red',
		});
		
		$("#recibirOfertas").iCheck({
			checkboxClass: 'icheckbox_square-red',
	        radioClass: 'iradio_square-red',
		});
		
		
		
		
	},

	guardar : function() {

		var productor = $("#productorId").val();
		var tienda = $("#tiendaId").val();
		var usuarioId = $("#usuarioId").val();
		var perfil = {
			detalle : $("#productorDetalle").val(),
			perfil : {
				titulo : $("#productorTitulo").val(),
				cuerpo : $("#productorCuerpo").val()
			},
			usuario : {
				nombre : $("#usuarioNom").val(),
				apellido : $("#usuarioApe").val(),
				mail : $("#usuarioMail").val(),
				tipoIdentificador : $("#usuarioTipoI").val()
			},
			paginaWeb : $("#productorPaginaWeb").val(),
			facebook : $("#productorFacebook").val(),
			instagram : $("#productorInstagram").val(),
			twitter : $("#productorTwitter").val()
		};

		$.ajax({
			url : '/api/productor/' + productor + '',
			beforeSend : function(request) {
				request.setRequestHeader("tiendaId", tienda);
				request.setRequestHeader("usuarioId", usuarioId);
			},
			dataType : 'json',
			contentType : 'application/json',
			data : JSON.stringify(perfil),
			processData : false,
			type : 'POST',
			success : function(data) {
				if (data.error != null) {
					console.log(data.error);
				} else {
					fueModificado = false;
					$("#mensajePerfil").show(500);
				}
			}
		});
	}

}