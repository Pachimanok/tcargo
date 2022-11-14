package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.TipoRequisitoOferta;
import com.tcargo.web.enumeraciones.EstadoContraOferta;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.repositorios.*;
import com.tcargo.web.servicios.TipoRequisitoOfertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContraOfertaConverter extends Convertidor<ContraOfertaModel, ContraOferta> {

    private final ContraOfertaRepository contraOfertaRepository;
    private final MonedaRepository monedaRepository;
    private final PedidoRepository pedidoRepository;
    private final RemolqueRepository remolqueRepository;
    private final TipoRequisitoOfertaService tipoRequisitoOfertaService;
    private final TransportadorRepository transportadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;

    public ContraOfertaModel entidadToModelo(ContraOferta contraOferta) {
        ContraOfertaModel model = new ContraOfertaModel();
        try {
            BeanUtils.copyProperties(contraOferta, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de provincia", e);
        }

        if (contraOferta.getDador() != null) {
            model.setIdDador(contraOferta.getDador().getId());
        }
        if (contraOferta.getTransportador() != null) {
            model.setIdTransportador(contraOferta.getTransportador().getId());
        }
        if (contraOferta.getCreador() != null) {
            model.setIdCreador(contraOferta.getCreador().getId());
        }
        if (contraOferta.getPedido() != null) {
            model.setIdPedido(contraOferta.getPedido().getId().toString());
        }
        if (contraOferta.getVehiculo() != null) {
            model.setIdVehiculo(contraOferta.getVehiculo().getId());
        }
        if (contraOferta.getRemolque() != null) {
            model.setIdRemolque(contraOferta.getRemolque().getId());
        }
        if (contraOferta.getMoneda() != null) {
            model.setIdMoneda(contraOferta.getMoneda().getId());
            model.setSimboloMoneda(contraOferta.getMoneda().getSimbolo());
        }

        if (contraOferta.getRequisitosDeOferta() != null) {
            for (TipoRequisitoOferta requisito : contraOferta.getRequisitosDeOferta()) {
                model.getIdRequisitosContraOferta().add(requisito.getId());
            }
        }

        return model;
    }

    public ContraOferta modeloToEntidad(ContraOfertaModel model) {
        ContraOferta contraOferta = new ContraOferta();
        model.setEstado(EstadoContraOferta.PENDIENTE);
        if (model.getId() != null && !model.getId().isEmpty()) {
            contraOferta = contraOfertaRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, contraOferta);
        } catch (Exception e) {
            log.error("Error al convertir el modelo la contraOferta en entidad", e);
        }

        if (model.getIdDador() != null) {
            contraOferta.setDador(usuarioRepository.getOne(model.getIdDador()));
        }
        if (model.getIdTransportador() != null) {
            contraOferta.setTransportador(transportadorRepository.getOne(model.getIdTransportador()));
        }
        if (model.getIdCreador() != null) {
            contraOferta.setCreador(usuarioRepository.buscarPorId(model.getIdCreador()));
        }
        if (model.getIdPedido() != null) {
            contraOferta.setPedido(pedidoRepository.getOne(Long.valueOf(model.getIdPedido())));
        }
        if (model.getIdVehiculo() != null) {
            contraOferta.setVehiculo(vehiculoRepository.getOne(model.getIdVehiculo()));
        }
        if (model.getIdRemolque() != null) {
            contraOferta.setRemolque(remolqueRepository.getOne(model.getIdRemolque()));
        }
        if (model.getIdMoneda() != null) {
            contraOferta.setMoneda(monedaRepository.getOne(model.getIdMoneda()));
        }

        if (model.getIdRequisitosContraOferta() != null) {
            for (String requisito : model.getIdRequisitosContraOferta()) {
                TipoRequisitoOferta tro = tipoRequisitoOfertaService.buscarEntidad(requisito);
                contraOferta.getRequisitosDeOferta().add(tro);
            }
        }

        return contraOferta;
    }

    public List<ContraOfertaModel> entidadesToModelos(List<ContraOferta> contraOfertaes) {
        List<ContraOfertaModel> model = new ArrayList<>();
        for (ContraOferta contraOferta : contraOfertaes) {
            model.add(entidadToModelo(contraOferta));
        }
        return model;
    }

} 
