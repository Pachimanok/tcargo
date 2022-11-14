package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.DocumentacionConverter;
import com.tcargo.web.entidades.Documentacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.DocumentacionModel;
import com.tcargo.web.repositorios.DocumentacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class DocumentacionService {

    private final DocumentacionConverter documentacionConverter;
    private final DocumentacionRepository documentacionRepository;
    private final MessageSource messages;
    
    @Autowired
    public DocumentacionService(DocumentacionConverter documentacionConverter, DocumentacionRepository documentacionRepository,MessageSource messages) {
        this.documentacionConverter = documentacionConverter;
        this.documentacionRepository = documentacionRepository;
        this.messages = messages;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Documentacion guardar(DocumentacionModel model, List<String> archivos) throws WebException {
        Documentacion documentacion = documentacionConverter.modeloToEntidad(model);
        validarDocumentacion(documentacion);

        if (!archivos.isEmpty() && !archivos.get(0).equals("")) {
            for (String archivo : archivos) {
                documentacion.getArchivos().add(archivo);
            }
        }

        documentacion.setModificacion(new Date());
        return documentacionRepository.save(documentacion);
    }

    private void validarDocumentacion(Documentacion documentacion) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (documentacion.getEliminado() != null) {
            throw new WebException(messages.getMessage("documentacion.back.error.dada.baja", null, locale));
        }

        if (documentacion.getVencimiento() == null) {
            throw new WebException(messages.getMessage("documentacion.back.error.vencimiento", null, locale));
        }

        if (documentacion.getTipoDocumentacion() == null) {
            throw new WebException(messages.getMessage("documentacion.back.error.tipo.docuementacion", null, locale));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Documentacion eliminar(String id) throws WebException {
        Documentacion documentacion = documentacionRepository.getOne(id);
        validarDocumentacion(documentacion);
        documentacion.setEliminado(new Date());
        return documentacionRepository.save(documentacion);
    }

    public Page<Documentacion> listarActivos(Pageable paginable, String q) {
        return documentacionRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Documentacion> listarActivos(Pageable paginable) {
        return documentacionRepository.buscarActivos(paginable);
    }

    public List<Documentacion> listarActivos() {
        return documentacionRepository.buscarActivos();
    }

    public DocumentacionModel buscarModel(String id) {
        Documentacion documentacion = documentacionRepository.getOne(id);
        return documentacionConverter.entidadToModelo(documentacion);
    }

    public Documentacion buscarEntidad(String id) {
        return documentacionRepository.getOne(id);
    }

    public Page<Documentacion> buscarPorIdChofer(Pageable pageable, String idChofer, String q) {
        if (q != null && !q.isEmpty()) {
            return documentacionRepository.buscarPorIdChoferConQuery(pageable, idChofer, "%" + q + "%");
        }
        return documentacionRepository.buscarPorIdChofer(pageable, idChofer);
    }

    public Page<Documentacion> buscarPorIdVehiculo(Pageable pageable, String idVehiculo, String q) {
        if (q != null && !q.isEmpty()) {
            return documentacionRepository.buscarPorIdVehiculoConQuery(pageable, idVehiculo, "%" + q + "%");
        }
        return documentacionRepository.buscarPorIdVehiculo(pageable, idVehiculo);
    }

    public Page<Documentacion> buscarPorIdRemolque(Pageable pageable, String idRemolque, String q) {
        if (q != null && !q.isEmpty()) {
            return documentacionRepository.buscarPorIdRemolqueConQuery(pageable, idRemolque, "%" + q + "%");
        }
        return documentacionRepository.buscarPorIdRemolque(pageable, idRemolque);
    }

    public boolean esDocumentacionDeOtroTransportador(String idDocumentacion, String idTransportador) {
        Documentacion documentacion = buscarEntidad(idDocumentacion);
        if (documentacion.getChofer() == null && documentacion.getRemolque() == null && documentacion.getVehiculo() == null) {
            return true;
        }
        
        if(!idTransportador.equals(null)) {
            return !documentacion.getChofer().getTransportador().getId().equals(idTransportador) &&
                    !documentacion.getRemolque().getTransportador().getId().equals(idTransportador) &&
                    !documentacion.getVehiculo().getTransportador().getId().equals(idTransportador);
        }

        return false;
    }

}