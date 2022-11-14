package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Ubicacion;

import java.util.List;

public interface CustomizedUbicacionRepository {

    List<Ubicacion> buscarCerca(Ubicacion ubicacion, Double radio);

}
