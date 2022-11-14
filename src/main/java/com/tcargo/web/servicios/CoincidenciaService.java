package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.CoincidenciaConverter;
import com.tcargo.web.convertidores.UbicacionConverter;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.EstadoNotificacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.CoincidenciaModel;
import com.tcargo.web.modelos.CoincidenciaParaViajePropioModel;
import com.tcargo.web.repositorios.CoincidenciaRepository;
import com.tcargo.web.repositorios.ComisionRepository;
import com.tcargo.web.repositorios.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CoincidenciaService {

    private final CoincidenciaConverter coincidenciaConverter;
    private final CoincidenciaRepository coincidenciaRepository;
    private final ComisionRepository comisionRepository;
    private final PedidoRepository pedidoRepository;
    private final UbicacionConverter ubicacionConverter;
    private final MessageSource messages;
    private static final Log LOG = LogFactory.getLog(CoincidenciaService.class);

    public Coincidencia buscarPorId(String id) {
        return coincidenciaRepository.findById(id).orElse(null);
    }

    public List<Coincidencia> buscarPorPedido(Long idPedido) {
        return coincidenciaRepository.buscarPorIdpedido(idPedido);
    }

    public Coincidencia buscarPorPedidoEntidad(Long idPedido) {
        return coincidenciaRepository.buscarPorIdpedidoEntidad(idPedido);
    }

    public Page<Coincidencia> buscarActivosPorIdTransportador(Pageable page, String idTransportador, String q) {
        if (q == null || q.isEmpty()) {
            return coincidenciaRepository.buscarActivosPorIdTransportador(page, idTransportador);
        } else {
            return coincidenciaRepository.buscarActivosPorIdTransportadorQ(page, idTransportador, "%" + q + "%");
        }
    }

    public Page<Coincidencia> buscarParaExcel(Pageable pageable, String idTransportador, String q) {
        List<Coincidencia> coincidencias;
        if (q != null && !q.isEmpty()) {
            coincidencias = coincidenciaRepository.buscarActivosPorIdTransportadorOrdenados(pageable.getSort(), idTransportador, "%" + q + "%");
        } else {
            coincidencias = coincidenciaRepository.buscarActivosPorIdTransportadorOrdenados(pageable.getSort(), idTransportador);
        }
        return new PageImpl<>(coincidencias, pageable, coincidencias.size());
    }

    public List<Coincidencia> buscarActivosPorIdTransportadorList(Pageable page, String idTransportador, String q) {
        if (q == null || q.isEmpty()) {
            return coincidenciaRepository.buscarActivosPorIdTransportadorList(idTransportador);
        } else {
            return coincidenciaRepository.buscarActivosPorIdTransportadorList(page, idTransportador, q);
        }
    }

    public Page<Coincidencia> listarActivos(Pageable pageable, String q, boolean excel) {
        if (q == null || q.isEmpty()) {
            if (excel) {
                List<Coincidencia> coincidencias = coincidenciaRepository.buscarActivos(pageable.getSort());
                return new PageImpl<>(coincidencias, pageable, coincidencias.size());
            }
            return coincidenciaRepository.buscarActivos(pageable);
        } else {
            try {
                Double d = Double.parseDouble(q);
                if (excel) {
                    List<Coincidencia> coincidencias = coincidenciaRepository.findByCostoAndEliminadoIsNull(pageable.getSort(), d);
                    return new PageImpl<>(coincidencias, pageable, coincidencias.size());
                }
                return coincidenciaRepository.findByCostoAndEliminadoIsNull(pageable, d);
            } catch (Exception e) {
                LOG.info(e.getMessage());
                if (excel) {
                    List<Coincidencia> coincidencias = coincidenciaRepository.buscarActivosQ(pageable.getSort(), "%" + q + "%");
                    return new PageImpl<>(coincidencias, pageable, coincidencias.size());
                }
                return coincidenciaRepository.buscarActivosQ(pageable, "%" + q + "%");
            }
        }
    }

    public Page<Coincidencia> buscarPorPais(Pageable pageable, String idPais, String q, boolean excel) {
        if (q == null || q.isEmpty()) {
            if (excel) {
                List<Coincidencia> coincidencias = coincidenciaRepository.buscarActivosPorPais(pageable.getSort(), idPais);
                return new PageImpl<>(coincidencias, pageable, coincidencias.size());
            }
            return coincidenciaRepository.buscarActivosPorPais(pageable, idPais);
        } else {
            try {
                Double d = Double.parseDouble(q);
                if (excel) {
                    List<Coincidencia> coincidencias = coincidenciaRepository.findByCostoAndPedidoDadorPaisIdAndEliminadoIsNull(pageable.getSort(), d, idPais);
                    return new PageImpl<>(coincidencias, pageable, coincidencias.size());
                }
                return coincidenciaRepository.findByCostoAndPedidoDadorPaisIdAndEliminadoIsNull(pageable, d, idPais);
            } catch (Exception e) {
                LOG.info(e.getMessage());
                if (excel) {
                    List<Coincidencia> coincidencias = coincidenciaRepository.buscarActivosPorPaisQ(pageable.getSort(), idPais, "%" + q + "%");
                    return new PageImpl<>(coincidencias, pageable, coincidencias.size());
                }
                return coincidenciaRepository.buscarActivosPorPaisQ(pageable, idPais, "%" + q + "%");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Coincidencia guardar(CoincidenciaModel model, Pais pais) throws WebException {
        Coincidencia match = coincidenciaConverter.modeloToEntidad(model);
        final Locale locale = LocaleContextHolder.getLocale();

        if (match.getEliminado() != null) {
            throw new WebException(messages.getMessage("match.back.error.dada.baja", null, locale));
        }

        match.setModificacion(new Date());
        match.setComision(comisionRepository.findByPais(pais));
        match.setAprobado(false);
        match.setNotificacion(EstadoNotificacion.CREADO);
        return coincidenciaRepository.save(match);
    }

    public CoincidenciaModel crearModelo(Long idPedido) throws WebException {
        CoincidenciaModel model = new CoincidenciaModel();
        final Locale locale = LocaleContextHolder.getLocale();

        Pedido p = pedidoRepository.findById(idPedido).orElse(null);

        if (p == null) {
            throw new WebException(messages.getMessage("match.back.error.find.pedido", null, locale) + idPedido);
        }

        if (p.isAsignadoATransportador()) {
            throw new WebException(messages.getMessage("match.back.error.pedido.asignado", null, locale));
        }

        model.setIdPedido(idPedido);
        model.setCosto(p.getValor());
        model.setDetalle(p.getDescripcion());

        return model;
    }

    public List<Coincidencia> buscarPorDadorNotificacionCreada(Usuario dador, EstadoNotificacion notificacion) {
        return coincidenciaRepository.buscaPordadorNotificacionCreada(dador.getId(), notificacion);
    }

    public List<Coincidencia> buscarPorDadorNotificacionConformidadCreada(Usuario dador, EstadoNotificacion notificacion) {
        return coincidenciaRepository.buscaPordadorNotificacionConformidadCreada(dador.getId(), notificacion);
    }

    public List<CoincidenciaParaViajePropioModel> armarModelosParaViajePropiosLibres(Transportador transportador) {
        List<Coincidencia> matchs = coincidenciaRepository.buscarActivosPorIdTransportadorNoVencidas(transportador.getId(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return armarModelosParaViaje(matchs);
    }

    public List<CoincidenciaParaViajePropioModel> armarModelosParaViaje(List<Coincidencia> matchs) {
        List<CoincidenciaParaViajePropioModel> respuesta = new ArrayList<>();
        for (Coincidencia entidad : matchs) {
            CoincidenciaParaViajePropioModel model = new CoincidenciaParaViajePropioModel();
            model.setId(entidad.getId());
            model.setDesde(ubicacionConverter.entidadToModelo(entidad.getPedido().getUbicacionDesde()));
            model.setHasta(ubicacionConverter.entidadToModelo(entidad.getPedido().getUbicacionHasta()));
            model.setInicioCarga(entidad.getPedido().getPeriodoDeCarga().getInicio());
            model.setFinalCarga(entidad.getPedido().getPeriodoDeCarga().getFinalizacion());
            model.setInicioDescarga(entidad.getPedido().getPeriodoDeDescarga().getInicio());
            model.setFinalDescarga(entidad.getPedido().getPeriodoDeDescarga().getFinalizacion());
            model.setIdPedido(entidad.getPedido().getId());
            model.setDador(entidad.getPedido().getDador().getNombre());
            for (TipoCarga tc : entidad.getPedido().getCarga().getTipoCargas()) {
                model.getTipoCarga().add(tc.getCaracteristicas());
            }
            model.setProducto(entidad.getPedido().getCarga().getProducto().getNombre());
            model.setIdTransportador(entidad.getTransportador().getId());
            respuesta.add(model);
        }
        return respuesta;
    }

    public List<CoincidenciaModel> buscarPorChoferEnViaje(String idChofer) {
        return coincidenciaConverter.entidadesToModelos(coincidenciaRepository.buscarPorChoferEnViaje(idChofer));
    }

}
