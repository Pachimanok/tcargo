package com.tcargo.web.controladores.rest;

import com.tcargo.web.entidades.Documentacion;
import com.tcargo.web.entidades.TipoDocumentacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.Base64MultipartFile;
import com.tcargo.web.modelos.DocumentacionApiModel;
import com.tcargo.web.modelos.DocumentacionModel;
import com.tcargo.web.repositorios.DocumentacionRepository;
import com.tcargo.web.servicios.*;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.*;

import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/documentacion")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DocumentacionRestController {

    private final ArchivoService archivoService;
    private final ChoferService choferService;
    private final DocumentacionService documentacionService;
    private final DocumentacionRepository documentacionRepository;
    private final JWTService jwtService;
    private final RemolqueService remolqueService;
    private final TipoDocumentacionService tipoDocumentacionService;
    private final VehiculoService vehiculoService;
    private final UsuarioService usuarioService;

    @GetMapping(value = "/eliminararchivo", produces = "text/plain")
    public String eliminarArchivo(@RequestParam("archivo") String nombreArchivo, @RequestParam("idDocumentacion") String idDocumentacion) {
        Documentacion documento = documentacionRepository.findById(idDocumentacion).orElse(null);
        List<String> archivos;

        if (documento != null) {
            archivos = documento.getArchivos();
            if (archivos.contains(nombreArchivo)) {
                for (Iterator<String> it = archivos.iterator(); it.hasNext(); ) {
                    if (it.next().equals(nombreArchivo)) {
                        it.remove();
                        archivoService.eliminar(nombreArchivo);
                    }
                }
                documento.setArchivos(archivos);
                documentacionRepository.save(documento);
            }
        }

        return nombreArchivo;
    }

    @GetMapping("/listado/{entidad}/{idEntidad}")
    public Map<String, Object> listar(@PathVariable("entidad") String entidad, @PathVariable("idEntidad") String idEntidad,
                                      @RequestParam(value = "q", required = false) String query, Pageable pageable, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        Page<Documentacion> page;

        switch (entidad) {
            case "chofer":
                page = documentacionService.buscarPorIdChofer(pageable, idEntidad, query);
                break;
            case "vehiculo":
                page = documentacionService.buscarPorIdVehiculo(pageable, idEntidad, query);
                break;
            case "remolque":
                page = documentacionService.buscarPorIdRemolque(pageable, idEntidad, query);
                break;
            default:
                response.setStatus(BAD_REQUEST.value());
                res.put(CODIGO, BAD_REQUEST);
                return res;
        }

        res.put("documentaciones", page);
        return res;
    }

    @GetMapping("/form/{entidad}")
    public Map<String, Object> formulario(@PathVariable String entidad, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String idPais = jwtService.decodificarToken(request.getHeader(AUTHORIZATION)).getClaim(ID_PAIS_LABEL).asString();
        List<TipoDocumentacion> tiposDocumentacion;

        switch (entidad) {
            case "chofer":
                tiposDocumentacion = tipoDocumentacionService.buscarObligatorioChofer(idPais);
                break;
            case "remolque":
                tiposDocumentacion = tipoDocumentacionService.buscarObligatorioRemolque(idPais);
                break;
            case "vehiculo":
                tiposDocumentacion = tipoDocumentacionService.buscarObligatorioVehiculo(idPais);
                break;
            default:
                response.setStatus(BAD_REQUEST.value());
                res.put(CODIGO, BAD_REQUEST);
                res.put(ERROR, "entidad '" + entidad + "' no válida. Debe ser 'chofer', 'remolque' o 'vehiculo'");
                return res;
        }

        response.setStatus(OK.value());
        res.put(CODIGO, OK);
        res.put(TIPODOCUMENTACIONES_LABEL, tiposDocumentacion);
        return res;
    }

    @PostMapping("/guardar")
    public Map<String, Object> guardar(@RequestBody DocumentacionApiModel apiModel, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();

        try {
            DocumentacionModel model = convertir(apiModel);
            Documentacion documentacion = documentacionService.guardar(model, guardarArchivo(apiModel.getArchivos().toArray(new Base64MultipartFile[0])));
            response.setStatus(CREATED.value());
            res.put(CODIGO, CREATED);
            res.put("idDocumentacion", documentacion.getId());
        } catch (Exception we) {
            response.setStatus(BAD_REQUEST.value());
            res.put(CODIGO, BAD_REQUEST);
            res.put(ERROR, we.getMessage());
        }

        return res;
    }

    private DocumentacionModel convertir(DocumentacionApiModel model) throws WebException {
        DocumentacionModel documentacion = new DocumentacionModel();
        BeanUtils.copyProperties(model, documentacion);

        if (model.getChofer() != null) {
            model.getChofer().getUsuario().setId(usuarioService.guardar(model.getChofer().getUsuario()).getId());
            documentacion.setIdChofer(choferService.guardar(model.getChofer(), model.getChofer().getIdTransportador()).getId());
        }
        if (model.getRemolque() != null) {
            documentacion.setIdRemolque(remolqueService.guardar(model.getRemolque()).getId());
        }
        if (model.getVehiculo() != null) {
            documentacion.setIdVehiculo(vehiculoService.guardar(model.getVehiculo()).getId());
        }

        return documentacion;
    }

    private List<String> guardarArchivo(Base64MultipartFile[] archivos) throws WebException {
        List<String> nombresArchivos = new ArrayList<>();
        for (Base64MultipartFile file : archivos) {
            file.setContenido(Base64.getMimeDecoder().decode(file.getBase64()));
            nombresArchivos.add(archivoService.copiar(file));
        }
        return nombresArchivos;
    }

    @GetMapping("/download/{filename:.+}")
    public void download(@PathVariable String filename, HttpServletResponse response) throws IOException {
        File file = new File(Paths.get("/home/tcargo-qa-archivos/").resolve(filename).toString());

        if (file.exists()) {
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            response.setContentType(mimeType);
            response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            response.setContentLength((int) file.length());
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            }
        }
    }

}
