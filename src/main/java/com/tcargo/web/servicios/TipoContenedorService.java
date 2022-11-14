package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoContenedorConverter;
import com.tcargo.web.entidades.TipoContenedor;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoContenedorModel;
import com.tcargo.web.repositorios.TipoContenedorRepository;
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
public class TipoContenedorService {

    private final TipoContenedorConverter tipoContenedorConverter;
    private final TipoContenedorRepository tipoContenedorRepository;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoContenedor guardar(TipoContenedorModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("tipo.contenedor.back.error.pais", null, locale));
        }

        TipoContenedor tipoContenedor = tipoContenedorConverter.modeloToEntidad(model);

        if (tipoContenedor.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.contenedor.back.error.dada.baja", null, locale));
        }

        if (tipoContenedor.getCaracteristicas() == null || tipoContenedor.getCaracteristicas().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.contenedor.back.error.caracteristicas", null, locale));
        }

        List<TipoContenedor> otros = tipoContenedorRepository.buscarTipoContenedorPorCaracteristicas(tipoContenedor.getCaracteristicas());
        for (TipoContenedor otro : otros) {
            if (otro != null && !otro.getId().equals(tipoContenedor.getId()) && otro.getPais() == tipoContenedor.getPais()) {
                throw new WebException(messages.getMessage("tipo.contenedor.back.error.nombre.repedido", null, locale));
            }
        }

        tipoContenedor.setModificacion(new Date());

        return tipoContenedorRepository.save(tipoContenedor);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoContenedor eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        TipoContenedor tipoContenedor = tipoContenedorRepository.getOne(id);
        if (tipoContenedor.getEliminado() == null) {
            tipoContenedor.setEliminado(new Date());
            tipoContenedor = tipoContenedorRepository.save(tipoContenedor);
        } else {
            throw new WebException(messages.getMessage("tipo.contenedor.back.error.dada.baja", null, locale));
        }

        return tipoContenedor;
    }

    public Page<TipoContenedor> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoContenedor> tiposContenedor = tipoContenedorRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tiposContenedor, paginable, tiposContenedor.size());
        }
        return tipoContenedorRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoContenedor> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoContenedor> tiposContenedor = tipoContenedorRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tiposContenedor, paginable, tiposContenedor.size());
        }
        return tipoContenedorRepository.buscarActivos(paginable);
    }

    public List<TipoContenedor> listarActivos() {
        return tipoContenedorRepository.buscarActivos();
    }

    public TipoContenedorModel buscar(String id) {
        TipoContenedor tipoContenedor = tipoContenedorRepository.getOne(id);
        return tipoContenedorConverter.entidadToModelo(tipoContenedor);
    }

    public Page<TipoContenedor> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<TipoContenedor> tiposContenedor = tipoContenedorRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(tiposContenedor, paginable, tiposContenedor.size());
        }
        return tipoContenedorRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<TipoContenedor> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<TipoContenedor> tiposContenedor = tipoContenedorRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(tiposContenedor, paginable, tiposContenedor.size());
        }
        return tipoContenedorRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public List<TipoContenedorModel> listarActivosModel() {
        return tipoContenedorConverter.entidadesToModelos(tipoContenedorRepository.buscarActivos());
    }

}