package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.VehiculoConverter;
import com.tcargo.web.entidades.Vehiculo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.VehiculoModel;
import com.tcargo.web.modelos.busqueda.BusquedaVehiculoModel;
import com.tcargo.web.repositorios.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class VehiculoService {

    private final MessageSource messages;
    private final VehiculoConverter vehiculoConverter;
    private final VehiculoRepository vehiculoRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Vehiculo guardar(VehiculoModel model) throws WebException {
        Vehiculo vehiculo = vehiculoConverter.modeloToEntidad(model);
        Vehiculo v = vehiculoRepository.findByDominio(model.getDominio());
        final Locale locale = LocaleContextHolder.getLocale();

        if (vehiculo.getEliminado() != null) {
            throw new WebException(messages.getMessage("vehiculo.back.error.dado.de.baja", null, locale));
        }

        if (vehiculo.getModelo().getMarca().getNombre() == null || vehiculo.getModelo().getMarca().getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("vehiculo.back.error.dominio.vacio", null, locale));
        }

        if (vehiculo.getDominio() == null || vehiculo.getDominio().isEmpty()) {
            throw new WebException(messages.getMessage("vehiculo.back.error.dominio.vacio", null, locale));
        }

        if (v != null && !vehiculo.getId().equals(v.getId())) {
            throw new WebException(messages.getMessage("vehiculo.back.error.dominio.repetido", null, locale));
        }

        if (vehiculo.getTransportador() == null) {
            throw new WebException(messages.getMessage("vehiculo.back.error.vehiculo.asociado", null, locale));
        }

        if (vehiculo.getModelo() == null) {
            throw new WebException(messages.getMessage("vehiculo.back.error.vehiculo.modelo", null, locale));
        }

        if (vehiculo.getTipoVehiculo() == null) {
            throw new WebException(messages.getMessage("vehiculo.back.error.tipo.vehiculo", null, locale));
        }

        if (vehiculo.getAnioFabricacion() == null || vehiculo.getAnioFabricacion().equals("")) {
            throw new WebException(messages.getMessage("vehiculo.back.error.vehiculo.agno.fabricacion", null, locale));
        }


        vehiculo.setModificacion(new Date());

        return vehiculoRepository.save(vehiculo);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Vehiculo eliminar(String id) throws WebException {
        Vehiculo vehiculo = vehiculoRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (vehiculo.getEliminado() == null) {
            vehiculo.setEliminado(new Date());
            vehiculo = vehiculoRepository.save(vehiculo);
        } else {
            throw new WebException(messages.getMessage("vehiculo.back.error.dado.de.baja", null, locale));
        }

        return vehiculo;
    }

    public Page<Vehiculo> listarActivos(Pageable paginable, String q) {
        return vehiculoRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Vehiculo> listarActivos(Pageable paginable) {
        return vehiculoRepository.buscarActivos(paginable);
    }

    public Page<Vehiculo> listarActivosPorTransportador(String id, Pageable paginable) {
        return vehiculoRepository.buscarActivosPorTransportador(id, paginable);
    }

    public List<Vehiculo> listarActivosPorTransportadorList(String id) {
        return vehiculoRepository.buscarActivosPorTransportadorList(id);
    }

    public List<VehiculoModel> listarActivosPorTransportadorModel(String id) {
        return vehiculoConverter.entidadesToModelos(vehiculoRepository.buscarActivosPorTransportadorList(id));
    }

    public List<Vehiculo> listarActivos() {

        return vehiculoRepository.buscarActivos();
    }

    public Vehiculo buscarPorId(String id) {

        return vehiculoRepository.getOne(id);
    }

    public VehiculoModel buscar(String id) {
        Vehiculo vehiculo = vehiculoRepository.getOne(id);
        return vehiculoConverter.entidadToModelo(vehiculo);
    }

    public Page<Vehiculo> buscarPorCriterios(Pageable page, BusquedaVehiculoModel busqueda, String idTransportador) {
        return vehiculoRepository.buscarPorCriterios(page, busqueda, idTransportador);
    }

    public List<VehiculoModel> listarActivosPorTransportadorModelList(String id) {
        List<Vehiculo> vehiculos = listarActivosPorTransportadorList(id);
        return vehiculoConverter.entidadesToModelos(vehiculos);
    }

    public boolean esVehiculoDeOtroTransportador(String idVehiculo, String idTransportador) {
        return !buscar(idVehiculo).getIdTransportador().equals(idTransportador);
    }

}