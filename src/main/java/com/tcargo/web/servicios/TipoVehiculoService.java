package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoVehiculoConverter;
import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.TipoVehiculo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoVehiculoModel;
import com.tcargo.web.repositorios.TipoVehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class TipoVehiculoService {

    private final TipoVehiculoConverter tipoVehiculoConverter;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final MessageSource messages;

    @Autowired
    public TipoVehiculoService(TipoVehiculoConverter tipoVehiculoConverter, TipoVehiculoRepository tipoVehiculoRepository, MessageSource messages ) {
        this.tipoVehiculoConverter = tipoVehiculoConverter;
        this.tipoVehiculoRepository = tipoVehiculoRepository;
        this.messages = messages;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoVehiculo guardar(TipoVehiculoModel model) throws WebException {
        TipoVehiculo tipoVehiculo = tipoVehiculoConverter.modeloToEntidad(model);
        final Locale locale = LocaleContextHolder.getLocale();

        if (tipoVehiculo.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.vehiculo.back.error.dado.baja", null, locale));
        }

        if (tipoVehiculo.getNombre() == null || tipoVehiculo.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.vehiculo.back.error.nombre", null, locale));
        }

        List<TipoVehiculo> otros = tipoVehiculoRepository.buscarTipoVehiculoPorNombre(tipoVehiculo.getNombre());
        for (TipoVehiculo otro : otros) {
            if (otro != null && !otro.getId().equals(tipoVehiculo.getId())) {
                throw new WebException(messages.getMessage("tipo.vehiculo.back.error.nombre.dupicado", null, locale));
            }
        }

        tipoVehiculo.setModificacion(new Date());

        return tipoVehiculoRepository.save(tipoVehiculo);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoVehiculo eliminar(String id) throws WebException {
        TipoVehiculo tipoVehiculo = tipoVehiculoRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (tipoVehiculo.getEliminado() == null) {
            tipoVehiculo.setEliminado(new Date());
            tipoVehiculo = tipoVehiculoRepository.save(tipoVehiculo);
        } else {
            throw new WebException(messages.getMessage("tipo.vehiculo.back.error.dado.baja", null, locale));
        }

        return tipoVehiculo;
    }

    public Page<TipoVehiculo> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoVehiculo> tipoVehiculos = tipoVehiculoRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tipoVehiculos, paginable, tipoVehiculos.size());
        }
        return tipoVehiculoRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoVehiculo> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoVehiculo> tipoVehiculos = tipoVehiculoRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tipoVehiculos, paginable, tipoVehiculos.size());
        }
        return tipoVehiculoRepository.buscarActivos(paginable);
    }

    public List<TipoVehiculo> listarActivos() {
        return tipoVehiculoRepository.buscarActivos();
    }
    
    public List<TipoVehiculoModel> listarActivosModel() {
        return tipoVehiculoConverter.entidadesToModelos(tipoVehiculoRepository.buscarActivos());
    }

    public TipoVehiculoModel buscar(String id) {
        TipoVehiculo tipoVehiculo = tipoVehiculoRepository.getOne(id);
        return tipoVehiculoConverter.entidadToModelo(tipoVehiculo);
    }

}