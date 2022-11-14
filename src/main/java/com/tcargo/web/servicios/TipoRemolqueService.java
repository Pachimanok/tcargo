package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoRemolqueConverter;
import com.tcargo.web.entidades.TipoRemolque;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoRemolqueModel;
import com.tcargo.web.repositorios.TipoRemolqueRepository;
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
public class TipoRemolqueService {

    private final TipoRemolqueConverter tipoRemolqueConverter;
    private final TipoRemolqueRepository tipoRemolqueRepository;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoRemolque guardar(TipoRemolqueModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("tipo.remolque.back.error.pais", null, locale));
        }

        TipoRemolque tipoRemolque = tipoRemolqueConverter.modeloToEntidad(model);

        if (tipoRemolque.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.remolque.back.error.dado.baja", null, locale));
        }

        if (tipoRemolque.getCaracteristicas() == null || tipoRemolque.getCaracteristicas().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.remolque.back.error.nombre", null, locale));
        }

        List<TipoRemolque> otros = tipoRemolqueRepository.buscarTipoRemolquePorCaracteristicas(tipoRemolque.getCaracteristicas());
        for (TipoRemolque otro : otros) {
            if (otro != null && !otro.getId().equals(tipoRemolque.getId()) && otro.getPais() == tipoRemolque.getPais()) {
                throw new WebException(messages.getMessage("tipo.remolque.back.error.nombre.repetido", null, locale));
            }
        }

        tipoRemolque.setModificacion(new Date());

        return tipoRemolqueRepository.save(tipoRemolque);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoRemolque eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        TipoRemolque tipoRemolque = tipoRemolqueRepository.getOne(id);
        if (tipoRemolque.getEliminado() == null) {
            tipoRemolque.setEliminado(new Date());
            tipoRemolque = tipoRemolqueRepository.save(tipoRemolque);
        } else {
            throw new WebException(messages.getMessage("tipo.remolque.back.error.dado.baja", null, locale));
        }

        return tipoRemolque;
    }

    public Page<TipoRemolque> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoRemolque> tiposRemolque = tipoRemolqueRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tiposRemolque, paginable, tiposRemolque.size());
        }
        return tipoRemolqueRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoRemolque> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoRemolque> tiposRemolque = tipoRemolqueRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tiposRemolque, paginable, tiposRemolque.size());
        }
        return tipoRemolqueRepository.buscarActivos(paginable);
    }

    public List<TipoRemolque> listarActivos() {
        return tipoRemolqueRepository.buscarActivos();
    }

    public TipoRemolqueModel buscar(String id) {
        TipoRemolque tipoRemolque = tipoRemolqueRepository.getOne(id);
        return tipoRemolqueConverter.entidadToModelo(tipoRemolque);
    }

    public Page<TipoRemolque> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<TipoRemolque> tiposRemolque = tipoRemolqueRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(tiposRemolque, paginable, tiposRemolque.size());
        }
        return tipoRemolqueRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<TipoRemolque> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<TipoRemolque> tiposRemolque = tipoRemolqueRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(tiposRemolque, paginable, tiposRemolque.size());
        }
        return tipoRemolqueRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public List<TipoRemolqueModel> activosPorPaisModel(String idPais) {
        return tipoRemolqueConverter.entidadesToModelos(tipoRemolqueRepository.activosPorPais(idPais));
    }
}