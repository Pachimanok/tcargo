
const initialPos = {
    lat: -33.6091754,
    lng: -63.4685812
};

let map, searchBox, input, markers, geocoder, infowindow;

const mapas = {

	iniciar : function() {
		$('#botonRetiro').click(function() {
			$('#pac-input').val('');
			$('#asignarRetiro').show();
			$('#asignarEntrega').hide()
			$('#asignarInicio').hide();
			$('#asignarFinal').hide();
		});

		$('#botonOrigen').click(function() {
			$('#pac-input').val('');
			$('#asignarRetiro').hide();
			$('#asignarEntrega').hide()
			$('#asignarInicio').show();
			$('#asignarFinal').hide();
		});

		$('#botonDestino').click(function() {
			$('#pac-input').val('');
			$('#asignarRetiro').hide();
			$('#asignarEntrega').hide()
			$('#asignarInicio').hide();
			$('#asignarFinal').show();
		});

		$('#botonEntrega').click(function() {
			$('#pac-input').val('');
			$('#asignarEntrega').show()
			$('#asignarRetiro').hide();
			$('#asignarInicio').hide();
			$('#asignarFinal').hide();
		});
	},

	abrir : function() {

	},

	distacia : function() {
		const latitud_inicio = $('#inputlatitudDesde').val();
		const longitud_inicio = $('#inputlongitudDesde').val();

		const latitud_fin = $('#inputlatitudHasta').val();
		const longitud_fin = $('#inputlongitudHasta').val();

		if (latitud_inicio != "" && longitud_inicio != "" && latitud_fin != ""
				&& longitud_fin != "") {
			let servicio = new google.maps.DirectionsService();

			let inicio = new google.maps.LatLng(latitud_inicio, longitud_inicio);
			let fin = new google.maps.LatLng(latitud_fin, longitud_fin);

			let request = {
				origin : inicio,
				destination : fin,
				travelMode : google.maps.TravelMode.DRIVING
			};

			servicio.route(request, function(resultado, estado) {
				if (estado == google.maps.DirectionsStatus.OK) {
					let kms = resultado.routes[0].legs[0].distance.text
							.split(" ")[0];
					kms = kms.replace(/,/g, "");
					$("#kilometrosTotalesObjeto").val(kms);
				}
			});
		} else {
			$("#error").text("Ambas ubicaciones deben estar completas.")
			$("#errorDiv").show(350);
			$('html,body').animate({
				scrollTop : $(errorDiv).offset().top
			}, 'slow')
		}
	},

	autocompletar : function() {

		map = new google.maps.Map(document.getElementById('map'), {
			center : initialPos,
			zoom : 6.22,
			mapTypeId : 'roadmap',
			zoomControl : false,
			mapTypeControl : false
		});

		geocoder = new google.maps.Geocoder;
		infowindow = new google.maps.InfoWindow;

		markers = [];

		input = document.getElementById('pac-input');
		searchBox = new google.maps.places.SearchBox(input);
		map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

		map.addListener('bounds_changed', function() {
			searchBox.setBounds(map.getBounds());
		});

		searchBox.addListener('places_changed', function() {
			let places = searchBox.getPlaces();
			if (places.length === 0) {
				return;
			}

			markers.forEach(function(marker) {
				marker.setMap(null);
			});
			markers = [];

			let bounds = new google.maps.LatLngBounds();
			places.forEach(function(place) {
				if (!place.geometry) {
					console.log("Returned place contains no geometry");
					return;
				}
	
				markers.push(new google.maps.Marker({
					map : map,
					title : place.name,
					position : place.geometry.location
				}));
	
				if (place.geometry.viewport) {
					bounds.union(place.geometry.viewport);
				} else {
					bounds.extend(place);
				}
			});

			map.fitBounds(bounds);

			let lat = map.getCenter().lat();
			let lon = map.getCenter().lng();

			$("#asignarInicio").click(function() {
				
				$('#inputlatitudDesde').val(lat);
				$('#inputlongitudDesde').val(lon);
								
				let cadenas = $("#pac-input").val();
				let direccion = cadenas;
				
				$('#inputDireccionDesde').val(direccion);
				
				if ($('#inputDireccionDesde').val() === $('#inputDireccionHasta').val()) {
					Swal.queue([{
						title : 'Las direcciones nos pueden ser iguales!',
						confirmButtonText : 'OK',
						icon : "info"
					}]);
					
					$('#inputDireccionDesde').val("");
					$("#pac-input").val("");
				}
		
				$("#direccionInicial").hide();
			});

			$("#asignarFinal").click(function() {
				
				$('#inputlatitudHasta').val(lat);
				$('#inputlongitudHasta').val(lon);
												
				let cadenas = $("#pac-input").val();
												
				let direccion = cadenas;
												
				$('#inputDireccionHasta').val(direccion);
				
				if ($('#inputDireccionDesde').val() === $('#inputDireccionHasta').val()) {
					
					Swal.queue([{
						title : 'Las direcciones nos pueden ser iguales!',
						confirmButtonText : 'OK',
						icon : "info"
					}]);
		
					$('#inputDireccionHasta').val("");
				
					$("#pac-input").val("");
				}

				$("#direccionInicial").hide();

			});

			$("#asignarRetiro").click(function() {
				
				$('#inputlatitudRetiro').val(lat);
				$('#inputlongitudRetiro').val(lon);
				
				let cadenas = $("#pac-input").val();
												
				let direccion = cadenas;
											
				$('#inputDireccionRetiro').val(direccion);
												
				if ($('#inputDireccionRetiro').val() === $('#inputDireccionEntrega').val()) {
					Swal.queue([ {
						title : 'Las direcciones nos pueden ser iguales!',
						confirmButtonText : 'OK',
						icon : "info"
					}]);
					
					$('#inputDireccionRetiro').val("");
					$("#pac-input").val("");
												
				}
											
			});

							
			$("#asignarEntrega").click(function() {
												
				$('#inputlatitudEntrega').val(lat);
												
				$('#inputlongitudEntrega').val(lon);
												
				let cadenas = $("#pac-input").val();
												
				let direccion = cadenas;
												
				$('#inputDireccionEntrega').val(direccion);
												
				if ($('#inputDireccionRetiro').val() === $('#inputDireccionEntrega').val()) {
					Swal.queue([ {
						title : 'Las direcciones nos pueden ser iguales!',
						confirmButtonText : 'OK',
						icon : "info"
					}]);
													
					$('#inputDireccionEntrega').val("");
													
					$("#pac-input").val("");
												
				}
											
			});

						
		});

	},

}