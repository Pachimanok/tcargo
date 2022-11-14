package com.tcargo.web;

import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoInvitacion;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import com.tcargo.web.enumeraciones.EstadoValoracion;
import com.tcargo.web.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.tcargo.web.utiles.Textos.*;

public class InterceptorNotificaciones implements HandlerInterceptor {

    @Autowired
    private TransportadorService transportadorService;

    @Autowired
    private InvitacionService invitacionService;

    @Autowired
    private ContraOfertaService contraOfertaService;

    @Autowired
    private CoincidenciaService coincidenciaService;

    @Autowired
    private ValoracionService valoracionService;

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelo) throws Exception {
        if (modelo == null) return;
        HttpSession session = request.getSession();
        if (session != null && session.getAttribute(USUARIO_LABEL) != null) {
            Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
            String rolActual = (String) session.getAttribute(ROL_ACTUAL_LABEL);

            if (rolActual.equals(TRANSPORTADOR_LABEL)) {
                Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());
                modelo.addObject("contraOfertasEstadoAceptado", contraOfertaService.listarPorTransportadorNotifCreadaEstadoAceptado(t));
                modelo.addObject("contraOfertaNotificionCreada", contraOfertaService.listarPorTransportadorNotifCreada(t));
                modelo.addObject(INVITACIONES, invitacionService.buscarEstadoInvTransportadorAndEstadoNotif(EstadoInvitacion.ENVIADA, t.getId(), EstadoNotificacion.CREADO));
                modelo.addObject("valoracionesParaCompletar", valoracionService.buscarPropiasParaCompletar(EstadoValoracion.CREADA, usuario.getId()));
                modelo.addObject("valoracionesParaRevisar", valoracionService.buscarPropiasComoReceptor(EstadoValoracion.FINALIZADA_CREADOR, usuario.getId()));
                return;
            }

            if (rolActual.equals(DADORDECARGA_LABEL)) {
                modelo.addObject("contraOfertasEstadoAceptado", contraOfertaService.listarPorDadorNotifCreadaEstadoAceptado(usuario));
                modelo.addObject("contraOfertaNotificionCreada", contraOfertaService.listarPorDadorNotifCreada(usuario));
                modelo.addObject("coincidenciasNotifCreado", coincidenciaService.buscarPorDadorNotificacionCreada(usuario, EstadoNotificacion.CREADO));
                modelo.addObject("valoracionesParaCompletar", valoracionService.buscarPropiasParaCompletar(EstadoValoracion.CREADA, usuario.getId()));
                modelo.addObject("valoracionesParaRevisar", valoracionService.buscarPropiasComoReceptor(EstadoValoracion.FINALIZADA_CREADOR, usuario.getId()));
                return;
            }
        }
    }


}
