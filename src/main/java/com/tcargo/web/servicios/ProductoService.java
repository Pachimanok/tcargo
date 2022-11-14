package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ProductoConverter;
import com.tcargo.web.entidades.Producto;
import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ProductoModel;
import com.tcargo.web.repositorios.ProductoRepository;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductoService {

    private final ProductoConverter productoConverter;
    private final ProductoRepository productoRepository;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Producto guardar(ProductoModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIdPais() == null || model.getIdPais().equals("")) {
            throw new WebException(messages.getMessage("producto.back.error.pais", null, locale));
        }

        Producto producto = productoConverter.modeloToEntidad(model);

        if (producto.getEliminado() != null) {
            throw new WebException(messages.getMessage("producto.back.error.dad.baja", null, locale));
        }

        if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
            throw new WebException(messages.getMessage("producto.back.error.nombre", null, locale));
        }

        List<Producto> otros = productoRepository.buscarProductoPorNombre(producto.getNombre());
        for (Producto otro : otros) {
            if (otro != null && !otro.getId().equals(producto.getId()) && otro.getPais() == producto.getPais()) {
                throw new WebException(messages.getMessage("producto.back.error.nombre.repetido", null, locale));
            }
        }

        producto.setModificacion(new Date());

        return productoRepository.save(producto);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Producto eliminar(String id) throws WebException {
        Producto producto = productoRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (producto.getEliminado() == null) {
            producto.setEliminado(new Date());
            producto = productoRepository.save(producto);
        } else {
            throw new WebException(messages.getMessage("producto.back.error.dad.baja", null, locale));
        }

        return producto;
    }

    public Page<Producto> listarActivos(Pageable paginable, String q, boolean excel) {
        if (excel) {
            List<Producto> productos = productoRepository.buscarActivos(paginable.getSort(), "%" + q + "%");
            return new PageImpl<>(productos, paginable, productos.size());
        }
        return productoRepository.buscarActivos(paginable, "%" + q + "%");
    }

    public Page<Producto> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<Producto> productos = productoRepository.buscarActivos(paginable.getSort());
            return new PageImpl<>(productos, paginable, productos.size());
        }
        return productoRepository.buscarActivos(paginable);
    }

    public Page<Producto> listarActivosPorPais(Pageable paginable, String idPais, boolean excel) {
        if (excel) {
            List<Producto> productos = productoRepository.activosPorPais(idPais);
            return new PageImpl<>(productos, paginable, productos.size());
        }
        return productoRepository.buscarActivosPorPais(paginable, idPais);
    }

    public Page<Producto> listarActivosPorPais(Pageable paginable, String q, String idPais, boolean excel) {
        if (excel) {
            List<Producto> productos = productoRepository.buscarActivosPorPais(paginable.getSort(), "%" + q + "%", idPais);
            return new PageImpl<>(productos, paginable, productos.size());
        }
        return productoRepository.buscarActivosPorPais(paginable, "%" + q + "%", idPais);
    }

    public List<Producto> listarActivos() {
        return productoRepository.buscarActivos();
    }

    public List<ProductoModel> activosPorPaisModel(String idPais) {
        return productoConverter.entidadesToModelos(productoRepository.activosPorPais(idPais));
    }
    public List<ProductoModel> listarActivosModel() {
        List<Producto> resultado=productoRepository.buscarActivos();
        List<Producto> filtrado=resultado.stream().filter(distinctByKey(Producto::getNombre)).collect(Collectors.toList());
        return productoConverter.entidadesToModelos(filtrado);
    }

    public ProductoModel buscar(String id) {
        Producto producto = productoRepository.getOne(id);
        return productoConverter.entidadToModelo(producto);
    }
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}