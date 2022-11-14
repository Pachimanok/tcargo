package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.Transportador;
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

@Component("transportador-list.xlsx")
public class TransportadorXlsx extends AbstractXlsxView implements Xlsx<Transportador> {

    private static final int COLUMNA_NOMBRE = 0;
    private static final int COLUMNA_RAZON_SOCIAL = 1;
    private static final int COLUMNA_DOMICILIO = 2;
    private static final int COLUMNA_TELEFONO = 3;
    private static final int COLUMNA_EMAIL = 4;
    private static final int COLUMNA_CHOFERES = 5;
    private static final int COLUMNA_REMOLQUES = 6;
    private static final int COLUMNA_VEHICULOS = 7;
    private static final int COLUMNA_MATCHES = 8;
    private static final int COLUMNA_COMISION = 9;
    private static final int COLUMNA_MODIFICACION = 10;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Transportadoras");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell nombreTitulo = filaTitulos.createCell(COLUMNA_NOMBRE);
        nombreTitulo.setCellValue("Nombre");

        Cell razonSocialTitulo = filaTitulos.createCell(COLUMNA_RAZON_SOCIAL);
        razonSocialTitulo.setCellValue("Razon Social");

        Cell domicilioTitulo = filaTitulos.createCell(COLUMNA_DOMICILIO);
        domicilioTitulo.setCellValue("Domicilio");

        Cell telefonoTitulo = filaTitulos.createCell(COLUMNA_TELEFONO);
        telefonoTitulo.setCellValue("Telefono");

        Cell emailTitulo = filaTitulos.createCell(COLUMNA_EMAIL);
        emailTitulo.setCellValue("E-Mail");

        Cell choferesTitulo = filaTitulos.createCell(COLUMNA_CHOFERES);
        choferesTitulo.setCellValue("Choferes");

        Cell remolquesTitulo = filaTitulos.createCell(COLUMNA_REMOLQUES);
        remolquesTitulo.setCellValue("Remolques");

        Cell vehiculosTitulo = filaTitulos.createCell(COLUMNA_VEHICULOS);
        vehiculosTitulo.setCellValue("Vehiculos");

        Cell matchesTitulo = filaTitulos.createCell(COLUMNA_MATCHES);
        matchesTitulo.setCellValue("Matches");

        Cell comisionTitulo = filaTitulos.createCell(COLUMNA_COMISION);
        comisionTitulo.setCellValue("Comision");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Transportador> transportadores, int fila) {
        for (Transportador transportador : transportadores) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNombre(filaDatos, transportador);
            crearCeldaRazonSocial(filaDatos, transportador);
            crearCeldaDomicilio(filaDatos, transportador);
            crearCeldaTelefono(filaDatos, transportador);
            crearCeldaEmail(filaDatos, transportador);
            crearCeldaChoferes(filaDatos, transportador);
            crearCeldaRemolques(filaDatos, transportador);
            crearCeldaVehiculos(filaDatos, transportador);
            crearCeldaMatches(filaDatos, transportador);
            crearCeldaComision(filaDatos, transportador);
            crearCeldaModificacion(filaDatos, transportador);
        }
    }

    private void crearCeldaNombre(Row filaDatos, Transportador transportador) {
        Cell nombre = filaDatos.createCell(COLUMNA_NOMBRE);
        if (transportador.getNombre() != null) {
            nombre.setCellValue(transportador.getNombre());
        } else {
            nombre.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaRazonSocial(Row filaDatos, Transportador transportador) {
        Cell razonSocial = filaDatos.createCell(COLUMNA_RAZON_SOCIAL);
        if (transportador.getNombre() != null) {
            razonSocial.setCellValue(transportador.getRazonSocial());
        } else {
            razonSocial.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDomicilio(Row filaDatos, Transportador transportador) {
        Cell domicilio = filaDatos.createCell(COLUMNA_DOMICILIO);
        try {
            domicilio.setCellValue(transportador.getUsuario().getUbicacion().getDireccion());
        } catch (NullPointerException npe) {
            domicilio.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaTelefono(Row filaDatos, Transportador transportador) {
        Cell telefono = filaDatos.createCell(COLUMNA_TELEFONO);
        telefono.setCellValue(transportador.getUsuario().getTelefono());
    }

    private void crearCeldaEmail(Row filaDatos, Transportador transportador) {
        Cell email = filaDatos.createCell(COLUMNA_EMAIL);
        email.setCellValue(transportador.getUsuario().getMail());
    }

    private void crearCeldaChoferes(Row filaDatos, Transportador transportador) {
        Cell choferes = filaDatos.createCell(COLUMNA_CHOFERES);
        try {
            choferes.setCellValue(transportador.getChoferes().size());
        } catch (NullPointerException npe) {
            choferes.setCellValue(0);
        }
    }

    private void crearCeldaRemolques(Row filaDatos, Transportador transportador) {
        Cell remolques = filaDatos.createCell(COLUMNA_REMOLQUES);
        try {
            remolques.setCellValue(transportador.getRemolques().size());
        } catch (NullPointerException npe) {
            remolques.setCellValue(0);
        }
    }

    private void crearCeldaVehiculos(Row filaDatos, Transportador transportador) {
        Cell vehiculos = filaDatos.createCell(COLUMNA_VEHICULOS);
        try {
            vehiculos.setCellValue(transportador.getVehiculos().size());
        } catch (NullPointerException npe) {
            vehiculos.setCellValue(0);
        }
    }

    private void crearCeldaMatches(Row filaDatos, Transportador transportador) {
        Cell matches = filaDatos.createCell(COLUMNA_MATCHES);
        try {
            matches.setCellValue(transportador.getCoincidencias().size());
        } catch (NullPointerException npe) {
            matches.setCellValue(0);
        }
    }

    private void crearCeldaComision(Row filaDatos, Transportador transportador) {
        Cell comision = filaDatos.createCell(COLUMNA_COMISION);
        comision.setCellValue(transportador.getComision());
    }

    private void crearCeldaModificacion(Row filaDatos, Transportador transportador) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(transportador.getModificacion());
    }

}
