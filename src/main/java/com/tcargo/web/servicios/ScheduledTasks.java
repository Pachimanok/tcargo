package com.tcargo.web.servicios;

import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.repositorios.CoincidenciaRepository;
import com.tcargo.web.repositorios.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ScheduledTasks {

    private static final Log log = LogFactory.getLog(ScheduledTasks.class);

    private final PedidoRepository pedidoRepository;
    private final CoincidenciaRepository coincidenciaRepository;
    private final FirebaseService firebaseService;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 7 * * *")
    public void verificarPrioridadPedido() {
        LocalDateTime hoy = java.time.LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<Pedido> pedidos = pedidoRepository.buscarProximosAVencer(EstadoPedido.DISPONIBLE.getPrioridad(), hoy.plusDays(2).format(dtf), hoy.format(dtf));

        for (Pedido pedido : pedidos) {
            pedido.setEstadoPedido(EstadoPedido.PRIORIDAD);

            HashMap<String, String> datos = new HashMap<>();
            datos.put("tipo", "pedido-vencido");
            firebaseService.notificar(pedido.getDador().getId(), "Uno de los pedidos está por vencer.", datos);
            emailService.mensaje(pedido.getDador().getMail(), pedido.getDador().getNombre(), "Uno de los pedidos está por vencer", "Tu carga ".concat(pedido.getId().toString().concat(" está proxima a vencer.")));
            pedidoRepository.save(pedido);
        }
    }

    @Scheduled(cron = "0 30 7 * * *")
    public void verificarVencimientoPedido() {
        String hoy = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        List<Pedido> pedidos = pedidoRepository.buscarVencidos(EstadoPedido.prioridadEstadosDisponible(), hoy, EstadoViaje.getEstadosParaScheduledTask());
        for (Pedido pedido : pedidos) {
            pedido.setEstadoPedido(EstadoPedido.VENCIDO);

            HashMap<String, String> datos = new HashMap<>();
            datos.put("tipo", "pedido-vencido");
            firebaseService.notificar(pedido.getDador().getId(), "Uno de los pedidos venció.", datos);
            emailService.mensaje(pedido.getDador().getMail(), pedido.getDador().getNombre(), "Uno de los pedidos venció", "Tu carga ".concat(pedido.getId().toString().concat(" ha vencido.")));
            pedidoRepository.save(pedido);
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void notificarInicioPeriodoCarga() {
        LocalDate hoy = LocalDate.now();

        List<Coincidencia> proximasAIniciar = coincidenciaRepository.buscarProximosAIniciar(hoy.plusDays(2).toString().concat("%"));
        List<Coincidencia> inicianHoy = coincidenciaRepository.buscarProximosAIniciar(hoy.toString().concat("%"));

        for (Coincidencia coincidencia : proximasAIniciar) {
            HashMap<String, String> datos = new HashMap<>();
            datos.put("tipo", "periodo-carga-inicio-hoy");
            firebaseService.notificar(coincidencia.getTransportador().getUsuario().getId(), "Hoy inicia un periodo de carga.", datos);
            emailService.mensaje(coincidencia.getTransportador().getUsuario().getMail(), coincidencia.getTransportador().getUsuario().getNombre(), "Hoy inicia un periodo de carga", "Hoy comienta tu periodo de carga de la carga ".concat(coincidencia.getPedido().getId().toString()));
        }

        for (Coincidencia coincidencia : inicianHoy) {
            HashMap<String, String> datos = new HashMap<>();
            datos.put("tipo", "periodo-carga-inicio-dos-dias");
            firebaseService.notificar(coincidencia.getTransportador().getUsuario().getId(), "En dos dias inicia un periodo de carga", datos);
            emailService.mensaje(coincidencia.getTransportador().getUsuario().getMail(), coincidencia.getTransportador().getUsuario().getNombre(), "En dos dias inicia un periodo de carga", "El " + coincidencia.getPedido().getPeriodoDeCarga().getInicio() + " comienza la carga del pedido: ".concat(coincidencia.getPedido().getId().toString()));
        }
    }

}
