package com.tcargo.web.controladores.norest;

import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tcargo.web.errores.WebException;
import com.tcargo.web.servicios.ArchivoService;

@Controller
@RequestMapping("/public")
public class PublicController {

	@Autowired
	private ArchivoService archivoService;

	@GetMapping("/logo")
	public ResponseEntity<?> getImage() {
		String resourcePath = Paths.get("src").toAbsolutePath().toString();
		String path = "/proyectos/tcargo2" + resourcePath + "/main/resources/static/img/logo.png";

		try {
			Resource archivo = archivoService.cargar(path);
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/png").body(archivo);

		} catch (WebException e) {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain").body("Archivo no encontrado");
		}
	}

	@GetMapping("/locale")
	public String locale(HttpServletRequest request) {
		String lastUrl = request.getHeader("referer");

		return "redirect:".concat(lastUrl);
	}

}
