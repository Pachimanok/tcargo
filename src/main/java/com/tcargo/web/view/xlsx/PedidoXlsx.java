package com.tcargo.web.view.xlsx;

import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.entidades.TipoRequisitoOferta;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import com.tcargo.web.repositorios.DadorDeCargaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.NO_ASIGNADO;

@Component("pedido-list.xlsx")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PedidoXlsx extends AbstractXlsxView implements Xlsx<Pedido> {

    private final DadorDeCargaRepository dadorDeCargaRepository;

    private static final int COLUMNA_NUMERO_PEDIDO = 0;
    private static final int COLUMNA_DADOR = 1;
    private static final int COLUMNA_EMAIL_DADOR = 2;
    private static final int COLUMNA_TIPO_VIAJE = 3;
    private static final int COLUMNA_TIPO_INTERNACIONAL = 4;
    private static final int COLUMNA_TIPOS_CARGA = 5;
    private static final int COLUMNA_ESTADO = 6;
    private static final int COLUMNA_PRODUCTO = 7;
    private static final int COLUMNA_DESCRIPCION = 8;
    private static final int COLUMNA_PESO = 9;
    private static final int COLUMNA_TIPO_VEHICULO = 10;
    private static final int COLUMNA_TIPO_REMOLQUE = 11;
    private static final int COLUMNA_TIPO_EMBALAJE = 12;
    private static final int COLUMNA_CARGA_COMPLETA = 13;
    private static final int COLUMNA_DESDE = 14;
    private static final int COLUMNA_HASTA = 15;
    private static final int COLUMNA_CARGA = 16;
    private static final int COLUMNA_DESCARGA = 17;
    private static final int COLUMNA_REQUISITOS = 18;
    private static final int COLUMNA_VALOR = 19;
    private static final int COLUMNA_CONDICIONES_PAGO = 20;
    private static final int COLUMNA_MODIFICACION = 21;

    private static final String DATE_TIME_PATTERN = "dd/MM/yyyy HH:mm";

    @Override
    protected void buildExcelDocument(@NonNull Map<String, Object> model, @NonNull Workbook workbook, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        armarExcel(model, workbook, response, "Pedidos");
    }

    @Override
    public void crearFilaTitulos(Row filaTitulos) {
        Cell numeroPedidoTitulo = filaTitulos.createCell(COLUMNA_NUMERO_PEDIDO);
        numeroPedidoTitulo.setCellValue("N° Pedido");

        Cell dadorTitulo = filaTitulos.createCell(COLUMNA_DADOR);
        dadorTitulo.setCellValue("Dador");

        Cell mailDadorTitulo = filaTitulos.createCell(COLUMNA_EMAIL_DADOR);
        mailDadorTitulo.setCellValue("Email dador");

        Cell tipoViajeTitulo = filaTitulos.createCell(COLUMNA_TIPO_VIAJE);
        tipoViajeTitulo.setCellValue("Tipo de viaje");

        Cell tipoInternacionalTitulo = filaTitulos.createCell(COLUMNA_TIPO_INTERNACIONAL);
        tipoInternacionalTitulo.setCellValue("Tipo internacional");

        Cell tipoCargaTitulo = filaTitulos.createCell(COLUMNA_TIPOS_CARGA);
        tipoCargaTitulo.setCellValue("Tipos de carga");

        Cell estadoTitulo = filaTitulos.createCell(COLUMNA_ESTADO);
        estadoTitulo.setCellValue("Estado");

        Cell productoTitulo = filaTitulos.createCell(COLUMNA_PRODUCTO);
        productoTitulo.setCellValue("Producto");

        Cell descripcionTitulo = filaTitulos.createCell(COLUMNA_DESCRIPCION);
        descripcionTitulo.setCellValue("Descripcion");

        Cell pesoTitulo = filaTitulos.createCell(COLUMNA_PESO);
        pesoTitulo.setCellValue("Peso");

        Cell tipoVehiculoTitulo = filaTitulos.createCell(COLUMNA_TIPO_VEHICULO);
        tipoVehiculoTitulo.setCellValue("Tipo de vehiculo");

        Cell tipoRemolqueTitulo = filaTitulos.createCell(COLUMNA_TIPO_REMOLQUE);
        tipoRemolqueTitulo.setCellValue("Tipo de remolque");

        Cell tipoEmbalajeTitulo = filaTitulos.createCell(COLUMNA_TIPO_EMBALAJE);
        tipoEmbalajeTitulo.setCellValue("Tipo de embalaje");

        Cell cargaCompletaTitulo = filaTitulos.createCell(COLUMNA_CARGA_COMPLETA);
        cargaCompletaTitulo.setCellValue("¿Carga completa\no consolidada?");

        Cell desdeTitulo = filaTitulos.createCell(COLUMNA_DESDE);
        desdeTitulo.setCellValue("Desde");

        Cell hastaTitulo = filaTitulos.createCell(COLUMNA_HASTA);
        hastaTitulo.setCellValue("Hasta");

        Cell fechaCargaTitulo = filaTitulos.createCell(COLUMNA_CARGA);
        fechaCargaTitulo.setCellValue("Fecha de carga");

        Cell fechaDescargaTitulo = filaTitulos.createCell(COLUMNA_DESCARGA);
        fechaDescargaTitulo.setCellValue("Fecha de descarga");

        Cell requisitosTitulo = filaTitulos.createCell(COLUMNA_REQUISITOS);
        requisitosTitulo.setCellValue("Requisitos");

        Cell valorTitulo = filaTitulos.createCell(COLUMNA_VALOR);
        valorTitulo.setCellValue("Valor");

        Cell condicionPagoTitulo = filaTitulos.createCell(COLUMNA_CONDICIONES_PAGO);
        condicionPagoTitulo.setCellValue("Condiciones de pago");

        Cell modificacionTitulo = filaTitulos.createCell(COLUMNA_MODIFICACION);
        modificacionTitulo.setCellValue("Modificacion");
    }

    @Override
    public void crearFilasDatos(Sheet hoja, List<Pedido> pedidos, int fila) {
        for (Pedido pedido : pedidos) {
            Row filaDatos = hoja.createRow(fila++);

            crearCeldaNumeroPedido(filaDatos, pedido);
            crearCeldaDador(filaDatos, pedido);
            crearCeldaEmailDador(filaDatos, pedido);
            crearCeldaTipoViaje(filaDatos, pedido);
            crearCeldaTipoInternacional(filaDatos, pedido);
            crearCeldaTiposCarga(filaDatos, pedido);
            crearCeldaEstado(filaDatos, pedido);
            crearCeldaProducto(filaDatos, pedido);
            crearCeldaDescripcion(filaDatos, pedido);
            crearCeldaPeso(filaDatos, pedido);
            crearCeldaTipoVehiculo(filaDatos, pedido);
            crearCeldaTipoRemolque(filaDatos, pedido);
            crearCeldaTipoEmbalaje(filaDatos, pedido);
            crearCeldaCargaCompleta(filaDatos, pedido);
            crearCeldaDesde(filaDatos, pedido);
            crearCeldaHasta(filaDatos, pedido);
            crearCeldaFechaCarga(filaDatos, pedido);
            crearCeldaFechaDescarga(filaDatos, pedido);
            crearCeldaRequisitos(filaDatos, pedido);
            crearCeldaValor(filaDatos, pedido);
            crearCeldaCondicionesPago(filaDatos, pedido);
            crearCeldaModificacion(filaDatos, pedido);
        }
    }

    private void crearCeldaNumeroPedido(Row filaDatos, Pedido pedido) {
        Cell numeroPedido = filaDatos.createCell(COLUMNA_NUMERO_PEDIDO);
        numeroPedido.setCellValue(pedido.getId());
    }

    private void crearCeldaDador(Row filaDatos, Pedido pedido) {
        Cell dador = filaDatos.createCell(COLUMNA_DADOR);
        DadorDeCarga dadorDeCarga = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(pedido.getDador().getId());
        dador.setCellValue(dadorDeCarga.getRazonSocial());
    }

    private void crearCeldaEmailDador(Row filaDatos, Pedido pedido) {
        Cell mailDador = filaDatos.createCell(COLUMNA_EMAIL_DADOR);
        mailDador.setCellValue(pedido.getDador().getMail());
    }

    private void crearCeldaTipoViaje(Row filaDatos, Pedido pedido) {
        Cell tipoViaje = filaDatos.createCell(COLUMNA_TIPO_VIAJE);
        tipoViaje.setCellValue(pedido.getTipoDeViaje().toString());
    }

    private void crearCeldaTipoInternacional(Row filaDatos, Pedido pedido) {
        Cell tipoInternacional = filaDatos.createCell(COLUMNA_TIPO_INTERNACIONAL);
        if (pedido.getTipoDeViaje().equals(TipoDeViaje.NACIONAL)) {
            tipoInternacional.setCellValue(NO_ASIGNADO);
            return;
        }
        tipoInternacional.setCellValue(pedido.getTipoInternacional().toString());
    }

    private void crearCeldaTiposCarga(Row filaDatos, Pedido pedido) {
        Cell tiposCarga = filaDatos.createCell(COLUMNA_TIPOS_CARGA);
        StringBuilder sb = new StringBuilder();
        for (TipoCarga tipoCarga : pedido.getCarga().getTipoCargas()) {
            sb.append(tipoCarga.getCaracteristicas()).append("\n");
        }
        sb.deleteCharAt(sb.lastIndexOf("\n"));
        tiposCarga.setCellValue(sb.toString());
    }

    private void crearCeldaEstado(Row filaDatos, Pedido pedido) {
        Cell estado = filaDatos.createCell(COLUMNA_ESTADO);
        estado.setCellValue(pedido.getEstadoPedido().toString());
    }

    private void crearCeldaProducto(Row filaDatos, Pedido pedido) {
        Cell producto = filaDatos.createCell(COLUMNA_PRODUCTO);
        producto.setCellValue(pedido.getCarga().getProducto().getNombre());
    }

    private void crearCeldaDescripcion(Row filaDatos, Pedido pedido) {
        Cell descripcion = filaDatos.createCell(COLUMNA_DESCRIPCION);
        if (pedido.getCarga().getDescripcion() != null && !pedido.getCarga().getDescripcion().isEmpty()) {
            descripcion.setCellValue(pedido.getCarga().getDescripcion());
            return;
        }
        descripcion.setCellValue(NO_ASIGNADO);
    }

    private void crearCeldaPeso(Row filaDatos, Pedido pedido) {
        Cell peso = filaDatos.createCell(COLUMNA_PESO);
        peso.setCellValue(pedido.getCarga().getPeso() + " " + pedido.getCarga().getTipoPeso());
    }

    private void crearCeldaTipoVehiculo(Row filaDatos, Pedido pedido) {
        Cell tipoVehiculo = filaDatos.createCell(COLUMNA_TIPO_VEHICULO);
        tipoVehiculo.setCellValue(pedido.getCarga().getTipoVehiculo().getNombre());
    }

    private void crearCeldaTipoRemolque(Row filaDatos, Pedido pedido) {
        Cell tipoRemolque = filaDatos.createCell(COLUMNA_TIPO_REMOLQUE);
        if (pedido.getCarga() != null && pedido.getCarga().getTipoVehiculo() != null && pedido.getCarga().getTipoRemolque() != null && pedido.getCarga().getTipoRemolque().getCaracteristicas() != null
        		&& !pedido.getCarga().getTipoRemolque().getCaracteristicas().isEmpty() 
        		&& !pedido.getCarga().getTipoVehiculo().getNombre().trim().equalsIgnoreCase("simple")) {
            tipoRemolque.setCellValue(pedido.getCarga().getTipoRemolque().getCaracteristicas());
            return;
        }
        tipoRemolque.setCellValue(NO_ASIGNADO);
    }

    private void crearCeldaTipoEmbalaje(Row filaDatos, Pedido pedido) {
        Cell tipoEmbalaje = filaDatos.createCell(COLUMNA_TIPO_VEHICULO);
        tipoEmbalaje.setCellValue(pedido.getCarga().getTipoEmbalaje().getCaracteristicas());
    }

    private void crearCeldaCargaCompleta(Row filaDatos, Pedido pedido) {
        Cell cargaCompleta = filaDatos.createCell(COLUMNA_CARGA_COMPLETA);
        cargaCompleta.setCellValue(pedido.getCarga().isCargaCompleta() ? "Carga completa" : "Carga consolidada");
    }

    private void crearCeldaDesde(Row filaDatos, Pedido pedido) {
        Cell desde = filaDatos.createCell(COLUMNA_DESDE);
        desde.setCellValue(pedido.getUbicacionDesde().getDireccion());
    }

    private void crearCeldaHasta(Row filaDatos, Pedido pedido) {
        Cell hasta = filaDatos.createCell(COLUMNA_HASTA);
        hasta.setCellValue(pedido.getUbicacionHasta().getDireccion());
    }

    private void crearCeldaFechaCarga(Row filaDatos, Pedido pedido) {
        Cell fechaCarga = filaDatos.createCell(COLUMNA_CARGA);
        LocalDateTime inicio = LocalDateTime.parse(pedido.getPeriodoDeCarga().getInicio(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        LocalDateTime fin = LocalDateTime.parse(pedido.getPeriodoDeCarga().getFinalizacion(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        fechaCarga.setCellValue(inicio + " - " + fin);
    }

    private void crearCeldaFechaDescarga(Row filaDatos, Pedido pedido) {
        Cell fechaDescarga = filaDatos.createCell(COLUMNA_DESCARGA);
        LocalDateTime inicio = LocalDateTime.parse(pedido.getPeriodoDeDescarga().getInicio(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        LocalDateTime fin = LocalDateTime.parse(pedido.getPeriodoDeDescarga().getFinalizacion(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        fechaDescarga.setCellValue(inicio + " - " + fin);
    }

    private void crearCeldaRequisitos(Row filaDatos, Pedido pedido) {
        Cell requisitos = filaDatos.createCell(COLUMNA_REQUISITOS);
        StringBuilder sb = new StringBuilder();
        if (pedido.getRequisitos() != null && !pedido.getRequisitos().isEmpty()) {
            for (TipoRequisitoOferta requisito : pedido.getTipoRequisitos()) {
                sb.append(requisito.getNombre()).append("\n");
            }
            sb.deleteCharAt(sb.lastIndexOf("\n"));
            requisitos.setCellValue(sb.toString());
        } else {
            requisitos.setCellValue("Sin requisitos");
        }

    }

    private void crearCeldaValor(Row filaDatos, Pedido pedido) {
        Cell valor = filaDatos.createCell(COLUMNA_VALOR);
        String celda = "";
        if(pedido.getMoneda() != null && pedido.getMoneda().getSimbolo() != null && !pedido.getMoneda().getSimbolo().isEmpty()) {
        	celda = pedido.getMoneda().getSimbolo();
        	 if(pedido.getValor() != null) {
             	celda += pedido.getValor();
             }
        }else {
        	celda = "Sin asignar";
        }
       
        valor.setCellValue(celda);
    }

    private void crearCeldaCondicionesPago(Row filaDatos, Pedido pedido) {
        Cell condicionesPago = filaDatos.createCell(COLUMNA_CONDICIONES_PAGO);
        if (pedido.getDescripcion() != null && !pedido.getDescripcion().isEmpty()) {
            condicionesPago.setCellValue(pedido.getDescripcion());
            return;
        }
        condicionesPago.setCellValue(NO_ASIGNADO);
    }

    private void crearCeldaModificacion(Row filaDatos, Pedido pedido) {
        Cell modificacion = filaDatos.createCell(COLUMNA_MODIFICACION);
        modificacion.setCellValue(pedido.getModificacion());
    }

}
