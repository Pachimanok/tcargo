package com.tcargo.web.enumeraciones;

import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;

public enum EstadoPedido {

    PRIORIDAD(1, "PRIORIDAD"),
    DISPONIBLE(2, "DISPONIBLE"),
    ASIGNADO(3, "ASIGNADO"),
    VENCIDO(4, "VENCIDO"),
    ENTREGADO(5, "ENTREGADO"),
    CANCELADO(6, "CANCELADO");

    private final Integer prioridad;
    private final String texto;

    EstadoPedido(Integer prioridad, String texto) {
        this.prioridad = prioridad;
        this.texto = texto;
    }

    public Integer getPrioridad() {
        return this.prioridad;
    }

    @Override
    public String toString() {
        return this.texto;
    }

    public static List<Integer> prioridadEstadosDisponible() {
        return Arrays.asList(DISPONIBLE.prioridad, PRIORIDAD.prioridad);
    }

    public static List<EstadoPedido> estadosDisponible() {
        return Arrays.asList(DISPONIBLE, PRIORIDAD);
    }

    public static EstadoPedido buscarPorPrioridad(@NonNull Integer prioridad) {
        for (EstadoPedido estado : values()) {
            if (estado.prioridad.equals(prioridad)) {
                return estado;
            }
        }

        return null;
    }

}
