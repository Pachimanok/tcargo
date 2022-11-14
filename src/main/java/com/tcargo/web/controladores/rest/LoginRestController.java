package com.tcargo.web.controladores.rest;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.FacebookUserModel;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.servicios.UsuarioService;
import com.tcargo.web.servicios.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LoginRestController {

    private final JWTService jwtService;
    private final UsuarioService usuarioService;

    private static final Log LOG = LogFactory.getLog(LoginRestController.class);

    private static String clientId;
    private static String fbGraphUrl;

    @Value("${google-client-id}")
    private void setClientId(String clientId) {
        LoginRestController.clientId = clientId;
    }

    @Value("${facebook-graph-url}")
    private void setFbGraphUrl(String fbGraphUrl) {
        LoginRestController.fbGraphUrl = fbGraphUrl;
    }

    @PostMapping(value = "/login", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Object> login(@RequestBody UsuarioModel usuario, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        String token;

        if (usuario.getMail() == null) {
            if (usuario.getTelefono() == null || usuario.getTelefono().isEmpty()
                    || usuario.getClave() == null || usuario.getClave().isEmpty()) {
                res.put(ERROR, CREDENCIALES_INVALIDAS);
                res.put(CODIGO, BAD_REQUEST);
                response.setStatus(BAD_REQUEST.value());
                return res;
            }
        } else if (usuario.getTelefono() == null) {
            if (usuario.getMail() == null || usuario.getMail().isEmpty()
                    || usuario.getClave() == null || usuario.getClave().isEmpty()) {
                res.put(ERROR, CREDENCIALES_INVALIDAS);
                res.put(CODIGO, BAD_REQUEST);
                response.setStatus(BAD_REQUEST.value());
                return res;
            }
        } else {
            res.put(ERROR, CREDENCIALES_INVALIDAS);
            res.put(CODIGO, BAD_REQUEST);
            response.setStatus(BAD_REQUEST.value());
            return res;
        }



        token = jwtService.crearToken(usuario, false);

        if (token == null) {
            res.put(ERROR, ERROR_INESPERADO);
            res.put(CODIGO, INTERNAL_SERVER_ERROR);
            response.setStatus(INTERNAL_SERVER_ERROR.value());
            return res;
        }

        if (token.equals(CREDENCIALES_INVALIDAS) || token.equals(USUARIO_INEXISTENTE)) {
            res.put(ERROR, CREDENCIALES_INVALIDAS);
            res.put(CODIGO, NOT_FOUND);
            response.setStatus(NOT_FOUND.value());
            return res;
        }

        res.put(TOKEN, token);
        res.put(CODIGO, OK);
        response.setStatus(OK.value());
        return res;
    }

    @PostMapping("/login/google")
    public Map<String, Object> googleLogin(@RequestBody String idTokenString, HttpServletResponse response) {
        final NetHttpTransport transport = new NetHttpTransport();
        final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Map<String, Object> res = new HashMap<>();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String nombre = (String) payload.get("name");

                List<String> audiences = payload.getAudienceAsList();
                if (!audiences.contains(clientId)) {
                    throw new WebException("Token inválido");
                }

                usuarioService.comprobarUsuarioYGenerarToken(res, email, response, nombre);
            } else {
                res.put(ERROR, CREDENCIALES_INVALIDAS);
                response.setStatus(BAD_REQUEST.value());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            res.put(ERROR, CREDENCIALES_INVALIDAS);
            response.setStatus(BAD_REQUEST.value());
        }

        return res;
    }

    @PostMapping("/login/fb")
    public Map<String, Object> facebookLogin(@RequestBody FacebookUserModel fbUser, HttpServletResponse response) {
        Map<String, Object> res = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(fbGraphUrl, fbUser.getId(), fbUser.getAccessToken());
        FacebookUserModel facebookUserModel;
        try {
            facebookUserModel = restTemplate.getForObject(url, FacebookUserModel.class);
        } catch (RestClientException e) {
            String mensajeError = e.getMessage();
            if (mensajeError != null && mensajeError.contains("Malformed access token")) {
                res.put(ERROR, "Access token no válido");
            } else {
                res.put(ERROR, "UserID no válido");
            }
            return res;
        }

        if (facebookUserModel == null) {
            res.put(ERROR, ERROR_INESPERADO);
            return res;
        }

        usuarioService.comprobarUsuarioYGenerarToken(res, facebookUserModel.getEmail(), response, facebookUserModel.getName());

        return res;
    }

}
