package com.tcargo.web.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tcargo.web.entidades.Invitacion;
import com.tcargo.web.enumeraciones.EstadoInvitacion;
import com.tcargo.web.enumeraciones.EstadoNotificacion;

@Repository
public interface InvitacionRepository extends JpaRepository<Invitacion, String>{
	
	@Query("SELECT i FROM Invitacion i WHERE eliminado IS NULL AND transportador.id LIKE :idTransportador AND estadoNotificacion = :estado")
	List<Invitacion> buscarPorTransportadorYEstado(@Param("idTransportador")String idTransportador, @Param("estado")EstadoNotificacion estado);
	
	@Query("SELECT i FROM Invitacion i WHERE eliminado IS NULL AND chofer.id LIKE :idChofer")
	List<Invitacion> buscarPorChofer(@Param("idChofer")String idChofer);
	
	@Query("SELECT i FROM Invitacion i WHERE eliminado IS NULL AND estadoInvitacion = :estadoInvitacion")
	List<Invitacion> buscarPorEstado(@Param("estadoInvitacion")EstadoInvitacion estado);
	
	@Query("SELECT i FROM Invitacion i WHERE eliminado IS NULL AND estadoInvitacion = :estadoInvitacion AND transportador.id LIKE :idTransportador AND estadoNotificacion = :estado")
	List<Invitacion> buscarPorEstadoInvAndTransportadorAndEstadoNotif(@Param("estadoInvitacion")EstadoInvitacion estadoInv,@Param("idTransportador")String idTransportador, @Param("estado")EstadoNotificacion estado);
}
