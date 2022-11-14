package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Carga;
import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.modelos.CargaModel;
import com.tcargo.web.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CargaConverter extends Convertidor<CargaModel, Carga> {

    private final ProductoRepository productoRepository;
    private final TipoCargaRepository tipoCargaRepository;
    private final TipoEmbalajeRepository tipoEmbalajeRepository;
    private final TipoRemolqueRepository tipoRemolqueRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;

    @Override
    public Carga modeloToEntidad(CargaModel model) {
        Carga carga = new Carga();

        try {
            BeanUtils.copyProperties(model, carga);
            if (model.getIdProducto() != null) {
                carga.setProducto(productoRepository.getOne(model.getIdProducto()));
            }
            if (model.getIdTipoCargas() != null && model.getIdTipoCargas().isEmpty()) {
                for (String tipoCarga : model.getIdTipoCargas()) {
                    carga.getTipoCargas().add(tipoCargaRepository.getOne(tipoCarga));
                }
            }
            if (model.getIdTipoEmbalaje() != null) {
                carga.setTipoEmbalaje(tipoEmbalajeRepository.getOne(model.getIdTipoEmbalaje()));
            }
            if (model.getIdTipoRemolque() != null) {
                carga.setTipoRemolque(tipoRemolqueRepository.getOne(model.getIdTipoRemolque()));
            }
            if (model.getIdTipoVehiculo() != null) {
                carga.setTipoVehiculo(tipoVehiculoRepository.getOne(model.getIdTipoVehiculo()));
            }
        } catch (Exception e) {
            log.error("Error al convertir modelo CargaModel a entidad: " + e.getMessage());
        }

        return carga;
    }

    @Override
    public CargaModel entidadToModelo(Carga carga) {
        CargaModel model = new CargaModel();

        try {
            BeanUtils.copyProperties(carga, model);
            if (carga.getProducto() != null) {
                model.setIdProducto(carga.getProducto().getId());
            }
            if (carga.getTipoCargas() != null) {
                for (TipoCarga tipoCarga : carga.getTipoCargas()) {
                    model.getIdTipoCargas().add(tipoCarga.getId());
                }
            }
            if (carga.getTipoEmbalaje() != null) {
                model.setIdTipoEmbalaje(carga.getTipoEmbalaje().getId());
            }
            if (carga.getTipoRemolque() != null) {
                model.setIdTipoRemolque(carga.getTipoRemolque().getId());
            }
            if (carga.getTipoVehiculo() != null) {
                model.setIdTipoVehiculo(carga.getTipoVehiculo().getId());
            }
        } catch (Exception e) {
            log.error("Error al convertir entidad <Carga> a modelo: " + e.getMessage());
        }

        return model;
    }

}
