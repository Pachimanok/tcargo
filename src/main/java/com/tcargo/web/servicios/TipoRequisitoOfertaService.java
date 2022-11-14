package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoRequisitoOfertaConverter;
import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.TipoRequisitoOferta;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TipoRequisitoOfertaModel;
import com.tcargo.web.repositorios.TipoRequisitoOfertaRepository;
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
public class TipoRequisitoOfertaService {

    private final TipoRequisitoOfertaConverter tipoRequisitoOfertaConverter;
    private final TipoRequisitoOfertaRepository tipoRequisitoOfertaRepository;
    private final MessageSource messages;

    @Autowired
    public TipoRequisitoOfertaService(TipoRequisitoOfertaConverter tipoRequisitoOfertaConverter, TipoRequisitoOfertaRepository tipoRequisitoOfertaRepository, MessageSource messajes) {
        this.tipoRequisitoOfertaConverter = tipoRequisitoOfertaConverter;
        this.tipoRequisitoOfertaRepository = tipoRequisitoOfertaRepository;
        this.messages = messajes;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoRequisitoOferta guardar(TipoRequisitoOfertaModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("tipo.requisito.back.error.pais", null, locale));
        }

        TipoRequisitoOferta tipoRequisitoOferta = tipoRequisitoOfertaConverter.modeloToEntidad(model);

        if (tipoRequisitoOferta.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.requisito.back.error.dado.baja", null, locale));
        }

        if (tipoRequisitoOferta.getNombre() == null || tipoRequisitoOferta.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.requisito.back.error.nombre", null, locale));
        }

        List<TipoRequisitoOferta> otros = tipoRequisitoOfertaRepository.buscarTipoRequisitoOfertaPorNombre(tipoRequisitoOferta.getNombre(), tipoRequisitoOferta.getPais().getId());
        for (TipoRequisitoOferta otro : otros) {
            if (otro != null && !otro.getId().equals(tipoRequisitoOferta.getId())) {
                throw new WebException(messages.getMessage("tipo.requisito.back.error.nombre.duplicado", null, locale));
            }
        }

        tipoRequisitoOferta.setModificacion(new Date());
        tipoRequisitoOferta.setObligatorioOferta(true);
        tipoRequisitoOferta.setObligatorioPedido(true);
        return tipoRequisitoOfertaRepository.save(tipoRequisitoOferta);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoRequisitoOferta eliminar(String id) throws WebException {
        
    	final Locale locale = LocaleContextHolder.getLocale();
        TipoRequisitoOferta tipoRequisitoOferta = tipoRequisitoOfertaRepository.getOne(id);
        if (tipoRequisitoOferta.getEliminado() == null) {
            tipoRequisitoOferta.setEliminado(new Date());
            tipoRequisitoOferta = tipoRequisitoOfertaRepository.save(tipoRequisitoOferta);
        } else {
            throw new WebException(messages.getMessage("tipo.requisito.back.error.dado.baja", null, locale));
        }

        return tipoRequisitoOferta;
    }

    public Page<TipoRequisitoOferta> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoRequisitoOferta> tiposRequisitoOferta = tipoRequisitoOfertaRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tiposRequisitoOferta, paginable, tiposRequisitoOferta.size());
        }
        return tipoRequisitoOfertaRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoRequisitoOferta> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoRequisitoOferta> tiposRequisitoOferta = tipoRequisitoOfertaRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tiposRequisitoOferta, paginable, tiposRequisitoOferta.size());
        }
        return tipoRequisitoOfertaRepository.buscarActivos(paginable);
    }

    public List<TipoRequisitoOferta> listarActivos() {
        return tipoRequisitoOfertaRepository.buscarActivos();
    }

    public List<TipoRequisitoOferta> buscarObligatorioPedidoPortiPoDeViaje(Pais pais, TipoDeViaje tipoDeViaje) {
        return tipoRequisitoOfertaRepository.findByPaisAndTipoDeViajeOrTipoDeViajeAndObligatorioPedidoTrue(pais, tipoDeViaje, TipoDeViaje.AMBOS);
    }

    public Page<TipoRequisitoOferta> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<TipoRequisitoOferta> tiposRequisitoOferta = tipoRequisitoOfertaRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(tiposRequisitoOferta, paginable, tiposRequisitoOferta.size());
        }
        return tipoRequisitoOfertaRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<TipoRequisitoOferta> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<TipoRequisitoOferta> tiposRequisitoOferta = tipoRequisitoOfertaRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(tiposRequisitoOferta, paginable, tiposRequisitoOferta.size());
        }
        return tipoRequisitoOfertaRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public TipoRequisitoOferta buscarEntidad(String id) {
        return tipoRequisitoOfertaRepository.getOne(id);
    }

    public TipoRequisitoOfertaModel buscar(String id) {
        TipoRequisitoOferta tipoRequisitoOferta = tipoRequisitoOfertaRepository.getOne(id);
        return tipoRequisitoOfertaConverter.entidadToModelo(tipoRequisitoOferta);
    }

}