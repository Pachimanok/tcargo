package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.UbicacionConverter;
import com.tcargo.web.entidades.Contenedor;
import com.tcargo.web.entidades.TipoContenedor;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.PedidoRestModel;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.repositorios.ContenedorRepository;
import com.tcargo.web.repositorios.TipoContenedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final TipoContenedorRepository tipoContenedorRepository;
    private final UbicacionService ubicacionService;
    private final UbicacionConverter ubicacionConverter;

    public Contenedor buscarPorId(String id) {
        return contenedorRepository.findById(id).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Contenedor guardar(Contenedor contenedor) throws WebException {
        if (contenedor.getUbicacionRetiro() == null) {
            throw new WebException("El contenedor debe que tener una ubicacion de retiro.");
        }

        UbicacionModel retiro = ubicacionConverter.entidadToModelo(contenedor.getUbicacionRetiro());
        contenedor.setUbicacionRetiro(ubicacionService.guardar(retiro));

        if (contenedor.getUbicacionEntrega() != null && !contenedor.getUbicacionEntrega().getDireccion().isEmpty()) {
            UbicacionModel entrega = ubicacionConverter.entidadToModelo(contenedor.getUbicacionEntrega());
            contenedor.setUbicacionEntrega(ubicacionService.guardar(entrega));
        }

        contenedor.setModificacion(new Date());

        return contenedorRepository.save(contenedor);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public String guardar(PedidoRestModel pedido) throws WebException {
        Contenedor contenedor = new Contenedor();
        contenedor.setId(pedido.getIdContenedor());

        TipoContenedor tipoContenedor = tipoContenedorRepository.findById(pedido.getIdTipoContenedor()).orElseThrow(() -> new WebException("Falta id tipo contenedor"));
        contenedor.setTipoContenedor(tipoContenedor);
        contenedor.setDescripcion(pedido.getDescripcionContenedor());

        UbicacionModel retiro = new UbicacionModel();
        retiro.setDireccion(pedido.getDireccionRetiroContenedor());
        retiro.setLatitud(pedido.getLatitudRetiroContenedor());
        retiro.setLongitud(pedido.getLongitudRetiroContenedor());
        contenedor.setUbicacionRetiro(ubicacionService.guardar(retiro));

        if (pedido.getDireccionEntregaContenedor() != null) {
            UbicacionModel entrega = new UbicacionModel();
            entrega.setDireccion(pedido.getDireccionEntregaContenedor());
            entrega.setLatitud(pedido.getLatitudEntregaContenedor());
            entrega.setLongitud(pedido.getLongitudEntregaContenedor());
            contenedor.setUbicacionEntrega(ubicacionService.guardar(entrega));
        }

        contenedor.setModificacion(new Date());
        return contenedorRepository.save(contenedor).getId();
    }

    public Contenedor eliminar(Contenedor contenedor) throws WebException {
        Contenedor c = buscarPorId(contenedor.getId());
        if (c.getEliminado() == null) {
            c.setEliminado(new Date());
            c = contenedorRepository.save(c);
        } else {
            throw new WebException("El contenedor que desea eliminar se encuentra dado de baja.");
        }
        return c;
    }

}
