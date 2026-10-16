package co.edu.poli.examen2_Soto.servicios;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import co.edu.poli.examen2_Soto.modelo.Titular;

/**
 * Objeto de acceso a datos (DAO) para {@link Titular}.
 *
 * @author Julian Larrarte
 * @version 1.0
 */
public class DAOTitular implements CRUD<Titular> {

	/**
	 * No implementado en esta versión.
	 *
	 * @param t titular
	 * @return {@code null}
	 */
	@Override
	public String create(Titular t) {
		return null;
	}
	
	/**
	 * No implementado en esta versión.
	 *
	 * @param id  llave del titular
	 * @param <K> tipo de la llave
	 * @return {@code null}
	 */
	@Override
	public <K> Titular readone(K id) throws Exception {
		return null;
	}

	/**
	 * Lista todos los titulares registrados.
	 *
	 * @return lista de titulares (vacía si no hay registros)
	 * @throws Exception si ocurre un error de base de datos
	 */
	@Override
	public List<Titular> readall() throws Exception {

		Connection con = ConexionBD.getInstancia().getConexion();
		List<Titular> lista = new ArrayList<>();

		String SQL_SELECT_TITULAR = "SELECT ti.id AS titular_id, ti.nombre AS titular_nombre " 
		+ "FROM titular ti;";

		PreparedStatement ps = con.prepareStatement(SQL_SELECT_TITULAR);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			Titular t = new Titular(rs.getString("titular_id"), rs.getString("titular_nombre"));
			lista.add(t);
		}
		return lista;
	}

}
