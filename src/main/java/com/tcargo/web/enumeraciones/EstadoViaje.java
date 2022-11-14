package com.tcargo.web.enumeraciones;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum EstadoViaje {

    LISTO_PARA_CARGAR(0, "Listo para Cargar"),
    CARGANDO(1, "Cargando"),
    EN_TRANSITO(2, "En tránsito"),
    LISTO_PARA_DESCARGAR(3, "Listo para Descargar"),
    DESCARGANDO(4, "Descargando"),
    FINALIZADO(5, "Finalizado");

    private final Integer number;
    private final String texto;

    EstadoViaje(Integer number, String texto) {
        this.number = number;
        this.texto = texto;
    }

    public Integer getNumber() {
        return this.number;
    }

    @Override
    public String toString() {
        return this.texto;
    }

    public String getTexto() {
        return this.texto;
    }

    public static List<EstadoViaje> getEstados() {
        return Arrays.asList(values());
    }

    public static List<String> getEstadosString() {
        return Arrays.stream(values()).map(EstadoViaje::getTexto).collect(toList());
    }

    public static List<String> getEstadosParaScheduledTask() {
        return Arrays.asList(LISTO_PARA_CARGAR.name(), CARGANDO.name(), EN_TRANSITO.name(), LISTO_PARA_DESCARGAR.name());
    }

}
