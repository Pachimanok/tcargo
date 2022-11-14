package com.tcargo.web.enumeraciones;

import java.util.Arrays;
import java.util.List;

public enum Rol {

    ADMINISTRADOR("ADMINISTRADOR"), DADOR_CARGA("DADOR DE CARGA"), TRANSPORTADOR("TRANSPORTADORA"), AMBAS("AMBAS"), ADMIN_DADOR("ADMIN DADOR"), ADMIN_TRANSPORTADOR("ADMIN TRANSPORTADOR"), ADMIN_AMBAS("ADMIN AMBAS"), ADMINISTRADOR_LOCAL("ADMINISTRADOR LOCAL"), CHOFER("CHOFER"), SIN_ROL("SIN ROL"), INVITADO("INVITADO");

    Rol(String texto) {
        this.texto = texto;
    }

    public static List<Rol> getRolesTransportador() {
        return Arrays.asList(ADMIN_TRANSPORTADOR, ADMIN_AMBAS, TRANSPORTADOR, AMBAS);
    }

    public static List<Rol> getRolesDador() {
        return Arrays.asList(ADMIN_DADOR, ADMIN_AMBAS, DADOR_CARGA, AMBAS);
    }

    public static List<Rol> getRolesParaRegistro() {
        return Arrays.asList(DADOR_CARGA, TRANSPORTADOR, AMBAS, CHOFER);
    }

    private final String texto;

    public String getTexto() {
        return texto;
    }

    public static Rol getRol(String texto) {
        for (Rol rol : values()) {
            if (rol.texto.equals(texto)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("No hay rol para el texto [" + texto + "]");
    }

}
