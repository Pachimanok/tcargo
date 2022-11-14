package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.TipoContenedor;
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

@Component("tipoContenedor-list.xlsx")
public class TipoContenedorXlsx extends AbstractXlsxView implements Xlsx<TipoContenedor> {

    private static final int COLUMNA_CARACTERISTICAS = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_MODIFICACION = 3;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "TiposDeContenedor");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConCaracteristicas(filaTitulos);
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoContenedor> tiposContenedor, int fila) {
        for (TipoContenedor tipoContenedor : tiposContenedor) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaCaracteristicas(filaDatos, tipoContenedor);
            crearCeldaDescripcion(filaDatos, tipoContenedor);
            crearCeldaPais(filaDatos, tipoContenedor);
            crearCeldaModificacion(filaDatos, tipoContenedor);
        }
    }

    private void crearCeldaCaracteristicas(Row filaDatos, TipoContenedor tipoContenedor) {
        Cell caracteristicas = filaDatos.createCell(COLUMNA_CARACTERISTICAS);
        caracteristicas.setCellValue(tipoContenedor.getCaracteristicas());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoContenedor tipoContenedor) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (tipoContenedor.getDescripcion() != null && !tipoContenedor.getDescripcion().isEmpty()) {
            descripcion.setCellValue(tipoContenedor.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, TipoContenedor tipoContenedor) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(tipoContenedor.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, TipoContenedor tipoContenedor) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(tipoContenedor.getModificacion());
    }

}
