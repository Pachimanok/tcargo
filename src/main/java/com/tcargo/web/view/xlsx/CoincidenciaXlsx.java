package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Coincidencia;
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

@Component("match-list.xlsx")
public class CoincidenciaXlsx extends AbstractXlsxView implements Xlsx<Coincidencia> {

    private static final int COLUMNA_COSTO = 0;
    private static final int COLUMNA_DETALLE = 1;
    private static final int COLUMNA_VEHICULO = 2;
    private static final int COLUMNA_DOMINIO_VEHICULO = 3;
    private static final int COLUMNA_REMOLQUE = 4;
    private static final int COLUMNA_DOMINIO_REMOLQUE = 5;
    private static final int COLUMNA_CHOFER = 6;
    private static final int COLUMNA_DOCUMENTO_CHOFER = 7;
    private static final int COLUMNA_MODIFICACION = 8;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Matches");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell costoTitulo = filaTitulos.createCell(COLUMNA_COSTO);
        costoTitulo.setCellValue("Costo");

        Cell detalleTitulo = filaTitulos.createCell(COLUMNA_DETALLE);
        detalleTitulo.setCellValue("Detalle");

        Cell vehiculoTitulo = filaTitulos.createCell(COLUMNA_VEHICULO);
        vehiculoTitulo.setCellValue("Vehiculo");

        Cell dominioVehiculoTitulo = filaTitulos.createCell(COLUMNA_DOMINIO_VEHICULO);
        dominioVehiculoTitulo.setCellValue("Dominio vehiculo");

        Cell remolqueTitulo = filaTitulos.createCell(COLUMNA_REMOLQUE);
        remolqueTitulo.setCellValue("Remolque");

        Cell dominioRemolqueTitulo = filaTitulos.createCell(COLUMNA_DOMINIO_REMOLQUE);
        dominioRemolqueTitulo.setCellValue("Dominio remolque");

        Cell choferTitulo = filaTitulos.createCell(COLUMNA_CHOFER);
        choferTitulo.setCellValue("Chofer");

        Cell documentoChoferTitulo = filaTitulos.createCell(COLUMNA_DOCUMENTO_CHOFER);
        documentoChoferTitulo.setCellValue("Identificacion");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Coincidencia> matches, int fila) {
        for (Coincidencia match : matches) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaCosto(filaDatos, match);
            crearCeldaDetalle(filaDatos, match);
            crearCeldaVehiculo(filaDatos, match);
            crearCeldaDominioVehiculo(filaDatos, match);
            crearCeldaRemolque(filaDatos, match);
            crearCeldaDominioRemolque(filaDatos, match);
            crearCeldaChofer(filaDatos, match);
            crearCeldaDocumentoChofer(filaDatos, match);
            crearCeldaModificacion(filaDatos, match);
        }
    }

    private void crearCeldaCosto(Row filaDatos, Coincidencia match) {
        Cell costo = filaDatos.createCell(COLUMNA_COSTO);
        costo.setCellValue(match.getCosto().toString());
    }

    private void crearCeldaDetalle(Row filaDatos, Coincidencia match) {
        Cell detalle = filaDatos.createCell(COLUMNA_DETALLE);
        if (match.getDetalle() != null) {
            detalle.setCellValue(match.getDetalle());
        } else {
            detalle.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaVehiculo(Row filaDatos, Coincidencia match) {
        Cell vehiculo = filaDatos.createCell(COLUMNA_VEHICULO);
        try {
            vehiculo.setCellValue(match.getViaje().getVehiculo().getTipoVehiculo().getNombre());
        } catch (NullPointerException npe) {
            vehiculo.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDominioVehiculo(Row filaDatos, Coincidencia match) {
        Cell dominioVehiculo = filaDatos.createCell(COLUMNA_DOMINIO_VEHICULO);
        try {
            dominioVehiculo.setCellValue(match.getViaje().getVehiculo().getDominio());
        } catch (NullPointerException npe) {
            dominioVehiculo.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaRemolque(Row filaDatos, Coincidencia match) {
        Cell remolque = filaDatos.createCell(COLUMNA_REMOLQUE);
        try {
            remolque.setCellValue(match.getViaje().getRemolque().getTipoRemolque().getCaracteristicas());
        } catch (NullPointerException npe) {
            remolque.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDominioRemolque(Row filaDatos, Coincidencia match) {
        Cell dominioRemolque = filaDatos.createCell(COLUMNA_DOMINIO_REMOLQUE);
        try {
            dominioRemolque.setCellValue(match.getViaje().getRemolque().getDominio());
        } catch (NullPointerException npe) {
            dominioRemolque.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaChofer(Row filaDatos, Coincidencia match) {
        Cell chofer = filaDatos.createCell(COLUMNA_CHOFER);
        try {
            chofer.setCellValue(match.getViaje().getChofer().getUsuario().getNombre());
        } catch (NullPointerException npe) {
            chofer.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDocumentoChofer(Row filaDatos, Coincidencia match) {
        Cell documentoChofer = filaDatos.createCell(COLUMNA_DOCUMENTO_CHOFER);
        try {
            String documento = match.getViaje().getChofer().getTipoDocumento().getNombre().concat(": ").concat(match.getViaje().getChofer().getDocumento());
            documentoChofer.setCellValue(documento);
        } catch (NullPointerException npe) {
            documentoChofer.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaModificacion(Row filaDatos, Coincidencia match) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(match.getModificacion());
    }

}
