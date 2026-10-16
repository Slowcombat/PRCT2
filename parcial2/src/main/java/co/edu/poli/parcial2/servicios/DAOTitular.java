package co.edu.poli.parcial2.servicios;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import co.edu.poli.parcial2.modelo.Titular;

/**
 * Objeto de acceso a datos (DAO) para {@link Titular}.
 *
 * @author Julian Larrarte
 * @version 2.0
 */
public class DAOTitular implements CRUD<Titular> {

	/** Inserta un titular. */
	private static final String SQL_INSERT = "INSERT INTO titular (id, nombre) VALUES (?, ?)";

	/** Busca un titular por su identificación. */
	private static final String SQL_SELECT_UNO = "SELECT id, nombre FROM titular WHERE id = ?";

	/** Lista todos los titulares ordenados por nombre. */
	private static final String SQL_SELECT_TODOS = "SELECT id, nombre FROM titular ORDER BY nombre";

	/**
	 * Registra un titular nuevo.
	 *
	 * @param t titular a guardar
	 * @return mensaje que empieza por "✔" si se guardó, o el motivo del error
	 * @throws Exception si no se puede obtener la conexión
	 */
	@Override
	public String create(Titular t) throws Exception {
		String error = validarParaGuardar(t);
		if (error != null) {
			return error;
		}
		Connection con = ConexionBD.getInstancia().getConexion();
		try (PreparedStatement ps = con.prepareStatement(SQL_INSERT)) {
			ps.setString(1, t.getId().trim());
			ps.setString(2, t.getNombre().trim());
			ps.executeUpdate();
			return "✔ Titular " + t.getNombre().trim() + " registrado correctamente.";
		} catch (SQLIntegrityConstraintViolationException e) {
			return "⚠ Ya existe un titular con la identificación " + t.getId().trim() + ".";
		}
	}

	/**
	 * Valida que el titular tenga identificación y nombre. No requiere conexión.
	 *
	 * @param t titular a validar
	 * @return {@code null} si es válido; en caso contrario, el mensaje de error
	 */
	public String validarParaGuardar(Titular t) {
		if (t == null) {
			return "⚠ El titular es obligatorio.";
		}
		if (t.getId() == null || t.getId().isBlank()) {
			return "⚠ La identificación del titular es obligatoria.";
		}
		if (t.getNombre() == null || t.getNombre().isBlank()) {
			return "⚠ El nombre del titular es obligatorio.";
		}
		if (t.getId().trim().length() > 50 || t.getNombre().trim().length() > 100) {
			return "⚠ La identificación admite 50 caracteres y el nombre 100.";
		}
		return null;
	}

	/**
	 * Busca un titular por su identificación.
	 *
	 * @param id  identificación ({@link String})
	 * @param <K> tipo de la llave
	 * @return el titular o {@code null} si no existe
	 * @throws Exception si ocurre un error de base de datos
	 */
	@Override
	public <K> Titular readone(K id) throws Exception {
		Connection con = ConexionBD.getInstancia().getConexion();
		try (PreparedStatement ps = con.prepareStatement(SQL_SELECT_UNO)) {
			ps.setString(1, (String) id);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? new Titular(rs.getString("id"), rs.getString("nombre")) : null;
			}
		}
	}

	/**
	 * Lista todos los titulares registrados, ordenados por nombre.
	 *
	 * @return lista de titulares (vacía si no hay registros)
	 * @throws Exception si ocurre un error de base de datos
	 */
	@Override
	public List<Titular> readall() throws Exception {
		Connection con = ConexionBD.getInstancia().getConexion();
		List<Titular> lista = new ArrayList<>();
		try (PreparedStatement ps = con.prepareStatement(SQL_SELECT_TODOS); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(new Titular(rs.getString("id"), rs.getString("nombre")));
			}
		}
		return lista;
	}
}
