package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tcargo.web.utiles.Textos.ID_PAIS_LABEL;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String>, UsuarioRepositoryCustom {

    @Query("select a from Usuario a WHERE a.eliminado IS NULL")
    Page<Usuario> listarActivos(Pageable pageable);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND (a.mail LIKE :q OR a.telefono LIKE :q OR a.telefono LIKE :q OR a.cuit LIKE :q)")
    Page<Usuario> listarActivos(Pageable pageable, @Param("q") String q);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND (a.mail LIKE :q OR a.telefono LIKE :q OR a.telefono LIKE :q OR a.cuit LIKE :q)")
    List<Usuario> listarActivosList(Pageable pageable, @Param("q") String q);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND (a.pais.id LIKE :idPais)")
    Page<Usuario> listarActivosPorPais(Pageable pageable, @Param(ID_PAIS_LABEL) String idPais);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND (a.mail LIKE :q) AND (a.pais.id LIKE :idPais)")
    Page<Usuario> listarActivosPorPais(Pageable pageable, @Param("q") String q, @Param(ID_PAIS_LABEL) String idPais);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND a.id = :id")
    Usuario buscarPorId(@Param("id") String id);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND a.rol = :rol")
    List<Usuario> buscarPorRol(@Param("rol") Rol rol);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND (a.mail = :usuario OR a.telefono = :usuario)")
    Usuario buscarPorMailOTelefono(@Param("usuario") String usuario);

    @Query("select a from Usuario a WHERE a.eliminado IS NULL AND a.telefono = :telefono")
    Usuario buscarPorTelefono(@Param("telefono") String telefono);

    Usuario findByMailIgnoreCase(String mail);

}
