package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.RemolqueConverter;
import com.tcargo.web.entidades.Remolque;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.RemolqueModel;
import com.tcargo.web.repositorios.RemolqueRepository;
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
public class RemolqueService {

    @Autowired
    private RemolqueConverter remolqueConverter;

    @Autowired
    private RemolqueRepository remolqueRepository;

    @Autowired
    private MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Remolque guardar(RemolqueModel model) throws WebException {
        Remolque remolque = remolqueConverter.modeloToEntidad(model);
        final Locale locale = LocaleContextHolder.getLocale();

        Remolque v = new Remolque();
        try {
            v = remolqueRepository.findByDominio(model.getDominio());
        } catch (Exception ignored) {
        }

        if (remolque.getEliminado() != null) {
            throw new WebException(messages.getMessage("remolque.back.error.dada.baja", null, locale));
        }

        if (remolque.getTipoCargas() == null || remolque.getTipoCargas().isEmpty()) {
            throw new WebException(messages.getMessage("remolque.back.error.cantidad.cargas", null, locale));
        }

        if (remolque.getDominio() == null || remolque.getDominio().isEmpty()) {
            throw new WebException(messages.getMessage("remolque.back.error.dominio", null, locale));
        }

        if (v != null && !remolque.getId().equals(v.getId())) {
            throw new WebException(messages.getMessage("remolque.back.error.dominio.registrado", null, locale));
        }

        if (remolque.getTransportador() == null) {
            throw new WebException(messages.getMessage("remolque.back.error.sin.transportador", null, locale));
        }

        if (remolque.getTipoRemolque() == null) {
            throw new WebException(messages.getMessage("remolque.back.error.tipo.remolque", null, locale));
        }

        if (remolque.getAnioFabricacion() == null || remolque.getAnioFabricacion().equals("")) {
            throw new WebException(messages.getMessage("remolque.back.error.agno.fabricacion", null, locale));
        }

        remolque.setModificacion(new Date());

        return remolqueRepository.save(remolque);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Remolque eliminar(String id) throws WebException {
        Remolque remolque = remolqueRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (remolque.getEliminado() == null) {
            remolque.setEliminado(new Date());
            remolque = remolqueRepository.save(remolque);
        } else {
            throw new WebException(messages.getMessage("remolque.back.error.cantidad.cargas", null, locale));
        }

        return remolque;
    }

    public Page<Remolque> listarActivos(Pageable paginable, String q) {
        return remolqueRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Remolque> listarActivos(Pageable paginable) {
        return remolqueRepository.buscarActivos(paginable);
    }

    public Page<Remolque> listarActivosPorTransportador(String id, Pageable paginable) {
        return remolqueRepository.buscarActivosPorTransportador(id, paginable);
    }

    public List<Remolque> listarActivosPorTransportadorList(String id) {
        return remolqueRepository.buscarActivosPorTransportadorList(id);
    }

    public List<RemolqueModel> listarActivosPorTransportadorModel(String id) {
        return remolqueConverter.entidadesToModelos(remolqueRepository.buscarActivosPorTransportadorList(id));
    }

    public List<Remolque> listarActivos() {
        return remolqueRepository.buscarActivos();
    }

    public Remolque buscarPorId(String id) {
        return remolqueRepository.getOne(id);
    }

    public RemolqueModel buscar(String id) {
        Remolque remolque = remolqueRepository.getOne(id);
        return remolqueConverter.entidadToModelo(remolque);
    }

    public List<RemolqueModel> listarActivosPorTransportadorModelList(String id) {
        List<Remolque> remolques = listarActivosPorTransportadorList(id);
        return remolqueConverter.entidadesToModelos(remolques);
    }

    public boolean esRemolqueDeOtroTransportador(String idRemolque, String idTransportador) {
        return !buscar(idRemolque).getIdTransportador().equals(idTransportador);
    }

}