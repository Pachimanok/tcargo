package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.UbicacionConverter;
import com.tcargo.web.entidades.Ubicacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.repositorios.UbicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class UbicacionService {

    private final UbicacionConverter ubicacionConverter;
    private final UbicacionRepository ubicacionRepository;
    private final MessageSource messages;

    @Autowired
    public UbicacionService(UbicacionConverter ubicacionConverter, UbicacionRepository ubicacionRepository, MessageSource mesages) {
        this.ubicacionConverter = ubicacionConverter;
        this.ubicacionRepository = ubicacionRepository;
        this.messages = mesages;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Ubicacion guardar(UbicacionModel model) throws WebException {
        Ubicacion ubicacion = ubicacionConverter.modeloToEntidad(model);

        return controlUbicacion(ubicacion);
    }

    private Ubicacion controlUbicacion(Ubicacion ubicacion) throws WebException {
    	
        final Locale locale = LocaleContextHolder.getLocale();
    	
        if (ubicacion.getEliminado() != null) {
            throw new WebException(messages.getMessage("ubicacion.back.error.dada.baja", null, locale));
        }
        if (ubicacion.getDireccion() == null || ubicacion.getDireccion().isEmpty() || ubicacion.getDireccion().equals("")) {
            throw new WebException(messages.getMessage("ubicacion.back.error.direccion", null, locale));
        }
        if (ubicacion.getLatitud() == null) {
            throw new WebException(messages.getMessage("ubicacion.back.error.latitud", null, locale));
        }
        if (ubicacion.getLongitud() == null) {
            throw new WebException(messages.getMessage("ubicacion.back.error.longitud", null, locale));
        }

        ubicacion.setModificacion(new Date());
        return ubicacionRepository.save(ubicacion);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Ubicacion eliminar(String id) throws WebException {
        Ubicacion ubicacion = ubicacionRepository.getOne(id);
        Locale locale = LocaleContextHolder.getLocale();
        if (ubicacion.getEliminado() == null) {
            ubicacion.setEliminado(new Date());
            ubicacion = ubicacionRepository.save(ubicacion);
        } else {
            throw new WebException(messages.getMessage("ubicacion.back.error.dada.baja", null, locale));
        }

        return ubicacion;
    }

    public Page<Ubicacion> listarActivos(Pageable paginable, String q) {
        return ubicacionRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Ubicacion> listarActivos(Pageable paginable) {
        return ubicacionRepository.buscarActivos(paginable);
    }

    public List<Ubicacion> listarActivos() {
        return ubicacionRepository.buscarActivos();
    }

    public Page<Ubicacion> listarActivosPorPais(Pageable paginable, String idPais) {
        return ubicacionRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<Ubicacion> listarActivosPorPais(Pageable paginable, String q, String idPais) {
        return ubicacionRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public UbicacionModel buscar(String id) {
        Ubicacion ubicacion = ubicacionRepository.getOne(id);
        return ubicacionConverter.entidadToModelo(ubicacion);
    }

    public Ubicacion findById(String id) {
        return ubicacionRepository.findById(id).orElse(null);
    }

    /**
     * Busca ubicaciones cercanas a <b>ubicacion</b> dentro de un radio de <b>radio</b> kms.
     *
     * @param ubicacion Ubicación de la cual se quieren buscar ubicaciones cercanas. No puede ser nula.
     * @param radio     Radio de búsqueda. Puede ser nulo.
     * @return Lista de ubicaciones cercanas a la ubicación pasada por parámetro
     */
    public List<Ubicacion> buscarCercanos(@NotNull Ubicacion ubicacion, Double radio) {
        return ubicacionRepository.buscarCerca(ubicacion, radio);
    }

}