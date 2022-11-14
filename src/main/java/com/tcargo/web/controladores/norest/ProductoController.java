package com.tcargo.web.controladores.norest;

import com.tcargo.web.entidades.Producto;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ProductoModel;
import com.tcargo.web.servicios.PaisService;
import com.tcargo.web.servicios.ProductoService;
import com.tcargo.web.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Locale;

import static com.tcargo.web.utiles.Textos.*;

@Controller
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR','ROLE_ADMINISTRADOR_LOCAL','ROLE_ADMIN_DADOR','ROLE_ADMIN_TRANSPORTADOR','ROLE_ADMIN_AMBAS', 'ROLE_INVITADO')")
@RequestMapping("/producto")
public class ProductoController extends Controlador {

    private final MessageSource messages;
    private final PaisService paisService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    @Autowired
    public ProductoController(MessageSource messages, PaisService paisService, ProductoService productoService, UsuarioService usuarioService) {
        super("producto-list", "producto-form");
        this.messages = messages;
        this.paisService = paisService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    public String guardar(HttpSession session, @Valid @ModelAttribute(PRODUCTO_LABEL) ProductoModel producto, BindingResult resultado, ModelMap modelo, Locale locale) {
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        try {
            if (resultado.hasErrors()) {
                error(modelo, resultado);
            } else {
                if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
                    producto.setIdPais(u.getPais().getId());
                }
                productoService.guardar(producto);
                return "redirect:/producto/listado";
            }
        } catch (WebException e) {
            modelo.addAttribute(ERROR, messages.getMessage("producto.back.error.al.guardar", null, locale) + e.getMessage());
        } catch (Exception e) {
            modelo.addAttribute(ERROR, messages.getMessage("producto.back.error.al.inesperado", null, locale));
            log.error(ERROR_INESPERADO, e);
        }
        modelo.addAttribute(PAISES_LABEL, paisService.listarActivos());

        return vistaFormulario;
    }

    @PostMapping("/eliminar")
    public String eliminar(@ModelAttribute(PRODUCTO_LABEL) ProductoModel producto, ModelMap model, Locale locale) {
        model.addAttribute(ACCION_LABEL, "eliminar");
        try {
            productoService.eliminar(producto.getId());
            return "redirect:/producto/listado";
        } catch (Exception e) {
            model.addAttribute(ERROR, messages.getMessage("producto.back.error.al.eliminar", null, locale));
            return vistaFormulario;
        }
    }

    @GetMapping("/formulario")
    public ModelAndView formulario(@RequestParam(required = false) String id, @RequestParam(required = false, defaultValue = GUARDAR_LABEL) String accion) {
        ModelAndView modelo = new ModelAndView(vistaFormulario);
        ProductoModel producto = new ProductoModel();

        if (id != null) {
            producto = productoService.buscar(id);
        }

        modelo.addObject(PRODUCTO_LABEL, producto);
        modelo.addObject(ACCION_LABEL, accion);
        modelo.addObject(PAISES_LABEL, paisService.listarActivos());

        return modelo;
    }

    @GetMapping("/listado")
    public ModelAndView listar(HttpSession session, Pageable paginable, @RequestParam(required = false) String q, @RequestParam(value = "excel", required = false) String excel) {
        ModelAndView modelo = new ModelAndView(vistaListado);
        ordenar(paginable, modelo);
        Page<Producto> page;
        Usuario usuario = (Usuario) session.getAttribute(USUARIO_LABEL);
        Usuario u = usuarioService.buscar(usuario.getId());
        if (u.getRol() == Rol.ADMINISTRADOR_LOCAL) {
            if (q == null || q.isEmpty()) {
                page = productoService.listarActivosPorPais(paginable, u.getPais().getId(), excel != null);
            } else {
                page = productoService.listarActivosPorPais(paginable, q, u.getPais().getId(), excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        } else {
            if (q == null || q.isEmpty()) {
                page = productoService.listarActivos(paginable, excel != null);
            } else {
                page = productoService.listarActivos(paginable, q, excel != null);
                modelo.addObject(QUERY_LABEL, q);
            }
        }


        modelo.addObject(PAGE_LABEL, page);

        modelo.addObject(URL_LABEL, "/producto/listado");
        modelo.addObject(PRODUCTO_LABEL, new ProductoModel());
        return modelo;
    }


}
