package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Pais;
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

@Component("pais-list.xlsx")
public class PaisXlsx extends AbstractXlsxView implements Xlsx<Pais> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_MODIFICACION = 2;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Paises");
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
    public void crearFilasDatos(Sheet hoja, List<Pais> paises, int fila) {
        for (Pais pais : paises) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, pais);
            crearCeldaDescripcion(filaDatos, pais);
            crearCeldaModificacion(filaDatos, pais);
        }
    }

    private void crearCeldaNombre(Row filaDatos, Pais pais) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(pais.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, Pais pais) {
        Cell cellDos = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (pais.getDescripcion() != null && !pais.getDescripcion().isEmpty()) {
            cellDos.setCellValue(pais.getDescripcion());
        } else {
            cellDos.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaModificacion(Row filaDatos, Pais pais) {
        Cell cellTres = filaDatos.createCell(COLUMNA_MODIFICACION);
        cellTres.setCellValue(pais.getModificacion());
    }

}
