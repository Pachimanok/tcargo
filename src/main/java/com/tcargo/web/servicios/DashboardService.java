package com.tcargo.web.servicios;

import com.tcargo.web.entidades.*;
import com.tcargo.web.modelos.DashboarAdminReporteModel;
import com.tcargo.web.modelos.DashboarDadorReporteModel;
import com.tcargo.web.modelos.DashboarTransportadorReporteModel;
import com.tcargo.web.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DashboardService {

    private final TransportadorRepository transportadorRepository;
    private final PedidoRepository pedidoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ViajeRepository viajeRepository;
    private final ContraOfertaRepository contraOfertaRepository;
    private final ChoferRepository choferRepository;
    private final CoincidenciaRepository matchRepository;
    private final ViajePersonalRepository viajePersonalRepository;

    public DashboarAdminReporteModel armarReporteSuperAdmin() {
        DashboarAdminReporteModel reporte = new DashboarAdminReporteModel();

        for (String f : transportadorRepository.flotaTotalPorTransportador()) {
            String[] cadena = f.split(",");
            reporte.getCantidadCamionerPorTransportador().add(Integer.parseInt(cadena[0]));
            reporte.getNombres().add(cadena[1]);
        }

        for (String v : vehiculoRepository.cantidadVehiculosPorTipo()) {
            String[] cadena = v.split(",");
            reporte.getCantidadCamionesPorTipo().add(Integer.parseInt(cadena[0]));
            reporte.getTipoCamiones().add(cadena[1]);
        }

        reporte.setCargasConOfertas(pedidoRepository.cantidadPedidosConOFertas().size());
        reporte.setCargasTotales(pedidoRepository.buscarActivosSinPage().size());
        reporte.setCargasMatcheados(pedidoRepository.cantidadmatcheados().size());
        reporte.setCargasNegativas(viajeRepository.buscarActivosCargaNegativaList().size());
        reporte.setTransportadorasTotales(transportadorRepository.buscarActivos().size());
        reporte.setVehiculosTotales(vehiculoRepository.buscarActivos().size());

        return reporte;
    }

    public DashboarAdminReporteModel armarReporteAdminLocal(Pais pais) {
        DashboarAdminReporteModel reporte = new DashboarAdminReporteModel();

        for (String f : transportadorRepository.flotaTotalPorTransportadorParaAdminLocal(pais.getId())) {
            String[] cadena = f.split(",");
            reporte.getCantidadCamionerPorTransportador().add(Integer.parseInt(cadena[0]));
            reporte.getNombres().add(cadena[1]);
        }
        int totalVehiculos = 0;
        for (String v : vehiculoRepository.cantidadVehiculosPorTipoParaAdminLocal(pais.getId())) {
            String[] cadena = v.split(",");
            reporte.getCantidadCamionesPorTipo().add(Integer.parseInt(cadena[0]));
            reporte.getTipoCamiones().add(cadena[1]);
            totalVehiculos = totalVehiculos + Integer.parseInt(cadena[0]);
        }

        reporte.setCargasConOfertas(pedidoRepository.cantidadPedidosConOFertasParaAdminLocal(pais.getId()).size());
        reporte.setCargasTotales(pedidoRepository.buscarActivosSinPageParaAdminLocal(pais.getId()).size());
        reporte.setCargasMatcheados(pedidoRepository.cantidadmatcheadosParaAdminLocal(pais.getId()).size());
        reporte.setCargasNegativas(viajeRepository.buscarActivosCargaNegativaListParaAdminLocal(pais.getId()).size());
        reporte.setTransportadorasTotales(transportadorRepository.buscarActivosPorPaisList(pais.getId()).size());
        reporte.setVehiculosTotales(totalVehiculos);

        return reporte;
    }

    public DashboarTransportadorReporteModel armarReporteTransportador(Transportador transportador) {
        DashboarTransportadorReporteModel reporte = new DashboarTransportadorReporteModel();

        reporte.setCargasNegativas(viajeRepository.listarActivosCargaNegativaPorTransportadorId(transportador.getId()).size());
        reporte.setCargasOfertas(contraOfertaRepository.contarParaReporteTransportador(transportador.getUsuario().getId()));
        reporte.setChoferesTotales(choferRepository.contarChoferesPorTransportador(transportador.getId()));
        reporte.setMatchs(transportador.getCoincidencias().size());
        reporte.setVehiculosTotales(vehiculoRepository.buscarActivosPorTransportadorList(transportador.getId()).size());
        Double total = 0.0;
        Double dolares = 0.0;
        List<Map<String, Double>> test = new ArrayList<>(matchRepository.sumasPorTipodeMonedasTransportador(transportador.getId()));
        if (!test.isEmpty()) {
            reporte.setTotales(test);
        }
        reporte.setTotalMatchs(total);
        reporte.setTotalMatchDolar(dolares);

        return reporte;
    }

    public DashboarTransportadorReporteModel armarReporteChofer(Usuario usuario) {
        DashboarTransportadorReporteModel reporte = new DashboarTransportadorReporteModel();
        Chofer chofer = choferRepository.buscarPorUsuarioId(usuario.getId());
        Integer x = 0;
        Integer y = 0;
        try {
            x = viajeRepository.cantidadDeViajesPorChofer(chofer.getId());
            y = viajePersonalRepository.contrarViajesPorChofer(chofer.getId());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (x != null) {
            reporte.setViajes(x);
        } else {
            reporte.setViajes(0);
        }

//		CARGAS NEGATIVAS SERAN TRATADAS COMO VIAJES CONSOLIDADOS O VIAJES PERSONALES(SOLO EN LA ESTADISTICA)
        if (y != null) {
            reporte.setCargasNegativas(y);
        } else {
            reporte.setCargasNegativas(0);
        }
        return reporte;
    }

    public DashboarDadorReporteModel armarReporteDador(DadorDeCarga dador) {
        DashboarDadorReporteModel reporte = new DashboarDadorReporteModel();

        reporte.setPedidoPublicados(pedidoRepository.buscarActivosPorIdDadorList(dador.getUsuario().getId()).size());
        List<Coincidencia> matchs = matchRepository.buscarPorDador(dador.getUsuario().getId());
        reporte.setMatchs(matchs.size());
        reporte.setPedidoConOfertas(contraOfertaRepository.contarComoDadorNoPropias(dador.getUsuario().getId()));
        Double total = 0.0;
        Double dolares = 0.0;
        List<Map<String, Double>> test = new ArrayList<>(matchRepository.sumasPorTipodeMonedasDador(dador.getUsuario().getId()));
        if (!test.isEmpty()) {
            reporte.setTotales(test);
        }
        reporte.setTotalMatchs(total);
        reporte.setTotalMatchDolar(dolares);
        return reporte;
    }

}
