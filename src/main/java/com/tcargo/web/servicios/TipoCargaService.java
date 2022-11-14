package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.TipoCargaConverter;
import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.PaisModel;
import com.tcargo.web.modelos.TipoCargaModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.TipoCargaRepository;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TipoCargaService {

    private final TipoCargaConverter tipoCargaConverter;
    private final TipoCargaRepository tipoCargaRepository;
    private final MessageSource messages;
    private final PaisRepository paisRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoCarga guardar(TipoCargaModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();
        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("tipo.carga.back.error.pais", null, locale));
        }

        TipoCarga tipoCarga = tipoCargaConverter.modeloToEntidad(model);

        if (tipoCarga.getEliminado() != null) {
            throw new WebException(messages.getMessage("tipo.carga.back.error.dada.baja", null, locale));
        }

        if (tipoCarga.getCaracteristicas() == null || tipoCarga.getCaracteristicas().isEmpty()) {
            throw new WebException(messages.getMessage("tipo.carga.back.error.nombre", null, locale));
        }

        List<TipoCarga> otros = tipoCargaRepository.buscarTipoCargaPorCaracteristicas(tipoCarga.getCaracteristicas());
        for (TipoCarga otro : otros) {
            if (otro != null && !otro.getId().equals(tipoCarga.getId()) && otro.getPais() == tipoCarga.getPais()) {
                throw new WebException(messages.getMessage("tipo.carga.back.error.nombre.repetido", null, locale));
            }
        }

        tipoCarga.setModificacion(new Date());

        return tipoCargaRepository.save(tipoCarga);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public TipoCarga eliminar(String id) throws WebException {
        TipoCarga tipoCarga = tipoCargaRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (tipoCarga.getEliminado() == null) {
            tipoCarga.setEliminado(new Date());
            tipoCarga = tipoCargaRepository.save(tipoCarga);
        } else {
            throw new WebException(messages.getMessage("tipo.carga.back.error.dada.baja", null, locale));
        }

        return tipoCarga;
    }

    public Page<TipoCarga> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<TipoCarga> tipoCargas = tipoCargaRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(tipoCargas, paginable, tipoCargas.size());
        } 
        return tipoCargaRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<TipoCarga> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<TipoCarga> tipoCargas = tipoCargaRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(tipoCargas, paginable, tipoCargas.size());
        }
        return tipoCargaRepository.buscarActivos(paginable);
    }

    public List<TipoCarga> listarActivos() {
        return tipoCargaRepository.buscarActivos();
    }

    public TipoCargaModel buscar(String id) {
        TipoCarga tipoCarga = tipoCargaRepository.getOne(id);
        return tipoCargaConverter.entidadToModelo(tipoCarga);
    }

    public TipoCarga buscarEntidad(String id) {
        return tipoCargaRepository.findById(id).orElse(null);
    }

    public Page<TipoCarga> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<TipoCarga> tipoCargas = tipoCargaRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(tipoCargas, paginable, tipoCargas.size());
        }
        return tipoCargaRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<TipoCarga> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<TipoCarga> tipoCargas = tipoCargaRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(tipoCargas, paginable, tipoCargas.size());
        }
        return tipoCargaRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public List<TipoCargaModel> activosPorPaisModel(String idPais) {
        return tipoCargaConverter.entidadesToModelos(tipoCargaRepository.activosPorPais(idPais));
    }

    public List<TipoCarga> activosPorPais(String idPais) {
        return tipoCargaRepository.activosPorPais(idPais);
    }

    public List<TipoCargaModel> listarActivosModel() {
        return tipoCargaConverter.entidadesToModelos(tipoCargaRepository.buscarActivos());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public List<TipoCargaModel>listarActivosModelSinDuplicados(){
        List<TipoCarga>resultado=tipoCargaRepository.buscarActivos();
        List<TipoCarga>filtrado=resultado.stream().filter(distinctByKey(TipoCarga::getCaracteristicas)).collect(Collectors.toList());
        return tipoCargaConverter.entidadesToModelos(filtrado);
    }
}