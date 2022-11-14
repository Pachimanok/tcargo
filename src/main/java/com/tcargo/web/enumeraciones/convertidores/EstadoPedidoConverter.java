package com.tcargo.web.enumeraciones.convertidores;

import com.tcargo.web.enumeraciones.EstadoPedido;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPedidoConverter implements AttributeConverter<EstadoPedido, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EstadoPedido estadoPedido) {
        if (estadoPedido != null) {
            return estadoPedido.getPrioridad();
        }
        return EstadoPedido.DISPONIBLE.getPrioridad();
    }

    @Override
    public EstadoPedido convertToEntityAttribute(Integer prioridad) {
        if (prioridad != null) {
            EstadoPedido estado = EstadoPedido.buscarPorPrioridad(prioridad);
            if (estado != null) {
                return estado;
            }
        }
        return EstadoPedido.DISPONIBLE;
    }

}
