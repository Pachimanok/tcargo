package com.tcargo.web.view.xlsx;

import   com.tcargo.web.entidades.TipoVehiculo;
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

@Component("tipoVehiculo-list.xlsx")
public class TipoVehiculoXlsx extends AbstractXlsxView implements Xlsx<TipoVehiculo> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_MODIFICACION = 2;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "TiposDeVehiculos");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell caracteristicasTitulo = filaTitulos.createCell(COLUMNA_NOMBRE);
        caracteristicasTitulo.setCellValue("Nombre");

        Cell descripcionTitulo = filaTitulos.createCell(COLUMNA_DESCRIPCION);
        descripcionTitulo.setCellValue("Descripcion");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoVehiculo> TipoVehiculoes, int fila) {
        for (TipoVehiculo TipoVehiculo : TipoVehiculoes) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, TipoVehiculo);
            crearCeldaDescripcion(filaDatos, TipoVehiculo);
            crearCeldaModificacion(filaDatos, TipoVehiculo);
        }
    }

    private void crearCeldaNombre(Row filaDatos, TipoVehiculo TipoVehiculo) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(TipoVehiculo.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoVehiculo TipoVehiculo) {
        Cell cellDos = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (TipoVehiculo.getDescripcion() != null && !TipoVehiculo.getDescripcion().isEmpty()) {
            cellDos.setCellValue(TipoVehiculo.getDescripcion());
        } else {
            cellDos.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaModificacion(Row filaDatos, TipoVehiculo TipoVehiculo) {
        Cell cellTres = filaDatos.createCell(COLUMNA_MODIFICACION);
        cellTres.setCellValue(TipoVehiculo.getModificacion());
    }

}
