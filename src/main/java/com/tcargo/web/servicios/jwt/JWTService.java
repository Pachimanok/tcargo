package com.tcargo.web.servicios.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.tcargo.web.entidades.Chofer;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.TransportadorModel;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.repositorios.UsuarioRepository;
import com.tcargo.web.servicios.ChoferService;
import com.tcargo.web.servicios.TransportadorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.tcargo.web.utiles.Textos.*;

@Service
public class JWTService {

    @Autowired
    private ChoferService choferService;
    private final UsuarioRepository usuarioRepository;
    private final TransportadorService transportadorService;

    // Secreto para el jwt generado aleatoriamente
    private static final String SECRET = "d612a8Ae6F01DA33626a966445404a96BB9d11c029298826A6A43D8Cf8AAEa9b33B8f64b7f1CAEc8F6C63C8200BD7F63e509E310F8E7B9E7AaD24d326B4534562337BEF8F28524C9faed56d6b33Bb2ddf3E1415eaa372d8ED92e1f049cEE0B2903671DD9D72A401Ad5172B885DB844fF6dB48ED07524F3220Dd73758Cb1ef1ce9A14dEeb710269C36E858A03FcADe2D5a732955A2ED85dc968b27862B86b67915e7433Bc6bdC0F21db066a78693f43ab8E83D095f917F064f7E0d0d89586811FA4C449cfFd61b4Bbdba3cd495A92BFb992728E5Ec962325DE2Cd9241A01243E0146dc146c099A4fC84306aC8dcE3beCedB1355F9bcc8a9e056B1a3116B5c8952197620998A30EAE588568e110e414D068bb5ae63B1A18Ef559274Eb0C5890468F30F5D0Fa4DB29136fA767ceA0c84d5884198f7888AABC32cdA8337F80A84Be5";

    private static final Algorithm ALG = Algorithm.HMAC256(SECRET);
    private static final String ISS = "T-Cargo";
    private final Log log = LogFactory.getLog(JWTService.class);

    @Autowired
    public JWTService(UsuarioRepository usuarioRepository, TransportadorService transportadorService) {
        this.usuarioRepository = usuarioRepository;
        this.transportadorService = transportadorService;
    }

    public String crearToken(UsuarioModel model, boolean loginDesdeRedes) {
        Usuario usuario = usuarioRepository.buscarPorMailOTelefono(model.getMail() == null ? model.getTelefono() : model.getMail());

        try {
            if (!loginDesdeRedes) {
                checkPass(usuario, model);
            }

            JWTCreator.Builder builder = JWT.create().withIssuer(ISS).withIssuedAt(new Date()).withExpiresAt(new DateTime().plusWeeks(1).toDate()).withClaim("id", usuario.getId()).withClaim("nombre", usuario.getNombre()).withClaim(ROL_LABEL, usuario.getRol().toString()).withClaim("mail", usuario.getMail()).withClaim(ID_PAIS_LABEL, usuario.getPais() != null ? usuario.getPais().getId() : null);

            if (Rol.getRolesTransportador().contains(usuario.getRol())) {
                TransportadorModel transportador = transportadorService.buscarPorIdUsuarioModelo(usuario.getId());
                return builder.withClaim(ID_TRANSPORTADOR_LABEL, transportador.getId()).withClaim("nombreTransportadora", transportador.getNombre().concat(" ".concat(transportador.getRazonSocial()))).withClaim(REGEX_PATENTE, usuario.getPais().getRegexPatente()).sign(ALG);
            }

            if (usuario.getRol().equals(Rol.CHOFER)) {
                Chofer chofer = choferService.buscarPorUsuario(usuario.getId());
                return builder.withClaim("idChofer", chofer.getId()).sign(ALG);
            }

            return builder.sign(ALG);
        } catch (JWTCreationException e) {
            log.error(e);
            return "Error al crear el token";
        } catch (NullPointerException e) {
            log.info("Usuario <" + model.getMail() + "> no encontrado");
            return USUARIO_INEXISTENTE;
        } catch (WebException e) {
            log.info(e.getMessage());
            return CREDENCIALES_INVALIDAS;
        }
    }

    private void checkPass(Usuario usuario, UsuarioModel model) throws WebException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(model.getClave(), usuario.getClave())) {
            throw new WebException("Contraseña incorrecta");
        }
        if (!usuario.isVerificado()) {
            throw new WebException("Cuenta no verificada");
        }
    }

    public boolean verificarToken(String token) {
        JWTVerifier verifier;
        try {
            verifier = JWT.require(ALG).withIssuer(ISS).build();
            verifier.verify(token.replace("Bearer ", ""));
        } catch (JWTVerificationException e) {
            log.error(e);
            return false;
        }
        return true;
    }
    
    public DecodedJWT decodificarToken(String token) {
        DecodedJWT jwt = null;
        try {
            jwt = JWT.decode(token.replace("Bearer ", ""));
        } catch (JWTDecodeException e) {
            log.error("error: " + e.getMessage());
        }
        return jwt;
    }

    public String crearTokenValidacionRegistro(String email) {
        return JWT.create().withIssuer(ISS).withIssuedAt(new Date()).withClaim("email", email).sign(ALG);
    }

    public String crearTokenRecuperacionClave(String email) {
        Usuario usuario = usuarioRepository.findByMailIgnoreCase(email);
        usuario.setUltimoRecuperarClave(new Date());
        usuarioRepository.save(usuario);

        return JWT.create().withIssuer(ISS).withIssuedAt(new Date()).withExpiresAt(new DateTime().plusHours(1).toDate()).withClaim("email", email).sign(ALG);
    }

}
