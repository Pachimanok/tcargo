package com.tcargo.web.servicios;

import com.tcargo.web.convertidores.ValoracionConverter;
import com.tcargo.web.entidades.Valoracion;
import com.tcargo.web.enumeraciones.EstadoValoracion;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.ValoracionModel;
import com.tcargo.web.repositorios.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final ValoracionConverter valoracionConverter;
    private final MessageSource messages;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public Valoracion guardar(ValoracionModel valoracion) throws WebException {
        Valoracion v;
        final Locale locale = LocaleContextHolder.getLocale();
        v = valoracionConverter.modeloToEntidad(valoracion);

        if (v.getCreador() == null) {
            throw new WebException(messages.getMessage("valoracion.back.error.creador", null, locale));
        }
        if (v.getReceptor() == null) {
            throw new WebException(messages.getMessage("valoracion.back.error.receptor", null, locale));
        }
        if (v.getCoincidencia() == null) {
            throw new WebException(messages.getMessage("valoracion.back.error.match", null, locale));
        }
        if (v.getValoracion() == null) {
            throw new WebException(messages.getMessage("valoracion.back.error.valor", null, locale));
        }

        v.setModificacion(new Date());

        if (v.getEstadoValoracion() == null) {
            v.setEstadoValoracion(EstadoValoracion.FINALIZADA_CREADOR);
        }

        return valoracionRepository.save(v);
    }

    public ValoracionModel buscarModel(String idValoracion) {
        return valoracionConverter.entidadToModelo(valoracionRepository.getOne(idValoracion));
    }

    public List<Valoracion> buscarPropiasParaCompletar(EstadoValoracion estado, String idCreador) {
        return valoracionRepository.buscarPropiasParaCompletar(estado, idCreador);
    }

    public List<Valoracion> buscarPropiasComoReceptor(EstadoValoracion estado, String idReceptor) {
        return valoracionRepository.buscarPropiasComoReceptor(estado, idReceptor);
    }

    public ValoracionModel buscarPorIdCreadorYpedidoModel(String idCreador, Long idPedido) {
        return valoracionConverter.entidadToModelo(valoracionRepository.buscarPorIdCreadorAndPedido(idCreador, idPedido));
    }

    public Page<Valoracion> buscarPorIdCreador(Pageable page, String idCreador) {
        return valoracionRepository.buscarPorIdCreador(page, idCreador);
    }

    public Page<Valoracion> buscarPorIdReceptor(Pageable page, String idReceptor) {
        return valoracionRepository.buscarPorIdReceptor(page, idReceptor);
    }

    public List<ValoracionModel> buscarPorIdCreadorOrdenadoPorModificacion(String idCreador) {
        return valoracionConverter.entidadesToModelo(valoracionRepository.buscarPorIdCreadorList(idCreador));
    }

    public List<ValoracionModel> buscarPorIdReceptorOrdenadoPorModificacion(String idCreador) {
        return valoracionConverter.entidadesToModelo(valoracionRepository.buscarPorIdReceptorList(idCreador));
    }

    public int sacarPromedio(String idReceptor) {
        Integer promedio = valoracionRepository.promedioValoraciones(idReceptor);
        return promedio != null ? promedio : 0;
    }

}
