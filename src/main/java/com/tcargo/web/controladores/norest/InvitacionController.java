package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Chofer;
import com.tcargo.web.entidades.Invitacion;
import com.tcargo.web.enumeraciones.EstadoInvitacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.InvitacionModel;
import com.tcargo.web.servicios.*;
import com.tcargo.web.utiles.Textos;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@PreAuthorize("hasAnyRole('ROLE_TRANSPORTADOR')")
@RequestMapping("/invitacion")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InvitacionController {

    private final InvitacionService invitacionService;
    private final EmailService emailService;
    private final UsuarioService usuarioService;
    private final TransportadorService transportadorService;
    private final ChoferService choferService;

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id) {
        ModelAndView modelo = new ModelAndView("invitacion-form");
        InvitacionModel model = new InvitacionModel();
        if (id != null && !id.isEmpty()) {
            model = invitacionService.buscarModel(id);
        }
        modelo.addObject(Textos.INVITACION, model);
        return modelo;
    }

    @PostMapping("/guardar")
    public ModelAndView guardar(@ModelAttribute InvitacionModel model) {
        ModelAndView modelo = new ModelAndView();

        try {
            Invitacion i = invitacionService.guardar(model);
            if (i.getEstadoInvitacion() == EstadoInvitacion.ACEPTADA) {
                Chofer chofer = choferService.asignarTransportadora(i.getTransportador(), i.getChofer());
                transportadorService.addChofer(chofer);
                usuarioService.cambiarEstadoVerificado(i.getChofer().getId(), true);
                emailService.mensaje(i.getChofer().getMail(), i.getChofer().getNombre(), "Tu cuenta fue aprobada", "El transportador ".concat(i.getTransportador().getNombre().concat(" ha aceptado tu solicitud.")));
            } else if (i.getEstadoInvitacion() == EstadoInvitacion.RECHAZADA) {
                usuarioService.resetUsuarioChofer(i.getChofer().getId());
                emailService.mensaje(i.getChofer().getMail(), i.getChofer().getNombre(), "Tu cuenta fue rechazada", "El transportador ".concat(i.getTransportador().getNombre().concat(" ha rechazado tu solicitud.")));
            }
            return new ModelAndView("redirect:/");
        } catch (WebException e) {
            e.printStackTrace();
            modelo.addObject(Textos.ERROR, e.getMessage());
            return new ModelAndView("invitacion-form");
        }
    }

}
