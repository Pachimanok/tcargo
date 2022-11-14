package com.tcargo.web.repositorios;

import com.tcargo.web.entidades.ContraOferta;
import com.tcargo.web.entidades.Pedido;
import com.tcargo.web.entidades.Usuario;
import com.tcargo.web.enumeraciones.EstadoPedido;
import com.tcargo.web.modelos.busqueda.BusquedaPedidoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class PedidoRepositoryCustomImpl implements PedidoRepositoryCustom {

    private static final int CONCAT_STR_LENGTH = 5;
    private static final String AND = " and ";
    private static final String COMILLA_AND = "' and ";
    private static final String PARENTESIS_AND = ") and ";

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Pedido> buscarPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, Boolean matcheados, boolean excel) {
        return buscar(pageable, busqueda, null, null, null, matcheados, true, false, excel);
    }

    @Override
    public Page<Pedido> buscarPorCriteriosParaTransportador(Pageable pageable, BusquedaPedidoModel busqueda, List<ContraOferta> contraOfertas, boolean excel) {
        return buscar(pageable, busqueda, null, null, contraOfertas, null, false, true, excel);
    }

    @Override
    public Page<Pedido> buscarPorCriteriosParaDador(Pageable pageable, BusquedaPedidoModel busqueda, Usuario usuario, boolean excel) {
        return buscar(pageable, busqueda, null, usuario, null, null, false, false, excel);
    }

    @Override
    public Page<Pedido> buscarOfertadosPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, List<Long> idsPedidos, boolean excel) {
        return buscar(pageable, busqueda, idsPedidos, null, null, null, false, true, excel);
    }

    @Override
    public Page<Pedido> buscarMatcheadosPorCriterios(Pageable pageable, BusquedaPedidoModel busqueda, List<Long> idsPedidos, boolean excel) {
        return buscar(pageable, busqueda, idsPedidos, null, null, true, false, true, excel);
    }

    private Page<Pedido> buscar(Pageable pageable, BusquedaPedidoModel busqueda, List<Long> idsPedidos, Usuario usuario,
                                List<ContraOferta> contraOfertas, Boolean matcheado, boolean admin, boolean isTransportador, boolean excel) {
        String order;
        if (pageable.getSort().isSorted()) {
            order = StringUtils.collectionToCommaDelimitedString(
                    StreamSupport.stream(pageable.getSort().spliterator(), false)
                            .map(o -> o.getProperty() + " " + o.getDirection())
                            .collect(Collectors.toList())
            );
        } else {
            order = "p.estado_pedido asc, pc.inicio asc";
        }

        final String idsQuery = "select distinct max(p.id) as id from pedido p left join carga c on c.id=p.carga_id " +
                "left join contenedor co on co.id=p.contenedor_id left join usuario u on u.id=p.dador_id " +
                "left join periodo_de_carga pc on pc.id=p.periodo_de_carga_id left join periodo_de_carga pd on pd.id=p.periodo_de_descarga_id " +
                "left join ubicacion ud on ud.id=p.ubicacion_desde_id left join ubicacion uh on uh.id=p.ubicacion_hasta_id " +
                "left join moneda m on m.id=p.moneda_id left join tipo_embalaje te on te.id=c.tipo_embalaje_id " +
                "left join producto pr on pr.id=c.producto_id left join carga_tipo_cargas ctc on ctc.carga_id=c.id " +
                "left join tipo_carga tc on tc.id=ctc.tipo_cargas_id left join tipo_vehiculo tv on c.tipo_vehiculo_id = tv.id " +
                "left join tipo_remolque tr on tr.id = c.tipo_remolque_id" + where(busqueda, admin, isTransportador, idsPedidos, usuario, contraOfertas, matcheado) +
                " group by p.carga_id";
        
        Query pedidosQuery = em.createNativeQuery("select p.* from pedido p left join periodo_de_carga pc on p.periodo_de_carga_id = pc.id " +
                "where p.id in (" + idsQuery + ") order by " + order, Pedido.class);
        List<Pedido> pedidos;
        if (excel) {
            //noinspection unchecked
            pedidos = pedidosQuery.getResultList();
        } else {
            //noinspection unchecked
            pedidos = pedidosQuery.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        }

        Query countQuery = em.createNativeQuery("select count(*) from (" + idsQuery + ") as count");
        long count = ((BigInteger) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(pedidos, pageable, count);
    }

    private String where(BusquedaPedidoModel busqueda, boolean admin, boolean isTransportador, List<Long> idsPedidos, Usuario usuario,
                         List<ContraOferta> contraOfertas, Boolean matcheado) {
        StringBuilder sb = new StringBuilder(" where ");

        if (!admin) {
            String estadosDisponible = StringUtils.collectionToCommaDelimitedString(EstadoPedido.prioridadEstadosDisponible());
            if (Boolean.TRUE.equals(matcheado)) {
                estadosDisponible += "," + EstadoPedido.ASIGNADO.getPrioridad();
            }
            sb.append("p.eliminado is null and ");
            if (busqueda.getEstadoPedido() == null) {
                sb.append("p.estado_pedido in (").append(estadosDisponible).append(PARENTESIS_AND);
            }
            if (isTransportador) {
                if (matcheado != null && matcheado) {
                    sb.append("p.asignado_a_transportador=").append(true).append(AND);
                } else {
                    sb.append("p.asignado_a_transportador=false and ");
                }
            }
        }

        if (busqueda.getOtro() != null && !busqueda.getOtro().isEmpty()) {
            try {
                Long id = Long.valueOf(busqueda.getOtro());
                sb.append("p.id=").append(id).append(AND);
            } catch (NumberFormatException nfe) {
                busqueda.setOtro("%" + busqueda.getOtro() + "%");
                sb.append("(ud.direccion like '").append(busqueda.getOtro()).append("' ");
                sb.append("or uh.direccion like '").append(busqueda.getOtro()).append("' ");
                sb.append("or tv.nombre like '").append(busqueda.getOtro()).append("' ");
                sb.append("or tr.caracteristicas like '").append(busqueda.getOtro()).append("' ");
                sb.append("or tc.caracteristicas like '").append(busqueda.getOtro()).append("' ");
                sb.append("or p.descripcion like '").append(busqueda.getOtro()).append("') and ");
            }
        }

        if (busqueda.getIdProducto() != null && !busqueda.getIdProducto().isEmpty()) {
            sb.append("pr.id='").append(busqueda.getIdProducto()).append(COMILLA_AND);
        }

        if (busqueda.getIdTipoCarga() != null && !busqueda.getIdTipoCarga().isEmpty()) {
            sb.append("tc.id='").append(busqueda.getIdTipoCarga()).append(COMILLA_AND);
        }

        if (busqueda.getValor() != null && busqueda.getValor() > 0) {
            sb.append("p.valor<=").append(busqueda.getValor()).append(AND);
        }

        if (busqueda.getEstadoPedido() != null) {
            sb.append("p.estado_pedido=").append(busqueda.getEstadoPedido().getPrioridad()).append(AND);
        }

        if (busqueda.getFechaCarga() != null && !busqueda.getFechaCarga().isEmpty()) {
            sb.append("pc.inicio<='").append(busqueda.getFechaCarga().concat("%")).append("' and pc.finalizacion>='")
                    .append(busqueda.getFechaCarga()).append(COMILLA_AND);
        } else if (!admin && (idsPedidos == null || idsPedidos.isEmpty()) && (busqueda.getFechaDescarga() == null || busqueda.getFechaDescarga().isEmpty())) {
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String now = dateFormat.format(date);
            sb.append("pc.finalizacion>='").append(now).append(COMILLA_AND);
        }

        if (busqueda.getFechaDescarga() != null && !busqueda.getFechaDescarga().isEmpty()) {
            sb.append("pd.inicio<='").append(busqueda.getFechaDescarga().concat("%")).append("' and pd.finalizacion>='")
                    .append(busqueda.getFechaDescarga()).append(COMILLA_AND);
        }

        if (busqueda.getRecibirOfertas() != null) {
            sb.append("p.recibir_ofertas=").append(busqueda.getRecibirOfertas()).append(AND);
        }

        if (busqueda.getTipoDeViaje() != null) {
            sb.append("p.tipo_de_viaje='").append(busqueda.getTipoDeViaje()).append(COMILLA_AND);
        }

        if (idsPedidos != null && !idsPedidos.isEmpty()) {
            String ids = StringUtils.collectionToCommaDelimitedString(idsPedidos);
            sb.append("p.id in (").append(ids).append(PARENTESIS_AND);
        }

        if (usuario != null) {
            sb.append("p.dador_id='").append(usuario.getId()).append(COMILLA_AND);
        }

        if (contraOfertas != null && !contraOfertas.isEmpty()) {
            String idsContraOfertas = StringUtils.collectionToCommaDelimitedString(
                    contraOfertas.stream()
                            .map(ContraOferta::getPedido)
                            .map(Pedido::getId)
                            .collect(Collectors.toList())
            );
            sb.append("p.id not in (").append(idsContraOfertas).append(PARENTESIS_AND);
        }

        if (admin && matcheado != null) {
            sb.append("p.asignado_a_transportador=").append(matcheado);
        }

        String where = sb.toString();

        if (where.endsWith(" where ")) {
            return "";
        }

        if (where.endsWith(AND)) {
            where = where.substring(0, where.length() - CONCAT_STR_LENGTH);
        }

        return where;
    }

}
