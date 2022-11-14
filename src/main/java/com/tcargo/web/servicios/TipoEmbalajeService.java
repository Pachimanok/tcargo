package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoEmbalajeConverter;
import com.tcargo.web.entidades.TipoEmbalaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoEmbalajeModel;
import com.tcargo.web.repositorios.TipoEmbalajeRepository;
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
public class TipoEmbalajeService {

    private final TipoEmbalajeConverter tipoEmbalajeConverter;
    private final TipoEmbalajeRepository tipoEmbalajeRepository;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoEmbalaje guardar(TipoEmbalajeModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("tipo.embalaje.back.error.pais", null, locale));
        }

        TipoEmbalaje tipoEmbalaje = tipoEmbalajeConverter.modeloToEntidad(model);

        if (tipoEmbalaje.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.embalaje.back.error.dado.baja", null, locale));
        }

        if (tipoEmbalaje.getCaracteristicas() == null || tipoEmbalaje.getCaracteristicas().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.embalaje.back.error.nombre", null, locale));
        }

        List<TipoEmbalaje> otros = tipoEmbalajeRepository.buscarTipoEmbalajePorCaracteristicas(tipoEmbalaje.getCaracteristicas());
        for (TipoEmbalaje otro : otros) {
            if (otro != null && !otro.getId().equals(tipoEmbalaje.getId()) && otro.getPais() == tipoEmbalaje.getPais()) {
                throw new WebException(messages.getMessage("tipo.embalaje.back.error.repetido", null, locale));
            }
        }

        tipoEmbalaje.setModificacion(new Date());

        return tipoEmbalajeRepository.save(tipoEmbalaje);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoEmbalaje eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        TipoEmbalaje tipoEmbalaje = tipoEmbalajeRepository.getOne(id);
        if (tipoEmbalaje.getEliminado() == null) {
            tipoEmbalaje.setEliminado(new Date());
            tipoEmbalaje = tipoEmbalajeRepository.save(tipoEmbalaje);
        } else {
            throw new WebException(messages.getMessage("tipo.embalaje.back.error.dado.baja", null, locale));
        }

        return tipoEmbalaje;
    }

    public Page<TipoEmbalaje> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoEmbalaje> tiposEmbalaje = tipoEmbalajeRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tiposEmbalaje, paginable, tiposEmbalaje.size());
        }
        return tipoEmbalajeRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoEmbalaje> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoEmbalaje> tiposEmbalaje = tipoEmbalajeRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tiposEmbalaje, paginable, tiposEmbalaje.size());
        }
        return tipoEmbalajeRepository.buscarActivos(paginable);
    }

    public List<TipoEmbalaje> listarActivos() {
        return tipoEmbalajeRepository.buscarActivos();
    }

    public TipoEmbalajeModel buscar(String id) {
        TipoEmbalaje tipoEmbalaje = tipoEmbalajeRepository.getOne(id);
        return tipoEmbalajeConverter.entidadToModelo(tipoEmbalaje);
    }

    public Page<TipoEmbalaje> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<TipoEmbalaje> tiposEmbalaje = tipoEmbalajeRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(tiposEmbalaje, paginable, tiposEmbalaje.size());
        }
        return tipoEmbalajeRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<TipoEmbalaje> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<TipoEmbalaje> tiposEmbalaje = tipoEmbalajeRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(tiposEmbalaje, paginable, tiposEmbalaje.size());
        }
        return tipoEmbalajeRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public List<TipoEmbalajeModel> activosPorPaisModel(String idPais) {
        return tipoEmbalajeConverter.entidadesToModelos(tipoEmbalajeRepository.activosPorPais(idPais));
    }

}