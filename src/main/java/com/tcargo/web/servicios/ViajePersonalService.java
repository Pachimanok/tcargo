package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ViajePersonalConverter;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.ViajePersonal;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ViajePersonalModel;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import com.tcargo.web.repositorios.ViajePersonalRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ViajePersonalService {

    private final ViajePersonalRepository viajePersonalRepository;
    private final ViajePersonalConverter viajePersonalConverter;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public ViajePersonal guardar(ViajePersonalModel model) throws WebException {
        ViajePersonal viaje = viajePersonalConverter.modeloToEntidad(model);
        final Locale locale = LocaleContextHolder.getLocale();

        if (viaje.getEliminado() != null) {
            throw new WebException(messages.getMessage("viaje.personal.back.eliminado", null, locale));
        }

        if (viaje.getVehiculo() == null) {
            throw new WebException(messages.getMessage("viaje.personal.back.vehiculo", null, locale));
        }

        if (viaje.getRemolque() == null) {
            throw new WebException(messages.getMessage("viaje.personal.back.remolque", null, locale));
        }

        if (viaje.getChofer() == null) {
            throw new WebException(messages.getMessage("viaje.personal.back.chofer", null, locale));
        }

        viaje.setModificacion(new Date());
        return viajePersonalRepository.save(viaje);
    }

    public ViajePersonal porIdEntidad(String id) {
        return viajePersonalRepository.findById(id).orElse(null);
    }

    public Page<ViajePersonal> listarActivos(Pageable pageable) {
        return viajePersonalRepository.buscarActivos(pageable);
    }

    public Page<ViajePersonal> buscarPorCriterios(Pageable page, BusquedaViajeModel viaje, Transportador t) {
        return viajePersonalRepository.buscarPorCriterios(viaje, t, page, false);
    }

    public Page<ViajePersonalModel> buscarPorCriteriosPropios(Pageable page, BusquedaViajeModel viaje, Transportador t) {
        Page<ViajePersonal> viajes = viajePersonalRepository.buscarPorCriterios(viaje, t, page, true);
        List<ViajePersonalModel> model = viajePersonalConverter.entidadesToModelos(viajes.getContent());
        return new PageImpl<>(model, page, viajes.getTotalElements());
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public ViajePersonal eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        Optional<ViajePersonal> viajeOptional = viajePersonalRepository.findById(id);

        if (!viajeOptional.isPresent()) {
            throw new WebException(messages.getMessage("viaje.personal.back.no_existente", null, locale));
        }

        ViajePersonal viaje = viajeOptional.get();

        if (viaje.getEliminado() == null) {
            viaje.setEliminado(new Date());
            viaje = viajePersonalRepository.save(viaje);
        } else {
            throw new WebException(messages.getMessage("viaje.personal.back.dado.de.baja", null, locale));
        }

        return viaje;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public ViajePersonal actualizarEstado(String id, EstadoViaje estado) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        Optional<ViajePersonal> viajeOptional = viajePersonalRepository.findById(id);

        if (!viajeOptional.isPresent()) {
            throw new WebException(messages.getMessage("viaje.personal.back.no_existente", null, locale));
        }

        ViajePersonal viaje = viajeOptional.get();

        if (viaje.getEliminado() == null) {
            viaje.setEstadoViaje(estado);
            viaje = viajePersonalRepository.save(viaje);
        } else {
            throw new WebException(messages.getMessage("viaje.personal.back.dado.de.baja", null, locale));
        }

        return viaje;
    }


}
