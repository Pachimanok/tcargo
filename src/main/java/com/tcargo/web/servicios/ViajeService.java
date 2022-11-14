package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ViajeConverter;
import com.tcargo.web.entidades.Carga;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Viaje;
import com.tcargo.web.enumeraciones.EstadoViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CargaNegativaModel;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.modelos.ViajeModel;
import com.tcargo.web.modelos.busqueda.BusquedaViajeModel;
import com.tcargo.web.repositorios.ViajeRepository;
import com.tcargo.web.utiles.Fecha;
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

import java.util.*;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ViajeService {

    private final ViajeRepository viajeRepository;
    private final ViajeConverter viajeConverter;
    private final UbicacionService ubicacionService;
    private final MessageSource messages;

    private static final String VIAJE_ELIMINADO = "viaje.back.end.service.eliminado";

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Viaje guardar(ViajeModel model) throws WebException {
        Viaje viaje = viajeConverter.modeloToEntidad(model);
        Locale locale = LocaleContextHolder.getLocale();
        if (viaje.getEliminado() != null) {
            throw new WebException(messages.getMessage(VIAJE_ELIMINADO, null, locale));
        }
        if (viaje.getUbicacionInicial() == null || viaje.getUbicacionFinal() == null) {
            throw new WebException(messages.getMessage("viaje.back.end.service.sin.ubicacion", null, locale));
        }
        if (viaje.getUbicacionInicial().getDireccion() == null || viaje.getUbicacionInicial().getDireccion().isEmpty()) {
            throw new WebException(messages.getMessage("viaje.back.end.service.sin.direccion.partida", null, locale));
        }
        if (viaje.getUbicacionFinal().getDireccion() == null || viaje.getUbicacionFinal().getDireccion().isEmpty()) {
            throw new WebException(messages.getMessage("viaje.back.end.service.sin.direccion.llegada", null, locale));
        }

        viaje.setModificacion(new Date());

        return viajeRepository.save(viaje);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Viaje eliminar(String id) throws WebException {
        Viaje viaje = viajeRepository.getOne(id);
        Locale locale = LocaleContextHolder.getLocale();

        if (viaje.getEliminado() == null) {
            viaje.setEliminado(new Date());
            viaje = viajeRepository.save(viaje);
        } else {
            throw new WebException(messages.getMessage(VIAJE_ELIMINADO, null, locale));
        }

        return viaje;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Viaje guardar(CargaNegativaModel model) throws WebException {

        Viaje viaje = viajeConverter.modeloToEntidad(model);
        Locale locale = LocaleContextHolder.getLocale();

        if (viaje.getEliminado() != null) {
            throw new WebException(messages.getMessage(VIAJE_ELIMINADO, null, locale));
        }

        if (model.getPartidaCargaNegativa() != null && !model.getPartidaCargaNegativa().equals("")) {
            viaje.setPartidaCargaNegativa(Fecha.obtenerFecha(model.getPartidaCargaNegativa()));
        }

        if (model.getLlegadaCargaNegativa() != null && !model.getLlegadaCargaNegativa().equals("")) {
            viaje.setLlegadaCargaNegativa(Fecha.obtenerFecha(model.getLlegadaCargaNegativa()));
        }

        if (model.getUbicacionInicial().getId() == null || model.getUbicacionInicial().getId().equals("")) {
            viaje.setUbicacionInicial(ubicacionService.guardar(model.getUbicacionInicial()));
        }

        if (model.getUbicacionFinal() != null) {
            viaje.setUbicacionFinal(ubicacionService.guardar(model.getUbicacionFinal()));
        }

        viaje.setModificacion(new Date());

        return viajeRepository.save(viaje);
    }

    public List<ViajeModel> listarActivos() {
        return viajeConverter.entidadesToModelos(viajeRepository.findByEliminadoNull());
    }

    public Viaje porIdEntidad(String id) {
        return viajeRepository.findById(id).orElse(null);
    }

    public ViajeModel porIdModel(String id) {
        return viajeConverter.entidadToModelo(viajeRepository.findById(id).orElse(null));
    }

    public Page<Viaje> listarActivos(Pageable pageable) {
        return viajeRepository.buscarActivos(pageable);
    }

    public Map<String, Object> armarMapa(List<Viaje> viajes) {
        Map<String, Object> mapa = new HashMap<>();

        for (Viaje viaje : viajes) {
            CargaNegativaModel v = new CargaNegativaModel();
            UbicacionModel ub = new UbicacionModel();
            v.setUbicacionInicial(ub);
            v.setId(viaje.getId());
            v.getUbicacionInicial().setLatitud(viaje.getUbicacionInicial().getLatitud());
            v.getUbicacionInicial().setLongitud(viaje.getUbicacionInicial().getLongitud());
            if (viaje.getUbicacionFinal() != null) {
                UbicacionModel ubs = new UbicacionModel();
                v.setUbicacionFinal(ubs);
                v.getUbicacionFinal().setLatitud(viaje.getUbicacionFinal().getLatitud());
                v.getUbicacionFinal().setLongitud(viaje.getUbicacionFinal().getLongitud());
            }
            if (viaje.getWayPoints() != null && !viaje.getWayPoints().isEmpty()) {
                v.setWayPoints(viaje.getWayPoints());
            }
            mapa.put(viaje.getId(), v);
        }

        return mapa;
    }

    public Page<Viaje> buscarPorCriterios(Pageable page, BusquedaViajeModel viaje, Transportador transportador) {
        return viajeRepository.buscarPorCriterios(viaje, transportador, page, false);
    }

    public Page<ViajeModel> buscarParaDador(Pageable pageable, BusquedaViajeModel busqueda) {
        Page<Viaje> viajes = viajeRepository.buscarPorCriterios(busqueda, null, pageable, false);
        return new PageImpl<>(viajeConverter.entidadesToModelos(viajes.getContent()), pageable, viajes.getTotalElements());
    }

    public Page<Viaje> buscarPorCriteriosPropios(Pageable page, BusquedaViajeModel viaje, Transportador transportador) {
        return viajeRepository.buscarPorCriterios(viaje, transportador, page, true);
    }

    public Viaje actualizarEstado(Viaje viaje, EstadoViaje estado) {
        viaje.setEstadoViaje(estado);
        viaje.setModificacion(new Date());
        return viajeRepository.save(viaje);
    }

    public Viaje buscarPorCarga(Carga carga) {
        return viajeRepository.findByEliminadoNullAndCarga(carga);
    }

}
