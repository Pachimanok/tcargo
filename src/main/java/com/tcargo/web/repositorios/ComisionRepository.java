package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Comision;
import com.tcargo.web.entidades.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComisionRepository extends JpaRepository<Comision, String> {

    Comision findByPais(Pais pais);

}
