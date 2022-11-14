package com.tcargo.web.filtros;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcargo.web.filtros.clases.CachedBodyHttpServletRequest;
import com.tcargo.web.servicios.jwt.JWTService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Component
public class JWTValidationFilter extends GenericFilterBean {

    private final JWTService jwtService;

    // Estas exclusiones del filtro son para endpoints rest que no se usan en la app mobile y para el login y registroCompleto.
    private static final List<String> exclusiones = Arrays.asList("/api/registro","/api/login", "/api/pedido/eliminar-grupo",
            "/api/viaje/guardarcarganegativa", "/api/viaje/guardarDesdeContraOferta", "/api/viaje/guardar",
            "/api/registroCompleto", "/api/ubicacion/near", "/api/documentacion/eliminararchivo", "/api/documentacion/download/",
            "/api/contraoferta/aceptarcontraoferta", "/api/contraoferta/desestimarcontraoferta", "/api/match/guardarDesdeContraOferta",
            "/api/viajepersonal/guardar", "/api/viaje/actualizarestado", "/api/viajepersonal/actualizarestado", "/api/invitacion",
            "/api/valoracion/remito");
    private static final Log LOG = LogFactory.getLog(JWTValidationFilter.class);

    @Autowired
    public JWTValidationFilter(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest((HttpServletRequest) servletRequest);

        String path = ((HttpServletRequest) servletRequest).getRequestURI();

        for (String exclusion : exclusiones) {
            if (path.startsWith(exclusion)) {
                filterChain.doFilter(cachedRequest, servletResponse);
                return;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String token = cachedRequest.getHeader(AUTHORIZATION);

        if (token != null && jwtService.verificarToken(token)) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setHeader(CONTENT_TYPE, "application/json;charset=utf-8");
            filterChain.doFilter(cachedRequest, response);
            return;
        }

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Map<String, Object> responseDetails = new HashMap<>();
        responseDetails.put("mensaje", "Token no válido");
        responseDetails.put("codigo", HttpStatus.BAD_REQUEST);

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        mapper.writeValue(response.getWriter(), responseDetails);
    }

}
