package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Producto;
import com.tcargo.web.modelos.ProductoModel;
import com.tcargo.web.repositorios.PaisRepository;
import com.tcargo.web.repositorios.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductoConverter extends Convertidor<ProductoModel, Producto> {

    private final ProductoRepository productoRepository;
    private final PaisRepository paisRepository;

    public ProductoModel entidadToModelo(Producto producto) {
        ProductoModel model = new ProductoModel();

        try {
            BeanUtils.copyProperties(producto, model);
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo de producto", e);
        }

        if (producto.getPais() != null) {
            model.setIdPais(producto.getPais().getId());
        }

        return model;
    }

    public Producto modeloToEntidad(ProductoModel model) {
        Producto producto = new Producto();

        if (model.getId() != null && !model.getId().isEmpty()) {
            producto = productoRepository.getOne(model.getId());
        }

        try {
            BeanUtils.copyProperties(model, producto);
        } catch (Exception e) {
            log.error("Error al convertir el modelo de producto en entidad", e);
        }

        if (model.getIdPais() != null) {
            producto.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return producto;
    }

    public List<ProductoModel> entidadesToModelos(List<Producto> productos) {
        List<ProductoModel> model = new ArrayList<>();

        for (Producto producto : productos) {
            model.add(entidadToModelo(producto));
        }

        return model;
    }

} 
