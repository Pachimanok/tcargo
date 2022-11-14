package com.tcargo.web.utiles;

import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static com.tcargo.web.utiles.Textos.*;

public final class Roles {
    
    private static final Log log = LogFactory.getLog(Roles.class);
    
    private Roles() {
    }


    public static String getRolActual(HttpSession session) throws WebException {
        Usuario user = (Usuario) session.getAttribute(USUARIO_LABEL);
        List<String> roles = getRoles(user);
        String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);

        if (rol == null) {
            session.setAttribute(ROL_ACTUAL_LABEL, defaultRol(user.getRol()));
            rol = defaultRol(user.getRol());
        }

        if (roles.contains(rol)) {
            return rol;
        } else {
            throw new WebException("Error: el rol actual de la sesión no está en la lista de roles.");
        }
    }

    private static List<String> getRoles(Usuario user) {
        List<String> roles = new ArrayList<>();

        switch (user.getRol()) {
            case ADMINISTRADOR_LOCAL:
                roles.add(ADMINISTRADORLOCAL_LABEL);
                break;
            case ADMINISTRADOR:
                roles.add(ADMINISTRADOR_LABEL);
                break;
            case DADOR_CARGA:
                roles.add(DADORDECARGA_LABEL);
                break;
            case TRANSPORTADOR:
                roles.add(TRANSPORTADOR_LABEL);
                break;
            case AMBAS:
                roles.add(DADORDECARGA_LABEL);
                roles.add(TRANSPORTADOR_LABEL);
                break;
            case INVITADO:
            	roles.add(INVITADO_LABEL);
            	break;
            case ADMIN_DADOR:
                roles.add(ADMINISTRADOR_LABEL);
                roles.add(DADORDECARGA_LABEL);
                break;
            case ADMIN_TRANSPORTADOR:
                roles.add(ADMINISTRADOR_LABEL);
                roles.add(TRANSPORTADOR_LABEL);
                break;
            case ADMIN_AMBAS:
                roles.add(ADMINISTRADOR_LABEL);
                roles.add(DADORDECARGA_LABEL);
                roles.add(TRANSPORTADOR_LABEL);
                break;
            case SIN_ROL:
            	roles.add(SIN_ROL_LABEL);
            	break;
            case CHOFER:
            	roles.add(CHOFER_LABEL);
            	break;
            default:
                break;
        }

        return roles;
    }

    /**
     * Da un valor por defecto al atributo "rolActual" de la sesión dependiendo
     * de los roles que tenga el usuario logueado.
     *
     * @param rol Rol del usuario logueado
     * @return un String con el valor del rol por defecto.
     */
    public static String defaultRol(Rol rol) {
        String defaultRol;

        switch (rol) {
            case ADMINISTRADOR:
            case ADMIN_AMBAS:
            case ADMIN_DADOR:
            case ADMIN_TRANSPORTADOR:
                defaultRol = ADMINISTRADOR_LABEL;
                break;
            case ADMINISTRADOR_LOCAL:
                defaultRol = ADMINISTRADORLOCAL_LABEL;
                break;
            case AMBAS:
            case TRANSPORTADOR:
                defaultRol = TRANSPORTADOR_LABEL;
                break;
            case SIN_ROL:
                defaultRol = SIN_ROL_LABEL;
                break;
            case INVITADO: 
            	defaultRol = INVITADO_LABEL;
            	break;
            case CHOFER:
            	defaultRol = CHOFER_LABEL;
            	break;
            default:
                defaultRol = DADORDECARGA_LABEL;
                break;
        }

        return defaultRol;
    }

}
