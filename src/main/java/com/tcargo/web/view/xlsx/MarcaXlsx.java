package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Marca;
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

@Component("marca-list.xlsx")
public class MarcaXlsx extends AbstractXlsxView implements Xlsx<Marca> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_MODIFICACION = 1;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Marcas");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell nombreTitulo = filaTitulos.createCell(COLUMNA_NOMBRE);
        nombreTitulo.setCellValue("Nombre");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Marca> marcas, int fila) {
        for (Marca marca : marcas) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, marca);
            crearCeldaModificacion(filaDatos, marca);
        }
    }

    private void crearCeldaNombre(Row filaDatos, Marca marca) {
        Cell cellUno = filaDatos.createCell(COLUMNA_NOMBRE);
        cellUno.setCellValue(marca.getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, Marca marca) {
        Cell cellDos = filaDatos.createCell(COLUMNA_MODIFICACION);
        cellDos.setCellValue(marca.getModificacion());
    }

}
