package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Modelo;
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

@Component("modelo-list.xlsx")
public class ModeloXlsx extends AbstractXlsxView implements Xlsx<Modelo> {

    private static final int COLUMNA_MARCA = 0;
    private static final int COLUMNA_MODELO = 1;
    private static final int COLUMNA_DESCRIPCION = 2;
    private static final int COLUMNA_PAIS = 3;
    private static final int COLUMNA_MODIFICACION = 4;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Modelos");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell marcaTitulo = filaTitulos.createCell(COLUMNA_MARCA);
        marcaTitulo.setCellValue("Marca");

        Cell modeloTitulo = filaTitulos.createCell(COLUMNA_MODELO);
        modeloTitulo.setCellValue("Modelo");

        Cell descripcionTitulo = filaTitulos.createCell(COLUMNA_DESCRIPCION);
        descripcionTitulo.setCellValue("Descripcion");

        Cell paisTitulo = filaTitulos.createCell(COLUMNA_PAIS);
        paisTitulo.setCellValue("Pais");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Modelo> modelos, int fila) {
        for (Modelo modelo : modelos) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaMarca(filaDatos, modelo);
            crearCeldaModelo(filaDatos, modelo);
            crearCeldaDescripcion(filaDatos, modelo);
            crearCeldaPais(filaDatos, modelo);
            crearCeldaModificacion(filaDatos, modelo);
        }
    }

    private void crearCeldaMarca(Row filaDatos, Modelo modelo) {
        Cell marca = filaDatos.createCell(COLUMNA_MARCA);
        marca.setCellValue(modelo.getMarca().getNombre());
    }

    private void crearCeldaModelo(Row filaDatos, Modelo modelo) {
        Cell modeloCelda = filaDatos.createCell(COLUMNA_MODELO);
        modeloCelda.setCellValue(modelo.getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, Modelo modelo) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (modelo.getDescripcion() != null && !modelo.getDescripcion().isEmpty()) {
            descripcion.setCellValue(modelo.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, Modelo modelo) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(modelo.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, Modelo modelo) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(modelo.getModificacion());
    }

}
