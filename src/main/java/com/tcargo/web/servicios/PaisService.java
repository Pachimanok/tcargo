package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.PaisConverter;
import com.tcargo.web.entidades.Pais;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.PaisModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.UsuarioRepository;
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
public class PaisService {

    private final MessageSource messages;
    private final PaisConverter paisConverter;
    private final PaisRepository paisRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Pais guardar(PaisModel model) throws WebException {
        Pais pais = paisConverter.modeloToEntidad(model);
        final Locale locale = LocaleContextHolder.getLocale();
        if (pais.getEliminado() != null) {
            throw new WebException(messages.getMessage("pais.back.en.service.eliminado", null, locale));
        }

        if (pais.getNombre() == null || pais.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("pais.back.en.service.nombre.empty", null, locale));
        }

        if (pais.getRegexPatente() == null || pais.getRegexPatente().isEmpty()) {
            throw new WebException(messages.getMessage("pais.back.en.service.regex.vacio", null, locale));
        }

        List<Pais> otros = paisRepository.buscarPaisPorNombre(pais.getNombre());
        for (Pais otro : otros) {
            if (otro != null && !otro.getId().equals(pais.getId())) {
                throw new WebException(messages.getMessage("pais.back.en.service.nombre.duplicado", null, locale));
            }
        }

        pais.setModificacion(new Date());

        return paisRepository.save(pais);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Pais eliminar(String id) throws WebException {
        Pais pais = paisRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();
        if (pais.getEliminado() == null) {
            pais.setEliminado(new Date());
            pais = paisRepository.save(pais);
        } else {
            throw new WebException(messages.getMessage("pais.back.en.service.nombre.duplicado", null, locale));
        }

        return pais;
    }

    public Page<Pais> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<Pais> paises = paisRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(paises, paginable, paises.size());
        }
        return paisRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Pais> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<Pais> paises = paisRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(paises, paginable, paises.size());
        }
        return paisRepository.buscarActivos(paginable);
    }

    public List<Pais> listarActivos() {
        return paisRepository.buscarActivos();
    }

    public List<PaisModel> listarActivosModel() {
        return paisConverter.entidadesToModelos(paisRepository.buscarActivos());
    }

    public PaisModel buscar(String id) {
        Pais pais = paisRepository.getOne(id);
        return paisConverter.entidadToModelo(pais);
    }

    public Pais buscarParaMatch(String id) {
        return usuarioRepository.buscarPorId(id).getPais();
    }

    public Pais buscarEntidad(String idPais) {
        return paisRepository.findById(idPais).orElse(null);
    }

}
