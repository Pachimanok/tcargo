package com.tcargo.web.controladores.norest;

import static com.tcargo.web.utiles.Textos.ACCION_LABEL;
import static com.tcargo.web.utiles.Textos.DOCUMENTACION_LABEL;
import static com.tcargo.web.utiles.Textos.ERROR;
import static com.tcargo.web.utiles.Textos.EXITO_LABEL;
import static com.tcargo.web.utiles.Textos.GUARDAR_LABEL;
import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;
import static com.tcargo.web.utiles.Textos.ID_TRANSPORTADOR_LABEL;
import static com.tcargo.web.utiles.Textos.PAGE_LABEL;
import static com.tcargo.web.utiles.Textos.QUERY_LABEL;
import static com.tcargo.web.utiles.Textos.ROL_ACTUAL_LABEL;
import static com.tcargo.web.utiles.Textos.TIPODOCUMENTACIONES_LABEL;
import static com.tcargo.web.utiles.Textos.TRANSPORTADOR_LABEL;
import static com.tcargo.web.utiles.Textos.URL_LABEL;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.tcargo.web.entidades.Documentacion;
import com.tcargo.web.entidades.TipoDocumentacion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ChoferModel;
import com.tcargo.web.modelos.DocumentacionModel;
import com.tcargo.web.modelos.RemolqueModel;
import com.tcargo.web.modelos.VehiculoModel;
import com.tcargo.web.servicios.ArchivoService;
import com.tcargo.web.servicios.ChoferService;
import com.tcargo.web.servicios.DocumentacionService;
import com.tcargo.web.servicios.RemolqueService;
import com.tcargo.web.servicios.TipoDocumentacionService;
import com.tcargo.web.servicios.VehiculoService;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_TRANSPORTADOR', 'ROLE_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/documentacion")
public class DocumentacionController extends Controlador {

    private static final String ID_ENTIDAD = "idEntidad";
    private static final String ENTIDAD = "entidad";
    private static final String ARCHIVOS = "archivos";
    private static final String NOMBRE_ENTIDAD = "nombreEntidad";
    private static final String DASHBOARD = "/dashboard";
    private static final String ID = "id";
    private static final String REMOLQUE = "remolque";
    private static final String VEHICULO = "vehiculo";
    private static final String CHOFER = "chofer";

    private final ArchivoService archivoService;
    private final ChoferService choferService;
    private final DocumentacionService documentacionService;
    private final RemolqueService remolqueService;
    private final TipoDocumentacionService tipoDocumentacionService;
    private final VehiculoService vehiculoService;

    @Autowired
    public DocumentacionController(ArchivoService archivoService, ChoferService choferService, DocumentacionService documentacionService, RemolqueService remolqueService, TipoDocumentacionService tipoDocumentacionService, VehiculoService vehiculoService) {
        super("documentacion-list", "documentacion-form");
        this.archivoService = archivoService;
        this.choferService = choferService;
        this.documentacionService = documentacionService;
        this.remolqueService = remolqueService;
        this.tipoDocumentacionService = tipoDocumentacionService;
        this.vehiculoService = vehiculoService;
    }

    @PostMapping("/cargarArchivos")
    public @ResponseBody
    String nuevoguardar(MultipartHttpServletRequest request) {
        Iterator<String> iterator = request.getFileNames();
        StringBuilder archivos = new StringBuilder();
        while (iterator.hasNext()) {
            String archivo = iterator.next();
            MultipartFile file = request.getFile(archivo);
            try {
                if (file != null) {
                    archivos.append(archivoService.copiar(file)).append("\n");
                }
            } catch (WebException e) {
                log.error("Error al cargar el archivo: " + archivo);
            }
        }
        return archivos.toString();
    }

    @GetMapping("/ver/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Object> verArchivo(@PathVariable String filename) {
        try {
            Resource archivo = archivoService.cargar(filename);

            String tipo = "image/jpg";

            String nombre = archivo.getFilename();
            if (nombre != null) {
                if (nombre.endsWith("pdf")) {
                    tipo = "application/pdf";
                } else if (nombre.endsWith("gif")) {
                    tipo = "image/gif";
                } else if (nombre.endsWith("png")) {
                    tipo = "image/jpg";
                } else if (nombre.endsWith("jpeg")) {
                    tipo = "image/jpeg";
                }
            }
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, tipo).body(archivo);
        } catch (WebException e) {
            log.error("Error al ver el archivo. " + e.getMessage());
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "text/plain").body("Archivo no encontrado");
        }
    }

    @PostMapping("/guardar")
    public RedirectView guardar(@Valid @ModelAttribute DocumentacionModel model, BindingResult result, @RequestParam(value = ARCHIVOS, required = false) List<String> archivos, RedirectAttributes attributes, HttpSession session) {
        String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);
        if (!rol.equals(TRANSPORTADOR_LABEL) || transportadorNoValido(session, "", null, model)) {
            archivoService.eliminar(archivos);
            return new RedirectView(DASHBOARD);
        }

        RedirectView redirectView;

        try {
            if (result.hasErrors()) {
                StringBuilder sb = new StringBuilder();
                for (ObjectError error : result.getAllErrors()) {
                    sb.append(error.getDefaultMessage()).append(System.getProperty("line.separator"));
                }
                attributes.addFlashAttribute(ERROR, sb.toString());
                archivoService.eliminar(archivos);
                redirectView = new RedirectView(getRedirectURL(model, true));
            } else {
                documentacionService.guardar(model, archivos);
                attributes.addFlashAttribute(EXITO_LABEL, "Documentación cargada correctamente.");
                redirectView = new RedirectView(getRedirectURL(model, false));
            }
        } catch (WebException we) {
            log.error(we.getMessage());
            we.printStackTrace();
            attributes.addFlashAttribute(ERROR, we.getMessage());
            archivoService.eliminar(archivos);
            redirectView = new RedirectView(getRedirectURL(model, true));
        }

        return redirectView;
    }

    @PostMapping("/eliminar")
    public RedirectView eliminar(@ModelAttribute DocumentacionModel model, RedirectAttributes attributes, HttpSession session) {
        String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);
        if (!rol.equals(TRANSPORTADOR_LABEL) || model.getId() == null || model.getId().isEmpty() || transportadorNoValido(session, "", null, model))
            return new RedirectView(DASHBOARD);

        try {
            documentacionService.eliminar(model.getId());
            attributes.addFlashAttribute(EXITO_LABEL, "Documentación eliminada correctamente.");
        } catch (WebException we) {
            log.error(we.getMessage());
            we.printStackTrace();
            attributes.addFlashAttribute(ERROR, we.getMessage());
            return new RedirectView(getRedirectURL(model, true));
        }

        return new RedirectView(getRedirectURL(model, false));
    }

    @GetMapping("/formulario/{entidad}/{idEntidad}")
    public ModelAndView formulario(@PathVariable String entidad, @PathVariable String idEntidad, @RequestParam(value = ACCION_LABEL, defaultValue = GUARDAR_LABEL) String accion, HttpSession session) {
        String rol = (String) session.getAttribute(ROL_ACTUAL_LABEL);
        if (!rol.equals(TRANSPORTADOR_LABEL) || transportadorNoValido(session, entidad, idEntidad, null)) {
            return new ModelAndView(new RedirectView(DASHBOARD));
        }

        ModelAndView mav = new ModelAndView(vistaFormulario);
        String idPais = (String) session.getAttribute(ID_PAIS_LABEL);
        List<TipoDocumentacion> tiposDocumentacion;
        DocumentacionModel documentacion = new DocumentacionModel();

        switch (entidad) {
            case CHOFER:
                documentacion.setIdChofer(idEntidad);
                tiposDocumentacion = tipoDocumentacionService.buscarObligatorioChofer(idPais);
                break;
            case VEHICULO:
                documentacion.setIdVehiculo(idEntidad);
                tiposDocumentacion = tipoDocumentacionService.buscarObligatorioVehiculo(idPais);
                break;
            case REMOLQUE:
                documentacion.setIdRemolque(idEntidad);
                tiposDocumentacion = tipoDocumentacionService.buscarObligatorioRemolque(idPais);
                break;
            case ID:
                documentacion = documentacionService.buscarModel(idEntidad);
                Documentacion doc = documentacionService.buscarEntidad(idEntidad);
                tiposDocumentacion = Collections.singletonList(doc.getTipoDocumentacion());
                break;
            default:
                return new ModelAndView(new RedirectView(DASHBOARD));
        }

        mav.addObject(TIPODOCUMENTACIONES_LABEL, tiposDocumentacion);
        mav.addObject(DOCUMENTACION_LABEL, documentacion);
        mav.addObject(ACCION_LABEL, accion);

        return mav;
    }

    @GetMapping("/listado/{entidad}/{idEntidad}")
    public ModelAndView listado(@PathVariable String entidad, @PathVariable String idEntidad, @RequestParam(required = false) String q, Pageable pageable) {
        ModelAndView mav = new ModelAndView(vistaListado);
        ordenar(pageable, mav);
        Page<Documentacion> page;

        switch (entidad) {
            case CHOFER:
                ChoferModel chofer = choferService.buscar(idEntidad);
                mav.addObject(NOMBRE_ENTIDAD, "chofer " + chofer.getUsuario().getNombre());
                page = documentacionService.buscarPorIdChofer(pageable, idEntidad, q);
                break;
            case VEHICULO:
                VehiculoModel vehiculo = vehiculoService.buscar(idEntidad);
                mav.addObject(NOMBRE_ENTIDAD, "vehiculo " + vehiculo.getDominio());
                page = documentacionService.buscarPorIdVehiculo(pageable, idEntidad, q);
                break;
            case REMOLQUE:
                RemolqueModel remolque = remolqueService.buscar(idEntidad);
                mav.addObject(NOMBRE_ENTIDAD, "remolque " + remolque.getDominio());
                page = documentacionService.buscarPorIdRemolque(pageable, idEntidad, q);
                break;
            default:
                return new ModelAndView(new RedirectView(DASHBOARD));
        }

        Map<String, List<String>> archivos = new HashMap<>();
        for (Documentacion d : page.getContent()) {
            archivos.put(d.getId(), d.getArchivos());
        }

        mav.addObject(PAGE_LABEL, page);
        mav.addObject(URL_LABEL, "/documentacion/listado/" + entidad + "/" + idEntidad);
        mav.addObject(QUERY_LABEL, q);
        mav.addObject(ARCHIVOS, archivos);
        mav.addObject(ENTIDAD, entidad);
        mav.addObject(ID_ENTIDAD, idEntidad);

        return mav;
    }

    private boolean transportadorNoValido(HttpSession session, String entidad, String id, DocumentacionModel model) {
        String idTransportador = (String) session.getAttribute(ID_TRANSPORTADOR_LABEL);
        if (id == null) {
            entidad = "";
        }

        switch (entidad) {
            case CHOFER:
                return choferService.esChoferDeOtroTransportador(id, idTransportador);
            case REMOLQUE:
                return remolqueService.esRemolqueDeOtroTransportador(id, idTransportador);
            case VEHICULO:
                return vehiculoService.esVehiculoDeOtroTransportador(id, idTransportador);
            case ID:
                return documentacionService.esDocumentacionDeOtroTransportador(id, idTransportador);
            default:
                return modeloNoValido(model, idTransportador);
        }
    }

    private boolean modeloNoValido(DocumentacionModel model, String idTransportador) {
        if (model == null) {
            return true;
        }

        if (model.getIdChofer() != null && !model.getIdChofer().isEmpty()) {
            return choferService.esChoferDeOtroTransportador(model.getIdChofer(), idTransportador);
        }

        if (model.getIdRemolque() != null && !model.getIdRemolque().isEmpty()) {
            return remolqueService.esRemolqueDeOtroTransportador(model.getIdRemolque(), idTransportador);
        }

        if (model.getIdVehiculo() != null && !model.getIdVehiculo().isEmpty()) {
            return vehiculoService.esVehiculoDeOtroTransportador(model.getIdVehiculo(), idTransportador);
        }

        return true;
    }

    private String getRedirectURL(DocumentacionModel model, boolean error) {
        final String mapping = error ? "formulario" : "listado";
        final String documentacionUrl = "/documentacion/";
        if (model.getIdChofer() != null && !model.getIdChofer().isEmpty()) {
            return documentacionUrl + mapping + "/chofer/" + model.getIdChofer();
        } else if (model.getIdRemolque() != null && !model.getIdRemolque().isEmpty()) {
            return documentacionUrl + mapping + "/remolque/" + model.getIdRemolque();
        }
        return documentacionUrl + mapping + "/vehiculo/" + model.getIdVehiculo();
    }

}
