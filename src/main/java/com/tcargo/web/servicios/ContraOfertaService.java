package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ContraOfertaConverter;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.EstadoContraOferta;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ContraOfertaModel;
import com.tcargo.web.modelos.busqueda.BusquedaHistorialModel;
import com.tcargo.web.repositorios.ContraOfertaRepository;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ContraOfertaService {

    private final ContraOfertaConverter contraOfertaConverter;
    private final ContraOfertaRepository contraOfertaRepository;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final MessageSource messages;

    private static final String DADA_DE_BAJA = "contra.oferta.back.error.dada.baja";

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public ContraOferta guardar(ContraOfertaModel model) throws WebException {
        ContraOferta contraOferta = contraOfertaConverter.modeloToEntidad(model);
        final Locale locale = LocaleContextHolder.getLocale();


        if (contraOferta.getEliminado() != null) {
            throw new WebException(messages.getMessage(DADA_DE_BAJA, null, locale));
        }
        if (contraOferta.getValor() == null) {
            throw new WebException(messages.getMessage("contra.oferta.back.error.valor", null, locale));
        }
        if (contraOferta.getDador() == null) {
            throw new WebException(messages.getMessage("contra.oferta.back.error.dador", null, locale));
        }
        if (contraOferta.getTransportador() == null) {
            throw new WebException(messages.getMessage("contra.oferta.back.error.transp", null, locale));
        }

        contraOferta.setModificacion(new Date());
        contraOferta.setNotificacion(EstadoNotificacion.CREADO);
        return contraOfertaRepository.save(contraOferta);
    }

    public void guardarFinal(ContraOferta contraOferta, String idCreador) {
        ContraOferta co = new ContraOferta();
        co.setCreador(usuarioService.buscar(idCreador));
        co.setTransportador(contraOferta.getTransportador());
        co.setDador(contraOferta.getDador());
        co.setIsFinal(true);
        co.setPedido(contraOferta.getPedido());
        pedidoService.verificarTieneContraOfertas(contraOferta.getPedido());
        contraOferta.setNotificacion(EstadoNotificacion.CREADO);
        co.setEstado(EstadoContraOferta.ACEPTADO);
        co.setModificacion(new Date());
        contraOfertaRepository.save(co);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public void copiarYguardar(ContraOferta co, Long idPedido) {
        ContraOferta nueva = new ContraOferta();

        if (co.getVehiculo() != null) {
            nueva.setVehiculo(co.getVehiculo());
        }
        if (co.getComentarios() != null) {
            nueva.setComentarios(co.getComentarios());
        }
        if (co.getRemolque() != null) {
            nueva.setRemolque(co.getRemolque());
        }
        if (co.getDiasLibres() != null) {
            nueva.setDiasLibres(co.getDiasLibres());
        }
        if (co.getValor() != null) {
            nueva.setValor(co.getValor());
        }
        if (co.getCreador() != null) {
            nueva.setCreador(co.getCreador());
        }
        if (co.getTransportador() != null) {
            nueva.setTransportador(co.getTransportador());
        }
        if (co.getDador() != null) {
            nueva.setDador(co.getDador());
        }
        if (!co.getRequisitosDeOferta().isEmpty()) {
            nueva.setRequisitosDeOferta(new ArrayList<>());
            for (TipoRequisitoOferta requisito : co.getRequisitosDeOferta()) {
                nueva.getRequisitosDeOferta().add(requisito);
            }
        }

        nueva.setPedido(pedidoService.buscarPorId(idPedido));
        nueva.setModificacion(new Date());
        nueva.setNotificacion(EstadoNotificacion.CREADO);
        nueva.setEstado(EstadoContraOferta.PENDIENTE);
        nueva.setId(null);
        contraOfertaRepository.save(nueva);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public ContraOferta eliminar(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        ContraOferta contraOferta = contraOfertaRepository.getOne(id);
        if (contraOferta.getEliminado() == null) {
            contraOferta.setEliminado(new Date());
            contraOferta.setNotificacion(EstadoNotificacion.RECHAZADO);
            contraOferta = contraOfertaRepository.save(contraOferta);
        } else {
            throw new WebException(messages.getMessage(DADA_DE_BAJA, null, locale));
        }
        pedidoService.verificarTieneContraOfertas(contraOferta.getPedido());
        return contraOferta;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public ContraOferta aceptarContraoferta(String id) throws WebException {
        ContraOferta contraOferta = contraOfertaRepository.getOne(id);
        final Locale locale = LocaleContextHolder.getLocale();

        if (contraOferta.getEliminado() == null) {
            contraOferta.setEstado(EstadoContraOferta.ACEPTADO);
            contraOferta.setNotificacion(EstadoNotificacion.CREADO);
            contraOferta = contraOfertaRepository.save(contraOferta);
        } else {
            throw new WebException(messages.getMessage(DADA_DE_BAJA, null, locale));
        }

        return contraOferta;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public ContraOferta desestimarContraoferta(String id) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        ContraOferta contraOferta = contraOfertaRepository.getOne(id);
        if (contraOferta.getEliminado() == null) {
            contraOferta.setEstado(EstadoContraOferta.RECHAZADO);
            contraOferta.setNotificacion(EstadoNotificacion.RECHAZADO);
            contraOferta = contraOfertaRepository.save(contraOferta);
        } else {
            throw new WebException(messages.getMessage(DADA_DE_BAJA, null, locale));
        }

        return contraOferta;
    }

    public Page<ContraOferta> listarActivos(Pageable paginable, boolean excel) {
        if (excel) {
            List<ContraOferta> contraOfertas = contraOfertaRepository.buscarActivos();
            return new PageImpl<>(contraOfertas, paginable, contraOfertas.size());
        }
        return contraOfertaRepository.buscarActivos(paginable);
    }

    public List<ContraOferta> listarPorTransportadorIdList(Transportador transportador) {
        return contraOfertaRepository.findByTransportadorAndEliminadoIsNull(transportador);
    }

    public List<ContraOferta> buscarPorIdPedidoYIdTransportador(Long idPedido, String idTransportador) {
        return contraOfertaRepository.buscarPorIdPedidoYIdTransportador(idPedido, idTransportador);
    }

    //    LISTAR CONTRAOFERTAS: ESTADO PENDIENTE POR TRANSPORTADOR NOTIFICACION CREADA
    public List<ContraOferta> listarPorTransportadorNotifCreada(Transportador transportador) {
        return contraOfertaRepository.findByTransportadorAndNotificacionAndCreadorNotLikeAndEstadoAndEliminadoIsNull(
                transportador, EstadoNotificacion.CREADO, transportador.getUsuario(), EstadoContraOferta.PENDIENTE);
    }

    //  LISTAR CONTRAOFERTAS: ESTADO ACEPTADO POR TRANSPORTADOR NOTIFICACION CREADA
    public List<ContraOferta> listarPorTransportadorNotifCreadaEstadoAceptado(Transportador transportador) {
        return contraOfertaRepository.findByTransportadorAndNotificacionAndCreadorAndEstadoAndEliminadoIsNull(
                transportador, EstadoNotificacion.CREADO, transportador.getUsuario(), EstadoContraOferta.ACEPTADO);
    }

    //  LISTAR CONTRAOFERTAS: ACEPTADO PENDIENTE POR DADOR NOTIFICACION CREADA
    public List<ContraOferta> listarPorDadorNotifCreadaEstadoAceptado(Usuario dador) {
        return contraOfertaRepository.findByDadorAndNotificacionAndCreadorAndEstadoAndEliminadoIsNull(
                dador, EstadoNotificacion.CREADO, dador, EstadoContraOferta.ACEPTADO);
    }

    //  LISTAR CONTRAOFERTAS: ESTADO PENDIENTE POR DADOR NOTIFICACION CREADA
    public List<ContraOferta> listarPorDadorNotifCreada(Usuario dador) {
        return contraOfertaRepository.findByDadorAndNotificacionAndCreadorNotLikeAndEstadoAndEliminadoIsNull(
                dador, EstadoNotificacion.CREADO, dador, EstadoContraOferta.PENDIENTE);
    }

    //  LISTAR CONTRAOFERTAS: POR DADOR ESTADO PENDIENTE
    public List<ContraOferta> listarPorDadorPendiente(Usuario dador) {
        return contraOfertaRepository.findByDadorAndCreadorNotLikeAndEstadoAndEliminadoIsNullAndPedidoAsignadoATransportadorIsNull(
                dador, dador, EstadoContraOferta.PENDIENTE);
    }

    //  LISTAR CONTRAOFERTAS: ESTADO PENDIENTE
    public List<ContraOferta> listarEstadoPendientes() {
        return contraOfertaRepository.findDistinctPedidoIdByEstadoAndEliminadoIsNull(EstadoContraOferta.PENDIENTE);
    }

    public List<ContraOfertaModel> listarPorPedidoList(Pedido pedido) {
        List<ContraOfertaModel> contraOfertas = new ArrayList<>();
        for (ContraOferta x : contraOfertaRepository.buscarContraOfertasPorIdPedido(pedido.getId())) {
            contraOfertas.add(contraOfertaConverter.entidadToModelo(x));
        }
        return contraOfertas;
    }

    public Page<ContraOferta> busqueda(Pageable pageable, Long idPedido, Double valor,String vehiculo, String q, boolean excel) {
        return contraOfertaRepository.buscarPorCriterios(pageable, idPedido, valor, vehiculo, q, excel);
    }

    public ContraOfertaModel buscar(String id) {
        ContraOferta contraOferta = contraOfertaRepository.getOne(id);
        return contraOfertaConverter.entidadToModelo(contraOferta);
    }

    public List<ContraOferta> buscarPorIdPedidoEstadoYDador(Pedido pedido, Usuario dador) {
        return contraOfertaRepository.findByPedidoAndEstadoAndDadorAndCreadorNotLikeAndEliminadoIsNull(
                pedido, EstadoContraOferta.PENDIENTE, dador, dador);
    }

    public List<ContraOferta> buscarPorIdPedidoEstadoYTransportador(Pedido pedido, Transportador transportador) {
        return contraOfertaRepository.findByPedidoAndEstadoAndTransportadorAndCreadorNotLikeAndEliminadoIsNull(
                pedido, EstadoContraOferta.PENDIENTE, transportador, transportador.getUsuario());
    }

    public Page<ContraOfertaModel> buscarPorTransportador(Pageable pageable, String idTransportador, BusquedaHistorialModel busqueda) {
        Page<ContraOferta> page = contraOfertaRepository.buscarPorTransportador(pageable, idTransportador, busqueda);
        List<ContraOfertaModel> aux = new ArrayList<>(contraOfertaConverter.entidadesToModelos(page.getContent()));
        return new PageImpl<>(aux, pageable, page.getTotalElements());
    }

    public Page<ContraOfertaModel> buscarPorDador(Pageable pageable, String idDador, BusquedaHistorialModel busqueda) {
        Page<ContraOferta> page = contraOfertaRepository.buscarPorDador(pageable, idDador, busqueda);
        List<ContraOfertaModel> aux = new ArrayList<>(contraOfertaConverter.entidadesToModelos(page.getContent()));
        return new PageImpl<>(aux, pageable, page.getTotalElements());
    }

}