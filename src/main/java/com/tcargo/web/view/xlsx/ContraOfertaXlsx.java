package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.ContraOferta;
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

@Component("contraoferta-list-admin.xlsx")
public class ContraOfertaXlsx extends AbstractXlsxView implements Xlsx<ContraOferta> {

    private static final int COLUMNA_NUMERO_PEDIDO = 0;
    private static final int COLUMNA_TRANSPORTADOR = 1;
    private static final int COLUMNA_DADOR = 2;
    private static final int COLUMNA_CREADOR = 3;
    private static final int COLUMNA_DESDE = 4;
    private static final int COLUMNA_HASTA = 5;
    private static final int COLUMNA_KM = 6;
    private static final int COLUMNA_ESTADO = 7;
    private static final int COLUMNA_VEHICULO = 8;
    private static final int COLUMNA_DOMINIO_VEHICULO = 9;
    private static final int COLUMNA_REMOLQUE = 10;
    private static final int COLUMNA_DOMINIO_REMOLQUE = 11;
    private static final int COLUMNA_MODIFICACION = 12;

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Ofertas");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell numeroPedidoTitulo = filaTitulos.createCell(COLUMNA_NUMERO_PEDIDO);
        numeroPedidoTitulo.setCellValue("N° Pedido");

        Cell transportadorTitulo = filaTitulos.createCell(COLUMNA_TRANSPORTADOR);
        transportadorTitulo.setCellValue("Transportador");

        Cell dadorTitulo = filaTitulos.createCell(COLUMNA_DADOR);
        dadorTitulo.setCellValue("Dador");

        Cell creadorTitulo = filaTitulos.createCell(COLUMNA_CREADOR);
        creadorTitulo.setCellValue("Creador");

        Cell desdeTitulo = filaTitulos.createCell(COLUMNA_DESDE);
        desdeTitulo.setCellValue("Desde");

        Cell hastaTitulo = filaTitulos.createCell(COLUMNA_HASTA);
        hastaTitulo.setCellValue("Hasta");

        Cell kmTitulo = filaTitulos.createCell(COLUMNA_KM);
        kmTitulo.setCellValue("Kilometros");

        Cell estadoTitulo = filaTitulos.createCell(COLUMNA_ESTADO);
        estadoTitulo.setCellValue("Estado");

        Cell vehiculoTitulo = filaTitulos.createCell(COLUMNA_VEHICULO);
        vehiculoTitulo.setCellValue("Vehiculo");

        Cell dominioVehiculoTitulo = filaTitulos.createCell(COLUMNA_DOMINIO_VEHICULO);
        dominioVehiculoTitulo.setCellValue("Dominio vehiculo");

        Cell remolqueTitulo = filaTitulos.createCell(COLUMNA_REMOLQUE);
        remolqueTitulo.setCellValue("Remolque");

        Cell dominioRemolqueTitulo = filaTitulos.createCell(COLUMNA_DOMINIO_REMOLQUE);
        dominioRemolqueTitulo.setCellValue("Dominio remolque");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<ContraOferta> ofertas, int fila) {
        for (ContraOferta oferta : ofertas) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNumeroPedido(filaDatos, oferta);
            crearCeldaTransportador(filaDatos, oferta);
            crearCeldaDador(filaDatos, oferta);
            crearCeldaCreador(filaDatos, oferta);
            crearCeldaDesde(filaDatos, oferta);
            crearCeldaHasta(filaDatos, oferta);
            crearCeldaKm(filaDatos, oferta);
            crearCeldaEstado(filaDatos, oferta);
            crearCeldaVehiculo(filaDatos, oferta);
            crearCeldaDominioVehiculo(filaDatos, oferta);
            crearCeldaRemolque(filaDatos, oferta);
            crearCeldaDominioRemolque(filaDatos, oferta);
            crearCeldaModificacion(filaDatos, oferta);
        }
    }

    private void crearCeldaNumeroPedido(Row filaDatos, ContraOferta oferta) {
        Cell numeroPedido = filaDatos.createCell(COLUMNA_NUMERO_PEDIDO);
        numeroPedido.setCellValue(oferta.getPedido().getId());
    }

    private void crearCeldaTransportador(Row filaDatos, ContraOferta oferta) {
        Cell transportador = filaDatos.createCell(COLUMNA_TRANSPORTADOR);
        try {
            transportador.setCellValue(oferta.getTransportador().getNombre());
        } catch (NullPointerException npe) {
            transportador.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDador(Row filaDatos, ContraOferta oferta) {
        Cell dador = filaDatos.createCell(COLUMNA_DADOR);
        try {
            dador.setCellValue(oferta.getDador().getNombre());
        } catch (NullPointerException npe) {
            dador.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaCreador(Row filaDatos, ContraOferta oferta) {
        Cell creador = filaDatos.createCell(COLUMNA_CREADOR);
        creador.setCellValue(oferta.getCreador().getRol().getTexto());
    }

    private void crearCeldaDesde(Row filaDatos, ContraOferta oferta) {
        Cell desde = filaDatos.createCell(COLUMNA_DESDE);
        desde.setCellValue(oferta.getPedido().getUbicacionDesde().getDireccion());
    }

    private void crearCeldaHasta(Row filaDatos, ContraOferta oferta) {
        Cell hasta = filaDatos.createCell(COLUMNA_HASTA);
        hasta.setCellValue(oferta.getPedido().getUbicacionHasta().getDireccion());
    }

    private void crearCeldaKm(Row filaDatos, ContraOferta oferta) {
        Cell km = filaDatos.createCell(COLUMNA_KM);
        try {
            km.setCellValue(oferta.getPedido().getKmTotales());
        } catch (NullPointerException npe) {
            km.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaEstado(Row filaDatos, ContraOferta oferta) {
        Cell estado = filaDatos.createCell(COLUMNA_ESTADO);
        estado.setCellValue(oferta.getEstado().toString());
    }

    private void crearCeldaVehiculo(Row filaDatos, ContraOferta oferta) {
        Cell vehiculo = filaDatos.createCell(COLUMNA_VEHICULO);
        try {
            vehiculo.setCellValue(oferta.getVehiculo().getTipoVehiculo().getNombre());
        } catch (NullPointerException npe) {
            vehiculo.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDominioVehiculo(Row filaDatos, ContraOferta oferta) {
        Cell dominioVehiculo = filaDatos.createCell(COLUMNA_DOMINIO_VEHICULO);
        try {
            dominioVehiculo.setCellValue(oferta.getVehiculo().getDominio());
        } catch (NullPointerException npe) {
            dominioVehiculo.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaRemolque(Row filaDatos, ContraOferta oferta) {
        Cell remolque = filaDatos.createCell(COLUMNA_REMOLQUE);
        try {
            remolque.setCellValue(oferta.getRemolque().getTipoRemolque().getCaracteristicas());
        } catch (NullPointerException npe) {
            remolque.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaDominioRemolque(Row filaDatos, ContraOferta oferta) {
        Cell dominioRemolque = filaDatos.createCell(COLUMNA_DOMINIO_REMOLQUE);
        try {
            dominioRemolque.setCellValue(oferta.getRemolque().getDominio());
        } catch (NullPointerException npe) {
            dominioRemolque.setCellValue(NO_ASIGNADO);
        }
    }

    private void crearCeldaModificacion(Row filaDatos, ContraOferta oferta) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(oferta.getModificacion().toString());
    }

}
