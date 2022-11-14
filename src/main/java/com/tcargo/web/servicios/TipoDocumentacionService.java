package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoDocumentacionConverter;
import com.tcargo.web.entidades.TipoDocumentacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoDocumentacionModel;
import com.tcargo.web.repositorios.TipoDocumentacionRepository;
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
public class TipoDocumentacionService {

    private final MessageSource messages;
    private final TipoDocumentacionConverter tipoDocumentacionConverter;
    private final TipoDocumentacionRepository tipoDocumentacionRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoDocumentacion guardar(TipoDocumentacionModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIdPais() == null || model.getIdPais().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.documentacion.back.error.pais", null, locale));
        }

        TipoDocumentacion tipoDocumentacion = tipoDocumentacionConverter.modeloToEntidad(model);

        if (tipoDocumentacion.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.documentacion.back.error.dado.baja", null, locale));
        }

        if (!tipoDocumentacion.isObligatorioChofer() && !tipoDocumentacion.isObligatorioRemolque() && !tipoDocumentacion.isObligatorioVehiculo()) {
            throw new WebException(messages.getMessage("tipo.documentacion.back.error.tipo.doc", null, locale));
        }

        if (tipoDocumentacion.getNombre() == null || tipoDocumentacion.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.documentacion.back.error.tipo.nombre", null, locale));
        }

        List<TipoDocumentacion> otros = tipoDocumentacionRepository.buscarTipoDocumentacionPorNombre(tipoDocumentacion.getNombre());
        for (TipoDocumentacion otro : otros) {
            if (otro != null && !otro.getId().equals(tipoDocumentacion.getId()) && tipoDocumentacion.getPais().getId().equals(otro.getPais().getId())) {
                throw new WebException(messages.getMessage("tipo.documentacion.back.error.tipo.nombre.duplicado", null, locale));
            }
        }

        tipoDocumentacion.setModificacion(new Date());

        return tipoDocumentacionRepository.save(tipoDocumentacion);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoDocumentacion eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        TipoDocumentacion tipoDocumentacion = tipoDocumentacionRepository.getOne(id);
        if (tipoDocumentacion.getEliminado() == null) {
            tipoDocumentacion.setEliminado(new Date());
            tipoDocumentacion = tipoDocumentacionRepository.save(tipoDocumentacion);
        } else {
            throw new WebException(messages.getMessage("tipo.documentacion.back.error.dado.baja", null, locale));
        }

        return tipoDocumentacion;
    }

    public Page<TipoDocumentacion> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoDocumentacion> tiposDocumentacion = tipoDocumentacionRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tiposDocumentacion, paginable, tiposDocumentacion.size());
        }
        return tipoDocumentacionRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoDocumentacion> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoDocumentacion> tiposDocumentacion = tipoDocumentacionRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tiposDocumentacion, paginable, tiposDocumentacion.size());
        }
        return tipoDocumentacionRepository.buscarActivos(paginable);
    }

    public List<TipoDocumentacion> listarActivos() {
        return tipoDocumentacionRepository.buscarActivos();
    }

    public List<TipoDocumentacion> buscarObligatorioVehiculo(String idPais) {
        return tipoDocumentacionRepository.buscarObligatorioVehiculo(idPais);
    }

    public List<TipoDocumentacion> buscarObligatorioRemolque(String idPais) {
        return tipoDocumentacionRepository.buscarObligatorioRemolque(idPais);
    }

    public List<TipoDocumentacion> buscarObligatorioChofer(String idPais) {
        return tipoDocumentacionRepository.buscarObligatorioChofer(idPais);
    }

    public Page<TipoDocumentacion> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<TipoDocumentacion> tiposDocumentacion = tipoDocumentacionRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(tiposDocumentacion, paginable, tiposDocumentacion.size());
        }
        return tipoDocumentacionRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<TipoDocumentacion> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<TipoDocumentacion> tiposDocumentacion = tipoDocumentacionRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(tiposDocumentacion, paginable, tiposDocumentacion.size());
        }
        return tipoDocumentacionRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }


    public TipoDocumentacionModel buscar(String id) {
        TipoDocumentacion tipoDocumentacion = tipoDocumentacionRepository.getOne(id);
        return tipoDocumentacionConverter.entidadToModelo(tipoDocumentacion);
    }

}