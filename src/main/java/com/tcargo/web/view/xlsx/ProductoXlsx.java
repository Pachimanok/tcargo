package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Producto;
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

@Component("producto-list.xlsx")
public class ProductoXlsx extends AbstractXlsxView implements Xlsx<Producto> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_MODIFICACION = 3;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Productos");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConNombre(filaTitulos);
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Producto> productos, int fila) {
        for (Producto producto : productos) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, producto);
            crearCeldaDescripcion(filaDatos, producto);
            crearCeldaPais(filaDatos, producto);
            crearCeldaModificacion(filaDatos, producto);
        }
    }

    private void crearCeldaNombre(Row filaDatos, Producto producto) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(producto.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, Producto producto) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
            descripcion.setCellValue(producto.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, Producto producto) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(producto.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, Producto producto) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(producto.getModificacion());
    }

}
