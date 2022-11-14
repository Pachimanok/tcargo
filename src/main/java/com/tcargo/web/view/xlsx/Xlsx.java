package com.tcargo.web.view.xlsx;

import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.PAGE_LABEL;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

public interface Xlsx<T> {

    int FILA_TITULOS = 0;
    int PRIMERA_FILA_DATOS = 1;

    int COLUMNA_CARACTERISTICAS = 0;
    int COLUMNA_NOMBRE = 0;
    int COLUMNA_DESCRIPCION = 1;
    int COLUMNA_PAIS = 2;
    int COLUMNA_MODIFICACION = 3;

    default void armarExcel(Map<String, Object> model, Workbook workbook, HttpServletResponse response, String nombreArchivo) {
        response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx\"");
        @SuppressWarnings("unchecked")
        List<T> matches = ((Page<T>) model.get(PAGE_LABEL)).getContent();

        Sheet hoja = workbook.createSheet();
        Row filaTitulos = hoja.createRow(FILA_TITULOS);

        crearFilaTitulos(filaTitulos);
        setEstiloTitulos(filaTitulos, workbook);
        crearFilasDatos(hoja, matches, PRIMERA_FILA_DATOS);
    }

    default void setEstiloTitulos(Row filaTitulos, Workbook workbook) {
        Font fuente = workbook.createFont();
        fuente.setBold(true);

        CellStyle estiloTitulos = workbook.createCellStyle();
        estiloTitulos.setFont(fuente);

        for (int i = 0; i < filaTitulos.getLastCellNum(); i++) {
            filaTitulos.getCell(i).setCellStyle(estiloTitulos);
        }
    }

    void crearFilaTitulos(Row filaTitulos);

    void crearFilasDatos(Sheet hoja, List<T> t, int fila);

    default void titulosTiposConCaracteristicas(Row filaTitulos) {
        Cell caracteristicasTitulo = filaTitulos.createCell(COLUMNA_CARACTERISTICAS);
        caracteristicasTitulo.setCellValue("Caracteristicas");

        Cell descripcionTitulo = filaTitulos.createCell(COLUMNA_DESCRIPCION);
        descripcionTitulo.setCellValue("Descripcion");

        Cell paisTitulo = filaTitulos.createCell(COLUMNA_PAIS);
        paisTitulo.setCellValue("Pais");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    default void titulosTiposConNombre(Row filaTitulos) {
        Cell nombreTitulo = filaTitulos.createCell(COLUMNA_NOMBRE);
        nombreTitulo.setCellValue("Nombre");

        Cell descripcionTitulo = filaTitulos.createCell(COLUMNA_DESCRIPCION);
        descripcionTitulo.setCellValue("Descripcion");

        Cell paisTitulo = filaTitulos.createCell(COLUMNA_PAIS);
        paisTitulo.setCellValue("Pais");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

}
