package co.edu.poli.parcial2.servicios;

import co.edu.poli.parcial2.modelo.Credito;
import co.edu.poli.parcial2.modelo.Debito;
import co.edu.poli.parcial2.modelo.Tarjeta.Estado;
import co.edu.poli.parcial2.modelo.Tarjeta;
import co.edu.poli.parcial2.modelo.Titular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para {@link Tarjeta} y sus subclases.
 * <p>
 * Implementa la persistencia con herencia JOINED: cada tarjeta ocupa una fila en
 * {@code tarjeta} y otra en {@code tarjeta_debito} o {@code tarjeta_credito}.
 * </p>
 * <p>
 * <b>HU-01:</b> el estado se guarda en la columna {@code tarjeta.estado} como el
 * nombre de la constante de {@link Estado} ({@code ACTIVA},
 * {@code BLOQUEADA} o {@code CANCELADA}).
 * </p>
 *
 * @author Julian Larrarte
 * @version 2.0
 */
public class DAOTarjeta implements CRUD<Tarjeta> {

	/** Inserta los datos comunes de la tarjeta. */
	private static final String SQL_INSERT_TARJETA = "INSERT INTO tarjeta (numero, fecha_exp, estado, titular_id) "
			+ "VALUES (?, ?, ?, ?)";

	/** Inserta los datos propios de una tarjeta débito. */
	private static final String SQL_INSERT_DEBITO = "INSERT INTO tarjeta_debito (numero, saldo) VALUES (?, ?)";

	/** Inserta los datos propios de una tarjeta crédito. */
	private static final String SQL_INSERT_CREDITO = "INSERT INTO tarjeta_credito (numero, limite) VALUES (?, ?)";

	/** Consulta una tarjeta débito con su titular. */
	private static final String SQL_SELECT_DEBITO = "SELECT t.numero, t.fecha_exp, t.estado, "
			+ "ti.id AS titular_id, ti.nombre AS titular_nombre, d.saldo " + "FROM tarjeta_debito d "
			+ "INNER JOIN tarjeta t ON d.numero = t.numero " + "INNER JOIN titular ti ON t.titular_id = ti.id "
			+ "WHERE d.numero = ?";

	/** Consulta una tarjeta crédito con su titular. */
	private static final String SQL_SELECT_CREDITO = "SELECT t.numero, t.fecha_exp, t.estado, "
			+ "ti.id AS titular_id, ti.nombre AS titular_nombre, c.limite " + "FROM tarjeta_credito c "
			+ "INNER JOIN tarjeta t ON c.numero = t.numero " + "INNER JOIN titular ti ON t.titular_id = ti.id "
			+ "WHERE c.numero = ?";

	/** Lista todas las tarjetas con su titular y su saldo o límite. */
	private static final String SQL_SELECT_TODAS = "SELECT t.numero, t.fecha_exp, t.estado, "
			+ "ti.id AS titular_id, ti.nombre AS titular_nombre, d.saldo, c.limite, "
			+ "CASE WHEN d.numero IS NOT NULL THEN 'D' ELSE 'C' END AS tipo " + "FROM tarjeta t "
			+ "INNER JOIN titular ti ON t.titular_id = ti.id " + "LEFT JOIN tarjeta_debito d ON d.numero = t.numero "
			+ "LEFT JOIN tarjeta_credito c ON c.numero = t.numero " + "ORDER BY ti.nombre, t.numero";

	/** Actualiza únicamente el estado de una tarjeta. */
	private static final String SQL_UPDATE_ESTADO = "UPDATE tarjeta SET estado = ? WHERE numero = ?";

	/**
	 * Guarda una tarjeta nueva dentro de una transacción.
	 * <p>
	 * Si la tarjeta no tiene un estado válido no se ejecuta ningún SQL y se
	 * devuelve un mensaje de error (criterio de aceptación de la HU-01).
	 * </p>
	 *
	 * @param t tarjeta a guardar ({@link Debito} o {@link Credito})
	 * @return mensaje que empieza por "✔" si se guardó, o el mensaje de error
	 * @throws Exception si no se puede obtener la conexión
	 */
	@Override
	public String create(Tarjeta t) throws Exception {

		String error = validarParaGuardar(t);
		if (error != null) {
			return error;
		}

		Connection con = ConexionBD.getInstancia().getConexion();
		con.setAutoCommit(false);

		try (PreparedStatement psTarjeta = con.prepareStatement(SQL_INSERT_TARJETA)) {
			psTarjeta.setString(1, t.getNumero());
			psTarjeta.setString(2, t.getFechaExp());
			psTarjeta.setString(3, t.getEstado().name());
			psTarjeta.setString(4, t.getTitular().getId());
			psTarjeta.executeUpdate();

			String sql = (t instanceof Debito) ? SQL_INSERT_DEBITO : SQL_INSERT_CREDITO;
			try (PreparedStatement psHija = con.prepareStatement(sql)) {
				psHija.setString(1, t.getNumero());
				if (t instanceof Debito)
					psHija.setDouble(2, ((Debito) t).getSaldo());
				else
					psHija.setDouble(2, ((Credito) t).getLimite());
				psHija.executeUpdate();
			}

			con.commit();
			return "✔ Tarjeta " + t.getTipo().toLowerCase() + " " + t.getNumero() + " guardada con estado "
					+ t.getEstado().getEtiqueta() + ".";
		} catch (Exception e) {
			con.rollback();
			return e.getMessage();
		} finally {
			con.setAutoCommit(true);
		}
	}

	/**
	 * Valida los datos mínimos de una tarjeta antes de guardarla.
	 * No requiere conexión a la base de datos.
	 *
	 * @param t tarjeta a validar
	 * @return {@code null} si es válida; en caso contrario, el mensaje de error
	 */
	public String validarParaGuardar(Tarjeta t) {
		if (t == null) {
			return "⚠ La tarjeta es obligatoria.";
		}
		if (t.getEstado() == null) {
			return "⚠ El estado de la tarjeta es obligatorio.";
		}
		if (t.getTitular() == null) {
			return "⚠ El titular de la tarjeta es obligatorio.";
		}
		return null;
	}

	/**
	 * Busca una tarjeta por su número, primero como débito y luego como crédito.
	 *
	 * @param num número de la tarjeta ({@link String})
	 * @param <K> tipo de la llave
	 * @return la tarjeta encontrada, con su estado, o {@code null} si no existe
	 * @throws Exception si ocurre un error de base de datos
	 */
	@Override
	public <K> Tarjeta readone(K num) throws Exception {

		Connection con = ConexionBD.getInstancia().getConexion();

		try (PreparedStatement ps = con.prepareStatement(SQL_SELECT_DEBITO)) {
			ps.setString(1, (String) num);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new Debito(rs.getString("numero"), rs.getString("fecha_exp"),
							Estado.desdeTexto(rs.getString("estado")), leerTitular(rs), rs.getDouble("saldo"));
				}
			}
		}

		try (PreparedStatement ps = con.prepareStatement(SQL_SELECT_CREDITO)) {
			ps.setString(1, (String) num);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new Credito(rs.getString("numero"), rs.getString("fecha_exp"),
							Estado.desdeTexto(rs.getString("estado")), leerTitular(rs), rs.getDouble("limite"));
				}
			}
		}

		return null;
	}

	/**
	 * Cambia el estado de una tarjeta existente (asignación de estado, HU-01).
	 *
	 * @param numero      número de la tarjeta
	 * @param nuevoEstado estado a asignar (obligatorio)
	 * @return mensaje de resultado; empieza por "✔" si se actualizó
	 * @throws Exception si ocurre un error de base de datos
	 */
	public String actualizarEstado(String numero, Estado nuevoEstado) throws Exception {
		if (nuevoEstado == null) {
			return "⚠ El estado de la tarjeta es obligatorio.";
		}
		Connection con = ConexionBD.getInstancia().getConexion();
		try (PreparedStatement ps = con.prepareStatement(SQL_UPDATE_ESTADO)) {
			ps.setString(1, nuevoEstado.name());
			ps.setString(2, numero);
			int filas = ps.executeUpdate();
			if (filas == 0) {
				return "No existe la tarjeta " + numero + ".";
			}
			return "✔ Tarjeta [" + numero + "] ahora está " + nuevoEstado.getEtiqueta() + ".";
		}
	}

	/**
	 * Construye el {@link Titular} a partir de la fila actual del resultado.
	 *
	 * @param rs resultado posicionado en una fila
	 * @return titular de la tarjeta
	 * @throws Exception si las columnas no existen
	 */
	private Titular leerTitular(ResultSet rs) throws Exception {
		return new Titular(rs.getString("titular_id"), rs.getString("titular_nombre"));
	}

	/**
	 * Lista todas las tarjetas registradas con su titular, estado y saldo o límite.
	 *
	 * @return lista de tarjetas ({@link Debito} o {@link Credito}); vacía si no hay
	 * @throws Exception si ocurre un error de base de datos
	 */
	@Override
	public List<Tarjeta> readall() throws Exception {
		Connection con = ConexionBD.getInstancia().getConexion();
		List<Tarjeta> lista = new ArrayList<>();
		try (PreparedStatement ps = con.prepareStatement(SQL_SELECT_TODAS); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Estado estado = Estado.desdeTexto(rs.getString("estado"));
				if ("D".equals(rs.getString("tipo"))) {
					lista.add(new Debito(rs.getString("numero"), rs.getString("fecha_exp"), estado, leerTitular(rs),
							rs.getDouble("saldo")));
				} else {
					lista.add(new Credito(rs.getString("numero"), rs.getString("fecha_exp"), estado, leerTitular(rs),
							rs.getDouble("limite")));
				}
			}
		}
		return lista;
	}
}
