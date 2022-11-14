package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.ViajePersonal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViajePersonalRepository extends JpaRepository<ViajePersonal, String>, ViajePersonalRepositoryCustom {

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL")
    Page<ViajePersonal> buscarActivos(Pageable pageable);

    @Query("SELECT a from Viaje a WHERE a.eliminado IS NULL ORDER BY a.modificacion")
    List<ViajePersonal> buscarActivos();

    @Query(value = "SELECT COUNT(v.chofer_id) FROM viaje_personal v LEFT JOIN chofer c ON c.id LIKE v.chofer_id WHERE c.eliminado IS NULL AND v.eliminado IS NULL AND v.estado_viaje NOT LIKE 'FINALIZADO' AND c.id LIKE :idChofer GROUP BY v.chofer_id;", nativeQuery = true)
    Integer contrarViajesPorChofer(@Param("idChofer") String idChofer);

}
	