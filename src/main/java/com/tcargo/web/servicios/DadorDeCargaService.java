package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.DadorDeCargaConverter;
import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.entidades.Ubicacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.DadorDeCargaModel;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.repositorios.DadorDeCargaRepository;
import com.tcargo.web.repositorios.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DadorDeCargaService {

    private final DadorDeCargaConverter dadorDeCargaConverter;
    private final DadorDeCargaRepository dadorDeCargaRepository;
    private final UbicacionService ubicacionService;
    private final UbicacionRepository ubicacionRepository;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public DadorDeCarga guardar(DadorDeCargaModel model, UbicacionModel ubicacion) throws WebException {

        Ubicacion u;
        if (ubicacion.getIdProvisiorio() != null && !ubicacion.getIdProvisiorio().isEmpty()) {
            ubicacion.setId(ubicacion.getIdProvisiorio());
            ubicacionService.findById(ubicacion.getId());
        }
        u = ubicacionService.guardar(ubicacion);
        if (u != null) {
            model.setIdUbicacion(u.getId());
        }
        DadorDeCarga dadorDeCarga = dadorDeCargaConverter.modeloToEntidad(model);
        return verificarYGuardar(dadorDeCarga);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public void guardarDesdeEdicion(DadorDeCargaModel model) throws WebException {

        DadorDeCarga dadorDeCarga = dadorDeCargaConverter.modeloToEntidadSinUbicacion(model);

        verificarYGuardar(dadorDeCarga);
    }

    private DadorDeCarga verificarYGuardar(DadorDeCarga dador) throws WebException {
        if (dador.getEliminado() != null) {
            throw new WebException("El dador que intenta modificar se encuentra dada de baja.");
        }

        if (dador.getNombre() == null || dador.getNombre().isEmpty()) {
            throw new WebException("El nombre del dador no puede ser vacío.");
        }

        if (dador.getRazonSocial() == null || dador.getRazonSocial().isEmpty()) {
            throw new WebException("La razon social no puede estar ser vacía.");
        }

        if (dador.getUsuario() == null) {
            throw new WebException("El dadorDeCarga debe estar asociando a un usuario.");
        }

        dador.setModificacion(new Date());

        return dadorDeCargaRepository.save(dador);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public DadorDeCarga eliminar(String id, String idUbicacion) throws WebException {
        DadorDeCarga dadorDeCarga = dadorDeCargaRepository.getOne(id);
        Ubicacion ubicacion = ubicacionService.findById(idUbicacion);
        if (dadorDeCarga.getEliminado() == null) {
            dadorDeCarga.setEliminado(new Date());
            ubicacion.setEliminado(new Date());
            ubicacionRepository.save(ubicacion);
            dadorDeCarga = dadorDeCargaRepository.save(dadorDeCarga);
        } else {
            throw new WebException("La dadorDeCarga que intenta eliminar ya se encuentra dado de baja.");
        }

        return dadorDeCarga;
    }

    public Page<DadorDeCarga> listarActivos(Pageable paginable, String q) {
        return dadorDeCargaRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<DadorDeCarga> listarActivos(Pageable paginable) {
        return dadorDeCargaRepository.buscarActivos(paginable);
    }

    public List<DadorDeCarga> listarActivos() {
        return dadorDeCargaRepository.buscarActivos();
    }

    public List<DadorDeCargaModel> listarActivosModel() {
        List<DadorDeCarga> dadorDeCargaes = dadorDeCargaRepository.buscarActivos();
        return dadorDeCargaConverter.entidadesToModelos(dadorDeCargaes);
    }

    public DadorDeCarga buscarPorIdUsuario(String id) {
        return dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(id);
    }

    public DadorDeCargaModel bucarPorIdUsuarioModel(String id) {
        DadorDeCarga dc = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(id);
        if(dc != null) {
        	return dadorDeCargaConverter.entidadToModelo(dc);
        }else {
        	return null;
        }
    }

    public DadorDeCargaModel buscar(String id) {
        DadorDeCarga dadorDeCarga = dadorDeCargaRepository.getOne(id);
        return dadorDeCargaConverter.entidadToModelo(dadorDeCarga);
    }

}