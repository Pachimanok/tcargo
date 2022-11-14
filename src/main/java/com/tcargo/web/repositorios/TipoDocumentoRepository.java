package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.TipoDocumento;
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
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, String> {

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NULL")
    Page<TipoDocumento> buscarActivos(Pageable pageable);

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NULL")
    List<TipoDocumento> buscarActivos(Sort sort);

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NULL ORDER BY a.nombre")
    List<TipoDocumento> buscarActivos();

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NULL AND a.pais.id LIKE :idPais")
    List<TipoDocumento> buscarActivosPorPais(@Param(ID_PAIS_LABEL) String idPais);

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NOT NULL")
    Page<TipoDocumento> buscarEliminados(Pageable pageable);

    @Query("SELECT a from TipoDocumento a")
    Page<TipoDocumento> buscarTodos(Pageable pageable);

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre OR a.pais.nombre LIKE :nombre)")
    Page<TipoDocumento> buscarActivos(Pageable pageable, @Param("nombre") String nombre);

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NULL AND (a.nombre LIKE :nombre OR a.pais.nombre LIKE :nombre)")
    List<TipoDocumento> buscarActivos(Sort sort, @Param("nombre") String nombre);

    @Query("SELECT a from TipoDocumento a WHERE a.eliminado IS NULL AND a.nombre = :nombre")
    List<TipoDocumento> buscarTipoDocumentoPorNombre(@Param("nombre") String nombre);
}

