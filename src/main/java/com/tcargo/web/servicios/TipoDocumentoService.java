package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoDocumentoConverter;
import com.tcargo.web.entidades.TipoDocumento;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoDocumentoModel;
import com.tcargo.web.repositorios.TipoDocumentoRepository;
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
public class TipoDocumentoService {

    private final MessageSource messages;
    private final TipoDocumentoConverter tipoDocumentoConverter;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoDocumento guardar(TipoDocumentoModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("tipo.documento.back.error.pais", null, locale));
        }

        TipoDocumento tipoDocumento = tipoDocumentoConverter.modeloToEntidad(model);

        if (tipoDocumento.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.documento.back.dado.baja", null, locale));
        }

        if (tipoDocumento.getNombre() == null || tipoDocumento.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.documento.back.error.nombre", null, locale));
        }

        if (tipoDocumento.getPais() == null) {
            throw new WebException(messages.getMessage("tipo.documento.back.error.pais", null, locale));
        }

        List<TipoDocumento> otros = tipoDocumentoRepository.buscarTipoDocumentoPorNombre(tipoDocumento.getNombre());
        for (TipoDocumento otro : otros) {
            if (otro != null && !otro.getId().equals(tipoDocumento.getId()) && tipoDocumento.getPais().getId().equals(otro.getPais().getId())) {
                throw new WebException(messages.getMessage("tipo.documento.back.error.nombre.duplicado", null, locale));
            }
        }

        tipoDocumento.setModificacion(new Date());

        return tipoDocumentoRepository.save(tipoDocumento);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoDocumento eliminar(String id) throws WebException {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (tipoDocumento.getEliminado() == null) {
            tipoDocumento.setEliminado(new Date());
            tipoDocumento = tipoDocumentoRepository.save(tipoDocumento);
        } else {
            throw new WebException(messages.getMessage("tipo.documento.back.dado.baja", null, locale));
        }

        return tipoDocumento;
    }

    public Page<TipoDocumento> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoDocumento> tiposDocumento = tipoDocumentoRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tiposDocumento, paginable, tiposDocumento.size());
        }
        return tipoDocumentoRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoDocumento> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoDocumento> tiposDocumento = tipoDocumentoRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tiposDocumento, paginable, tiposDocumento.size());
        }
        return tipoDocumentoRepository.buscarActivos(paginable);
    }

    public List<TipoDocumento> listarActivos() {
        return tipoDocumentoRepository.buscarActivos();
    }

    public List<TipoDocumento> listarActivosPorPais(String idPais) {
        return tipoDocumentoRepository.buscarActivosPorPais(idPais);
    }


    public TipoDocumentoModel buscar(String id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.getOne(id);
        return tipoDocumentoConverter.entidadToModelo(tipoDocumento);
    }

}