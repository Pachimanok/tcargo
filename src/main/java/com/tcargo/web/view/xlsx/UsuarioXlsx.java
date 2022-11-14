package com.tcargo.web.view.xlsx;

import com.tcargo.web.modelos.UsuarioModelReporte;
import com.tcargo.web.utiles.Fecha;

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

@Component("usuario-list.xlsx")
public class UsuarioXlsx extends AbstractXlsxView implements Xlsx<UsuarioModelReporte> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_TELEFONO = 1;
    private static final int COLUMNA_EMAIL = 2;
    private static final int COLUMNA_PAIS = 3;
    private static final int COLUMNA_CUIT = 4;
    private static final int COLUMNA_ROL = 5;
    private static final int COLUMNA_MODIFICACION = 6;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Usuarios");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell nombreTitulo = filaTitulos.createCell(COLUMNA_NOMBRE);
        nombreTitulo.setCellValue("Nombre");

        Cell telefonoTitulo = filaTitulos.createCell(COLUMNA_TELEFONO);
        telefonoTitulo.setCellValue("Telefono");

        Cell mailTitulo = filaTitulos.createCell(COLUMNA_EMAIL);
        mailTitulo.setCellValue("E-Mail");

        Cell paisTitulo = filaTitulos.createCell(COLUMNA_PAIS);
        paisTitulo.setCellValue("País");

        Cell cuitTitulo = filaTitulos.createCell(COLUMNA_CUIT);
        cuitTitulo.setCellValue("Cuit/Cuil");

        Cell rolTitulo = filaTitulos.createCell(COLUMNA_ROL);
        rolTitulo.setCellValue("Rol");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<UsuarioModelReporte> usuarios, int fila) {
        for (UsuarioModelReporte usuario : usuarios) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, usuario);
            crearCeldaTelefono(filaDatos, usuario);
            crearCeldaEmail(filaDatos, usuario);
            crearCeldaPais(filaDatos, usuario);
            crearCeldaCuit(filaDatos, usuario);
            crearCeldaRol(filaDatos, usuario);
            crearCeldaModificacion(filaDatos, usuario);
        }
    }

    private void crearCeldaNombre(Row filaDatos, UsuarioModelReporte usuario) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        nombre.setCellValue(usuario.getNombre());
    }

    private void crearCeldaTelefono(Row filaDatos, UsuarioModelReporte usuario) {
        Cell telefono = filaDatos.createCell(COLUMNA_TELEFONO);
        telefono.setCellValue(usuario.getTelefono());
    }

    private void crearCeldaEmail(Row filaDatos, UsuarioModelReporte usuario) {
        Cell mail = filaDatos.createCell(COLUMNA_EMAIL);
        mail.setCellValue(usuario.getMail());
    }

    private void crearCeldaPais(Row filaDatos, UsuarioModelReporte usuario) {
        Cell pais = filaDatos.createCell(COLUMNA_PAIS);
        pais.setCellValue(usuario.getNombrePais());
    }

    private void crearCeldaCuit(Row filaDatos, UsuarioModelReporte usuario) {
        Cell cuit = filaDatos.createCell(COLUMNA_CUIT);
        cuit.setCellValue(usuario.getCuit());
    }

    private void crearCeldaRol(Row filaDatos, UsuarioModelReporte usuario) {
        Cell rol = filaDatos.createCell(COLUMNA_ROL);
        rol.setCellValue(usuario.getRol().toString());
    }

    private void crearCeldaModificacion(Row filaDatos, UsuarioModelReporte usuario) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(Fecha.formatFecha(usuario.getModificacion()));
    }

}
