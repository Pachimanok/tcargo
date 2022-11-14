package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.TipoDocumentacion;
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

@Component("tipoDocumentacion-List.xlsx")
public class TipoDocumentacionXlsx extends AbstractXlsxView implements Xlsx<TipoDocumentacion> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_OBLIGATORIO = 3;
    private static final int COLUMNA_MODIFICACION = 4;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "TiposDeDocumentacion");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConNombre(filaTitulos);

        filaTitulos.getCell(COLUMNA_OBLIGATORIO).setCellValue("Obligatorio para");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoDocumentacion> tiposDocumentacion, int fila) {
        for (TipoDocumentacion tipoDocumentacion : tiposDocumentacion) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, tipoDocumentacion);
            crearCeldaDescripcion(filaDatos, tipoDocumentacion);
            crearCeldaPais(filaDatos, tipoDocumentacion);
            crearCeldaObligatorio(filaDatos, tipoDocumentacion);
            crearCeldaModificacion(filaDatos, tipoDocumentacion);
        }
    }

    private void crearCeldaNombre(Row filaDatos, TipoDocumentacion tipoDocumentacion) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(tipoDocumentacion.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoDocumentacion tipoDocumentacion) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (tipoDocumentacion.getDescripcion() != null && !tipoDocumentacion.getDescripcion().isEmpty()) {
            descripcion.setCellValue(tipoDocumentacion.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, TipoDocumentacion tipoDocumentacion) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(tipoDocumentacion.getPais().getNombre());
    }

    private void crearCeldaObligatorio(Row filaDatos, TipoDocumentacion tipoDocumentacion) {
        Cell obligatorio = filaDatos.createCell(COLUMNA_OBLIGATORIO);
        StringBuilder sb = new StringBuilder();

        if (tipoDocumentacion.isObligatorioChofer()) {
            sb.append("-Chofer").append("\n");
        }

        if (tipoDocumentacion.isObligatorioRemolque()) {
            sb.append("-Remolque").append("\n");
        }

        if (tipoDocumentacion.isObligatorioVehiculo()) {
            sb.append("-Vehiculo").append("\n");
        }

        sb.deleteCharAt(sb.lastIndexOf("\n"));
        obligatorio.setCellValue(sb.toString());
    }

    private void crearCeldaModificacion(Row filaDatos, TipoDocumentacion tipoDocumentacion) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(tipoDocumentacion.getModificacion());
    }

}
