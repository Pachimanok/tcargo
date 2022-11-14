package com.tcargo.web.controladores.norest;

import com.tcargo.web.convertidores.CoincidenciaConverter;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.*;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.ValoracionModel;
import com.tcargo.web.modelos.busqueda.BusquedaPedidoModel;
import com.tcargo.web.repositorios.UbicacionRepository;
import com.tcargo.web.servicios.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMIN_DADOR','ROLE_ADMIN_AMBAS', 'ROLE_DADOR_CARGA','ROLE_AMBAS','ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL', 'ROLE_INVITADO', 'ROLE_CHOFER')")
@RequestMapping("/pedido")
public class PedidoController extends Controlador {

    private static final String TIPO_MONEDAS = "tipoMonedas";
    private static final String TIPO_INTERANACIONAL = "tipoInteranacional";
    private static final String PEDIDO_CARGAS = "/pedido/cargas";
    private static final String MATCHS_MODELS = "matchsModels";
    private static final String ESTADO_PEDIDOS = "estadoPedidos";
    private static final String IDS_PARA_NOTIFICAR = "idsParaNotificar";
    private static final String BUSQUEDA = "busqueda";
    private static final String DURACION_ESTADIAS = "duracionEstadias";

    private final CoincidenciaService coincidenciaService;
    private final ContraOfertaService contraOfertaService;
    private final EmailService emailService;
    private final MonedaService monedaService;
    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final TipoCargaService tipoCargaService;
    private final TipoContenedorService tipoContenedorService;
    private final TipoEmbalajeService tipoEmbalajeService;
    private final TipoRemolqueService tipoRemolqueService;
    private final TipoRequisitoOfertaService tipoRequisitoOfertaService;
    private final TipoVehiculoService tipoVehiculoService;
    private final TransportadorService transportadorService;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioService usuarioService;
    private final CoincidenciaConverter matchConverter;
    private final ChoferService choferService;
    private final MessageSource messages;
    private final ValoracionService valoracionService;

    @Autowired
    public PedidoController(CoincidenciaService coincidenciaService, ContraOfertaService contraOfertaService, EmailService emailService, MonedaService monedaService, PedidoService pedidoService, ProductoService productoService, TipoCargaService tipoCargaService, TipoContenedorService tipoContenedorService, TipoEmbalajeService tipoEmbalajeService, TipoRemolqueService tipoRemolqueService, TipoRequisitoOfertaService tipoRequisitoOfertaService, TipoVehiculoService tipoVehiculoService, TransportadorService transportadorService, UbicacionRepository ubicacionRepository, UsuarioService usuarioService, CoincidenciaConverter matchConverter, ChoferService choferService, MessageSource messages, ValoracionService valoracionService) {
        super("pedido-list", "pedido-form");
        this.coincidenciaService = coincidenciaService;
        this.contraOfertaService = contraOfertaService;
        this.emailService = emailService;
        this.monedaService = monedaService;
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.tipoCargaService = tipoCargaService;
        this.tipoContenedorService = tipoContenedorService;
        this.tipoEmbalajeService = tipoEmbalajeService;
        this.tipoRemolqueService = tipoRemolqueService;
        this.tipoRequisitoOfertaService = tipoRequisitoOfertaService;
        this.tipoVehiculoService = tipoVehiculoService;
        this.transportadorService = transportadorService;
        this.ubicacionRepository = ubicacionRepository;
        this.usuarioService = usuarioService;
        this.matchConverter = matchConverter;
        this.choferService = choferService;
        this.messages = messages;
        this.valoracionService = valoracionService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(PEDIDO_LABEL) Pedido pedido, BindingResult resultado, ModelMap modelo, Pageable paginable, @RequestParam(value = "dadorId", required = false) String dadorId, @RequestParam(value = "requiereEntrega", required = false) Boolean requiereEntrega, @RequestParam(value = "viaje", required = false) String viajeId, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        int cantCamiones = 0;

        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {

                if (!pedido.getContenedor().getUbicacionEntrega().getDireccion().isEmpty() && requiereEntrega == null) {
                    Pedido p = pedidoService.buscarPorId(pedido.getId());
                    Ubicacion u = ubicacionRepository.getOne(p.getContenedor().getUbicacionEntrega().getId());
                    ubicacionRepository.delete(u);
                    pedido.getContenedor().setUbicacionEntrega(null);
                }

                pedido.setGrupo(false);
                if (pedido.getCarga().getCantidadCamiones() != null && pedido.getCarga().getCantidadCamiones() > 1) {
                    cantCamiones = pedido.getCarga().getCantidadCamiones();
                    pedido.getCarga().setCantidadCamiones(1);
                    pedido.setGrupo(true);
                }

                pedido.setDador(usuarioService.buscar(dadorId));
                Pedido p = pedidoService.guardar(pedido);

                if (viajeId != null && !viajeId.isEmpty()) {
                    Transportador t = transportadorService.porIdViaje(viajeId);
                    emailService.cargaNegativaSeleccionada(t.getUsuario().getMail(), t.getUsuario().getNombre(), p.getId());
                }

                if (cantCamiones > 1) {
                    int i = 1;
                    while (i < cantCamiones) {
                        pedidoService.copiarYGuardarNuevo(pedido.getId());
                        i = i + 1;
                    }
                }

                return "redirect:/pedido/cargas";

            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("pedido.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("pedido.back.error.al.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        if (cantCamiones > 1) {
            pedido.getCarga().setCantidadCamiones(cantCamiones);
            pedido.setGrupo(false);
        }
        pedido.getUbicacionDesde().setId(null);
        pedido.getUbicacionHasta().setId(null);
        if (viajeId != null && !viajeId.isEmpty()) {
            modelo.addAttribute(VIAJE_LABEL, viajeId);
        } else {
            modelo.addAttribute(VIAJE_LABEL, null);
        }
        modelo.addAttribute("dadorId", dadorId);
        Usuario us = usuarioService.buscar(usuario.getId());
        return returnWithError(modelo, us.getPais().getId(), paginable, us.getPais(), pedido);
    }

    @PostMapping("/eliminar")
    public RedirectView eliminar(@ModelAttribute Pedido pedido, RedirectAttributes attributes, Locale locale) {
        RedirectView redirectView = new RedirectView(PEDIDO_CARGAS);
        try {
            pedidoService.eliminar(pedido);
        } catch (WebException we) {
            attributes.addFlashAttribute(ERROR, messages.getMessage("pedido.back.error.eliminar", null, locale) + we.getMessage());
        } catch (Exception e) {
            attributes.addFlashAttribute(ERROR, messages.getMessage("pedido.back.error.eliminar", null, locale));
        }
        return redirectView;
    }

    @PostMapping("/editar-pedido-grupo")
    public String editarPedidoGrupo(HttpSession session, @Valid @ModelAttribute(PEDIDO_LABEL) Pedido pedido, BindingResult result, Pageable paginable, ModelMap modelo, Locale locale) {

        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (result.hasErrors()) {
                error(modelo, result);
            } else {
                List<Pedido> pedidosGrupo = pedidoService.buscarPorIdCarga(pedido.getCarga().getId());
                int cantidadCamionesActual = pedidosGrupo.size();
                int cantidadCamionesNueva = pedido.getCarga().getCantidadCamiones();
                pedido.setGrupo(true);

                if (cantidadCamionesActual > cantidadCamionesNueva) {
                    int cantidadAEliminar = cantidadCamionesActual - cantidadCamionesNueva;

                    for (int i = 0; i < cantidadAEliminar; i++) {
                        pedidosGrupo.get(i).setEliminado(new Date());
                        pedidoService.guardar(pedidosGrupo.get(i));
                        pedidosGrupo.remove(pedidosGrupo.get(i));
                    }
                } else if (cantidadCamionesActual < cantidadCamionesNueva) {
                    int cantidadACrear = cantidadCamionesNueva - cantidadCamionesActual;

                    for (int i = 0; i < cantidadACrear; i++) {
                        Long id = pedido.getId();
                        Pedido p = pedidoService.copiarYGuardarNuevo(id);
                        pedidosGrupo.add(p);
                    }
                }

                pedido.getCarga().setCantidadCamiones(1);

                for (Pedido p : pedidosGrupo) {
                    BeanUtils.copyProperties(pedido, p, "id", "dador");
                    if (cantidadCamionesNueva == 1) {
                        p.setGrupo(false);
                    }
                    pedidoService.guardar(p);
                }

                return "redirect:/pedido/cargas";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("pedido.back.error.al.guardar", null, locale) + e.getMessage());
        }

        return returnWithError(modelo, u.getPais().getId(), paginable, u.getPais(), pedido);
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(HttpSession session, @RequestParam(required = false, value = "id") Long id, @RequestParam(required = false) String accion, @RequestParam(required = false) boolean grupo, @RequestParam(value = "viajeId", required = false) String viajeId) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        Pedido pedido = new Pedido();
        pedido.setCarga(new Carga());
        pedido.setUbicacionDesde(new Ubicacion());
        pedido.setUbicacionHasta(new Ubicacion());
        pedido.setPeriodoDeCarga(new PeriodoDeCarga());
        pedido.setPeriodoDeDescarga(new PeriodoDeCarga());
        pedido.setContenedor(new Contenedor());
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        if (accion == null || accion.isEmpty()) {
            accion = GUARDAR_LABEL;
        }

        if (id != null) {
            pedido = pedidoService.buscarPorId(id);
            if (grupo) {
                pedido.getCarga().setCantidadCamiones(pedidoService.buscarPorIdCarga(pedido.getCarga().getId()).size());
            }
        }

        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);
        List<TipoDeViaje> tiposDeViajes = Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL);
        List<TipoPeso> tipoPesos = Arrays.asList(TipoPeso.KG, TipoPeso.TON);
        List<TipoDeViaje> tipoInternacional = Arrays.asList(TipoDeViaje.EXPORTACION, TipoDeViaje.IMPORTACION);
        List<Moneda> tipoMonedas = monedaService.listarActivosPorPais(idPais);
        modelo.addObject(TIPOCONTENEDORES_LABEL, tipoContenedorService.listarActivos());
        modelo.addObject(TIPOPESOS_LABEL, tipoPesos);
        modelo.addObject(TIPOSDEVIAJE_LABEL, tiposDeViajes);
        modelo.addObject(TIPO_INTERANACIONAL, tipoInternacional);
        modelo.addObject(TIPO_MONEDAS, tipoMonedas);
        modelo.addObject(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivos());
        modelo.addObject(TIPOREMOLQUES_LABEL, tipoRemolqueService.listarActivosPorPais(Pageable.unpaged(), u.getPais().getId(), true));
        modelo.addObject(TIPOEMBALAJES_LABEL, tipoEmbalajeService.listarActivosPorPais(Pageable.unpaged(), u.getPais().getId(), true));
        modelo.addObject(PRODUCTOS_LABEL, productoService.listarActivosPorPais(Pageable.unpaged(), u.getPais().getId(), true));
        modelo.addObject(TIPOCARGAS_LABEL, tipoCargaService.listarActivosPorPais(Pageable.unpaged(), u.getPais().getId(), true));
        modelo.addObject(TIPOREQUISITOSOFERTASNACIONAL_LABEL, tipoRequisitoOfertaService.buscarObligatorioPedidoPortiPoDeViaje(u.getPais(), TipoDeViaje.NACIONAL));
        modelo.addObject(TIPOREQUISITOSOFERTASINTERNACIONAL_LABEL, tipoRequisitoOfertaService.buscarObligatorioPedidoPortiPoDeViaje(u.getPais(), TipoDeViaje.INTERNACIONAL));
        modelo.addObject(VIAJE_LABEL, viajeId);
        modelo.addObject(PEDIDO_LABEL, pedido);
        modelo.addObject(ACCION_LABEL, accion);
        return modelo;
    }

    @GetMapping("/ver")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView ver(Pageable paginable, @RequestParam(required = false) Long id) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Pedido pedido = pedidoService.buscarPorId(id);
        List<Pedido> p = new ArrayList<>();
        p.add(pedido);

        if (pedido.isAsignadoATransportador()) {
            ValoracionModel valoracion = valoracionService.buscarPorIdCreadorYpedidoModel(pedido.getDador().getId(), pedido.getId());
            if (valoracion != null) {
                modelo.addObject("valoracion", valoracion);
            }
        }

        Page<Pedido> page = new PageImpl<>(p, paginable, p.size());

        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(DURACION_ESTADIAS, armarEstadias(page.getContent()));
        modelo.addObject(BUSQUEDA, new BusquedaPedidoModel());
        return modelo;
    }

    @GetMapping({"/cargas", "/cargas/{id}"})
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listar(Pageable pagina, HttpSession session, @ModelAttribute BusquedaPedidoModel busqueda,
                               @PathVariable(value = "id", required = false) String cargaId,
                               @RequestParam(value = "matcheados", required = false) Boolean matcheados,
                               @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView mav = new ModelAndView(vistaListado);
        Pageable pageable;
        if (cargaId == null) {
            if (busqueda.getValor() != null) {
                pageable = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), pagina.getSortOr(Sort.by("p.valor").descending().and(Sort.by("pc.inicio").ascending())));
            } else {
                pageable = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), pagina.getSortOr(Sort.by("p.estado_pedido").ascending().and(Sort.by("pc.inicio").ascending())));
            }

        } else {
            pageable = pagina;
        }
        ordenar(pageable, mav);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        String rolActual = (String) session.getAttribute(ROL_ACTUAL_LABEL);
        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);
        Page<Pedido> page;
        switch (rolActual) {
            case SIN_ROL_LABEL:
            case INVITADO_LABEL:
            case ADMINISTRADOR_LABEL:
            case ADMINISTRADORLOCAL_LABEL:
                if (cargaId != null && !cargaId.isEmpty()) {
                    List<Pedido> p = pedidoService.buscarPorIdCarga(cargaId);
                    page = new PageImpl<>(p, pageable, p.size());
                } else {
                    page = pedidoService.buscarPorCriterios(pageable, busqueda, matcheados, excel != null);
                }
                break;
            case TRANSPORTADOR_LABEL:
                if (cargaId != null) {
                    page = pedidoService.buscarPorIdCargaPageEliminarMatcheados(pageable, cargaId);
                } else {
                    List<ContraOferta> contraOfertas = contraOfertaService.listarPorTransportadorIdList(transportadorService.buscarPorIdUsuario(usuario.getId()));
                    page = pedidoService.buscarPorCriteriosParaTransportador(pageable, busqueda, contraOfertas, excel != null);
                }
                break;
            case DADORDECARGA_LABEL:
                if (cargaId != null) {
                    List<Pedido> p = pedidoService.buscarPorIdCarga(cargaId);
                    page = new PageImpl<>(p, pageable, p.size());
                } else {
                    page = pedidoService.buscarPorCriteriosParaDador(pageable, busqueda, usuario, excel != null);
                }

                List<String> ids = new ArrayList<>();
                if (page != null) {
                    for (Pedido pedido : page.getContent()) {
                        List<ContraOferta> contraOfertas = contraOfertaService.buscarPorIdPedidoEstadoYDador(pedido, usuario);
                        for (ContraOferta contraOferta : contraOfertas) {
                            ids.add(contraOferta.getId());
                        }
                    }
                }

                mav.addObject(IDS_PARA_NOTIFICAR, ids);
                break;
            default:
                page = null;
        }

        List<Coincidencia> matchs = new ArrayList<>();

            for (Pedido p : page.getContent()) {
                Coincidencia coincidencia = coincidenciaService.buscarPorPedidoEntidad(p.getId());
                if (coincidencia != null) {
                    matchs.add(coincidencia);
                }
            }
        List<CoincidenciaModel> matchsModel = matchConverter.entidadesToModelos(matchs);

        String url = cargaId != null ? "/pedido/cargas/" + cargaId : PEDIDO_CARGAS;

        if(idPais == null || idPais.isEmpty()){
            mav.addObject(TIPOCARGAS_LABEL, tipoCargaService.listarActivosModelSinDuplicados());
            mav.addObject(PRODUCTOS_LABEL, productoService.listarActivosModel());
        }else{
            mav.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
            mav.addObject(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
        }

        mav.addObject(URL_LABEL, url + busqueda.toString());
        mav.addObject(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        mav.addObject(ESTADO_PEDIDOS, Arrays.asList(EstadoPedido.values()));
        mav.addObject(DURACION_ESTADIAS, armarEstadias(page.getContent()));

        pedidoService.formatearFechasPedidos(page.getContent());

        mav.addObject(PAGE_LABEL, page);
        mav.addObject(BUSQUEDA, busqueda);
        mav.addObject(MATCHS_MODELS, matchsModel);
        return mav;
    }

    // armar map con periodos de tiempo
    public Map<String, String> armarEstadias(List<Pedido> pedidos) {
        Map<String, String> estadias = new HashMap<>();
        DateTimeFormatter f = new DateTimeFormatterBuilder().parseCaseInsensitive().append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toFormatter();

        for (Pedido p : pedidos) {
            LocalDateTime primero = LocalDateTime.parse(p.getPeriodoDeCarga().getInicio(), f);
            LocalDateTime segunda = LocalDateTime.parse(p.getPeriodoDeCarga().getFinalizacion(), f);
            LocalDateTime tercera = LocalDateTime.parse(p.getPeriodoDeDescarga().getInicio(), f);
            LocalDateTime cuarta = LocalDateTime.parse(p.getPeriodoDeDescarga().getFinalizacion(), f);

            Duration duration1 = Duration.between(primero, segunda);
            long diff = Math.abs(duration1.toMinutes()) / 60;
            Duration duration2 = Duration.between(tercera, cuarta);
            long diff2 = Math.abs(duration2.toMinutes()) / 60;

            estadias.put(p.getId().toString() + "carga", Long.toString(diff));
            estadias.put(p.getId().toString() + "descarga", Long.toString(diff2));
        }

        return estadias;
    }

    @GetMapping("/pedidosofertados")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listarPedidosConOfertas(HttpSession session, Pageable pagina, @ModelAttribute BusquedaPedidoModel busqueda) {
        ModelAndView modelo = new ModelAndView("pedido-list-mis-fletes");
        Pageable pageable = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), pagina.getSortOr(Sort.by("pc.inicio").descending()));
        ordenar(pageable, modelo);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
        List<ContraOferta> cOferta = contraOfertaService.listarPorTransportadorIdList(t);
        List<Coincidencia> matchs = coincidenciaService.buscarActivosPorIdTransportadorList(pageable, t.getId(), null);
        List<Long> idPedidos = new ArrayList<>();
        Long id;

        for (ContraOferta contraOferta : cOferta) {
            id = contraOferta.getPedido().getId();
            if (!idPedidos.contains(id) && contraOferta.getEstado() == EstadoContraOferta.PENDIENTE) {
                idPedidos.add(id);
            }
        }

        if (!matchs.isEmpty()) {
            for (Coincidencia match : matchs) {
                idPedidos.remove(match.getPedido().getId());
            }
        }

        if (busqueda.getRecibirOfertas() == null) {
            busqueda.setRecibirOfertas(true);
        }

        Page<Pedido> page = pedidoService.buscarOfertadosPorCriterios(pageable, busqueda, idPedidos);

        List<Long> ids = new ArrayList<>();
        for (Pedido pedido : page.getContent()) {
            List<ContraOferta> cos = contraOfertaService.buscarPorIdPedidoEstadoYTransportador(pedido, t);
            for (ContraOferta c : cos) {
                ids.add(c.getPedido().getId());
            }
        }

        modelo.addObject(IDS_PARA_NOTIFICAR, ids);
        modelo.addObject(ESTADO_PEDIDOS, Arrays.asList(EstadoPedido.values()));
        modelo.addObject(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        modelo.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(u.getPais().getId()));
        modelo.addObject(PRODUCTOS_LABEL, productoService.activosPorPaisModel(u.getPais().getId()));
        modelo.addObject(DURACION_ESTADIAS, armarEstadias(page.getContent()));
        pedidoService.formatearFechasPedidos(page.getContent());
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/pedido/pedidosofertados" + busqueda.toString());
        modelo.addObject(BUSQUEDA, busqueda);

        return modelo;
    }

    @GetMapping("/matchs")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listarPedidosConMatcheados(HttpSession session, Pageable pagina, @ModelAttribute BusquedaPedidoModel busqueda) {
        ModelAndView modelo = new ModelAndView("pedido-list-mis-fletes");
        Pageable pageable = PageRequest.of(pagina.getPageNumber(), pagina.getPageSize(), pagina.getSortOr(Sort.by("pc.inicio").descending()));
        ordenar(pageable, modelo);
        Page<Pedido> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        Transportador t = transportadorService.buscarPorIdUsuario(u.getId());
        List<Coincidencia> matchs = coincidenciaService.buscarActivosPorIdTransportadorList(pageable, t.getId(), null);
        List<CoincidenciaModel> matchsModel = matchConverter.entidadesToModelos(matchs);
        Set<Long> idPedidos = matchs.stream().map(m -> m.getPedido().getId()).collect(Collectors.toSet());

        if (!idPedidos.isEmpty()) {
            page = pedidoService.buscarMatcheadosPorCriterios(pageable, busqueda, new ArrayList<>(idPedidos));
            modelo.addObject(MATCHS_MODELS, matchsModel);
        } else {
            page = new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        modelo.addObject(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        modelo.addObject(ESTADO_PEDIDOS, Arrays.asList(EstadoPedido.values()));
        modelo.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(u.getPais().getId()));
        modelo.addObject(PRODUCTOS_LABEL, productoService.activosPorPaisModel(u.getPais().getId()));
        modelo.addObject("viajeEstados", EstadoViaje.getEstados());
        modelo.addObject(DURACION_ESTADIAS, armarEstadias(page.getContent()));
        pedidoService.formatearFechasPedidos(page.getContent());
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/pedido/matchs" + busqueda.toString());
        modelo.addObject(BUSQUEDA, busqueda);

        return modelo;
    }

    @GetMapping("/chofer/matchs")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listarPedidosConMatcheadosParaChofer(HttpSession session, @PageableDefault(value = 20, sort = {"p.id"}) Pageable paginable, @ModelAttribute BusquedaPedidoModel busqueda) {
        ModelAndView modelo = new ModelAndView("pedido-list-mis-fletes");
        ordenar(paginable, modelo);
        Page<Pedido> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Chofer chofer = choferService.buscarPorUsuario(usuario.getId());
        List<CoincidenciaModel> matchsModel = coincidenciaService.buscarPorChoferEnViaje(chofer.getId());
        Set<Long> idPedidos = matchsModel.stream().map(CoincidenciaModel::getIdPedido).collect(Collectors.toSet());

        if (!idPedidos.isEmpty()) {
            page = pedidoService.buscarMatcheadosPorCriterios(paginable, busqueda, new ArrayList<>(idPedidos));
            modelo.addObject(MATCHS_MODELS, matchsModel);
        } else {
            page = new PageImpl<>(new ArrayList<>(), paginable, 0);
        }

        modelo.addObject(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        modelo.addObject(ESTADO_PEDIDOS, Arrays.asList(EstadoPedido.values()));
        modelo.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(chofer.getUsuario().getPais().getId()));
        modelo.addObject(PRODUCTOS_LABEL, productoService.activosPorPaisModel(chofer.getUsuario().getPais().getId()));
        modelo.addObject("viajeEstados", EstadoViaje.getEstados());
        modelo.addObject(DURACION_ESTADIAS, armarEstadias(page.getContent()));
        pedidoService.formatearFechasPedidos(page.getContent());
        modelo.addObject(PAGE_LABEL, page);
        modelo.addObject(URL_LABEL, "/pedido/chofer/matchs" + busqueda.toString());
        modelo.addObject(BUSQUEDA, busqueda);

        return modelo;
    }

    private String returnWithError(ModelMap modelo, String idPais, Pageable paginable, Pais pais, Pedido pedido) {
        modelo.addAttribute(TIPOCONTENEDORES_LABEL, tipoContenedorService.listarActivos());
        modelo.addAttribute(TIPO_INTERANACIONAL, Arrays.asList(TipoDeViaje.EXPORTACION, TipoDeViaje.IMPORTACION));
        modelo.addAttribute(TIPOVEHICULOS_LABEL, tipoVehiculoService.listarActivos());
        modelo.addAttribute(TIPOPESOS_LABEL, Arrays.asList(TipoPeso.KG, TipoPeso.TON));
        modelo.addAttribute(TIPO_MONEDAS, monedaService.listarActivos());
        modelo.addAttribute(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        modelo.addAttribute(TIPOREQUISITOSOFERTASNACIONAL_LABEL, tipoRequisitoOfertaService.buscarObligatorioPedidoPortiPoDeViaje(pais, TipoDeViaje.NACIONAL));
        modelo.addAttribute(TIPOREQUISITOSOFERTASINTERNACIONAL_LABEL, tipoRequisitoOfertaService.buscarObligatorioPedidoPortiPoDeViaje(pais, TipoDeViaje.INTERNACIONAL));
        modelo.addAttribute(TIPOREMOLQUES_LABEL, tipoRemolqueService.listarActivosPorPais(paginable, idPais, true));
        modelo.addAttribute(TIPOEMBALAJES_LABEL, tipoEmbalajeService.listarActivosPorPais(paginable, idPais, true));
        modelo.addAttribute(PRODUCTOS_LABEL, productoService.listarActivosPorPais(paginable, idPais, true));
        modelo.addAttribute(TIPOCARGAS_LABEL, tipoCargaService.listarActivosPorPais(paginable, idPais, true));
        modelo.addAttribute(PEDIDO_LABEL, pedido);


        return vistaFormulario;
    }

    @GetMapping({"/conofertas"})
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listarDadorConOfertas(Pageable pageable, HttpSession session, @ModelAttribute BusquedaPedidoModel busqueda, @PathVariable(value = "id", required = false) String cargaId) {
        ModelAndView mav = new ModelAndView(vistaListado);
        ordenar(pageable, mav);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);
        String rolActual = (String) session.getAttribute(ROL_ACTUAL_LABEL);
        Page<Pedido> page = null;
        List<ContraOferta> contraOfertas;
        List<Pedido> p;
        switch (rolActual) {
            case "sinRol":
            case INVITADO_LABEL:
            case ADMINISTRADOR_LABEL:
            case ADMINISTRADORLOCAL_LABEL:
                contraOfertas = contraOfertaService.listarEstadoPendientes();
                p = new ArrayList<>();
                for (ContraOferta c : contraOfertas) {
                    p.add(c.getPedido());
                }
                page = new PageImpl<>(p, pageable, p.size());
                break;
            case DADORDECARGA_LABEL:
                contraOfertas = contraOfertaService.listarPorDadorPendiente(usuario);
                p = new ArrayList<>();
                for (ContraOferta c : contraOfertas) {
                    p.add(c.getPedido());
                }
                page = new PageImpl<>(p, pageable, p.size());
                break;
        }

        List<String> ids = new ArrayList<>();
        if (page != null) {
            for (Pedido pedido : page.getContent()) {
                List<ContraOferta> co = contraOfertaService.buscarPorIdPedidoEstadoYDador(pedido, usuario);
                for (ContraOferta contraOferta : co) {
                    ids.add(contraOferta.getId());
                }
            }
        }

        mav.addObject(IDS_PARA_NOTIFICAR, ids);

        String url = cargaId != null ? "/dador/conofertas" + cargaId : PEDIDO_CARGAS;

        mav.addObject(URL_LABEL, url + busqueda.toString());
        mav.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        mav.addObject(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
        mav.addObject(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        mav.addObject(ESTADO_PEDIDOS, Arrays.asList(EstadoPedido.values()));
        mav.addObject(PAGE_LABEL, page);
        mav.addObject(DURACION_ESTADIAS, armarEstadias(page.getContent()));
        mav.addObject(BUSQUEDA, busqueda);

        return mav;
    }

    @GetMapping({"/dador/matcheados"})
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listarDadorMatcheados(Pageable pageable, HttpSession session, @RequestParam(value = "excel", required = false) String excel, @ModelAttribute BusquedaPedidoModel busqueda) {
        ModelAndView mav = new ModelAndView(vistaListado);
        pageable = armarPageableDadorMatcheados(pageable);
        ordenar(pageable, mav);
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);

        Page<Pedido> page = pedidoService.buscarPorDadorMatcheadas(pageable, usuario.getId(), excel != null);

        List<String> ids = new ArrayList<>();
        if (page != null) {
            for (Pedido pedido : page.getContent()) {
                List<ContraOferta> co = contraOfertaService.buscarPorIdPedidoEstadoYDador(pedido, usuario);
                for (ContraOferta contraOferta : co) {
                    ids.add(contraOferta.getId());
                }
            }
        }

        mav.addObject(IDS_PARA_NOTIFICAR, ids);

        String url = "/pedido/dador/matcheados";

        mav.addObject(URL_LABEL, url + busqueda.toString());
        mav.addObject(TIPOCARGAS_LABEL, tipoCargaService.activosPorPaisModel(idPais));
        mav.addObject(PRODUCTOS_LABEL, productoService.activosPorPaisModel(idPais));
        mav.addObject(TIPOSDEVIAJE_LABEL, Arrays.asList(TipoDeViaje.NACIONAL, TipoDeViaje.INTERNACIONAL));
        mav.addObject(ESTADO_PEDIDOS, Arrays.asList(EstadoPedido.values()));
        mav.addObject(PAGE_LABEL, page);
        mav.addObject(DURACION_ESTADIAS, armarEstadias(page.getContent()));
        mav.addObject(BUSQUEDA, busqueda);

        return mav;
    }

    private Pageable armarPageableDadorMatcheados(Pageable pageable) {
        final String estado = "estadoPedido";

        if (pageable.getSort().isEmpty()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(estado).ascending());
        } else {
            List<Sort.Order> orders = new ArrayList<>();
            for (Iterator<Sort.Order> it = pageable.getSort().stream().iterator(); it.hasNext(); ) {
                Sort.Order order = it.next();
                String prop = estado;
                if (order.getProperty().startsWith("pc.")) {
                    prop = "periodoDeCarga.inicio";
                } else if (order.getProperty().startsWith("p.")) {
                    prop = order.getProperty().substring(2);
                    if (order.getProperty().contains("_")) {
                        prop = estado;
                    }
                }
                orders.add(order.getDirection().isAscending() ? Sort.Order.asc(prop) : Sort.Order.desc(prop));
            }
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
        }

        return pageable;
    }

}
