package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.servicios.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.ERROR;
import static com.tcargo.web.utiles.Textos.USUARIO_LABEL;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RegistroRestController {

    private final UsuarioService usuarioService;

    /*@PostMapping
    public Map<String, Object> registro(@RequestBody UsuarioModel model, HttpServletResponse response) {
        System.out.println("registro");
        Map<String, Object> res = new HashMap<>();

        try {
            System.out.println("linea34");
            Usuario usuario = usuarioService.registroDeUsuario(model);
            response.setStatus(CREATED.value());
            res.put(USUARIO_LABEL, usuario.getMail());
        } catch (WebException e) {
            System.out.println("error: "+e.getMessage());
            e.printStackTrace();
            res.put(ERROR, e.getMessage());
        }

        return res;
    }*/

    @PostMapping
    public Map<String, Object> registro(@RequestParam String mail,
                                        @RequestParam String clave,
                                        @RequestParam String clave1,
                                        @RequestParam String telefono,
                                        @RequestParam String rol,
                                        HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            UsuarioModel model = new UsuarioModel();
            model.setMail(mail);
            model.setClave(clave);
            model.setClave1(clave1);
            model.setTelefono(telefono);
            model.setRol(Rol.valueOf(rol));
            Usuario usuario = usuarioService.registroDeUsuario(model);
            response.setStatus(CREATED.value());
            res.put("id",usuario.getId());
            res.put("rol",usuario.getRol());
            res.put(USUARIO_LABEL, usuario.getMail());
        } catch (WebException e) {
            e.printStackTrace();
            res.put(ERROR, e.getMessage());
        }

        return res;
    }

}
