package com.tcargo.web.servicios;

import com.tcargo.web.servicios.jwt.JWTService;
import com.tcargo.web.utiles.Fecha;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EmailService {

    private final JavaMailSender sender;
    private final JWTService jwtService;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${url_plataforma}")
    private String url;

    private final Log log = LogFactory.getLog(EmailService.class);

    public void registroCompleto(String email, String nombre) {
        String asunto = "¡Bienvenido a T-Cargo!";

        Context context = new Context();
        context.setVariable("nombre", nombre);
        String token = jwtService.crearTokenValidacionRegistro(email);
        context.setVariable("link", url + "/registro/verificacion?t=" + token);

        String body = templateEngine.process("/email-templates/registro-completo.html", context);
        enviar(email, asunto, body);
    }

    public void registroBasicoConRol(String email) {
        String asunto = "¡Bienvenido a T-Cargo!";
        String mensaje = "1º Complete su información en tcargo.la\n" +
                        "2º Recibirá un segundo mail para verificar su cuenta.\n" +
                        "3º Siga las instrucciones y comience a disfrutar las ventajas de la plataforma";

        Context context = new Context();
        context.setVariable("nombre", "");
        context.setVariable("mensaje", mensaje);

        String body = templateEngine.process("/email-templates/registro-completo.html", context);
        enviar(email, asunto, body);
    }


    public void registroBasico(String email) {
        String asunto = "¡Bienvenido a T-Cargo!";
        String mensaje = "Gracias por registrarte y usar nuestra plataforma:";


        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("fecha", Fecha.formatearFecha(new Date()));
        context.setVariable("mensaje", mensaje);

        String body = templateEngine.process("/email-templates/mensajeBienvenida.html", context);
        enviar(email, asunto, body);
    }

    public void mensaje(String email, String nombre, String asunto, String mensaje) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("fecha", Fecha.formatearFecha(new Date()));
        context.setVariable("mensaje", mensaje);
        context.setVariable("nombre", nombre);

        String body = templateEngine.process("/email-templates/mensaje.html", context);
        enviar(email, asunto, body);
    }

    public void cargaNegativaSeleccionada(String email, String nombre, Long id) {
        String mensaje = "Hay una carga para tu viaje";

        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("fecha", Fecha.formatearFecha(new Date()));
        context.setVariable("mensaje", mensaje);
        context.setVariable("nombre", nombre);
        context.setVariable("url", url + "/pedido/ver?id=" + id);

        String body = templateEngine.process("/email-templates/notificacion.html", context);
        enviar(email, mensaje, body);
    }

    public void match(String email, String nombre, Long id) {
        String mensaje = "Tenes un nuevo match";
        // nombre, fecha, email, mensaje, url
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("fecha", Fecha.formatearFecha(new Date()));
        context.setVariable("mensaje", mensaje);
        context.setVariable("nombre", nombre);
        context.setVariable("url", url + "/pedido/ver?id=" + id);

        String body = templateEngine.process("/email-templates/notificacion.html", context);
        enviar(email, mensaje, body);
    }

    public void actualizacionViaje(String email, String nombre, Long id, String estado) {
        String asunto = "Tu viaje tiene un nuevo estado";
        String mensaje = "Tu viaje ha cambiado a estado: " + estado;
        // nombre, fecha, email, mensaje, url
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("fecha", Fecha.formatearFecha(new Date()));
        context.setVariable("mensaje", mensaje);
        context.setVariable("nombre", nombre);
        context.setVariable("url", url + "/pedido/ver?id=" + id);

        String body = templateEngine.process("/email-templates/notificacion.html", context);
        enviar(email, asunto, body);
    }

    public void recuperarClave(String email) {
        String asunto = "Recuperación de clave";
        String mensaje = "Para recuperar tu clave, clickea el siguiente link.";
        String link = url + "/r/recuperar?t=" + jwtService.crearTokenRecuperacionClave(email);
        String template = templateConLinkRecuperarContrasena(new Date(), email, mensaje, link);
        enviar(email, asunto, template);
    }

    private void enviar(String to, String asunto, String mensaje) {
        new Thread(() -> {
            String[] para = new String[]{to};
            try {
                MimeMessage message = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                helper.setFrom(from, "T-Cargo");
                helper.setTo(para);
                helper.setSubject(asunto);
                helper.setText(mensaje, true);

                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(mensaje, "text/html");

                String urlArchivos = "/proyectos/tcargo/src/main/resources/static/img";
               /*String urlArchivos = "C:\\Users\\Nahue\\Documents\\workspace\\T-Cargo\\src\\main\\resources\\static\\img";*/

                String logoEncabezado = urlArchivos + "/logo-blanco.png";
                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.attachFile(logoEncabezado);
                imagePart.setContentID("<logoBlancoEncabezado>");
                imagePart.setDisposition(Part.INLINE);

                String logoLinkedin = urlArchivos + "/icono-linkedin.png";
                MimeBodyPart imageLinkedin = new MimeBodyPart();
                imageLinkedin.attachFile(logoLinkedin);
                imageLinkedin.setContentID("<logoLinkedin>");
                imageLinkedin.setDisposition(Part.INLINE);

                String logoFacebook = urlArchivos + "/icono-facebook.png";
                MimeBodyPart imageFacebook = new MimeBodyPart();
                imageFacebook.attachFile(logoFacebook);
                imageFacebook.setContentID("<logoFacebook>");
                imageFacebook.setDisposition(Part.INLINE);

                String logoInst = urlArchivos + "/icono-instagram.png";
                MimeBodyPart imageInst = new MimeBodyPart();
                imageInst.attachFile(logoInst);
                imageInst.setContentID("<logoInst>");
                imageInst.setDisposition(Part.INLINE);

                String logoTwiter = urlArchivos + "/icono-twitter.png";
                MimeBodyPart imageTwiter = new MimeBodyPart();
                imageTwiter.attachFile(logoTwiter);
                imageTwiter.setContentID("<logoTwiter>");
                imageTwiter.setDisposition(Part.INLINE);

                String logoFooter = urlArchivos + "/logo-con-pie.png";
                MimeBodyPart imageFooter = new MimeBodyPart();
                imageFooter.attachFile(logoFooter);
                imageFooter.setContentID("<logoFooter>");
                imageFooter.setDisposition(Part.INLINE);

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                multipart.addBodyPart(imagePart);
                multipart.addBodyPart(imageLinkedin);
                multipart.addBodyPart(imageFacebook);
                multipart.addBodyPart(imageInst);
                multipart.addBodyPart(imageTwiter);
                multipart.addBodyPart(imageFooter);
                message.setContent(multipart);
                sender.send(message);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }).start();
    }


    //FIXME Refactor a futuro usar las herramientas de template que trae thymeleaf para mail.
    public String templateConLinkAltaChofer(Date fecha, String email, String mensaje, String id) {
        String template = "<table width=\"100%\" border=\"0\" align=\"center\" cellpadding=\"0\"  cellspacing=\"0\" bgcolor=\"#DADADA\"><tbody>    <tr>      <td><table width=\"600\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#ffffff\"><tbody>	<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"40\"><table			width=\"100%\" bgcolor=\"#DADADA\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">				<tr>					<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>						</tr>							</table></td>						</tr>	<tr>		<td bgcolor=\"#D51317\"><table width=\"100%\" border=\"0\"			align=\"center\" cellpadding=\"15\" cellspacing=\"0\"			bgcolor=\"#D51317\" style=\"padding: 10px 10px 10px 10px;\">				<tbody>					<tr>"
                + "						<td style=\"text-align: center;\"><img						src=\" cid:logoBlancoEncabezado \" alt=\"\" /></td>					</tr></tbody></table></td></tr><tr>	<td style=\"font-size: 0; line-height: 0;\" height=\"10\"><table	width=\"100%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">		<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>		</tr>	</table></td></tr><tr>	<td><table width=\"92%\" align=\"center\" cellpadding=\"0\"	cellspacing=\"0\">	<tr>		<td align=\"left\"		style=\"font-size: 14px; font-style: normal; font-weight: 100; color: #747474; line-height: 1.5; text-align: justify; font-family: Helvetica, Arial, sans-serif;\"><h2>"
                + "			<span style=\"font-size: 16px; font-weight: 300; line-height: 2.5em; color: #D51317; font-family: Helvetica, Arial, sans-serif; text-align: left; text-transform: uppercase;\">Hola				<strong></strong>, recibiste un mensaje de <strong>T-CARGO</strong>.<br>				</span>		</h2>		<p>			<strong>Fecha: </strong>" + Fecha.formatearFecha(fecha) + "		</p>		<p>			<strong>Email: </strong>" + email + "		</p>		<p>		<strong>Mensaje: </strong>" + mensaje + "		</p>		<p>		<strong>Link: </strong><a href='" + url + "/invitacion/formulario?id=" + id + "'>Ver</a>		</p></td></tr></table></td></tr><tr>	<td style=\"font-size: 0; line-height: 0;\" height=\"40\"><table	width=\"100%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">		<tr>"
                + "			<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>		</tr>	</table></td></tr>	<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"1\"><table		width=\"92%\" border=\"0\" align=\"center\" cellpadding=\"0\"		cellspacing=\"0\" bgcolor=\"#757474\">	<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"1\">&nbsp;</td>	</tr></table></td>\n	</tr>	<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"20\"><table		width=\"100%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">		<tr>			<td style=\"font-size: 0; line-height: 0;\" height=\"20\">&nbsp;</td>		</tr></table></td>	</tr><tr>	<td><table width=\"92%\" border=\"0\" align=\"center\"    cellpadding=\"0\" cellspacing=\"0\" class=\"catalog\" 	 style=\"margin-left: 12.5%\">	<tr>"
                + "		<td><table width=\"20%\" border=\"0\" align=\"left\"		cellpadding=\"0\" cellspacing=\"0\" class=\"responsive-table\"		style=\"margin: 10px 0px 10px 0px;\">			<tbody>			<tr>				<td><table width=\"100%\" border=\"0\" align=\"center\"					cellpadding=\"0\" cellspacing=\"0\">\n				<tbody>					<tr align=\"center\">		<td style=\"font-size: 10px; font-style: normal; font-weight: bolder; color: #747474; line-height: 1.3px; /* [disabled]text-align: left; */ font-family: Helvetica, Arial, sans-serif; padding-top: 10px\">			<p>Encontranos en:</p> <img src=\"cid:logoLinkedin\"				alt=\"linkedin\" width=\"20\" height=\"20\"> <img				src=\"cid:logoFacebook\" width=\"20\"					height=\"20\" alt=\"facebook\" /> <img				src=\"cid:logoInst\" width=\"20\""
                + "					height=\"20\" alt=\"instagram\" /> <img				src=\"cid:logoTwiter\" width=\"20\"					height=\"20\" alt=\"twitter\" /></td>\n					</tr>				</tbody>			</table></td>		</tr>	</tbody></table><table width=\"80%\" border=\"0\" align=\"left\" cellpadding=\"0\"	cellspacing=\"0\" class=\"responsive-table\"   style=\"margin: 10px 0px 10px 0px;\">	<tbody>		<tr>			<td><table width=\"100%\" border=\"0\" cellpadding=\"0\"				cellspacing=\"0\">\n					<tbody>						<tr>							<td align=\"center\" style=\"\"><img							src=\"cid:logoFooter\" alt=\"logo\"></td>						</tr>					</tbody>\n			</table></td>		</tr>	</tbody></table></td></tr>"
                + "</table></td></tr><tr>\n<td style=\"font-size: 0; line-height: 0;\" height=\"20\"><table	width=\"96%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">	<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"20\">&nbsp;</td>	</tr></table></td>	</tr>\n	<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"40\"><table			width=\"100%\" bgcolor=\"#DADADA\" align=\"center\" cellpadding=\"0\"\n			cellspacing=\"0\">				<tr>					<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>						</tr>		</table></td>\n</tr></tr>  </tbody></table>";
        return template;
    }

    public String templateConLinkRecuperarContrasena(Date fecha, String email, String mensaje, String link) {
        String template = "<table width=\"100%\" border=\"0\" align=\"center\" cellpadding=\"0\"  cellspacing=\"0\" bgcolor=\"#DADADA\">"
                + "<tbody>    <tr>"
                + "      <td><table width=\"600\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#ffffff\">"
                + "<tbody>	<tr>		<td style=\"font-size: 0; line-height: 0;\" height=\"40\"><table"
                + "			width=\"100%\" bgcolor=\"#DADADA\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">"
                + "				<tr>"
                + "					<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>"
                + "						</tr>							</table></td>"
                + "						</tr>	<tr>"
                + "		<td bgcolor=\"#D51317\"><table width=\"100%\" border=\"0\""
                + "			align=\"center\" cellpadding=\"15\" cellspacing=\"0\""
                + "			bgcolor=\"#D51317\" style=\"padding: 10px 10px 10px 10px;\">				<tbody>"
                + "					<tr>						<td style=\"text-align: center;\"><img"
                + "						src=\" cid:logoBlancoEncabezado \" alt=\"\" /></td>"
                + "					</tr></tbody></table></td></tr><tr>"
                + "	<td style=\"font-size: 0; line-height: 0;\" height=\"10\"><table"
                + "	width=\"100%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">		<tr>"
                + "		<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>		</tr>"
                + "	</table></td></tr><tr>	<td><table width=\"92%\" align=\"center\" cellpadding=\"0\""
                + "	cellspacing=\"0\">	<tr>		<td align=\"left\""
                + "		style=\"font-size: 14px; font-style: normal; font-weight: 100; color: #747474; line-height: 1.5; text-align: justify; font-family: Helvetica, Arial, sans-serif;\"><h2>"
                + "			<span style=\"font-size: 16px; font-weight: 300; line-height: 2.5em; color: #D51317; font-family: Helvetica, Arial, sans-serif; text-align: left; text-transform: uppercase;\">Hola"
                + "				<strong></strong>, recibiste un mensaje de <strong>T-CARGO</strong>.<br>"
                + "				</span>		</h2>		<p>			<strong>Fecha: </strong>"
                + Fecha.formatearFecha(fecha) + "		</p>		<p>			<strong>Email: </strong>"
                + email + "		</p>		<p>		<strong>Mensaje: </strong>" + mensaje + "		</p>"
                + "		<p>		<strong>Link: </strong>"
                + "<a href='" + link + "'>Recuperar clave</a>		</p></td>"
                + "</tr></table></td></tr><tr>"
                + "	<td style=\"font-size: 0; line-height: 0;\" height=\"40\"><table"
                + "	width=\"100%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">		<tr>"
                + "			<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>		</tr>"
                + "	</table></td></tr>	<tr>"
                + "		<td style=\"font-size: 0; line-height: 0;\" height=\"1\"><table"
                + "		width=\"92%\" border=\"0\" align=\"center\" cellpadding=\"0\""
                + "		cellspacing=\"0\" bgcolor=\"#757474\">	<tr>"
                + "		<td style=\"font-size: 0; line-height: 0;\" height=\"1\">&nbsp;</td>	</tr>"
                + "</table></td>\n	</tr>	<tr>"
                + "		<td style=\"font-size: 0; line-height: 0;\" height=\"20\"><table"
                + "		width=\"100%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">		<tr>"
                + "			<td style=\"font-size: 0; line-height: 0;\" height=\"20\">&nbsp;</td>		</tr>"
                + "</table></td>	</tr><tr>	<td><table width=\"92%\" border=\"0\" align=\"center\""
                + "    cellpadding=\"0\" cellspacing=\"0\" class=\"catalog\" 	 style=\"margin-left: 12.5%\">"
                + "	<tr>		<td><table width=\"20%\" border=\"0\" align=\"left\""
                + "		cellpadding=\"0\" cellspacing=\"0\" class=\"responsive-table\""
                + "		style=\"margin: 10px 0px 10px 0px;\">			<tbody>			<tr>"
                + "				<td><table width=\"100%\" border=\"0\" align=\"center\""
                + "					cellpadding=\"0\" cellspacing=\"0\">\n				<tbody>"
                + "					<tr align=\"center\">"
                + "		<td style=\"font-size: 10px; font-style: normal; font-weight: bolder; color: #747474; line-height: 1.3px; /* [disabled]text-align: left; */ font-family: Helvetica, Arial, sans-serif; padding-top: 10px\">"
                + "			<p>Encontranos en:</p> <img src=\"cid:logoLinkedin\""
                + "				alt=\"linkedin\" width=\"20\" height=\"20\"> <img"
                + "				src=\"cid:logoFacebook\" width=\"20\""
                + "					height=\"20\" alt=\"facebook\" /> <img"
                + "				src=\"cid:logoInst\" width=\"20\""
                + "					height=\"20\" alt=\"instagram\" /> <img"
                + "				src=\"cid:logoTwiter\" width=\"20\""
                + "					height=\"20\" alt=\"twitter\" /></td>\n					</tr>"
                + "				</tbody>			</table></td>		</tr>	</tbody></table>"
                + "<table width=\"80%\" border=\"0\" align=\"left\" cellpadding=\"0\""
                + "	cellspacing=\"0\" class=\"responsive-table\"   style=\"margin: 10px 0px 10px 0px;\">"
                + "	<tbody>		<tr>			<td><table width=\"100%\" border=\"0\" cellpadding=\"0\""
                + "				cellspacing=\"0\">\n					<tbody>						<tr>"
                + "							<td align=\"center\" style=\"\"><img"
                + "							src=\"cid:logoFooter\" alt=\"logo\"></td>						</tr>"
                + "					</tbody>\n			</table></td>		</tr>	</tbody>"
                + "</table></td></tr></table></td></tr><tr>\n"
                + "<td style=\"font-size: 0; line-height: 0;\" height=\"20\"><table"
                + "	width=\"96%\" align=\"left\" cellpadding=\"0\" cellspacing=\"0\">	<tr>"
                + "		<td style=\"font-size: 0; line-height: 0;\" height=\"20\">&nbsp;</td>	</tr>"
                + "</table></td>	</tr>\n	<tr>"
                + "		<td style=\"font-size: 0; line-height: 0;\" height=\"40\"><table"
                + "			width=\"100%\" bgcolor=\"#DADADA\" align=\"center\" cellpadding=\"0\"\n"
                + "			cellspacing=\"0\">				<tr>"
                + "					<td style=\"font-size: 0; line-height: 0;\" height=\"40\">&nbsp;</td>"
                + "						</tr>		</table></td>\n</tr></tr>  </tbody>"
                + "</table>";
        return template;
    }

}
