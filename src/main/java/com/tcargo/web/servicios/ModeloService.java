package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ModeloConverter;
import com.tcargo.web.entidades.Modelo;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ModeloModel;
import com.tcargo.web.repositorios.ModeloRepository;
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
public class ModeloService {

    private final ModeloConverter modeloConverter;
    private final ModeloRepository modeloRepository;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Modelo guardar(ModeloModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("modelo.back.error.pais", null, locale));
        }

        Modelo modelo = modeloConverter.modeloToEntidad(model);

        if (modelo.getEliminado() != null) {
            throw new WebException(messages.getMessage("modelo.back.error.dado.baja", null, locale));
        }

        if (modelo.getNombre() == null || modelo.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("modelo.back.error.nombre", null, locale));
        }

        if (modelo.getMarca() == null) {
            throw new WebException(messages.getMessage("modelo.back.error.marca", null, locale));
        }

        List<Modelo> otros = modeloRepository.buscarModeloPorNombre(modelo.getNombre());
        for (Modelo otro : otros) {
            if (otro != null && !otro.getId().equals(modelo.getId()) && otro.getPais() == modelo.getPais()) {
                throw new WebException(messages.getMessage("modelo.back.error.nombre.repetido", null, locale));
            }
        }

        modelo.setModificacion(new Date());

        return modeloRepository.save(modelo);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Modelo eliminar(String id) throws WebException {
        Modelo modelo = modeloRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (modelo.getEliminado() == null) {
            modelo.setEliminado(new Date());
            modelo = modeloRepository.save(modelo);
        } else {
            throw new WebException(messages.getMessage("modelo.back.error.dado.baja", null, locale));
        }

        return modelo;
    }

    public Page<Modelo> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<Modelo> modelos = modeloRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(modelos, paginable, modelos.size());
        }
        return modeloRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Modelo> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<Modelo> modelos = modeloRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(modelos, paginable, modelos.size());
        }
        return modeloRepository.buscarActivos(paginable);
    }

    public List<Modelo> listarActivos() {
        return modeloRepository.buscarActivos();
    }

    public ModeloModel buscar(String id) {
        Modelo modelo = modeloRepository.getOne(id);
        return modeloConverter.entidadToModelo(modelo);
    }

    public Page<Modelo> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<Modelo> modelos = modeloRepository.buscarActivosPorPais(paginable.getSort(), idPais);
            return new PageImpl<>(modelos, paginable, modelos.size());
        }
        return modeloRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<Modelo> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<Modelo> modelos = modeloRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(modelos, paginable, modelos.size());
        }
        return modeloRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public List<Modelo> listarActivosPorPais(String idPais) {
        return modeloRepository.buscarActivosPorPais(idPais);
    }

    public List<ModeloModel> listarActivosPorPaisModel(String idPais) {
        return modeloConverter.entidadesToModelos(modeloRepository.buscarActivosPorPais(idPais));
    }

}