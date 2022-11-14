package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TransportadorConverter;
import com.tcargo.web.entidades.Chofer;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Vehiculo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TransportadorModel;
import com.tcargo.web.modelos.busqueda.BusquedaTransportadorModel;
import com.tcargo.web.repositorios.TransportadorRepository;
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

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TransportadorService {

    private final TransportadorConverter transportadorConverter;
    private final TransportadorRepository transportadorRepository;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Transportador guardar(TransportadorModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        Transportador transportador = transportadorConverter.modeloToEntidad(model);

        if (transportador.getEliminado() != null) {
            throw new WebException(messages.getMessage("transportador.back.error.dado.baja", null, locale));
        }

        if (transportador.getNombre() == null || transportador.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("transportador.back.error.nombre", null, locale));
        }

        if (transportador.getRazonSocial() == null || transportador.getRazonSocial().isEmpty()) {
            throw new WebException(messages.getMessage("transportador.back.error.razon.social", null, locale));
        }

        if (transportador.getUsuario() == null) {
            throw new WebException(messages.getMessage("transportador.back.error.usuario", null, locale));
        }

        transportador.setModificacion(new Date());

        return transportadorRepository.save(transportador);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Transportador eliminar(String id) throws WebException {
        Transportador transportador = transportadorRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (transportador.getEliminado() == null) {
            transportador.setEliminado(new Date());
            transportador = transportadorRepository.save(transportador);
        } else {
            throw new WebException(messages.getMessage("transportador.back.error.dado.baja", null, locale));
        }

        return transportador;
    }

    @Transactional
    public void addVehiculo(Vehiculo vehiculo) {
        Transportador t = transportadorRepository.getOne(vehiculo.getTransportador().getId());
        t.getVehiculos().add(vehiculo);
        transportadorRepository.save(t);
    }

    @Transactional
    public void addChofer(Chofer chofer) {
        Transportador t = transportadorRepository.getOne(chofer.getTransportador().getId());
        t.getChoferes().add(chofer);
        transportadorRepository.save(t);
    }

    public Page<Transportador> listarActivos(Pageable paginable, String q) {
        return transportadorRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Transportador> listarActivos(Pageable paginable) {
        return transportadorRepository.buscarActivos(paginable);
    }

    public Page<Transportador> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<Transportador> transportadores = transportadorRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(transportadores, paginable, transportadores.size());
        }
        return transportadorRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<Transportador> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<Transportador> transportadores = transportadorRepository.buscarActivos(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(transportadores, paginable, transportadores.size());
        }
        return transportadorRepository.buscarActivos(paginable, "%" + q + "%", idPais);
    }

    public List<Transportador> listarActivos() {
        return transportadorRepository.buscarActivos();
    }

    public List<TransportadorModel> listarActivosModel() {
        List<Transportador> transportadores = transportadorRepository.buscarActivos();
        return transportadorConverter.entidadesToModelos(transportadores);
    }

    public Transportador buscarPorIdUsuario(String id) {
        return transportadorRepository.buscarTransportadorPorIdUsuario(id);
    }

    public TransportadorModel buscarPorIdUsuarioModelo(String id) {
        Transportador t = transportadorRepository.buscarTransportadorPorIdUsuario(id);
        return t != null ? transportadorConverter.entidadToModelo(t) : null;
    }

    public Transportador porIdViaje(String viajeId) {
        return transportadorRepository.findByViajesId(viajeId);
    }


    public TransportadorModel buscar(String id) {
        Transportador transportador = transportadorRepository.getOne(id);
        return transportadorConverter.entidadToModelo(transportador);
    }

    public Transportador buscarEntidad(String id) {
        return transportadorRepository.findById(id).orElse(null);
    }

    public Page<Transportador> buscarFiltrados(Pageable pageable, BusquedaTransportadorModel busqueda, boolean excel) {
        return transportadorRepository.buscarFiltrados(pageable, busqueda, excel);
    }

}