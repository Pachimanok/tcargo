package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.TipoCarga;
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

@Component("tipoCarga-list.xlsx")
public class TipoCargaXlsx extends AbstractXlsxView implements Xlsx<TipoCarga> {

    private static final int COLUMNA_CARACTERISTICAS = 0;
    private static final int COLUMNA_DESCRIPCION = 1;
    private static final int COLUMNA_PAIS = 2;
    private static final int COLUMNA_MODIFICACION = 3;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "TiposDeCarga");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        titulosTiposConCaracteristicas(filaTitulos);
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<TipoCarga> tiposDeCarga, int fila) {
        for (TipoCarga tipoDeCarga : tiposDeCarga) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaCaracteristicas(filaDatos, tipoDeCarga);
            crearCeldaDescripcion(filaDatos, tipoDeCarga);
            crearCeldaPais(filaDatos, tipoDeCarga);
            crearCeldaModificacion(filaDatos, tipoDeCarga);
        }
    }

    private void crearCeldaCaracteristicas(Row filaDatos, TipoCarga tipoCarga) {
        Cell caracteristicas = filaDatos.createCell(COLUMNA_CARACTERISTICAS);
        caracteristicas.setCellValue(tipoCarga.getCaracteristicas());
    }

    private void crearCeldaDescripcion(Row filaDatos, TipoCarga tipoCarga) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (tipoCarga.getDescripcion() != null && !tipoCarga.getDescripcion().isEmpty()) {
            descripcion.setCellValue(tipoCarga.getDescripcion());
        } else {
            descripcion.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaPais(Row filaDatos, TipoCarga tipoCarga) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(tipoCarga.getPais().getNombre());
    }

    private void crearCeldaModificacion(Row filaDatos, TipoCarga tipoCarga) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(tipoCarga.getModificacion());
    }

}
