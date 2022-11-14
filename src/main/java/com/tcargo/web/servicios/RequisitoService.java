package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.RequisitoConverter;
import com.tcargo.web.entidades.Requisito;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.RequisitoModel;
import com.tcargo.web.repositorios.RequisitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class RequisitoService {

    private final RequisitoRepository requisitoRepository;
    private final RequisitoConverter requisitoConverter;

    @Autowired
    public RequisitoService(RequisitoRepository requisitoRepository, RequisitoConverter requisitoConverter) {
        this.requisitoRepository = requisitoRepository;
        this.requisitoConverter = requisitoConverter;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Requisito guardar(RequisitoModel model) throws WebException {
        Requisito requisito = requisitoConverter.modeloToEntidad(model);

        if (requisito.getEliminado() != null) {
            throw new WebException("El requisito que intenta modificar se encuentra dada de baja.");
        }

        if (requisito.getNombre() == null || requisito.getNombre().isEmpty()) {
            throw new WebException("El nombre del requisito no puede ser vacío.");
        }

        List<Requisito> otros = requisitoRepository.buscarRequisitoPorNombre(requisito.getNombre());
        for (Requisito otro : otros) {
            if (otro != null && !otro.getId().equals(requisito.getId())) {
                throw new WebException("Ya existe un requisito con ese nombre.");
            }
        }

        requisito.setModificacion(new Date());

        return requisitoRepository.save(requisito);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Requisito eliminar(String id) throws WebException {
        Requisito requisito = requisitoRepository.getOne(id);
        if (requisito.getEliminado() == null) {
            requisito.setEliminado(new Date());
            requisito = requisitoRepository.save(requisito);
        } else {
            throw new WebException("El requisito que intenta eliminar ya se encuentra dado de baja.");
        }

        return requisito;
    }

    public Page<Requisito> listarActivos(Pageable paginable, String q) {
        return requisitoRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Requisito> listarActivos(Pageable paginable) {
        return requisitoRepository.buscarActivos(paginable);
    }

    public List<Requisito> listarActivos() {
        return requisitoRepository.buscarActivos();
    }

    public Requisito buscar(String id) {
        return requisitoRepository.getOne(id);
    }

}