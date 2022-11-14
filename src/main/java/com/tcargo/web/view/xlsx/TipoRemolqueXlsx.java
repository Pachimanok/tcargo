package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.TipoRemolque;
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

@Component("tipoRemolque-list.xlsx")
public class TipoRemolqueXlsx extends AbstractXlsxView implements Xlsx<TipoRemolque> {

    private static final int COLUMNA_CARACTERISTICAS = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_MODIFICACION = 3;

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, @NonNull HttpServletRequest request, HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "TiposDeRemolque");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConCaracteristicas(filaTitulos);
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoRemolque> tiposRemolque, int fila) {
        for (TipoRemolque tipoRemolque : tiposRemolque) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaCaracteristicas(filaDatos, tipoRemolque);
            crearCeldaDescripcion(filaDatos, tipoRemolque);
            crearCeldaPais(filaDatos, tipoRemolque);
            crearCeldaModificacion(filaDatos, tipoRemolque);
        }
    }

    private void crearCeldaCaracteristicas(Row filaDatos, TipoRemolque tipoRemolque) {
        Cell caracteristicas = filaDatos.createCell(COLUMNA_CARACTERISTICAS);
        caracteristicas.setCellValue(tipoRemolque.getCaracteristicas());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoRemolque tipoRemolque) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (tipoRemolque.getDescripcion() != null && !tipoRemolque.getDescripcion().isEmpty()) {
            descripcion.setCellValue(tipoRemolque.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, TipoRemolque tipoRemolque) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(tipoRemolque.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, TipoRemolque tipoRemolque) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(tipoRemolque.getModificacion());
    }

}
