importScripts('https://www.gstatic.com/firebasejs/7.21.1/firebase-app.js')
importScripts('https://www.gstatic.com/firebasejs/7.21.1/firebase-messaging.js')

var firebaseConfig = {
    apiKey: "AIzaSyA2UZbkXox8F0IYZPQvDFGTCy6ZopqhRvQ",
    authDomain: "dynamic-digit-206320.firebaseapp.com",
    databaseURL: "https://dynamic-digit-206320.firebaseio.com",
    projectId: "dynamic-digit-206320",
    storageBucket: "dynamic-digit-206320.appspot.com",
    messagingSenderId: "23106851544",
    appId: "1:23106851544:web:832aaf8e359ce69b7fbdad",
    measurementId: "G-55G8EVXKRW"
};

firebase.initializeApp(firebaseConfig);
// firebase.analytics();

var messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function (payload) {
    var title = "";
    var options = {};

    switch (payload.data.tipo) {
        case "listo-para-cargar":
            title = 'Se generó el match. Origen: ' + payload.data.origen
                + ' Destino: ' + payload.data.destino;
            options = {
                body: payload.data.origen + payload.data.destino
            }
            console.log(payload.data.obra + " ");
            break;
        case "pedido-proximo-vencimiento":
        	title = 'Tiene pedidos próximos a vencer';
        	options = {
        	    body: payload.data.status
            }
            break;
        case "pedido-vencido":
            title = 'Tiene pedidos vencidos';
            options = {
                body: payload.data.status
            }
            console.log(payload.data.obra + " ");
            break;
        case "periodo-carga-inicio-hoy":
            title = 'Tiene periodos de carga que inician hoy.';
            options = {
                body: payload.data.status
            }
            console.log(payload.data.obra + " ");
            break;
        case "periodo-carga-inicio-dos-dias":
            title = 'Tiene periodos de carga que inician en dos días';
            options = {
                body: payload.data.status
            }
            console.log(payload.data.obra + " ");
            break;
        case "nueva-contra-oferta":
            title = 'Tienes una nueva oferta';
            option = {
                body: payload.data.status
            }
            console.log(payload.data.obra + " ");
            break;
        case "nuevo-estado-viaje":
            title = 'Estado de viaje actualizado';
            option = {
                body: payload.data.status
            }
            console.log(payload.data.obra + " ");
            break;
        case "pedido-editado":
            title = 'Pedido editado';
            option = {
                body: payload.data.status
            }
            console.log(payload.data.obra + " ");
            break;
    }

    return self.registration.showNotification(title, options);
});