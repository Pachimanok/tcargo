function soloNumeros(e) {
    e = (e) ? e : window.event;
    let charCode = (e.which) ? e.which : e.keyCode;
    return !(charCode > 31 && (charCode < 48 || charCode > 57));

}

function soloLetras(e) {
    let key = e.keyCode || e.which;
    let tecla = String.fromCharCode(key).toLowerCase();
    let letras = " áéíóúabcdefghijklmnñopqrstuvwxyz";
    let especiales = "8-37-39-46";
    let tecla_especial = false;
    for (let i in especiales) {
        if (key === especiales[i]) {
            tecla_especial = true;
            break;
        }
    }
    if (letras.indexOf(tecla) === -1) {
        if (!tecla_especial) {
            return false;
        }
    }
}

function notificacionContraOfertaVista(idUsuario) {
    let rol;
    if (window.location.toString().includes("dador")) {
        rol = 'DADOR_CARGA';
    } else {
        rol = 'TRANSPORTADOR';
    }
    let entidad = {
        id: idUsuario,
        rol: rol,
    }

    $.ajax({
        url: '/notificacion/notificacionContraOfertaVista',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify(entidad),
        type: 'POST',
        success: function (data) {

            if (rol === 'DADOR_CARGA') {
                window.location = '/pedido/cargas';
            } else {
                window.location = '/pedido/pedidosofertados';
            }
        },
        error: function (data) {

        }
    });
}

function notificacionOfertaAceptadaVista(idUsuario, idPedido, contraOfertaId) {
    let rol;
    if (window.location.toString().includes("dador")) {
        rol = 'DADOR_CARGA';
    } else {
        rol = 'TRANSPORTADOR';
    }
    let entidad = {
        id: contraOfertaId,
    }

    $.ajax({
        url: '/notificacion/notificacionvista',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify(entidad),
        type: 'POST',
        success: function (data) {

            if (rol === 'DADOR_CARGA') {
                window.location = '/contraoferta/dador/listado/?idPedido=' + idPedido + '&rol=' + rol;
            } else {
                window.location = '/contraoferta/listado/?idPedido=' + idPedido + '&rol=' + rol;
            }
        },
        error: function (data) {

        }
    });
}

function notificacionMatchVista(idPedido, idMatch) {
    let entidad = {
        id: idMatch,
        idPedido: idPedido,
    }

    $.ajax({
        url: '/notificacion/match/notificacionvista',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify(entidad),
        type: 'POST',
        success: function (data) {

            window.location = '/pedido/ver?id=' + idPedido;
        },
        error: function (data) {

        }
    });
}

function notificacionConformidadVista(idPedido, idMatch) {
    let entidad = {
        id: idMatch,
        idPedido: idPedido,
    }

    $.ajax({
        url: '/notificacion/conformidad/notificacionvista',
        contentType: 'application/json',
        dataType: 'json',
        data: JSON.stringify(entidad),
        type: 'POST',
        success: function (data) {


            window.location = '/pedido/ver?id=' + idPedido;

        },
        error: function (data) {

        }
    });
}


$("#clave").focusout(function () {
    let clave = $("#clave").val();
    if (validarLargoClave(clave) === false) {
        $("#alert-pass-corto").show(200);
    } else {
        $("#alert-pass-corto").hide(200);
    }
})

$("#clave1").focusout(function () {
    validarIgualdadClave();
})

function validarLargoClave(clave) {
    return clave.length >= 6;
}

function validarIgualdadClave() {
    let clave = $("#clave").val();
    let clave1 = $("#clave1").val();
    if (clave !== clave1) {
        $("#alert-pass-distintos").show(200);
    } else {
        $("#alert-pass-distintos").hide(200);
    }
}

function verClave() {
    var clave = document.getElementById('clave');
    if (clave.type === 'password') {
        clave.type = 'text'
    } else {
        clave.type = 'password'
    }
}

function verClave1() {
    var clave = document.getElementById('clave1');
    if (clave.type === 'password') {
        clave.type = 'text'
    } else {
        clave.type = 'password'
    }
}

$("#nombre").change(function () {
    let nombre = $("#nombre").val();
    if (nombre != null && nombre.length > 0) {
        $("#danger-nombre").hide(200);
    } else {
        $("#danger-nombre").show(200);
    }
});

$("#mail").change(function () {
    let mail = $("#mail").val();
    if (mail != null && mail.length > 0) {
        $("#danger-mail").hide(200);
    } else {
        $("#danger-mail").show(200);
    }
});

$("#clave").change(function () {
    let clave = $("#clave").val();
    if (clave != null && clave.length > 0) {
        $("#danger-clave").hide(200);
    } else {
        $("#danger-clave").show(200);
    }
});

$("#clave1").change(function () {
    let clave1 = $("#clave1").val();
    if (clave1 != null && clave1.length > 0) {
        $("#danger-clave1").hide(200);
    } else {
        $("#danger-clave1").show(200);
    }
});

$("#cuit").change(function () {
    let cuit = $("#cuit").val();
    if (cuit != null && cuit.length > 0) {
        $("#danger-cuit").hide(200);
    } else {
        $("#danger-cuit").show(200);
    }
});

$("#telefono").change(function () {
    let telefono = $("#telefono").val();
    if (telefono != null && telefono.length > 0) {
        $("#danger-telefono").hide(200);
    } else {
        $("#danger-telefono").show(200);
    }
});