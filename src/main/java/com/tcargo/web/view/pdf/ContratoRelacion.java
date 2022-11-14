package com.tcargo.web.view.pdf;

import com.google.zxing.WriterException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tcargo.web.entidades.Coincidencia;
import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.TipoCarga;
import com.tcargo.web.modelos.ValoracionModel;
import com.tcargo.web.servicios.CoincidenciaService;
import com.tcargo.web.servicios.DadorDeCargaService;
import com.tcargo.web.utiles.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.tcargo.web.utiles.Textos.PAGE_LABEL;

@Component("pedido-list")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ContratoRelacion extends AbstractPdfView {

    private final CoincidenciaService coincidenciaService;
    private final DadorDeCargaService dadorService;

    @Value("${url_plataforma}")
    private String url;

    @Override
    protected void buildPdfDocument(Map<String, Object> model, @NonNull Document document, @NonNull PdfWriter writer, HttpServletRequest request, @NonNull HttpServletResponse response) throws Exception {
        @SuppressWarnings("unchecked")
        Page<Pedido> pedidos = (Page<Pedido>) model.get(PAGE_LABEL);
        armarPDF(document, pedidos.getContent().get(0), (ValoracionModel) model.get("valoracion"), request.getRequestURL().toString());
    }

    public ByteArrayInputStream contrato(Pedido pedido, ValoracionModel valoracion) throws IOException, WriterException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();
        armarPDF(document, pedido, valoracion, url + "/pedido/ver");
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void armarPDF(Document document, Pedido pedido, ValoracionModel valoracion, String url) throws IOException, WriterException {
        Coincidencia match = coincidenciaService.buscarPorPedido(pedido.getId()).get(0);
        DadorDeCarga dador = dadorService.buscarPorIdUsuario(pedido.getDador().getId());

        Image logo = Image.getInstance("/proyectos/tcargo-qa/src/main/resources/static/img/logo-gde.png");
        logo.setAlignment(Element.ALIGN_CENTER);
        logo.scaleAbsolute(233, 54);
        document.add(logo);

        Paragraph titulo = new Paragraph("Contrato de relación Pedido número: " + pedido.getId().toString(), FontFactory.getFont(FontFactory.HELVETICA, 15));
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(5);
        document.add(titulo);

        Paragraph detalle = new Paragraph("Tipo de viaje: ".concat(pedido.getTipoDeViaje().toString() + "  Producto: ".concat(pedido.getCarga().getProducto().getNombre()) + "  Peso: ".concat(pedido.getCarga().getPeso() + pedido.getCarga().getTipoPeso().toString())));
        detalle.setAlignment(Element.ALIGN_CENTER);
        detalle.setSpacingBefore(15);
        document.add(detalle);

        StringBuilder tiposDeCarga = new StringBuilder("Tipos de carga: ");
        for (TipoCarga tc : pedido.getCarga().getTipoCargas()) {
            tiposDeCarga.append(tc.getCaracteristicas()).append(" - ");
        }
        Paragraph detalleCarga = new Paragraph(tiposDeCarga.toString());
        detalleCarga.setAlignment(Element.ALIGN_CENTER);
        document.add(detalleCarga);

        PdfPTable tablaDetallesUsarios = new PdfPTable(2);
        tablaDetallesUsarios.getDefaultCell().setBorder(0);
        tablaDetallesUsarios.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaDetallesUsarios.setSpacingAfter(2);
        tablaDetallesUsarios.setSpacingBefore(20);
        tablaDetallesUsarios.setWidthPercentage(100);
        tablaDetallesUsarios.addCell("Datos Dador");
        tablaDetallesUsarios.addCell("Datos Transportadora");
        tablaDetallesUsarios.addCell(dador.getNombre());
        tablaDetallesUsarios.addCell(match.getTransportador().getNombre());
        tablaDetallesUsarios.addCell(dador.getRazonSocial());
        tablaDetallesUsarios.addCell(match.getTransportador().getRazonSocial());
        tablaDetallesUsarios.addCell(dador.getUsuario().getTelefono());
        tablaDetallesUsarios.addCell(match.getTransportador().getUsuario().getTelefono());
        document.add(tablaDetallesUsarios);

        PdfPTable tabla = new PdfPTable(2);
        tabla.getDefaultCell().setBorder(0);
        tabla.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.setSpacingAfter(50);
        tabla.setSpacingBefore(20);
        tabla.setWidthPercentage(100);
        tabla.addCell("Carga");
        tabla.addCell("Descarga");
        tabla.addCell(formatearFecha(pedido.getPeriodoDeCarga().getInicio()));
        tabla.addCell(formatearFecha(pedido.getPeriodoDeDescarga().getInicio()));
        tabla.addCell(formatearFecha(pedido.getPeriodoDeCarga().getFinalizacion()));
        tabla.addCell(formatearFecha(pedido.getPeriodoDeDescarga().getFinalizacion()));
        tabla.addCell("\nDesde");
        tabla.addCell("\nHasta");
        tabla.addCell(pedido.getUbicacionDesde().getDireccion());
        tabla.addCell(pedido.getUbicacionHasta().getDireccion());
        tabla.addCell("\nVehículo: " + match.getViaje().getVehiculo().getModelo().getNombre() + " Dominio: " + match.getViaje().getVehiculo().getDominio());
        if (match.getViaje().getRemolque() != null) {
            tabla.addCell("\nRemolque: " + match.getViaje().getRemolque().getTipoRemolque().getCaracteristicas() + " Dominio: " + match.getViaje().getRemolque().getDominio());
        }
        document.add(tabla);
        Paragraph costo = new Paragraph("\nCotización: ".concat("$" + match.getCosto().toString()));
        costo.setAlignment(Element.ALIGN_CENTER);
        document.add(costo);

        Paragraph estado = new Paragraph();
        estado.setAlignment(Element.ALIGN_CENTER);
        estado.add("El estado del viaje es: " + match.getViaje().getEstadoViaje().getTexto());
        document.add(estado);

        if (valoracion != null && valoracion.getId() != null && !valoracion.getId().isEmpty()) {
            Paragraph conformidadEncabezado = new Paragraph("Estado conformidad", FontFactory.getFont(FontFactory.HELVETICA_BOLD));
            conformidadEncabezado.setAlignment(Element.ALIGN_CENTER);
            document.add(conformidadEncabezado);

            Paragraph conformidadBoolean = new Paragraph(valoracion.isConformidad() ? "Conformado" : "No Conformado");
            conformidadBoolean.setAlignment(Element.ALIGN_CENTER);
            document.add(conformidadBoolean);

            Paragraph comentariosConformidad = new Paragraph(valoracion.getComentarios());
            comentariosConformidad.setAlignment(Element.ALIGN_CENTER);
            document.add(comentariosConformidad);
            Paragraph fechaImpacto = new Paragraph(formatearFecha(valoracion.getModificacion().toString()), FontFactory.getFont(FontFactory.HELVETICA, 8));
            fechaImpacto.setAlignment(Element.ALIGN_CENTER);
            document.add(fechaImpacto);
        }

        Image qr = Image.getInstance(QRCodeGenerator.generar(url + "?id=" + pedido.getId(), 150, 150));
        qr.setAlignment(Element.ALIGN_CENTER);
        document.add(qr);

        Paragraph leyenda = new Paragraph("T-cargo es agente y no responsable.",
                FontFactory.getFont(FontFactory.HELVETICA, 8));
        leyenda.setSpacingBefore(15);
        document.add(leyenda);
    }

    private String formatearFecha(String fecha) {
        return LocalDateTime.parse(fecha.replace(" ", "T")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

}
