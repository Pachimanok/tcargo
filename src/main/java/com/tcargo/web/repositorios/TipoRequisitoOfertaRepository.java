package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Pais;
import com.tcargo.web.entidades.TipoRequisitoOferta;
import com.tcargo.web.enumeraciones.TipoDeViaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;

@Repository
public interface TipoRequisitoOfertaRepository extends JpaRepository<TipoRequisitoOferta, String> {

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL")
    Page<TipoRequisitoOferta> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL")
    List<TipoRequisitoOferta> buscarActivos(Sort sort);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL AND a.obligatorioOferta = true")
    List<TipoRequisitoOferta> buscarActivosObligatorioOferta(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL AND a.obligatorioPedido = true AND a.pais.id = :idPais")
    List<TipoRequisitoOferta> buscarActivosObligatorioPedido(@Param(ID_PAIS_LABEL) String idPais);

    List<TipoRequisitoOferta> findByPaisAndTipoDeViajeOrTipoDeViajeAndObligatorioPedidoTrue(Pais pais, TipoDeViaje tipoDeViaje, TipoDeViaje tipoAmbos);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<TipoRequisitoOferta> buscarActivos();

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NOT NULL")
    Page<TipoRequisitoOferta> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoRequisitoOferta a")
    Page<TipoRequisitoOferta> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL AND ( a.nombre LIKE :q OR a.descripcion LIKE :q OR a.pais.nombre LIKE :q)")
    Page<TipoRequisitoOferta> buscarActivos(Pageable pageable, @Param("q") String q);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre")
    List<TipoRequisitoOferta> buscarActivos(Sort sort, @Param("nombre") String caracteristicas);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL AND a.nombre = :nombre AND a.pais.id = :idPais")
    List<TipoRequisitoOferta> buscarTipoRequisitoOfertaPorNombre(@Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoRequisitoOferta a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    Page<TipoRequisitoOferta> buscarActivosPorPais(Pageable pageable, String idPais);

    @Query("SELECT a from TipoRequisitoOferta a WHERE (a.eliminado IS NULL) AND (a.pais.id LIKE :idPais)")
    List<TipoRequisitoOferta> buscarActivosPorPais(Sort sort, String idPais);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    Page<TipoRequisitoOferta> buscarActivosPorPais(Pageable pageable, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoRequisitoOferta a WHERE a.eliminado IS NULL AND a.nombre LIKE :nombre AND a.pais.id LIKE :idPais")
    List<TipoRequisitoOferta> buscarActivosPorPais(Sort sort, @Param("nombre") String nombre, @Param(ID_PAIS_LABEL) String idPais);

}

