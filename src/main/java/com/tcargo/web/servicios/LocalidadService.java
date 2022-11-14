package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.LocalidadConverter;
import com.tcargo.web.entidades.Localidad;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.LocalidadModel;
import com.tcargo.web.repositorios.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class LocalidadService {

    @Autowired
    private LocalidadConverter localidadConverter;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Localidad guardar(LocalidadModel model) throws WebException {
        Localidad localidad = localidadConverter.modeloToEntidad(model);

        if (localidad.getEliminado() != null) {
            throw new WebException("La localidad que intenta modificar se encuentra dada de baja.");
        }

        if (localidad.getNombre() == null || localidad.getNombre().isEmpty()) {
            throw new WebException("El nombre de la localidad no puede ser vacío.");
        }

        if (localidad.getCodigoPostal() == null || localidad.getCodigoPostal().isEmpty()) {
            throw new WebException("El CP de la localidad no puede ser vacío.");
        }

        if (localidad.getProvincia() == null) {
            throw new WebException("La localidad debe estar vinculada a una provincia.");
        }

        List<Localidad> otros = localidadRepository.buscarLocalidadPorNombre(localidad.getNombre());
        for (Localidad otro : otros) {
            if (otro != null && !otro.getId().equals(localidad.getId())) {
                throw new WebException("Ya existe una localidad con ese nombre.");
            }
        }

        localidad.setModificacion(new Date());

        return localidadRepository.save(localidad);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Localidad eliminar(String id) throws WebException {
        Localidad localidad = localidadRepository.getOne(id);
        if (localidad.getEliminado() == null) {
            localidad.setEliminado(new Date());
            localidad = localidadRepository.save(localidad);
        } else {
            throw new WebException("La localidad que intenta eliminar ya se encuentra dado de baja.");
        }

        return localidad;
    }

    public Page<Localidad> listarActivos(Pageable paginable, String q) {
        return localidadRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Localidad> listarActivos(Pageable paginable) {
        return localidadRepository.buscarActivos(paginable);
    }

    public List<Localidad> listarActivos() {
        return localidadRepository.buscarActivos();
    }


    public LocalidadModel buscar(String id) {
        Localidad localidad = localidadRepository.getOne(id);
        return localidadConverter.entidadToModelo(localidad);
    }

}