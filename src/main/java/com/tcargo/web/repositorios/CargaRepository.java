package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Carga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargaRepository extends JpaRepository<Carga, String> {
}
