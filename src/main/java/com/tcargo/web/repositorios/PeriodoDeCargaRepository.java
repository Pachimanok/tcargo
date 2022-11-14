package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.PeriodoDeCarga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PeriodoDeCargaRepository extends JpaRepository<PeriodoDeCarga, String> {


}
