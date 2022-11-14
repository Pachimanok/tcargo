package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UsuarioModelPerfil;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.UsuarioService;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/usuario")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UsuarioRestController {

    private final JWTService jwtService;
    private final PaisService paisService;
    private final UsuarioService usuarioService;

    @GetMapping
    public Map<String, Object> userInfo(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String userId = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim("id").asString();
        UsuarioModelPerfil usuario;

        try {
            usuario = usuarioService.armarUsuarioParaEdicionPerfil(userId);
            response.setStatus(OK.value());
            res.put(CODIGO, OK);
            res.put(USUARIO_LABEL, usuario);
            if (!usuario.getRol().equals(Rol.CHOFER)) {
                res.put(ROLES_LABEL, Rol.getRolesParaRegistro());
            }
            res.put(PAISES_LABEL, paisService.listarActivosModel());
        } catch (WebException we) {
            response.setStatus(INTERNAL_SERVER_ERROR.value());
            res.put(CODIGO, INTERNAL_SERVER_ERROR);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

    @PostMapping("/editar")
    public Map<String, Object> editar(@RequestBody UsuarioModelPerfil model, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        Rol rol = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ROL_LABEL).as(Rol.class);

        try {
            if (rol != model.getRol()) {
                Usuario usuario = usuarioService.buscar(model.getId());
                if (usuario != null) {
                    usuario.setRol(rol);
                    usuarioService.cambioDeRol(usuario, rol);
                } else {
                    throw new WebException("El usuario solicitado no existe");
                }
            }
            usuarioService.editarPerfil(model);
            response.setStatus(OK.value());
            res.put(CODIGO, OK);
        } catch (WebException we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

}
