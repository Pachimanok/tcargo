package com.tcargo.web.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tcargo.web.entidades.Token;


@Repository
public interface TokenRepository extends JpaRepository<Token, String>, PagingAndSortingRepository<Token, String> {


	@Query("SELECT a from Token a WHERE a.eliminado IS NULL AND a.texto = :texto")
	public Token buscarPorTexto(@Param("texto") String texto);
	
	@Query("SELECT a from Token a WHERE a.eliminado IS NULL AND a.id = :idToken")
	public Token buscarPorId(@Param("idToken") String idToken);
	
	@Query("SELECT a from Token a WHERE a.eliminado IS NULL AND a.usuario.id = :idUsuario")
	public List<Token> buscarPorUsuarioId(@Param("idUsuario") String idUsuario);

}
