package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.MarcaConverter;
import com.tcargo.web.entidades.Marca;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.MarcaModel;
import com.tcargo.web.repositorios.MarcaRepository;
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
public class MarcaService {

    private final MarcaConverter marcaConverter;
    private final MarcaRepository marcaRepository;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Marca guardar(MarcaModel model) throws WebException {
        Marca marca = marcaConverter.modeloToEntidad(model);
        final Locale locale = LocaleContextHolder.getLocale();

        if (marca.getEliminado() != null) {
            throw new WebException(messages.getMessage("marca.back.error.dada.baja", null, locale));
        }

        if (marca.getNombre() == null || marca.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("marca.back.error.nombre", null, locale));
        }

        List<Marca> otros = marcaRepository.buscarMarcaPorNombre(marca.getNombre());
        for (Marca otro : otros) {
            if (otro != null && !otro.getId().equals(marca.getId())) {
                throw new WebException(messages.getMessage("pais.back.en.service.eliminado", null, locale));
            }
        }

        marca.setModificacion(new Date());

        return marcaRepository.save(marca);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Marca eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        Marca marca = marcaRepository.getOne(id);
        if (marca.getEliminado() == null) {
            marca.setEliminado(new Date());
            marca = marcaRepository.save(marca);
        } else {
            throw new WebException(messages.getMessage("marca.back.error.dada.baja", null, locale));
        }

        return marca;
    }

    public Page<Marca> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<Marca> marcas = marcaRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(marcas, paginable, marcas.size());
        }
        return marcaRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Marca> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<Marca> marcas = marcaRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(marcas, paginable, marcas.size());
        }
        return marcaRepository.buscarActivos(paginable);
    }

    public List<Marca> listarActivos() {
        return marcaRepository.buscarActivos();
    }


    public MarcaModel buscar(String id) {
        Marca marca = marcaRepository.getOne(id);
        return marcaConverter.entidadToModelo(marca);
    }

}