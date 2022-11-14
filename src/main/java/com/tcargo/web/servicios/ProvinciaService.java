package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ProvinciaConverter;
import com.tcargo.web.entidades.Provincia;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ProvinciaModel;
import com.tcargo.web.repositorios.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ProvinciaService {

    @Autowired
    private ProvinciaConverter provinciaConverter;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Provincia guardar(ProvinciaModel model) throws WebException {
        Provincia provincia = provinciaConverter.modeloToEntidad(model);

        if (provincia.getEliminado() != null) {
            throw new WebException("La provincia que intenta modificar se encuentra dada de baja.");
        }

        if (provincia.getNombre() == null || provincia.getNombre().isEmpty()) {
            throw new WebException("El nombre de la provincia no puede ser vacío.");
        }

        if (provincia.getPais() == null) {
            throw new WebException("La provincia debe estar vinculada a un país.");
        }

        List<Provincia> otros = provinciaRepository.buscarProvinciaPorNombre(provincia.getNombre());
        for (Provincia otro : otros) {
            if (otro != null && !otro.getId().equals(provincia.getId())) {
                throw new WebException("Ya existe un provincia con ese nombre.");
            }
        }

        provincia.setModificacion(new Date());

        return provinciaRepository.save(provincia);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Provincia eliminar(String id) throws WebException {
        Provincia provincia = provinciaRepository.getOne(id);
        if (provincia.getEliminado() == null) {
            provincia.setEliminado(new Date());
            provincia = provinciaRepository.save(provincia);
        } else {
            throw new WebException("La provincia que intenta eliminar ya se encuentra dado de baja.");
        }

        return provincia;
    }

    public Page<Provincia> listarActivos(Pageable paginable, String q) {
        return provinciaRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Provincia> listarActivos(Pageable paginable) {
        return provinciaRepository.buscarActivos(paginable);
    }

    public List<Provincia> listarActivos() {
        return provinciaRepository.buscarActivos();
    }


    public ProvinciaModel buscar(String id) {
        Provincia provincia = provinciaRepository.getOne(id);
        return provinciaConverter.entidadToModelo(provincia);
    }

}