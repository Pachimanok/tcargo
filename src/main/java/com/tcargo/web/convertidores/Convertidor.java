package com.tcargo.web.convertidores;

import com.tcargo.web.errores.WebException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Convertidor<M, E> {

    public abstract E modeloToEntidad(M m) throws WebException;

    public abstract M entidadToModelo(E e);

    protected Log log;

    protected Convertidor() {
        this.log = LogFactory.getLog(getClass());
    }

}
