package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.PedidoConverter;
import com.tcargo.web.convertidores.UbicacionConverter;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.EstadoContraOferta;
import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.PedidoChoferModel;
import com.tcargo.web.modelos.PedidoModel;
import com.tcargo.web.modelos.PedidoRestModel;
import com.tcargo.web.modelos.UbicacionModel;
import com.tcargo.web.modelos.busqueda.BusquedaPedidoModel;
import com.tcargo.web.repositorios.ContraOfertaRepository;
import com.tcargo.web.repositorios.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PedidoService {

    private final CargaService cargaService;
    private final CoincidenciaService coincidenciaService;
    private final ContenedorService contenedorService;
    private final PedidoConverter pedidoConverter;
    private final PedidoRepository pedidoRepository;
    private final PeriodoDeCargaService periodoDeCargaService;
    private final UbicacionConverter ubicacionConverter;
    private final UbicacionService ubicacionService;
    private final ContraOfertaRepository contraOfertaRepository;
    private final FirebaseService firebaseService;
    private final MessageSource messages;
    private final ViajeService viajeService;

    public Page<Pedido> buscarPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, Boolean matcheado, boolean excel) {
        return pedidoRepository.buscarPorCriterios(pageable, busqueda, matcheado, excel);
    }

    public Page<Pedido> buscarPorCriteriosParaTransportador(Pageable pageable, BusquedaPedidoModel busqueda, List<ContraOferta> contraOfertas, boolean excel) {
        return pedidoRepository.buscarPorCriteriosParaTransportador(pageable, busqueda, contraOfertas, excel);
    }

    public Page<Pedido> buscarPorCriteriosParaDador(Pageable pageable, BusquedaPedidoModel busqueda, Usuario usuario, boolean excel) {
        return pedidoRepository.buscarPorCriteriosParaDador(pageable, busqueda, usuario, excel);
    }

    public Page<Pedido> buscarOfertadosPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, List<Long> idsPedidos) {
        return pedidoRepository.buscarOfertadosPorCriterios(pageable, busqueda, idsPedidos, false);
    }

    public Page<Pedido> buscarMatcheadosPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, List<Long> idsPedidos) {
        return pedidoRepository.buscarMatcheadosPorCriterios(pageable, busqueda, idsPedidos, false);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public Pedido copiarYGuardarNuevo(Long id) {
        Pedido p = buscarPorId(id);
        Pedido nuevo = new Pedido();
        BeanUtils.copyProperties(p, nuevo);
        nuevo.setCoincidencias(new ArrayList<>(p.getCoincidencias()));
        nuevo.setRequisitos(new ArrayList<>(p.getRequisitos()));
        nuevo.setTipoRequisitos(new ArrayList<>(p.getTipoRequisitos()));
        nuevo.setId(null);
        pedidoRepository.save(nuevo);
        return nuevo;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Pedido guardar(Pedido pedido) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (pedido.getId() != null) {
            Pedido x = buscarPorId(pedido.getId());
            if (x.getCarga().getMetrosCubicos() != null && pedido.getCarga().isCargaCompleta()) {
                pedido.getCarga().setMetrosCubicos(null);
            }
        }

        if (pedido.getCarga().getTipoCargas().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.tipo.carga", null, locale));
        }

        if (pedido.getTipoDeViaje() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.tipo.viaje", null, locale));
        }

        if (pedido.getUbicacionDesde() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.ubicacion.inicio", null, locale));
        }

        if (pedido.getUbicacionHasta() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.ubicacion.final", null, locale));
        }

        if (pedido.getPeriodoDeCarga() == null || pedido.getPeriodoDeDescarga() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.periodo.carga", null, locale));
        }

        if (!pedido.isRecibirOfertas() && pedido.getValor() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.valor", null, locale));
        }

        if (pedido.getMoneda() == null && !pedido.isRecibirOfertas()) {
            throw new WebException(messages.getMessage("pedido.back.error.tipo.moneda", null, locale));
        }

        if (pedido.getTipoDeViaje() == TipoDeViaje.INTERNACIONAL && pedido.getContenedor().getUbicacionRetiro() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.ubicacion.retiro", null, locale));
        }

        if (pedido.getTipoRequisitos() == null || pedido.getTipoRequisitos().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.tipo.requisito", null, locale));
        }

        if (pedido.getTipoDeViaje() == TipoDeViaje.INTERNACIONAL) {
            pedido.getTipoRequisitos().removeIf(tr -> tr.getTipoDeViaje().equals(TipoDeViaje.NACIONAL));
        } else if (pedido.getTipoDeViaje() == TipoDeViaje.NACIONAL) {
            pedido.getTipoRequisitos().removeIf(tr -> tr.getTipoDeViaje().equals(TipoDeViaje.INTERNACIONAL));
        }

        if (pedido.getContenedor() != null && pedido.getContenedor().getTipoContenedor() != null && !pedido.getContenedor().getTipoContenedor().getId().isEmpty()) {
            pedido.setContenedor(contenedorService.guardar(pedido.getContenedor()));
        } else {
            pedido.setContenedor(null);
        }
        if (pedido.getCoincidencias() == null) {
            pedido.setCoincidencias(new ArrayList<>());
        }
        if (pedido.getRequisitos() == null) {
            pedido.setRequisitos(new ArrayList<>());
        }
        if (pedido.getEstadoPedido() == null || EstadoPedido.estadosDisponible().contains(pedido.getEstadoPedido())) {
            LocalDateTime pasadoManiana = LocalDateTime.now().plusDays(2L);
            LocalDateTime finPeriodoCarga = LocalDateTime.parse(pedido.getPeriodoDeCarga().getFinalizacion());
            pedido.setEstadoPedido(EstadoPedido.DISPONIBLE);
            if (finPeriodoCarga.isBefore(pasadoManiana)) {
                pedido.setEstadoPedido(EstadoPedido.PRIORIDAD);
            }
        }

        pedido.setCarga(cargaService.guardar(pedido.getCarga()));
        pedido.setUbicacionDesde(ubicacionService.guardar(ubicacionConverter.entidadToModelo(pedido.getUbicacionDesde())));
        pedido.setUbicacionHasta(ubicacionService.guardar(ubicacionConverter.entidadToModelo(pedido.getUbicacionHasta())));
        pedido.setPeriodoDeCarga(periodoDeCargaService.guardar(pedido.getPeriodoDeCarga()));
        pedido.setPeriodoDeDescarga(periodoDeCargaService.guardar(pedido.getPeriodoDeDescarga()));
        pedido.setModificacion(new Date());

        checkearContraOfertas(pedidoRepository.save(pedido));
        return pedido;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Pedido guardarModel(PedidoModel model) throws WebException {
        Pedido pedido = pedidoConverter.modeloToEntidad(model);
        int camiones = pedido.getCarga().getCantidadCamiones() - 1; // Se resta uno para que no se guarde uno de más si es grupo
        pedido.getCarga().setCantidadCamiones(1);
        cargaService.guardar(pedido.getCarga());

        if (model.getEstadoPedido() == null || EstadoPedido.estadosDisponible().contains(model.getEstadoPedido())) {
            LocalDateTime pasadoManiana = LocalDateTime.now().plusDays(2L);
            PeriodoDeCarga periodoDeCarga = periodoDeCargaService.buscarPorId(model.getIdPeriodoDeCarga());
            LocalDateTime finPeriodoCarga = LocalDateTime.parse(periodoDeCarga.getFinalizacion().replace(" ", "T"));
            pedido.setEstadoPedido(EstadoPedido.DISPONIBLE);
            if (finPeriodoCarga.isBefore(pasadoManiana)) {
                pedido.setEstadoPedido(EstadoPedido.PRIORIDAD);
            }
        }

        pedido.setFinalizado(model.isFinalizado());

        pedido.setModificacion(new Date());
        Pedido guardado = pedidoRepository.save(pedido);

        if (model.getIsGrupo()) {
            for (int i = 0; i < camiones; i++) {
                copiarYGuardarNuevo(guardado.getId());
            }
        }

        return guardado;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Long guardarDesdeMobile(PedidoRestModel model) throws WebException {
        PedidoModel pedido = new PedidoModel();
        pedido.setIdCarga(cargaService.guardar(model));
        pedido.setIsGrupo(model.getCantidadCamiones() > 1);
        pedido.setIdContenedor(contenedorService.guardar(model));

        UbicacionModel desde = new UbicacionModel();
        desde.setDireccion(model.getDireccionDesde());
        desde.setLatitud(model.getLatitudDesde());
        desde.setLongitud(model.getLongitudDesde());
        pedido.setIdUbicacionDesde(ubicacionService.guardar(desde).getId());

        UbicacionModel hasta = new UbicacionModel();
        hasta.setDireccion(model.getDireccionHasta());
        hasta.setLatitud(model.getLatitudHasta());
        hasta.setLongitud(model.getLongitudHasta());
        pedido.setIdUbicacionHasta(ubicacionService.guardar(hasta).getId());

        PeriodoDeCarga periodoDeCarga = new PeriodoDeCarga();
        periodoDeCarga.setId(model.getIdPeriodoCarga());
        periodoDeCarga.setInicio(model.getInicioPeriodoCarga());
        periodoDeCarga.setFinalizacion(model.getFinPeriodoCarga());
        periodoDeCarga.setCargaNocturna(model.isCargaNocturnaPeriodoCarga());
        periodoDeCarga.setDescripcion(model.getDescripcionPeriodoCarga());
        pedido.setIdPeriodoDeCarga(periodoDeCargaService.guardar(periodoDeCarga).getId());

        PeriodoDeCarga periodoDeDescarga = new PeriodoDeCarga();
        periodoDeDescarga.setId(model.getIdPeriodoDescarga());
        periodoDeDescarga.setInicio(model.getInicioPeriodoDescarga());
        periodoDeDescarga.setFinalizacion(model.getFinPeriodoDescarga());
        periodoDeDescarga.setCargaNocturna(model.isCargaNocturnaPeriodoDescarga());
        periodoDeDescarga.setDescripcion(model.getDescripcionPeriodoDescarga());
        pedido.setIdPeriodoDeDescarga(periodoDeCargaService.guardar(periodoDeDescarga).getId());

        pedido.setId(model.getIdPedido());
        pedido.setIdDador(model.getIdDador());
        pedido.setIdRequisitos(model.getIdsRequisitos());
        pedido.setRecibirOfertas(model.isRecibirOfertas());
        pedido.setIdMoneda(model.getIdMoneda());
        pedido.setValor(model.getValor());
        pedido.setDescripcion(model.getCondicionPago());
        pedido.setPagaAlTransportista(model.isPagaAlTransportista());
        pedido.setTipoDeViaje(model.getTipoDeViaje());
        pedido.setAsignadoATransportador(model.isAsignadoATransportador());
        // TODO eliminar el hardcodeo de este campo
        pedido.setKmTotales("123");

        if (model.getTipoDeViaje().equals(TipoDeViaje.INTERNACIONAL)) {
            pedido.setTipoInternacional(model.getTipoInternacional());
        }

        validarPedido(pedido);

        return guardarModel(pedido).getId();
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Pedido eliminar(Pedido p) throws WebException {
        Pedido pedido;
        final Locale locale = LocaleContextHolder.getLocale();

        try {
            pedido = buscarPorId(p.getId());

        } catch (NullPointerException e) {
            throw new WebException(messages.getMessage("pedido.back.error.eliminar", null, locale));
        }

        if (pedido.getEliminado() == null) {
            pedido.setEstadoPedido(EstadoPedido.CANCELADO);
            if (!pedido.isGrupo()) {
                pedido.setEliminado(new Date());
                cargaService.eliminar(pedido.getCarga().getId());
                ubicacionService.eliminar(pedido.getUbicacionDesde().getId());
                ubicacionService.eliminar(pedido.getUbicacionHasta().getId());
                periodoDeCargaService.eliminar(pedido.getPeriodoDeCarga().getId());
                periodoDeCargaService.eliminar(pedido.getPeriodoDeDescarga().getId());
                if (pedido.getContenedor() != null) {
                    contenedorService.eliminar(pedido.getContenedor());
                }
                pedidoRepository.save(pedido);
            } else {
                pedido.setEliminado(new Date());
                pedidoRepository.save(pedido);

                List<Pedido> pe = pedidoRepository.buscarActivosPorIdCarga(pedido.getCarga().getId());
                if (pe.size() == 1) {
                    pe.get(0).setGrupo(false);
                }
            }

        } else {
            throw new WebException(messages.getMessage("pedido.back.error.al.inesperado", null, locale));
        }

        return pedido;
    }

    @Transactional
    public void eliminarGrupo(List<Pedido> pedidos) throws WebException {
        for (int i = 0; i < pedidos.size(); i++) {
            if (i == 0) {
                pedidos.get(i).setCarga(cargaService.eliminar(pedidos.get(i).getCarga().getId()));
                pedidos.get(i).setUbicacionDesde(ubicacionService.eliminar(pedidos.get(i).getUbicacionDesde().getId()));
                pedidos.get(i).setUbicacionHasta(ubicacionService.eliminar(pedidos.get(i).getUbicacionHasta().getId()));
                pedidos.get(i).setPeriodoDeCarga(periodoDeCargaService.eliminar(pedidos.get(i).getPeriodoDeCarga().getId()));
                pedidos.get(i).setPeriodoDeDescarga(periodoDeCargaService.eliminar(pedidos.get(i).getPeriodoDeDescarga().getId()));
                if (pedidos.get(i).getContenedor() != null) {
                    pedidos.get(i).setContenedor(contenedorService.eliminar(pedidos.get(i).getContenedor()));
                }
            }
            pedidos.get(i).setEliminado(new Date());
            pedidos.get(i).setEstadoPedido(EstadoPedido.CANCELADO);
            pedidoRepository.save(pedidos.get(i));
        }
    }

    public Page<Pedido> listarActivos(Pageable pageable) {
        return pedidoRepository.buscarActivos(pageable);
    }

    public Page<Pedido> buscarPorDadorMatcheadas(Pageable page, String idDador, boolean excel) {
        if (excel) {
            List<Pedido> pedidos = pedidoRepository.buscarMatcheadas(page.getSort(), idDador);
            return new PageImpl<>(pedidos, page, pedidos.size());
        }
        return pedidoRepository.buscarMatcheadas(page, idDador);
    }

    public List<Pedido> buscarPorIdCarga(String idCarga) {
        return pedidoRepository.buscarActivosPorIdCarga(idCarga);
    }

    public Page<Pedido> buscarPorIdCargaPageEliminarMatcheados(Pageable pageable, String idCarga) {
        List<Pedido> pedidos = pedidoRepository.buscarActivosPorIdCarga(idCarga);

        if (pedidos != null) {
            for (int i = pedidos.size() - 1; i >= 0; i--) {
                List<Coincidencia> c = coincidenciaService.buscarPorPedido(pedidos.get(i).getId());
                if (c != null && !c.isEmpty()) {
                    pedidos.remove(pedidos.get(i));
                }
            }
        } else {
            pedidos = new ArrayList<>();
        }

        return new PageImpl<>(pedidos, pageable, pedidos.size());
    }

    public List<Pedido> porUbicacionDesde(Ubicacion u, String partidaViaje, String llegadaViaje) {
        return pedidoRepository.buscarParaGeoQuery(u, EstadoPedido.estadosDisponible(), partidaViaje, llegadaViaje);
    }

    public PedidoModel pedidoModelParaMatchForm(Long idPedido) {
        return pedidoConverter.pedidoModelParaMatchForm(pedidoRepository.getOne(idPedido));
    }

    private void validarPedido(PedidoModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        if (model.getIsGrupo() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.is.grupo", null, locale));
        }
        if (model.getAsignadoATransportador() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.asignar.transp", null, locale));
        }
        if (model.getPagaAlTransportista() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.pagar.trnsp", null, locale));
        }
        if (model.getRecibirOfertas() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.recibir.ofertas", null, locale));
        }
        if (model.getIdCarga() == null || model.getIdCarga().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.id.carga", null, locale));
        }
        if (model.getIdPeriodoDeCarga() == null || model.getIdPeriodoDeCarga().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.id.periodo.carga", null, locale));
        }
        if (model.getIdPeriodoDeDescarga() == null || model.getIdPeriodoDeDescarga().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.id.periodo.des", null, locale));
        }
        if (model.getIdUbicacionDesde() == null || model.getIdUbicacionDesde().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.id.ubicacion.desde", null, locale));
        }
        if (model.getIdUbicacionHasta() == null || model.getIdUbicacionHasta().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.id.ubicacion.hasta", null, locale));
        }
        if (model.getTipoDeViaje().equals(TipoDeViaje.INTERNACIONAL) && model.getTipoInternacional() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.tipo.inter", null, locale));
        }
        if (model.getIdMoneda() == null || model.getIdMoneda().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.tipo.moneda.rest", null, locale));
        }
        if (model.getKmTotales() == null || model.getKmTotales().isEmpty()) {
            throw new WebException(messages.getMessage("pedido.back.error.id.kms.totatales", null, locale));
        }
        if (model.getValor() == null) {
            throw new WebException(messages.getMessage("pedido.back.error.id.valor", null, locale));
        }
        if (model.getValor() < 0) {
            throw new WebException(messages.getMessage("pedido.back.error.id.valor.negativo", null, locale));
        }
    }

    public Pedido actualizarEstado(Long idPedido, EstadoPedido estado) {
        Pedido p = pedidoRepository.getOne(idPedido);
        p.setEstadoPedido(estado);
        return pedidoRepository.save(p);
    }

    public void asignarTransportadorTrue(Long idPedido) {
        Pedido p = pedidoRepository.getOne(idPedido);
        p.setAsignadoATransportador(true);
        p.setEstadoPedido(EstadoPedido.ASIGNADO);
        pedidoRepository.save(p);
    }

    public void checkearContraOfertas(Pedido pedido) {
        List<ContraOferta> contraOfertas = contraOfertaRepository.findByPedidoAndEstadoAndDadorAndCreadorNotLikeAndEliminadoIsNull(pedido, EstadoContraOferta.PENDIENTE, pedido.getDador(), pedido.getDador());
        if (contraOfertas != null && !contraOfertas.isEmpty()) {
            for (ContraOferta co : contraOfertas) {
                // envio de notificacion al dador del viaje y transportador, aviso que se generó
                // match.
                new Thread(() -> firebaseService.pedidoEditado(co.getCreador().getId(), pedido.getId())).start();
            }
        }
    }

    public List<PedidoChoferModel> armarPedidoChofer(List<Pedido> pedidos) {
        List<PedidoChoferModel> pedidoChoferModelList = new ArrayList<>();
        for (Pedido pedido : pedidos) {
            PedidoChoferModel pedidoChoferModel = new PedidoChoferModel();
            Viaje viaje = viajeService.buscarPorCarga(pedido.getCarga());
            pedidoChoferModel.setEstadoViaje(viaje.getEstadoViaje());
            pedidoChoferModel.setPedido(pedido);
            pedidoChoferModelList.add(pedidoChoferModel);
        }
        return pedidoChoferModelList;
    }

    public void formatearFechasPedidos(List<Pedido> pedidos) {
        String cargaId = "";
        for (Pedido pedido : pedidos) {
            if (pedido.isGrupo()) {
                if (cargaId.equals(pedido.getCarga().getId())) {
                    continue;
                }
                cargaId = pedido.getCarga().getId();
            }
            pedido.getPeriodoDeCarga().setInicio(formatearFecha(pedido.getPeriodoDeCarga().getInicio()));
            pedido.getPeriodoDeCarga().setFinalizacion(formatearFecha(pedido.getPeriodoDeCarga().getFinalizacion()));

            pedido.getPeriodoDeDescarga().setInicio(formatearFecha(pedido.getPeriodoDeDescarga().getInicio()));
            pedido.getPeriodoDeDescarga().setFinalizacion(formatearFecha(pedido.getPeriodoDeDescarga().getFinalizacion()));
        }
    }

    private String formatearFecha(String fechaCompleta) {
        String[] inicio = fechaCompleta.split(" ");
        String[] fecha = inicio[0].split("-");
        String hora = inicio[1];

        return fecha[2] + "/" + fecha[1] + "/" + fecha[0] + " " + hora;
    }

    public void verificarTieneContraOfertas(Pedido pedido) {
        pedido.setTieneContraOfertas(!contraOfertaRepository.buscarContraOfertasPorIdPedido(pedido.getId()).isEmpty());
    }

}
