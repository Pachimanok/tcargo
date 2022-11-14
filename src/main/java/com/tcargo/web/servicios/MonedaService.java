package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.MonedaConverter;
import com.tcargo.web.entidades.Moneda;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.MonedaModel;
import com.tcargo.web.repositorios.MonedaRepository;
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
public class MonedaService {

    private final MessageSource messages;
    private final MonedaConverter monedaConverter;
    private final MonedaRepository monedaRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Moneda guardar(MonedaModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("moneda.back.error.pais", null, locale));
        }

        Moneda moneda = monedaConverter.modeloToEntidad(model);

        if (moneda.getEliminado() != null) {
            throw new WebException(messages.getMessage("moneda.back.error.dada.baja", null, locale));
        }

        if (moneda.getNombre() == null || moneda.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("moneda.back.error.nombre", null, locale));
        }

        if (moneda.getSimbolo() == null || model.getSimbolo().equals("")) {
            throw new WebException(messages.getMessage("moneda.back.error.simbolo", null, locale));
        }

        List<Moneda> otros = monedaRepository.buscarMonedaPorNombre(moneda.getNombre());
        for (Moneda otro : otros) {
            if (otro != null && !otro.getId().equals(moneda.getId()) && otro.getPais() == moneda.getPais()) {
                throw new WebException(messages.getMessage("moneda.back.error.nombre.repetido", null, locale));
            }
        }

        moneda.setModificacion(new Date());

        return monedaRepository.save(moneda);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Moneda eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        Moneda moneda = monedaRepository.getOne(id);
        if (moneda.getEliminado() == null) {
            moneda.setEliminado(new Date());
            moneda = monedaRepository.save(moneda);
        } else {
            throw new WebException(messages.getMessage("moneda.back.error.dada.baja", null, locale));
        }

        return moneda;
    }

    public Page<Moneda> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<Moneda> monedas = monedaRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(monedas, paginable, monedas.size());
        }
        return monedaRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Moneda> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<Moneda> monedas = monedaRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(monedas, paginable, monedas.size());
        }
        return monedaRepository.buscarActivos(paginable);
    }

    public List<Moneda> listarActivos() {
        return monedaRepository.buscarActivos();
    }

    public MonedaModel buscar(String id) {
        Moneda moneda = monedaRepository.getOne(id);
        return monedaConverter.entidadToModelo(moneda);
    }

    public Moneda buscarEntidad(String id) {
        return monedaRepository.findById(id).orElse(null);
    }

    public Page<Moneda> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<Moneda> monedas = monedaRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(monedas, paginable, monedas.size());
        }
        return monedaRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<Moneda> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<Moneda> monedas = monedaRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(monedas, paginable, monedas.size());
        }
        return monedaRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public List<Moneda> listarActivosPorPais(String idPais) {
        return monedaRepository.buscarActivosPorPais(idPais);
    }

}