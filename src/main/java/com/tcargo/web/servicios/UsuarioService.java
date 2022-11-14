package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.UsuarioConverter;
import com.tcargo.web.entidades.*;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.*;
import com.tcargo.web.modelos.busqueda.BusquedaUsuarioModel;
import com.tcargo.web.repositorios.*;
import com.tcargo.web.servicios.jwt.JWTService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Pattern;

import static com.tcargo.web.utiles.Roles.defaultRol;
import static com.tcargo.web.utiles.Textos.*;
import static org.springframework.http.HttpStatus.*;

@Service("usuarioService")
public class UsuarioService implements UserDetailsService {

    @Autowired
    @Qualifier("usuarioRepository")
    private UsuarioRepository usuarioRepository;

    @Autowired
    @Qualifier("usuarioConverter")
    private UsuarioConverter usuarioConverter;

    @Autowired
    private TransportadorRepository transportadorRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private DadorDeCargaRepository dadorDeCargaRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private DadorDeCargaService dadorService;

    @Autowired
    private TransportadorService transportadorService;

    @Autowired
    private UbicacionService ubicacionService;

    @Autowired
    private ChoferService choferService;
    
    @Autowired
    private PaisService paisService;

    @Autowired
    private ChoferRepository choferRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MessageSource messajes;

    private final Log log = LogFactory.getLog(UsuarioService.class);

    public void cambiarMails() {
        Integer count = 1;
        for (Usuario usuario:
                usuarioRepository.findAll()) {
            usuario.setMail("@"+count);
            usuarioRepository.save(usuario);
            count++;
        }
    }

    public List<Usuario> buscarPorRol(Rol rol) {
        return usuarioRepository.buscarPorRol(rol);
    }

    public List<Usuario> buscarPorRoles(List<Rol> roles) {
        List<Usuario> usuarios = new ArrayList<>();

        for (Rol r : roles) {
            usuarios.addAll(usuarioRepository.buscarPorRol(r));
        }

        return usuarios;
    }

    public Usuario buscar(String id) {
        return usuarioRepository.getOne(id);
    }

    public UsuarioModel buscarModel(String id) {
        UsuarioModel usuario = usuarioConverter.entidadToModelo(usuarioRepository.getOne(id));
        Transportador t;
        DadorDeCarga d;

        if (usuario.getRol() == Rol.TRANSPORTADOR) {
            t = transportadorRepository.buscarTransportadorPorIdUsuario(id);
            usuario.setComisionTransportador(t.getComision());
        } else if (usuario.getRol() == Rol.DADOR_CARGA) {
            d = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(id);
            usuario.setComisionDador(d.getComision());
        } else if (usuario.getRol() == Rol.AMBAS) {
            d = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(id);
            t = transportadorRepository.buscarTransportadorPorIdUsuario(id);
            usuario.setComisionTransportador(t.getComision());
            usuario.setComisionDador(d.getComision());
        }
        return usuario;
    }

    public UsuarioModelPerfil armarUsuarioParaEdicionPerfil(String id) throws WebException {
        UsuarioModelPerfil usuario;

        if (id != null && !id.isEmpty()) {
            Usuario u = usuarioRepository.findById(id).get();
            usuario = usuarioConverter.entidadToModeloPerfil(u);
            return usuario;
        } else {
            throw new WebException("Error inesperado");
        }


    }

    public Page<Usuario> buscarTodos() {
        return usuarioRepository.listarActivos(null);
    }

    private String generarPassword(String email) {
        Calendar calendar = Calendar.getInstance();
        Integer year = calendar.get(Calendar.YEAR);
        return new BCryptPasswordEncoder().encode(email + year.toString());
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Usuario cambiarClave(UsuarioModel model) throws WebException {
        final Locale locale = LocaleContextHolder.getLocale();

        Usuario usuario = usuarioRepository.buscarPorId(model.getId());
        if (model.getClave1().length() <= 5) {
            throw new WebException(messajes.getMessage("usuario.back.error.blannqueo", null, locale));
        }
        if (!model.getClave1().equals(model.getClave2())) {
            throw new WebException(messajes.getMessage("usuario.back.error.blannqueo", null, locale));
        }
        usuario.setClave(new BCryptPasswordEncoder().encode(model.getClave1()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UserDetails loadUserByUsername(String username) {

        final Locale locale = LocaleContextHolder.getLocale();

        Usuario usuario = usuarioRepository.buscarPorMailOTelefono(username);


        if (usuario != null) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true); // true == allow create
            session.setAttribute("UserName", username);
            session.setAttribute(USUARIO_LABEL, usuario);
            session.setAttribute("DadorName", null);
            session.setAttribute("transportadorName", null);
            session.setAttribute("activo", usuario.isActivo());

            if (usuario.getPais() != null) {
                session.setAttribute(ID_PAIS_LABEL, usuario.getPais().getId());
            }

            Transportador transportador = transportadorRepository.buscarPorUsuario(usuario);

            if (transportador != null) {
                session.setAttribute(TRANSPORTADOR_LABEL, transportador.getNombre());
                session.setAttribute(ID_TRANSPORTADOR_LABEL, transportador.getId());
            }

            if (usuario.getRol() != null) {
                session.setAttribute(ROL_ACTUAL_LABEL, defaultRol(usuario.getRol()));

                if (usuario.getRol() == Rol.TRANSPORTADOR) {
                    Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());

                    if (t.getNombre() != null) {
                        session.setAttribute("transportadorName", t.getNombre());
                    }
                }

                if (usuario.getRol() == Rol.DADOR_CARGA) {
                    DadorDeCarga d = dadorService.buscarPorIdUsuario(usuario.getId());
                    if (d != null) {
                        if (d.getNombre() != null) {
                            session.setAttribute("dadorName", d.getNombre());
                        }
                    } else {
                        session.setAttribute("dadorName", "");
                    }
                    /*session.setAttribute("dadorName", d == null ? "" : d.getNombre() != null ? d.getNombre() : "" );*/

                }

                if (usuario.getRol() == Rol.AMBAS) {
                    DadorDeCarga d = dadorService.buscarPorIdUsuario(usuario.getId());
                    Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());

                    if (d.getNombre() != null) {
                        session.setAttribute("dadorName", d.getNombre());
                    }

                    if (t.getNombre() != null) {
                        session.setAttribute("transportadorName", t.getNombre());
                    }
                }

                if (usuario.getRol() == Rol.CHOFER && usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
                    session.setAttribute("choferName", usuario.getNombre());
                }
            }

            return transformar(usuario);
        } else {
        	log.info("USUARIO NO ENCONTRADO");
            throw new UsernameNotFoundException(messajes.getMessage("usuario.back.error.pass.user.incorrecto", null, locale));
        }
    }

    private Usuario convUsuario(Usuario user) {
        Usuario usuario = new Usuario();

        usuario.setUbicacion(user.getUbicacion());
        usuario.setId(user.getId());
        usuario.setMail(user.getMail());
        usuario.setNombre(user.getNombre());
        usuario.setVerificado(user.isVerificado());
        usuario.setActivo(user.isActivo());
        if (user.getRol() != null) {
            usuario.setRol(user.getRol());
        }

        return usuario;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Usuario guardar(UsuarioModel usuarioModel) throws WebException {
        Usuario usuario = usuarioConverter.modeloToEntidad(usuarioModel);
        final Locale locale = LocaleContextHolder.getLocale();

        if (usuarioModel.getRol() == null || usuarioModel.getRol().getTexto().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.rol", null, locale));
        }

        if (usuarioModel.getIdPais() == null || usuarioModel.getIdPais().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.pais", null, locale));
        }

        if (usuarioModel.getIdUbicacion() != null) {
            usuario.setUbicacion(ubicacionRepository.findById(usuarioModel.getIdUbicacion()).get());
        }

        if (usuario.getMail() == null || usuario.getMail().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail", null, locale));
        }
        if (!Pattern.compile("[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,4}").matcher(usuario.getMail()).matches()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail.validation", null, locale));
        }

        if (usuario.getNombre() == null || usuario.getNombre().equals("") || usuario.getNombre().length() < 3 ) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.nombre", null, locale));
        }

        if (usuario.getTelefono() == null || usuario.getTelefono().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.telefono", null, locale));
        }
        if (usuario.getCuit() == null || usuario.getCuit().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.cuit", null, locale));
        }

        if (!Pattern.compile("[+0-9]{9,15}").matcher(usuario.getTelefono()).matches()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.telefono.validation", null, locale));
        }

        if (!Pattern.compile("\\b(20|23|24|27|30|33|34)[0-9]{7,8}[0-9]").matcher(usuario.getCuit()).matches()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.cuit.validation", null, locale));
        }
        if (usuario.getMail() != null) {
            Usuario user = usuarioRepository.findByMailIgnoreCase(usuario.getMail());
            if (user != null) {
                if (user.getMail().equals(usuarioModel.getMail()) && user.getId() != usuarioModel.getId()) {
                    throw new WebException(messajes.getMessage("usuario.back.error.usuario.repetiro", null, locale));
                }
            }
        }

        if (usuario.getTelefono() != null) {
            Usuario user = usuarioRepository.buscarPorTelefono(usuario.getTelefono());
            if (user != null) {
                if (user.getTelefono().equals(usuarioModel.getTelefono()) && user.getId() != usuarioModel.getId()) {
                    throw new WebException(messajes.getMessage("usuario.back.error.usuario.repetido.telefono", null, locale));
                }
            }
        }

        if (usuarioModel.getClave() == null) {
            usuario.setClave(generarPassword(usuario.getMail()));
        }

        usuario.setModificacion(new Date());
        usuario.setVerificado(true);
        usuario.setActivo(true);
        usuario.setMail(usuario.getMail().toLowerCase());
        usuario = usuarioRepository.save(usuario);
        if (usuario.getRol() == Rol.TRANSPORTADOR) {

            Transportador t = null;

            if (usuarioModel.getId() != null && !usuarioModel.getId().equals("")) {
                t = transportadorRepository.buscarPorUsuario(usuario);
            }
            if (t == null) {
                t = new Transportador();
            }

            t.setModificacion(new Date());
            t.setUsuario(usuario);
            if (usuarioModel.getComisionTransportador() != null) {
                t.setComision(usuarioModel.getComisionTransportador());
            } else {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision", null, locale));
            }
            if (!Pattern.compile("[0-9]{1,2}|[0-9]{1,2}[.,][0-9]{1,2}").matcher(usuarioModel.getComisionTransportador().toString()).matches()) {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision.validation", null, locale));
            }
            transportadorRepository.save(t);
        } else if (usuario.getRol() == Rol.DADOR_CARGA) {
            DadorDeCarga d = null;

            if (usuarioModel.getId() != null && !usuarioModel.getId().equals("")) {
                d = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(usuario.getId());
            }
            if (d == null) {
                d = new DadorDeCarga();
            }

            d.setModificacion(new Date());
            d.setUsuario(usuario);
            if (usuarioModel.getComisionDador() != null) {
                d.setComision(usuarioModel.getComisionDador());
            } else {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision", null, locale));
            }
            if (!Pattern.compile("[0-9]{1,2}|[0-9]{1,2}[.,][0-9]{1,2}").matcher(usuarioModel.getComisionTransportador().toString()).matches()) {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision.validation", null, locale));
            }
            dadorDeCargaRepository.save(d);
        } else if (usuario.getRol() == Rol.AMBAS) {
//           TRANSPORTADOR
            Transportador t = null;
            if (usuarioModel.getId() != null && !usuarioModel.getId().equals("")) {
                t = transportadorRepository.buscarPorUsuario(usuario);
            }
            if (t == null) {
                t = new Transportador();
            }
            t.setModificacion(new Date());
            t.setUsuario(usuario);

            if (usuarioModel.getComisionTransportador() != null) {
                t.setComision(usuarioModel.getComisionTransportador());
            } else {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision", null, locale));
            }
            if (!Pattern.compile("[0-9]{1,2}|[0-9]{1,2}[.,][0-9]{1,2}").matcher(usuarioModel.getComisionTransportador().toString()).matches()) {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision.validation", null, locale));
            }
            transportadorRepository.save(t);

//            DADOR DE CARGA

            DadorDeCarga d = null;
            if (usuarioModel.getId() != null && !usuarioModel.getId().equals("")) {
                d = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(usuario.getId());
            }
            if (d == null) {
                d = new DadorDeCarga();
            }
            d.setModificacion(new Date());
            d.setUsuario(usuario);

            if (usuarioModel.getComisionDador() != null) {
                d.setComision(usuarioModel.getComisionDador());
            } else {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision", null, locale));
            }
            if (!Pattern.compile("[0-9]{1,2}|[0-9]{1,2}[.,][0-9]{1,2}").matcher(usuarioModel.getComisionTransportador().toString()).matches()) {
                throw new WebException(messajes.getMessage("usuario.back.error.select.comision.validation", null, locale));
            }

            dadorDeCargaRepository.save(d);
        }
        return usuario;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Usuario guardarUsuarioParaCrearTransportador(UsuarioModel usuarioModel) throws WebException {
        Usuario usuario = usuarioConverter.modeloToEntidad(usuarioModel);
        final Locale locale = LocaleContextHolder.getLocale();

        if (usuarioModel.getRol() == null || usuarioModel.getRol().getTexto().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.rol", null, locale));
        }

        if (usuarioModel.getIdPais() == null || usuarioModel.getIdPais().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.pais", null, locale));
        }


        if (usuario.getMail() == null || usuario.getMail().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail", null, locale));
        }

        if (!Pattern.compile("[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,4}").matcher(usuario.getMail()).matches()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail.validation", null, locale));
        }

        if (usuario.getNombre() == null || usuario.getNombre().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.nombre", null, locale));
        }

        if (usuario.getTelefono() == null || usuario.getTelefono().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.telefono", null, locale));
        }

        if (!Pattern.compile("[+0-9]{9,15}").matcher(usuario.getTelefono()).matches()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.telefono.validation", null, locale));
        }

        if (usuario.getMail() != null) {
            Usuario user = usuarioRepository.findByMailIgnoreCase(usuario.getMail());
            if (user != null) {
                if (user.getMail().equals(usuarioModel.getMail()) && user.getId() != usuarioModel.getId()) {
                    throw new WebException(messajes.getMessage("usuario.back.error.usuario.repetiro", null, locale));
                }
            }
        }

        if (usuario.getTelefono() != null) {
            Usuario user = usuarioRepository.buscarPorTelefono(usuario.getTelefono());
            if (user != null) {
                if (user.getTelefono().equals(usuarioModel.getTelefono()) && user.getId() != usuarioModel.getId()) {
                    throw new WebException(messajes.getMessage("usuario.back.error.usuario.repetido.telefono", null, locale));
                }
            }
        }

        usuario.setModificacion(new Date());
        usuario.setVerificado(true);
        usuario.setActivo(true);
        usuario.setMail(usuario.getMail().toLowerCase());
        usuario = usuarioRepository.save(usuario);
        return usuario;
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Usuario cambioDeRol(Usuario usuario, Rol rolAnterior) throws WebException {

        final Locale locale = LocaleContextHolder.getLocale();

        if (usuario.getRol() == null) {
            throw new WebException(messajes.getMessage("usuario.back.error.rol", null, locale));
        }

        if (rolAnterior == Rol.DADOR_CARGA) {
            if (usuario.getRol() == Rol.TRANSPORTADOR) {
                Transportador respuesta = transportadorRepository.buscarPorUsuario(usuario);
                if (respuesta == null) {
                    Transportador t = new Transportador();
                    t.setModificacion(new Date());
                    t.setUsuario(usuario);
                    transportadorRepository.save(t);
                } else {
                    respuesta.setEliminado(null);
                    transportadorRepository.save(respuesta);
                }
                DadorDeCarga d = dadorService.buscarPorIdUsuario(usuario.getId());
                d.setEliminado(new Date());
                dadorDeCargaRepository.save(d);

            } else if (usuario.getRol() == Rol.AMBAS) {
                Transportador respuesta = transportadorRepository.buscarPorUsuario(usuario);
                if (respuesta == null) {
                    Transportador t = new Transportador();
                    t.setModificacion(new Date());
                    t.setUsuario(usuario);
                    transportadorRepository.save(t);
                } else {
                    respuesta.setEliminado(null);
                    transportadorRepository.save(respuesta);
                }
            }
        } else if (rolAnterior == Rol.TRANSPORTADOR) {
            if (usuario.getRol() == Rol.DADOR_CARGA) {
                DadorDeCarga respuesta = dadorDeCargaRepository.buscarDadorDeCargaEliminadoPorIdUsuario(usuario.getId());
                if (respuesta == null) {
                    DadorDeCarga d = new DadorDeCarga();
                    d.setModificacion(new Date());
                    d.setUsuario(usuario);
                    dadorDeCargaRepository.save(d);
                } else {
                    respuesta.setEliminado(null);
                    dadorDeCargaRepository.save(respuesta);
                }
                Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());
                t.setEliminado(new Date());
                transportadorRepository.save(t);
            } else if (usuario.getRol() == Rol.AMBAS) {
                DadorDeCarga respuesta = dadorDeCargaRepository.buscarDadorDeCargaEliminadoPorIdUsuario(usuario.getId());
                if (respuesta == null) {
                    DadorDeCarga d = new DadorDeCarga();
                    d.setModificacion(new Date());
                    d.setUsuario(usuario);
                    dadorDeCargaRepository.save(d);
                } else {
                    respuesta.setEliminado(null);
                    dadorDeCargaRepository.save(respuesta);
                }
            }
        } else if (rolAnterior == Rol.AMBAS) {
            if (usuario.getRol() == Rol.TRANSPORTADOR) {
                DadorDeCarga d = dadorService.buscarPorIdUsuario(usuario.getId());
                d.setEliminado(new Date());
                dadorDeCargaRepository.save(d);
            } else if (usuario.getRol() == Rol.DADOR_CARGA) {
                Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());
                t.setEliminado(new Date());
                transportadorRepository.save(t);
            }

        } else if (rolAnterior == Rol.SIN_ROL) {
            if (usuario.getRol() == Rol.TRANSPORTADOR) {
                Transportador respuesta = transportadorRepository.buscarPorUsuario(usuario);
                if (respuesta == null) {
                    Transportador t = new Transportador();
                    t.setModificacion(new Date());
                    t.setUsuario(usuario);
                    transportadorRepository.save(t);
                }
            } else if (usuario.getRol() == Rol.DADOR_CARGA) {
                DadorDeCarga respuesta = dadorDeCargaRepository.buscarDadorDeCargaEliminadoPorIdUsuario(usuario.getId());
                if (respuesta == null) {
                    DadorDeCarga d = new DadorDeCarga();
                    d.setModificacion(new Date());
                    d.setUsuario(usuario);
                    dadorDeCargaRepository.save(d);
                }
            } else if (usuario.getRol() == Rol.AMBAS) {
                DadorDeCarga respuesta = dadorDeCargaRepository.buscarDadorDeCargaEliminadoPorIdUsuario(usuario.getId());
                if (respuesta == null) {
                    DadorDeCarga d = new DadorDeCarga();
                    d.setModificacion(new Date());
                    d.setUsuario(usuario);
                    dadorDeCargaRepository.save(d);
                }
                Transportador respuestat = transportadorRepository.buscarPorUsuario(usuario);
                if (respuestat == null) {
                    Transportador t = new Transportador();
                    t.setModificacion(new Date());
                    t.setUsuario(usuario);
                    transportadorRepository.save(t);
                }
            } else if (usuario.getRol() == Rol.CHOFER) {
                Chofer chofer = new Chofer();
                chofer.setModificacion(new Date());
                chofer.setUsuario(usuario);
                choferRepository.save(chofer);
            }

            usuario.setVerificado(false);

            if (usuario.getRol() != Rol.CHOFER) {
                emailService.registroCompleto(usuario.getMail(), usuario.getNombre());
            }
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public void editarPerfil(UsuarioModelPerfil MUsuario) throws WebException {
        Usuario usuario = usuarioConverter.modeloToEntidadPerfil(MUsuario);
        usuario = usuarioRepository.buscarPorId(usuario.getId());
        final Locale locale = LocaleContextHolder.getLocale();
        if (usuario.getPais() == null || MUsuario.getIdPais() == null || MUsuario.getIdPais().equals("") && usuario.getRol() != Rol.INVITADO) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.pais", null, locale));
        }

        if (usuario.getMail() == null || usuario.getMail().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail", null, locale));
        }

        if (usuario.getNombre() == null || usuario.getNombre().equals("") || usuario.getNombre().length() < 3 ) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.nombre", null, locale));
        }

        if (usuario.getTelefono() == null || usuario.getTelefono().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.telefono", null, locale));
        }

        if (usuario.getTelefono().length() < 9) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.telefono", null, locale));
        }

        if (usuario.getCuit() == null || usuario.getCuit().equals("")) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.cuit", null, locale));
        }

        if (usuario.getMail() != null) {
            if (!Pattern.compile("[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,4}").matcher(usuario.getMail()).matches()) {
                throw new WebException(messajes.getMessage("usuario.back.error.select.mail.validation", null, locale));
            }
            Usuario user = usuarioRepository.findByMailIgnoreCase(usuario.getMail());
            if (user != null) {
                if (user.getMail().equals(MUsuario.getMail()) && !user.getId().equals(MUsuario.getId())) {
                    throw new WebException(messajes.getMessage("usuario.back.error.select.mail.repetido", null, locale));
                }
            }
        }

        if (usuario.getTelefono() != null) {
            Usuario user = usuarioRepository.buscarPorTelefono(usuario.getTelefono());
            if (user != null) {
                if (user.getTelefono().equals(MUsuario.getTelefono()) && user.getId() != MUsuario.getId()) {
                    throw new WebException(messajes.getMessage("usuario.back.error.usuario.repetido.telefono", null, locale));
                }
            }
        }


        UbicacionModel ubicacion = new UbicacionModel();
        if (MUsuario.getIdUbicacion() != null && !MUsuario.getIdUbicacion().isEmpty()) {
            ubicacion.setId(MUsuario.getIdUbicacion());
        }
        if (MUsuario.getDireccion() != null && !MUsuario.getDireccion().isEmpty()) {
            ubicacion.setDireccion(MUsuario.getDireccion());

        }
        if (MUsuario.getLatitud() != null) {
            ubicacion.setLatitud(MUsuario.getLatitud());
        }
        if(MUsuario.getDescripcion() !=null && !MUsuario.getDescripcion().isEmpty()){
            ubicacion.setDescripcion(MUsuario.getDescripcion());
        }
        if (MUsuario.getLongitud() != null) {
            ubicacion.setLongitud(MUsuario.getLongitud());
        }
        usuario.setUbicacion(ubicacionService.guardar(ubicacion));
        usuario.setMail(usuario.getMail().toLowerCase());
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        if (MUsuario.getRol() == Rol.DADOR_CARGA) {

            DadorDeCargaModel d;
            try {
                d = dadorService.bucarPorIdUsuarioModel(MUsuario.getId());
            } catch (Exception e) {
                d = new DadorDeCargaModel();
                d.setIdUsuario(MUsuario.getId());
            }
            if (MUsuario.getNombreDador() != null && !MUsuario.getNombreDador().isEmpty()) {
                d.setNombre(MUsuario.getNombreDador());
            }
            if (MUsuario.getRazonSocialDador() != null && !MUsuario.getRazonSocialDador().isEmpty()) {
                d.setRazonSocial(MUsuario.getRazonSocialDador());
            }
            d.setIdUsuario(MUsuario.getId());
            dadorService.guardarDesdeEdicion(d);


        } else if (MUsuario.getRol() == Rol.TRANSPORTADOR) {

            TransportadorModel t;
            t = transportadorService.buscarPorIdUsuarioModelo(MUsuario.getId());
            if (t.getIdUsuario() == null || t.getIdUsuario().isEmpty()) {
                t.setIdUsuario(MUsuario.getId());
            }

            if (MUsuario.getNombreTransportador() != null && !MUsuario.getNombreTransportador().isEmpty()) {
                t.setNombre(MUsuario.getNombreTransportador());
            }
            if (MUsuario.getRazonSocialTransportador() != null && !MUsuario.getRazonSocialTransportador().isEmpty()) {
                t.setRazonSocial(MUsuario.getRazonSocialTransportador());
            }
            transportadorService.guardar(t);

        } else if (MUsuario.getRol() == Rol.AMBAS) {

            DadorDeCargaModel d;
            try {
                d = dadorService.bucarPorIdUsuarioModel(MUsuario.getId());
            } catch (Exception e) {
                d = new DadorDeCargaModel();
            }

            if (MUsuario.getNombreDador() != null && !MUsuario.getNombreDador().isEmpty()) {
                d.setNombre(MUsuario.getNombreDador());
            }
            if (MUsuario.getRazonSocialDador() != null && !MUsuario.getRazonSocialDador().isEmpty()) {
                d.setRazonSocial(MUsuario.getRazonSocialDador());
            }
            d.setIdUsuario(MUsuario.getId());
            dadorService.guardarDesdeEdicion(d);

            TransportadorModel t;
            try {
                t = transportadorService.buscarPorIdUsuarioModelo(MUsuario.getId());
            } catch (Exception e) {
                t = new TransportadorModel();
            }

            if (MUsuario.getNombreTransportador() != null && !MUsuario.getNombreTransportador().isEmpty()) {
                t.setNombre(MUsuario.getNombreTransportador());
            }
            if (MUsuario.getRazonSocialTransportador() != null && !MUsuario.getRazonSocialTransportador().isEmpty()) {
                t.setRazonSocial(MUsuario.getRazonSocialTransportador());
            }
            t.setIdUsuario(MUsuario.getId());
            transportadorService.guardar(t);

        } else if (MUsuario.getRol() == Rol.CHOFER) {
            ChoferModel chofer = choferService.buscarPorUsuarioModel(MUsuario.getId());
            if (MUsuario.getDocumento() != null && !MUsuario.getDocumento().isEmpty()) {
                chofer.setDocumento(MUsuario.getDocumento());
            }
            if (MUsuario.getTelefonoAdicional() != null && !MUsuario.getTelefonoAdicional().isEmpty()) {
                chofer.setTelefonoAdicional(MUsuario.getTelefonoAdicional());
            }
            if (MUsuario.getEmailAdicional() != null && !MUsuario.getEmailAdicional().isEmpty()) {
                chofer.setEmailAdicional(MUsuario.getEmailAdicional());
            }
            chofer.getUsuario().setId(MUsuario.getId());
            choferService.edit(chofer);
        }
        loadUserByUsername(usuario.getMail());

    }

    public Usuario editar(UsuarioModel usuarioModel) throws WebException {
        Usuario usuario = usuarioConverter.modeloToEntidad(usuarioModel);
        final Locale locale = LocaleContextHolder.getLocale();

        if (usuarioModel.getIdUbicacion() != null) {
            usuario.setUbicacion(ubicacionRepository.findById(usuarioModel.getIdUbicacion()).get());
        }

        if (usuario.getMail() == null) {
            throw new WebException("El mail no puede estar vacio");
        }

        if (!Pattern.compile("[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,4}").matcher(usuario.getMail()).matches()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail.validation", null, locale));
        }


        if (usuarioModel.getClave() == null) {
            usuario.setClave(generarPassword(usuario.getMail()));
        } else {
            usuario.setClave(usuarioModel.getClave());
        }

        usuario.setModificacion(new Date());
        usuario.setMail(usuario.getMail().toLowerCase());
        usuarioRepository.save(usuario);
        return usuario;
    }

    public void eliminar(String id) throws WebException {
        Locale locale = LocaleContextHolder.getLocale();
        if (id != null) {
            Usuario usuario = buscar(id);
            if (usuario == null) {
                throw new WebException(messajes.getMessage("usuario.back.error.no.encontrado", null, locale));
            }
            if (usuario.getRol() == Rol.TRANSPORTADOR) {
                Transportador t = transportadorRepository.buscarTransportadorPorIdUsuario(usuario.getId());
                t.setEliminado(new Date());
                transportadorRepository.save(t);
            } else if (usuario.getRol() == Rol.DADOR_CARGA) {
                DadorDeCarga d = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(usuario.getId());
                d.setEliminado(new Date());
                dadorDeCargaRepository.save(d);
            } else if (usuario.getRol() == Rol.AMBAS) {
                DadorDeCarga d = dadorDeCargaRepository.buscarDadorDeCargaPorIdUsuario(usuario.getId());
                d.setEliminado(new Date());
                dadorDeCargaRepository.save(d);
                Transportador t = transportadorRepository.buscarTransportadorPorIdUsuario(usuario.getId());
                t.setEliminado(new Date());
                transportadorRepository.save(t);
            }
            usuario.setEliminado(new Date());
            usuarioRepository.save(usuario);
        } else {
            throw new WebException(messajes.getMessage("usuario.back.error.no.encontrado", null, locale));
        }
    }

    private User transformar(Usuario usuario) {
        List<GrantedAuthority> permisos = new ArrayList<>();
        if (usuario != null) {
            GrantedAuthority permiso = null;
            if (usuario.getRol() != null) {
                permiso = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toString());
            }
            permisos.add(permiso);

            User user = new User(usuario.getMail(), usuario.getClave(), usuario.getEliminado() == null && usuario.isVerificado(), true, true, true, permisos);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession();

            usuario = convUsuario(usuario);
            session.setAttribute(USUARIO_LABEL, usuario);
            session.setAttribute("businesName", null);

            return user;
        } else {
            return null;
        }
    }

    public Page<Usuario> buscarTodos(Pageable paginable, String q) {
        if (q == null || q.isEmpty()) {
            return usuarioRepository.listarActivos(paginable);
        } else {
            List<Usuario> usuarios = usuarioRepository.listarActivosList(paginable, "%" + q + "%");
            List<Transportador> transportadores = transportadorRepository.findByRazonSocial(q);
            if (!transportadores.isEmpty()) {
                for (Transportador transportador : transportadores) {
                    usuarios.add(transportador.getUsuario());
                }
            }
            List<DadorDeCarga> dadores = dadorDeCargaRepository.findByRazonSocial(q);
            if (!dadores.isEmpty()) {
                for (DadorDeCarga dadorDeCarga : dadores) {
                    if (dadorDeCarga.getUsuario().getRol() != Rol.AMBAS) {
                        usuarios.add(dadorDeCarga.getUsuario());
                    }
                }
            }

            return new PageImpl<>(usuarios, paginable, usuarios.size());
        }
    }

    public Usuario registroDeUsuario(UsuarioModel usuarioModel) throws WebException {
        Locale locale = LocaleContextHolder.getLocale();
        Usuario usuario = usuarioConverter.modeloToEntidad(usuarioModel);

        if (usuario.getMail() == null) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail", null, locale));
        }
        if (!Pattern.compile("[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,4}").matcher(usuario.getMail()).matches()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail.validation", null, locale));
        }

        if (usuarioRepository.findByMailIgnoreCase(usuario.getMail()) != null) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.mail.repetido", null, locale));
        }

        if (usuario.getClave() == null || usuario.getClave().isEmpty()) {
            throw new WebException(messajes.getMessage("usuario.back.error.pass.empty", null, locale));
        }

        if (!usuarioModel.getClave().equals(usuarioModel.getClave1())) {
            throw new WebException(messajes.getMessage("usuario.back.error.pass.not.equals", null, locale));
        }

        if (usuarioModel.getClave().length() <= 5) {
            throw new WebException(messajes.getMessage("usuario.back.error.pass.not.length", null, locale));
        } else {
            usuario.setClave(new BCryptPasswordEncoder().encode(usuarioModel.getClave()));
        }

        if (usuarioModel.getRol() == null) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.rol", null, locale));
        }

        if (usuario.getTelefono() == null || usuario.getClave().isEmpty()) {
            throw new WebException(messajes.getMessage("usuario.back.error.select.telefono", null, locale));
        }

        if (usuarioRepository.buscarPorTelefono(usuario.getTelefono()) != null) {
            throw new WebException(messajes.getMessage("usuario.back.error.usuario.repetido.telefono", null, locale));
        }

        Ubicacion u = new Ubicacion();
        u.setModificacion(new Date());
        ubicacionRepository.save(u);

        usuario.setUbicacion(u);
        if(usuarioModel.getRol() != null) {
        	usuario.setRol(usuarioModel.getRol());
        }else {
        	usuario.setRol(Rol.SIN_ROL);
        }
        
        usuario.setModificacion(new Date());
        usuario.setVerificado(false);
        usuario.setMail(usuario.getMail().toLowerCase());
        usuario.setActivo(false);
        
        usuarioRepository.save(usuario);
        
        if (usuarioModel.getRol() == Rol.DADOR_CARGA) {

            DadorDeCarga d = dadorService.buscarPorIdUsuario(usuario.getId());
            if(d == null) {
                d = new DadorDeCarga();
                d.setUsuario(usuario);
                d.setModificacion(new Date());
            }
            dadorDeCargaRepository.save(d);

        } else if (usuarioModel.getRol() == Rol.TRANSPORTADOR) {

            Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());
            if (t == null) {
            	t = new Transportador();
                t.setUsuario(usuario);
                t.setModificacion(new Date());
            }
            transportadorRepository.save(t);

        } else if (usuarioModel.getRol() == Rol.AMBAS) {

        	DadorDeCarga d = dadorService.buscarPorIdUsuario(usuario.getId());
            if(d == null) {
                d = new DadorDeCarga();
                d.setUsuario(usuario);
                d.setModificacion(new Date());            
            }
            dadorDeCargaRepository.save(d);

            Transportador t = transportadorService.buscarPorIdUsuario(usuario.getId());
            if (t == null) {
            	t = new Transportador();
                t.setUsuario(usuario);
                t.setModificacion(new Date());
                t.setModificacion(new Date());
            }
            transportadorRepository.save(t);

        } else if (usuarioModel.getRol() == Rol.CHOFER) {
            Chofer chofer = choferService.buscarPorUsuario(usuario.getId());
            if (chofer == null) {
                chofer = new Chofer();
                chofer.setUsuario(usuario);
                chofer.setModificacion(new Date());
            }
            choferRepository.save(chofer);
        }



//        emailService.registroBasicoConRol(usuario.getMail());
        try {
        	emailService.registroCompleto(usuario.getMail(), usuario.getMail());
        }catch (Exception e) {
        	return usuario;
		}

        return usuario;
    }

    public Usuario registrarConRedes(String email, String nombre) {
        Usuario usuario = new Usuario();
        usuario.setMail(email.toLowerCase());
        usuario.setNombre(nombre);
        usuario.setClave(generarPassword(email));

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setModificacion(new Date());
        ubicacionRepository.save(ubicacion);
        usuario.setRol(Rol.SIN_ROL);
        usuario.setUbicacion(ubicacion);
        usuario.setModificacion(new Date());
        usuario.setVerificado(true);
        usuarioRepository.save(usuario);

        emailService.registroBasico(email);

        return usuario;
    }

    public void blanquearPass(String id) {
        Usuario usuario = buscar(id);
        usuario.setClave(generarPassword(usuario.getMail()));
        usuario.setModificacion(new Date());
        usuarioRepository.save(usuario);
        if(usuario.getMail() != null && !usuario.getMail().isEmpty()) {
        	emailService.recuperarClave(usuario.getMail());
        }
    }

    public String recuperarClave(Usuario usuario) throws NoSuchMessageException, WebException {
        if (usuario == null) {
        	final Locale locale = LocaleContextHolder.getLocale();
        	throw new WebException(messajes.getMessage("text.recuperarClave.form.span.correoNoValido", null, locale));
        }
        emailService.recuperarClave(usuario.getMail());
        return "Recuperar clave usuario: " + usuario.getMail();
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByMailIgnoreCase(email);
    }

    public UsuarioModel getFromPassRecoveryToken(String token) {
        String email = jwtService.decodificarToken(token).getClaim("email").asString();
        UsuarioModel model = new UsuarioModel();
        model.setId(usuarioRepository.findByMailIgnoreCase(email).getId());
        return model;
    }

    public Usuario verificar(String token) {
        String email = jwtService.decodificarToken(token).getClaim("email").asString();
        Usuario usuario = usuarioRepository.findByMailIgnoreCase(email);
        usuario.setVerificado(true);
        return usuarioRepository.save(usuario);
    }

    public void reenviarCorreoVerificacion(String email) {
        Usuario usuario = usuarioRepository.findByMailIgnoreCase(email);
        if (usuario != null && !usuario.isVerificado()) {
            emailService.registroCompleto(email, usuario.getNombre());
        }
    }

    public Page<UsuarioModelReporte> buscarPorCriterios(Pageable paginable, BusquedaUsuarioModel busqueda, boolean excel) {
        Page<Usuario> usuarios = usuarioRepository.buscarPorCriterios(paginable, busqueda, excel);
        List<UsuarioModelReporte> content = new ArrayList<>();
        for (Usuario usuario : usuarios.getContent()) {
            content.add(armarUserReporte(usuario));
        }
        return new PageImpl<>(content, paginable, usuarios.getTotalElements());
    }

    public UsuarioModelReporte armarUserReporte(Usuario usuario) {
        UsuarioModelReporte reporte = new UsuarioModelReporte();
        reporte.setId(usuario.getId());
        reporte.setNombre(usuario.getNombre());
        reporte.setTelefono(usuario.getTelefono());
        reporte.setCuit(usuario.getCuit());
        reporte.setMail(usuario.getMail());
        reporte.setNombrePais(usuario.getPais() != null ? usuario.getPais().getNombre() : "");
        reporte.setModificacion(usuario.getModificacion());
        reporte.setRol(usuario.getRol());
        return reporte;
    }

    public void guardarTokenFirebase(Usuario user, String token) {
        Usuario usuario = usuarioRepository.buscarPorId(user.getId());

        if (usuario != null) {
            Token tokenEntidad = tokenRepository.buscarPorTexto(token);
            if (tokenEntidad == null) {
                Token tokenNuevo = new Token();
                tokenNuevo.setCreado(new Date());
                tokenNuevo.setTexto(token);
                tokenNuevo.setUsuario(usuario);
                tokenRepository.save(tokenNuevo);
            }
        }
    }

    public void cambiarEstadoVerificado(String idUsuario, Boolean verificado) {
        Usuario u = usuarioRepository.buscarPorId(idUsuario);
        u.setVerificado(verificado);
        usuarioRepository.save(u);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Usuario resetUsuarioChofer(String idUsuario) {
        choferRepository.delete(choferRepository.buscarPorUsuarioId(idUsuario));
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario);
        usuario.setRol(Rol.SIN_ROL);
        usuario.setVerificado(true);

        return usuarioRepository.save(usuario);
    }

    public void comprobarUsuarioYGenerarToken(Map<String, Object> res, String email, HttpServletResponse response, String nombre) {
        Usuario usuario = buscarPorEmail(email);

        UsuarioModel model = new UsuarioModel();

        if (usuario != null) {
            model.setMail(email);
        }

        String token = jwtService.crearToken(model, true);

        if (token == null || token.equals("Error al crear el token")) {
            res.put(ERROR, ERROR_INESPERADO);
            response.setStatus(INTERNAL_SERVER_ERROR.value());
            return;
        }

        res.put("usuarioExistente", usuario != null);

        if (token.equals(CREDENCIALES_INVALIDAS)) {
            res.put(TOKEN, null);
            response.setStatus(NOT_FOUND.value());
        } else if (token.equals(USUARIO_INEXISTENTE)) {
            model = usuarioConverter.entidadToModelo(registrarConRedes(email, nombre));
            token = jwtService.crearToken(model, true);
            res.put(TOKEN, token);
            response.setStatus(CREATED.value());
        } else {
            res.put(TOKEN, token);
            response.setStatus(OK.value());
        }
    }

}
