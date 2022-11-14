package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.TipoDocumento;
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

@Component("tipoDocumento-list.xlsx")
public class TipoDocumentoXlsx extends AbstractXlsxView implements Xlsx<TipoDocumento> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_MODIFICACION = 3;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "TiposDeDocumento");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConNombre(filaTitulos);
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoDocumento> tiposDocumento, int fila) {
        for (TipoDocumento tipoDocumento : tiposDocumento) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, tipoDocumento);
            crearCeldaDescripcion(filaDatos, tipoDocumento);
            crearCeldaPais(filaDatos, tipoDocumento);
            crearCeldaModificacion(filaDatos, tipoDocumento);
        }
    }

    private void crearCeldaNombre(Row filaDatos, TipoDocumento tipoDocumento) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(tipoDocumento.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoDocumento tipoDocumento) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (tipoDocumento.getDescripcion() != null && !tipoDocumento.getDescripcion().isEmpty()) {
            descripcion.setCellValue(tipoDocumento.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, TipoDocumento tipoDocumento) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(tipoDocumento.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, TipoDocumento tipoDocumento) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(tipoDocumento.getModificacion());
    }

}
