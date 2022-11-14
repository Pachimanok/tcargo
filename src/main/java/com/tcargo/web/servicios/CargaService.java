package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.CargaConverter;
import com.tcargo.web.entidades.Carga;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CargaModel;
import com.tcargo.web.modelos.PedidoRestModel;
import com.tcargo.web.repositorios.CargaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class CargaService {

    private final CargaConverter cargaConverter;
    private final CargaRepository cargaRepository;

    @Autowired
    public CargaService(CargaConverter cargaConverter, CargaRepository cargaRepository) {
        this.cargaConverter = cargaConverter;
        this.cargaRepository = cargaRepository;
    }

    public Carga buscarPorId(String id) {
        return cargaRepository.findById(id).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Carga guardar(Carga carga) throws WebException {
        if (carga.isCargaCompleta() && carga.getCantidadCamiones() == null) {
            throw new WebException("La carga debe que tener una cantidad de camiones especificada.");
        }
        if (!carga.isCargaCompleta() && carga.getMetrosCubicos() == null) {
            throw new WebException("La carga debe tener los metros cubicos especificados.");
        }
        if (carga.getTipoEmbalaje() == null) {
            throw new WebException("La carga debe tener un tipo de embalaje.");
        }
        if (carga.getProducto() == null) {
            throw new WebException("La carga debe tener un producto.");
        }
        carga.setModificacion(new Date());

        return cargaRepository.save(carga);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Carga guardarModel(CargaModel model) throws WebException {
        validarModel(model);
        Carga carga = cargaConverter.modeloToEntidad(model);
        carga.setModificacion(new Date());
        return cargaRepository.save(carga);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public String guardar(PedidoRestModel pedido) throws WebException {
        CargaModel carga = new CargaModel();
        carga.setId(pedido.getIdCarga());
        carga.setDescripcion(pedido.getDescripcionCarga());
        carga.setCargaCompleta(pedido.isCargaCompleta());
        carga.setPeso(pedido.getPeso());
        carga.setTipoPeso(pedido.getTipoPeso());
        carga.setMetrosCubicos(pedido.getMetrosCubicos());
        carga.setCantidadCamiones(pedido.getCantidadCamiones());
        carga.setConCustodia(pedido.isConCustodia());
        carga.setSeguroCarga(pedido.isSeguroCarga());
        carga.setIndivisible(pedido.isIndivisible());
        carga.setIdProducto(pedido.getIdProducto());
        carga.setIdTipoCargas(pedido.getIdsTipoCarga());
        carga.setIdTipoEmbalaje(pedido.getIdTipoEmbalaje());
        carga.setIdTipoVehiculo(pedido.getIdTipoVehiculo());
        carga.setIdTipoRemolque(pedido.getIdTipoRemolque());
        return guardarModel(carga).getId();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Carga eliminar(String id) throws WebException {
        Carga carga = cargaRepository.getOne(id);
        if (carga.getEliminado() == null) {
            carga.setEliminado(new Date());
            carga = cargaRepository.save(carga);
        } else {
            throw new WebException("La carga que intenta eliminar ya se encuentra dado de baja.");
        }

        return carga;
    }

    private void validarModel(CargaModel model) throws WebException {
        if (model.getIdProducto() == null || model.getIdProducto().isEmpty()) {
            throw new WebException("La carga debe tener un producto");
        }
        if (model.getIdTipoCargas() == null || model.getIdTipoCargas().isEmpty()) {
            throw new WebException("La carga debe tener al menos un tipo de carga");
        }
        if (model.getIdTipoEmbalaje() == null || model.getIdTipoEmbalaje().isEmpty()) {
            throw new WebException("La carga debe tener tipo de embalaje");
        }
    }

}