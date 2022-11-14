package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Documentacion;
import com.tcargo.web.modelos.DocumentacionModel;
import com.tcargo.web.repositorios.*;
import com.tcargo.web.utiles.Fecha;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DocumentacionConverter extends Convertidor<DocumentacionModel, Documentacion> {

    private final ChoferRepository choferRepository;
    private final DocumentacionRepository documentacionRepository;
    private final RemolqueRepository remolqueRepository;
    private final TipoDocumentacionRepository tipoDocumentacionRepository;
    private final VehiculoRepository vehiculoRepository;

    public DocumentacionModel entidadToModelo(Documentacion documentacion) {
        DocumentacionModel model = new DocumentacionModel();

        try {
            BeanUtils.copyProperties(documentacion, model);
            model.setVencimiento(Fecha.formatearFecha(documentacion.getVencimiento()));
            if (documentacion.getTipoDocumentacion() != null) {
                model.setIdTipoDocumentacion(documentacion.getTipoDocumentacion().getId());
            }
            if (documentacion.getChofer() != null) {
                model.setIdChofer(documentacion.getChofer().getId());
            }
            if (documentacion.getRemolque() != null) {
                model.setIdRemolque(documentacion.getRemolque().getId());
            }
            if (documentacion.getVehiculo() != null) {
                model.setIdVehiculo(documentacion.getVehiculo().getId());
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de documentación", e);
        }

        return model;
    }

    public Documentacion modeloToEntidad(DocumentacionModel model) {
        Documentacion documentacion = new Documentacion();

        if (model.getId() != null && !model.getId().isEmpty()) {
            documentacion = documentacionRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, documentacion);
            documentacion.setVencimiento(Fecha.obtenerFecha(model.getVencimiento()));
            if (model.getIdTipoDocumentacion() != null) {
                documentacion.setTipoDocumentacion(tipoDocumentacionRepository.getOne(model.getIdTipoDocumentacion()));
            }
            if (model.getIdChofer() != null && !model.getIdChofer().isEmpty()) {
                documentacion.setChofer(choferRepository.getOne(model.getIdChofer()));
            }
            if (model.getIdRemolque() != null && !model.getIdRemolque().isEmpty()) {
                documentacion.setRemolque(remolqueRepository.getOne(model.getIdRemolque()));
            }
            if (model.getIdVehiculo() != null && !model.getIdVehiculo().isEmpty()) {
                documentacion.setVehiculo(vehiculoRepository.getOne(model.getIdVehiculo()));
            }
        } catch (Exception e) {
            log.error("Error al convertir el modelo de documentación en entidad", e);
        }

        return documentacion;
    }

    public List<DocumentacionModel> entidadesToModelos(List<Documentacion> documentaciones) {
        List<DocumentacionModel> model = new ArrayList<>();

        for (Documentacion documentacion : documentaciones) {
            model.add(entidadToModelo(documentacion));
        }

        return model;
    }

} 
