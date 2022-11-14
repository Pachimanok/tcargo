package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Viaje;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.NO_ASIGNADO;

@Component("viaje-list.xlsx")
public class ViajeXlsx extends AbstractXlsxView implements Xlsx<Viaje> {

    private static final int COLUMNA_DESDE = 0;
    private static final int COLUMNA_HASTA = 1;
    private static final int COLUMNA_PARTIDA = 2;
    private static final int COLUMNA_LLEGADA = 3;
    private static final int COLUMNA_VEHICULO = 4;
    private static final int COLUMNA_DOMINIO_VEHICULO = 5;
    private static final int COLUMNA_REMOLQUE = 6;
    private static final int COLUMNA_DOMINIO_REMOLQUE = 7;
    private static final int COLUMNA_PARADAS = 8;
    private static final int COLUMNA_MODIFICACION = 9;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Viajes");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell desdeTitulo = filaTitulos.createCell(COLUMNA_DESDE);
        desdeTitulo.setCellValue("Desde");

        Cell hastaTitulo = filaTitulos.createCell(COLUMNA_HASTA);
        hastaTitulo.setCellValue("Hasta");

        Cell partidaTitulo = filaTitulos.createCell(COLUMNA_PARTIDA);
        partidaTitulo.setCellValue("Partida");

        Cell llegadaTitulo = filaTitulos.createCell(COLUMNA_LLEGADA);
        llegadaTitulo.setCellValue("Llegada");

        Cell vehiculoTitulo = filaTitulos.createCell(COLUMNA_VEHICULO);
        vehiculoTitulo.setCellValue("Vehiculo");

        Cell dominioVehiculoTitulo = filaTitulos.createCell(COLUMNA_DOMINIO_VEHICULO);
        dominioVehiculoTitulo.setCellValue("Dominio");

        Cell remolqueTitulo = filaTitulos.createCell(COLUMNA_REMOLQUE);
        remolqueTitulo.setCellValue("Remolque");

        Cell dominioRemolqueTitulo = filaTitulos.createCell(COLUMNA_DOMINIO_REMOLQUE);
        dominioRemolqueTitulo.setCellValue("Dominio");

        Cell paradasTitulo = filaTitulos.createCell(COLUMNA_PARADAS);
        paradasTitulo.setCellValue("Paradas");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Viaje> viajes, int fila) {
        for (Viaje viaje : viajes) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaDesde(filaDatos, viaje);
            crearCeldaHasta(filaDatos, viaje);
            crearCeldaPartida(filaDatos, viaje);
            crearCeldaLlegada(filaDatos, viaje);
            crearCeldaVehiculo(filaDatos, viaje);
            crearCeldaDominioVehiculo(filaDatos, viaje);
            crearCeldaRemolque(filaDatos, viaje);
            crearCeldaDominioRemolque(filaDatos, viaje);
            crearCeldaParadas(filaDatos, viaje);
            crearCeldaModificacion(filaDatos, viaje);
        }
    }

    private void crearCeldaDesde(Row filaDatos, Viaje viaje) {
        Cell desde = filaDatos.createCell(COLUMNA_DESDE);
        desde.setCellValue(viaje.getUbicacionInicial().getDireccion());
    }

    private void crearCeldaHasta(Row filaDatos, Viaje viaje) {
        Cell hasta = filaDatos.createCell(COLUMNA_HASTA);
        hasta.setCellValue(viaje.getUbicacionFinal().getDireccion());
    }

    private void crearCeldaPartida(Row filaDatos, Viaje viaje) {
        Cell partida = filaDatos.createCell(COLUMNA_PARTIDA);
        if (viaje.getPartidaCargaNegativa() != null) {
            partida.setCellValue(viaje.getPartidaCargaNegativa().toString());
        } else {
            partida.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaLlegada(Row filaDatos, Viaje viaje) {
        Cell llegada = filaDatos.createCell(COLUMNA_LLEGADA);
        if (viaje.getLlegadaCargaNegativa() != null) {
            llegada.setCellValue(viaje.getLlegadaCargaNegativa().toString());
        } else {
            llegada.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaVehiculo(Row filaDatos, Viaje viaje) {
        Cell vehiculo = filaDatos.createCell(COLUMNA_VEHICULO);
        if (viaje.getVehiculo() != null) {
            vehiculo.setCellValue(viaje.getVehiculo().getTipoVehiculo().getNombre());
        } else {
            vehiculo.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDominioVehiculo(Row filaDatos, Viaje viaje) {
        Cell dominioVehiculo = filaDatos.createCell(COLUMNA_DOMINIO_VEHICULO);
        if (viaje.getVehiculo() != null) {
            dominioVehiculo.setCellValue(viaje.getVehiculo().getDominio());
        } else {
            dominioVehiculo.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaRemolque(Row filaDatos, Viaje viaje) {
        Cell remolque = filaDatos.createCell(COLUMNA_REMOLQUE);
        if (viaje.getRemolque() != null) {
            remolque.setCellValue(viaje.getRemolque().getTipoRemolque().getCaracteristicas());
        } else {
            remolque.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDominioRemolque(Row filaDatos, Viaje viaje) {
        Cell dominioRemolque = filaDatos.createCell(COLUMNA_DOMINIO_REMOLQUE);
        if (viaje.getRemolque() != null) {
            dominioRemolque.setCellValue(viaje.getRemolque().getDominio());
        } else {
            dominioRemolque.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaParadas(Row filaDatos, Viaje viaje) {
        Cell paradas = filaDatos.createCell(COLUMNA_PARADAS);
        StringBuilder sb = new StringBuilder();
        if (viaje.getWayPoints() != null && !viaje.getWayPoints().isEmpty()) {
            for (String wayPoint : viaje.getWayPoints()) {
                if (wayPoint.contains("direccion:")) {
                    sb.append(wayPoint.split("direccion:")[1]).append("\n");
                } else {
                    sb.append(wayPoint).append("\n");
                }

            }
            sb.deleteCharAt(sb.lastIndexOf("\n"));
            paradas.setCellValue(sb.toString());
        } else {
            paradas.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaModificacion(Row filaDatos, Viaje viaje) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(viaje.getModificacion());
    }

}
