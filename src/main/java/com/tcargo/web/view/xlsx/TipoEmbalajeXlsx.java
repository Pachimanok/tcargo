package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.TipoEmbalaje;
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

@Component("tipoEmbalaje-list.xlsx")
public class TipoEmbalajeXlsx extends AbstractXlsxView implements Xlsx<TipoEmbalaje> {

    private static final int COLUMNA_CARACTERISTICAS = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_MODIFICACION = 3;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "TiposDeEmbalaje");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConCaracteristicas(filaTitulos);
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoEmbalaje> tiposEmbalaje, int fila) {
        for (TipoEmbalaje tipoEmbalaje : tiposEmbalaje) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaCaracteristicas(filaDatos, tipoEmbalaje);
            crearCeldaDescripcion(filaDatos, tipoEmbalaje);
            crearCeldaPais(filaDatos, tipoEmbalaje);
            crearCeldaModificacion(filaDatos, tipoEmbalaje);
        }
    }

    private void crearCeldaCaracteristicas(Row filaDatos, TipoEmbalaje tipoEmbalaje) {
        Cell caracteristicas = filaDatos.createCell(COLUMNA_CARACTERISTICAS);
        caracteristicas.setCellValue(tipoEmbalaje.getCaracteristicas());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoEmbalaje tipoEmbalaje) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (tipoEmbalaje.getDescripcion() != null && !tipoEmbalaje.getDescripcion().isEmpty()) {
            descripcion.setCellValue(tipoEmbalaje.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, TipoEmbalaje tipoEmbalaje) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(tipoEmbalaje.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, TipoEmbalaje tipoEmbalaje) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(tipoEmbalaje.getModificacion());
    }

}
