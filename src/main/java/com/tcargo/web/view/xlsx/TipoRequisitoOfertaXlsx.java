package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.TipoRequisitoOferta;
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

@Component("tipoRequisitoOferta-List.xlsx")
public class TipoRequisitoOfertaXlsx extends AbstractXlsxView implements Xlsx<TipoRequisitoOferta> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_MODIFICACION = 3;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "RequisitosOfertas");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConNombre(filaTitulos);
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoRequisitoOferta> requisitos, int fila) {
        for (TipoRequisitoOferta requisito : requisitos) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, requisito);
            crearCeldaDescripcion(filaDatos, requisito);
            crearCeldaPais(filaDatos, requisito);
            crearCeldaModificacion(filaDatos, requisito);
        }
    }

    private void crearCeldaNombre(Row filaDatos, TipoRequisitoOferta requisito) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(requisito.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoRequisitoOferta requisito) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (requisito.getDescripcion() != null && !requisito.getDescripcion().isEmpty()) {
            descripcion.setCellValue(requisito.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, TipoRequisitoOferta requisito) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(requisito.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, TipoRequisitoOferta requisito) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(requisito.getModificacion());
    }

}
