package com.tcargo.web.convertidores;

import com.tcargo.web.entidades.Chofer;
import com.tcargo.web.entidades.DadorDeCarga;
import com.tcargo.web.entidades.Transportador;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import com.tcargo.web.modelos.UsuarioModel;
import com.tcargo.web.modelos.UsuarioModelPerfil;
import com.tcargo.web.repositorios.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UsuarioConverter extends Convertidor<UsuarioModel, Usuario> {

    private final UsuarioRepository usuarioRepository;
    private final DadorDeCargaRepository dadorRepository;
    private final TransportadorRepository transportadorRepository;
    private final PaisRepository paisRepository;
    private final ChoferRepository choferRepository;

    public UsuarioModel entidadToModelo(Usuario usuario) {
        UsuarioModel model = new UsuarioModel();
        try {
            BeanUtils.copyProperties(usuario, model);
            if (usuario.getUbicacion() != null) {
                model.setIdUbicacion(usuario.getUbicacion().getId());
            }
            if (usuario.getPais() != null) {
                model.setIdPais(usuario.getPais().getId());
            }

        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo del usuario", e);
        }
        return model;
    }

    public UsuarioModelPerfil entidadToModeloPerfil(Usuario usuario) {
        UsuarioModelPerfil model = new UsuarioModelPerfil();
        try {
            BeanUtils.copyProperties(usuario, model);
            if (usuario.getUbicacion() != null) {
                model.setIdUbicacion(usuario.getUbicacion().getId());
                if (usuario.getUbicacion().getDireccion() != null && !usuario.getUbicacion().getDireccion().isEmpty()) {
                    model.setDireccion(usuario.getUbicacion().getDireccion());
                    model.setLatitud(usuario.getUbicacion().getLatitud());
                    model.setLongitud(usuario.getUbicacion().getLongitud());
                    model.setDescripcion(usuario.getUbicacion().getDescripcion());
                }
            }
            if (usuario.getPais() != null) {
                model.setIdPais(usuario.getPais().getId());
            }
            if (usuario.getRol() == Rol.DADOR_CARGA) {
                DadorDeCarga d = dadorRepository.buscarDadorDeCargaPorIdUsuario(usuario.getId());
                if (d.getNombre() != null && !d.getNombre().isEmpty()) {
                    model.setNombreDador(d.getNombre());
                }
                if (d.getRazonSocial() != null && !d.getRazonSocial().isEmpty()) {
                    model.setRazonSocialDador(d.getRazonSocial());
                }
            }
            if (usuario.getRol() == Rol.TRANSPORTADOR) {
                Transportador t = transportadorRepository.buscarPorUsuario(usuario);
                if (t.getNombre() != null && !t.getNombre().isEmpty()) {
                    model.setNombreTransportador(t.getNombre());
                }
                if (t.getRazonSocial() != null && !t.getRazonSocial().isEmpty()) {
                    model.setRazonSocialTransportador(t.getRazonSocial());
                }
            }
            if (usuario.getRol() == Rol.AMBAS) {
                DadorDeCarga d = dadorRepository.buscarDadorDeCargaPorIdUsuario(usuario.getId());
                if (d.getNombre() != null && !d.getNombre().isEmpty()) {
                    model.setNombreDador(d.getNombre());
                }
                if (d.getRazonSocial() != null && !d.getRazonSocial().isEmpty()) {
                    model.setRazonSocialDador(d.getRazonSocial());
                }
                Transportador t = transportadorRepository.buscarPorUsuario(usuario);
                if (t.getNombre() != null && !t.getNombre().isEmpty()) {
                    model.setNombreTransportador(t.getNombre());
                }
                if (t.getRazonSocial() != null && !t.getRazonSocial().isEmpty()) {
                    model.setRazonSocialTransportador(t.getRazonSocial());
                }
            }
            if (usuario.getRol() == Rol.CHOFER) {
                Chofer chofer = choferRepository.buscarPorUsuarioId(usuario.getId());
                if (chofer.getEmailAdicional() != null && !chofer.getEmailAdicional().isEmpty()) {
                    model.setEmailAdicional(chofer.getEmailAdicional());
                }
                if (chofer.getTelefonoAdicional() != null && !chofer.getTelefonoAdicional().isEmpty()) {
                    model.setTelefonoAdicional(chofer.getTelefonoAdicional());
                }
                if (chofer.getTipoDocumento() != null) {
                    model.setIdTipoDocumento(chofer.getTipoDocumento().getId());
                    model.setNombreTipoDocumento(chofer.getTipoDocumento().getNombre());
                }

                model.setIdTransportador(chofer.getTransportador().getId());
                if (chofer.getTransportador() != null && chofer.getTransportador().getId() != null && !chofer.getTransportador().getId().isEmpty()) {
                    model.setNombreTransportador(chofer.getTransportador().getNombre());
                }
            }
        } catch (Exception e) {
            log.error("Error al convertir la entidad en el modelo del usuario", e);
        }
        return model;
    }

    public Usuario modeloToEntidad(UsuarioModel model) {
        Usuario usuario = new Usuario();
        boolean x = false;
        String pass = "";
        if (model.getId() != null && !model.getId().isEmpty()) {
            usuario = usuarioRepository.buscarPorId(model.getId());
            if (usuario.getClave() != null) {
                pass = usuario.getClave();
                x = true;
            }
        }

        try {
            BeanUtils.copyProperties(model, usuario);
        } catch (Exception e) {
            log.error("Error al convertir el modelo del usuario en entidad", e);
        }

        if (x) {
            model.setClave(pass);
        }

        if (model.getIdPais() != null) {
            usuario.setPais(paisRepository.getOne(model.getIdPais()));
        }
        if (usuario.getCuit() != null && !usuario.getCuit().isEmpty()) {
            usuario.setCuit(usuario.getCuit().replaceAll("\\D", ""));
        }

        return usuario;
    }

    public Usuario modeloToEntidadPerfil(UsuarioModelPerfil model) {
        Usuario usuario = new Usuario();
        boolean x = false;
        String pass = "";
        if (model.getId() != null && !model.getId().isEmpty()) {
            usuario = usuarioRepository.findById(model.getId()).get();
            if (usuario.getClave() != null) {
                pass = usuario.getClave();
                x = true;
            }
        }

        try {
            BeanUtils.copyProperties(model, usuario);
        } catch (Exception e) {
            log.error("Error al convertir el modelo del usuario en entidad", e);
        }

        if (x) {
            model.setClave(pass);
        }

        if (model.getIdPais() != null) {
            usuario.setPais(paisRepository.getOne(model.getIdPais()));
        }

        return usuario;
    }

    public List<UsuarioModel> entidadesToModelos(List<Usuario> usuarios) {
        List<UsuarioModel> model = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            model.add(entidadToModelo(usuario));
        }
        return model;
    }

}
