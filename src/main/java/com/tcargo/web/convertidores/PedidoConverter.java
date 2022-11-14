package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.TipoRequisitoOferta;
import com.tcargo.web.modelos.PedidoCercanoModel;
import com.tcargo.web.modelos.PedidoModel;
import com.tcargo.web.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PedidoConverter extends Convertidor<PedidoModel, Pedido> {

    private final CargaRepository cargaRepository;
    private final CoincidenciaRepository coincidenciaRepository;
    private final ContenedorRepository contenedorRepository;
    private final PeriodoDeCargaRepository periodoDeCargaRepository;
    private final TipoRequisitoOfertaRepository tipoRequisitoOfertaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public PedidoModel entidadToModelo(Pedido pedido) {
        PedidoModel model = new PedidoModel();

        try {
            BeanUtils.copyProperties(pedido, model);
            if (pedido.getCarga() != null) {
                model.setIdCarga(pedido.getCarga().getId());
            }
            if (pedido.getContenedor() != null) {
                model.setIdContenedor(pedido.getContenedor().getId());
            }
            if (pedido.getUbicacionDesde() != null) {
                model.setIdUbicacionDesde(pedido.getUbicacionDesde().getId());
            }
            if (pedido.getUbicacionHasta() != null) {
                model.setIdUbicacionHasta(pedido.getUbicacionHasta().getId());
            }
            if (pedido.getPeriodoDeCarga() != null) {
                model.setIdPeriodoDeCarga(pedido.getPeriodoDeCarga().getId());
            }
            if (pedido.getPeriodoDeDescarga() != null) {
                model.setIdPeriodoDeDescarga(pedido.getPeriodoDeDescarga().getId());
            }
            if (pedido.getDador() != null) {
                model.setIdDador(pedido.getDador().getId());
            }
            if (pedido.getCoincidencias() != null && !pedido.getCoincidencias().isEmpty()) {
                model.setIdCoincidencias(
                        pedido.getCoincidencias().stream()
                                .map(Coincidencia::getId)
                                .collect(Collectors.toList())
                );
            }
            if (pedido.getTipoRequisitos() != null && !pedido.getTipoRequisitos().isEmpty()) {
                model.setIdRequisitos(
                        pedido.getTipoRequisitos().stream()
                                .map(TipoRequisitoOferta::getId)
                                .collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            log.error("Error al convertir entidad <Pedido> a modelo: " + e.getMessage());
        }

        return model;
    }

    @Override
    public Pedido modeloToEntidad(PedidoModel model) {
        Pedido pedido = new Pedido();

        try {
            BeanUtils.copyProperties(model, pedido);
            if (model.getIdCarga() != null) {
                pedido.setCarga(cargaRepository.getOne(model.getIdCarga()));
            }
            if (model.getIdContenedor() != null) {
                pedido.setContenedor(contenedorRepository.getOne(model.getIdContenedor()));
            }
            if (model.getIdUbicacionDesde() != null) {
                pedido.setUbicacionDesde(ubicacionRepository.getOne(model.getIdUbicacionDesde()));
            }
            if (model.getIdUbicacionHasta() != null) {
                pedido.setUbicacionHasta(ubicacionRepository.getOne(model.getIdUbicacionHasta()));
            }
            if (model.getIdPeriodoDeCarga() != null) {
                pedido.setPeriodoDeCarga(periodoDeCargaRepository.getOne(model.getIdPeriodoDeCarga()));
            }
            if (model.getIdPeriodoDeDescarga() != null) {
                pedido.setPeriodoDeDescarga(periodoDeCargaRepository.getOne(model.getIdPeriodoDeDescarga()));
            }
            if (model.getIdDador() != null) {
                pedido.setDador(usuarioRepository.getOne(model.getIdDador()));
            }
            if (model.getIdCoincidencias() != null && !model.getIdCoincidencias().isEmpty()) {
                pedido.setCoincidencias(
                        model.getIdCoincidencias().stream()
                                .map(id -> coincidenciaRepository.findById(id).orElse(null))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
            }
            if (model.getIdRequisitos() != null && !model.getIdRequisitos().isEmpty()) {
                pedido.setTipoRequisitos(
                        model.getIdRequisitos().stream()
                                .map(id -> tipoRequisitoOfertaRepository.findById(id).orElse(null))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
            }
        } catch (Exception e) {
            log.error("Error al convertir modelo <PedidoModel> a entidad: " + e.getMessage());
        }

        return pedido;
    }

    public PedidoModel pedidoModelParaMatchForm(Pedido pedido) {
        PedidoModel model = new PedidoModel();

        model.setIdUbicacionDesde(pedido.getUbicacionDesde().getId());
        model.setIdUbicacionHasta(pedido.getUbicacionHasta().getId());
        model.setValor(pedido.getValor());
        model.setIdCarga(pedido.getCarga().getId());
        model.setNombresRequisitos(new ArrayList<>());
        pedido.getTipoRequisitos().forEach(r -> model.getNombresRequisitos().add(r.getNombre()));

        return model;
    }

    public List<PedidoModel> entidadesToModelos(List<Pedido> pedidos) {
        List<PedidoModel> models = new ArrayList<>();

        for (Pedido p : pedidos) {
            models.add(entidadToModelo(p));
        }

        return models;
    }

    public Set<PedidoCercanoModel> convertirParaPedidosCercanos(Set<Pedido> pedidos) {
        final String formatoBBDD = "yyyy-MM-dd HH:mm";
        final String formatoParaMostrar = "dd/MM/yyyy HH:mm";
        return pedidos.stream().map(pedido -> {
            PedidoCercanoModel cercano = new PedidoCercanoModel();
            final String productoConDescripcion = pedido.getCarga().getProducto().getNombre() + " " + pedido.getCarga().getDescripcion();
            final SimpleDateFormat sdf = new SimpleDateFormat(formatoBBDD);
            cercano.setId(pedido.getId());
            cercano.setIdCarga(pedido.getCarga().getId());
            cercano.setProducto(productoConDescripcion);
            cercano.setLatDesde(pedido.getUbicacionDesde().getLatitud());
            cercano.setLngDesde(pedido.getUbicacionDesde().getLongitud());
            cercano.setDesde(pedido.getUbicacionDesde().getDireccion());
            cercano.setHasta(pedido.getUbicacionHasta().getDireccion());
            try {
                Date fechaCargaDesde = sdf.parse(pedido.getPeriodoDeCarga().getInicio());
                Date fechaCargaHasta = sdf.parse(pedido.getPeriodoDeCarga().getFinalizacion());
                Date fechaDescargaDesde = sdf.parse(pedido.getPeriodoDeDescarga().getInicio());
                Date dechaDescargaHasta = sdf.parse(pedido.getPeriodoDeDescarga().getFinalizacion());
                sdf.applyPattern(formatoParaMostrar);
                cercano.setFechaCargaDesde(sdf.format(fechaCargaDesde));
                cercano.setFechaCargaHasta(sdf.format(fechaCargaHasta));
                cercano.setFechaDescargaDesde(sdf.format(fechaDescargaDesde));
                cercano.setFechaDescargaHasta(sdf.format(dechaDescargaHasta));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return cercano;
        }).collect(Collectors.toSet());
    }

}
