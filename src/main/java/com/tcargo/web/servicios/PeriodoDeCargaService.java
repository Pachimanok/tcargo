package com.tcargo.web.servicios;

import com.tcargo.web.entidades.PeriodoDeCarga;
import com.tcargo.web.errores.WebException;
import com.tcargo.web.modelos.PedidoRestModel;
import com.tcargo.web.repositorios.PeriodoDeCargaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class PeriodoDeCargaService {

    private final PeriodoDeCargaRepository periodoDeCargaRepository;

    @Autowired
    public PeriodoDeCargaService(PeriodoDeCargaRepository periodoDeCargaRepository) {
        this.periodoDeCargaRepository = periodoDeCargaRepository;
    }

    public PeriodoDeCarga buscarPorId(String id) {
        return periodoDeCargaRepository.findById(id).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public PeriodoDeCarga guardar(PeriodoDeCarga periodoDeCarga) throws WebException {
        if (periodoDeCarga.getInicio() == null || periodoDeCarga.getInicio().equals("")) {
            throw new WebException("El periodo de carga y descarga deben tener una fecha de inicio.");
        }
        if (periodoDeCarga.getFinalizacion() == null || periodoDeCarga.getFinalizacion().equals("")) {
            throw new WebException("El periodo de carga y descarga debe tener una fecha de finalización.");
        }
        if (periodoDeCarga.getCargaNocturna() == null) {
            throw new WebException("Indique si esta permitida la carga nocturna.");
        }
        periodoDeCarga.setInicio(periodoDeCarga.getInicio().replace("T", " "));
        periodoDeCarga.setFinalizacion(periodoDeCarga.getFinalizacion().replace("T", " "));

        periodoDeCarga.setModificacion(new Date());

        return periodoDeCargaRepository.save(periodoDeCarga);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {WebException.class, Exception.class})
    public PeriodoDeCarga eliminar(String id) throws WebException {
        PeriodoDeCarga periodoDeCarga = periodoDeCargaRepository.getOne(id);
        if (periodoDeCarga.getEliminado() == null) {
            periodoDeCarga.setEliminado(new Date());
            periodoDeCarga = periodoDeCargaRepository.save(periodoDeCarga);
        } else {
            throw new WebException("El periodoDeCarga que intenta eliminar ya se encuentra dado de baja.");
        }

        return periodoDeCarga;
    }

}