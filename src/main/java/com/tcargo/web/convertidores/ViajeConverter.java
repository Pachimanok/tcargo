package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Viaje;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.modelos.CargaNegativaModel;
import com.tcargo.web.modelos.ViajeModel;
import com.tcargo.web.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ViajeConverter extends Convertidor<ViajeModel, Viaje> {

    private final CargaRepository cargaRepository;
    private final ChoferRepository choferRepository;
    private final RemolqueRepository remolqueRepository;
    private final TransportadorRepository transportadorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final ViajeRepository viajeRepository;
    private final VehiculoRepository vehiculoRepository;

    public Viaje modeloToEntidad(ViajeModel model) {
        Viaje viaje = new Viaje();

        if (model.getId() != null && !model.getId().isEmpty()) {
            viaje = viajeRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, viaje);
        } catch (Exception e) {
            log.error("Error al convertir el modelo viaje a entidad", e);
        }

        if (viaje.getEstadoViaje() == null) {
            viaje.setEstadoViaje(EstadoViaje.LISTO_PARA_CARGAR);
        }

        if (model.getIdCarga() != null) {
            viaje.setCarga(cargaRepository.getOne(model.getIdCarga()));
        }

        if (model.getIdChofer() != null) {
            viaje.setChofer(choferRepository.getOne(model.getIdChofer()));
        }

        if (model.getIdVehiculo() != null) {
            viaje.setVehiculo(vehiculoRepository.getOne(model.getIdVehiculo()));
        }

        if (model.getIdRemolque() != null) {
            viaje.setRemolque(remolqueRepository.getOne(model.getIdRemolque()));
        }

        if (model.getIdUbicacionInicial() != null) {
            viaje.setUbicacionInicial(ubicacionRepository.getOne(model.getIdUbicacionInicial()));
        }

        if (model.getIdUbicacionFinal() != null) {
            viaje.setUbicacionFinal(ubicacionRepository.getOne(model.getIdUbicacionFinal()));
        }

        return viaje;
    }

    public Viaje modeloToEntidad(CargaNegativaModel model) {
        Viaje viaje = new Viaje();

        if (model.getId() != null && !model.getId().isEmpty()) {
            viaje = viajeRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, viaje);
        } catch (Exception e) {
            log.error("Error al convertir el modelo viaje a entidad", e);
        }

        if (viaje.getEstadoViaje() == null) {
            viaje.setEstadoViaje(EstadoViaje.LISTO_PARA_CARGAR);
        }

        if (model.getIdCarga() != null) {
            viaje.setCarga(cargaRepository.getOne(model.getIdCarga()));
        }

        if (model.getIdChofer() != null) {
            viaje.setChofer(choferRepository.getOne(model.getIdChofer()));
        }

        if (model.getIdVehiculo() != null) {
            viaje.setVehiculo(vehiculoRepository.getOne(model.getIdVehiculo()));
        }

        if (model.getIdRemolque() != null) {
            viaje.setRemolque(remolqueRepository.getOne(model.getIdRemolque()));
        }

        if (model.getUbicacionInicial() != null && model.getUbicacionInicial().getId() != null) {
            viaje.setUbicacionInicial(ubicacionRepository.getOne(model.getUbicacionInicial().getId()));
        }

        if (model.getUbicacionFinal() != null && model.getUbicacionFinal().getId() != null) {
            viaje.setUbicacionFinal(ubicacionRepository.getOne(model.getUbicacionFinal().getId()));
        }

        if (model.getIdTransportador() != null) {
            viaje.setTransportador(transportadorRepository.getOne(model.getIdTransportador()));
        }

        return viaje;
    }

    public ViajeModel entidadToModelo(Viaje viaje) {
        ViajeModel model = new ViajeModel();

        try {
            BeanUtils.copyProperties(viaje, model);
            if (viaje.getCarga() != null) {
                model.setIdCarga(viaje.getCarga().getId());
            }
            if (viaje.getChofer() != null) {
                model.setIdChofer(viaje.getChofer().getId());
            }
            if (viaje.getVehiculo() != null) {
                model.setIdVehiculo(viaje.getVehiculo().getId());
                model.setMarcaVehiculo(viaje.getVehiculo().getModelo().getMarca().getNombre());
            }
            if (viaje.getRemolque() != null) {
                model.setIdRemolque(viaje.getRemolque().getId());
                model.setDescripcionRemolque(viaje.getRemolque().getTipoRemolque().getCaracteristicas());
            }
            if (viaje.getUbicacionInicial() != null) {
                model.setIdUbicacionInicial(viaje.getUbicacionInicial().getId());
                model.setDesde(viaje.getUbicacionInicial().getDireccion());
            }
            if (viaje.getUbicacionFinal() != null) {
                model.setIdUbicacionFinal(viaje.getUbicacionFinal().getId());
                model.setHasta(viaje.getUbicacionFinal().getDireccion());
            }
            if (viaje.getPartidaCargaNegativa() != null) {
                model.setPartida(viaje.getPartidaCargaNegativa().toString());
            }
            if (viaje.getLlegadaCargaNegativa() != null) {
                model.setPartida(viaje.getLlegadaCargaNegativa().toString());
            }
            model.setEstadoString(viaje.getEstadoViaje().getTexto());
            model.setModificacion(viaje.getModificacion().toString());
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de viaje", e);
        }

        return model;
    }

    public List<ViajeModel> entidadesToModelos(List<Viaje> viajes) {
        List<ViajeModel> models = new ArrayList<>();

        for (Viaje v : viajes) {
            models.add(entidadToModelo(v));
        }

        return models;
    }

}
