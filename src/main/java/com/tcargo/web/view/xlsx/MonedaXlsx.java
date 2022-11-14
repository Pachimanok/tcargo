package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Moneda;
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

@Component("moneda-list.xlsx")
public class MonedaXlsx extends AbstractXlsxView implements Xlsx<Moneda> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_SIMBOLO = 2;
    private static final int COLUMNA_PAIS = 3;
    private static final int COLUMNA_MODIFICACION = 4;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Monedas");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell nombreTitulo = filaTitulos.createCell(COLUMNA_NOMBRE);
        nombreTitulo.setCellValue("Nombre");

        Cell descripcionTitulo = filaTitulos.createCell(COLUMNA_DESCRIPCION);
        descripcionTitulo.setCellValue("Descripcion");

        Cell simboloTitulo = filaTitulos.createCell(COLUMNA_SIMBOLO);
        simboloTitulo.setCellValue("Simbolo");

        Cell paisTitulo = filaTitulos.createCell(COLUMNA_PAIS);
        paisTitulo.setCellValue("Pais");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Moneda> monedas, int fila) {
        for (Moneda moneda : monedas) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, moneda);
            crearCeldaDescripcion(filaDatos, moneda);
            crearCeldaSimbolo(filaDatos, moneda);
            crearCeldaPais(filaDatos, moneda);
            crearCeldaModificacion(filaDatos, moneda);
        }
    }

    private void crearCeldaNombre(Row filaDatos, Moneda moneda) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(moneda.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, Moneda moneda) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (moneda.getDescripcion() != null && !moneda.getDescripcion().isEmpty()) {
            descripcion.setCellValue(moneda.getDescripcion());
        } else {
            descripcion.setCellValue("No asignado");
        }
    }

    private void crearCeldaSimbolo(Row filaDatos, Moneda moneda) {
        Cell simbolo = filaDatos.createCell(COLUMNA_SIMBOLO);
        if (moneda.getSimbolo() != null && !moneda.getSimbolo().isEmpty()) {
            simbolo.setCellValue(moneda.getSimbolo());
        } else {
            simbolo.setCellValue("No asignado");
        }
    }

    private void crearCeldaPais(Row filaDatos, Moneda moneda) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(moneda.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, Moneda moneda) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(moneda.getModificacion());
    }

}
