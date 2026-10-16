package co.edu.poli.parcial2.tests.integracion;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import co.edu.poli.parcial2.modelo.Credito;
import co.edu.poli.parcial2.modelo.Debito;
import co.edu.poli.parcial2.modelo.Tarjeta.Estado;
import co.edu.poli.parcial2.modelo.Tarjeta;
import co.edu.poli.parcial2.modelo.Titular;
import co.edu.poli.parcial2.servicios.ConexionBD;
import co.edu.poli.parcial2.servicios.DAOTarjeta;

/**
 * Pruebas de integración de {@link DAOTarjeta} contra MySQL (HU-01).
 * Requieren la base {@code examen2_soto} creada con {@code ScriptDB.sql} y el
 * archivo {@code .env}. Se ejecutan con {@code mvn test -Pintegracion}.
 * <p>
 * Cada prueba borra antes y después las tarjetas de prueba (999xxx), así que
 * pueden ejecutarse varias veces seguidas.
 * </p>
 *
 * @author Julian Larrarte
 */
@Tag("integracion")
public class TestDAOTarjeta {

	private static final String[] NUMEROS_PRUEBA = { "999001", "999002", "999003", "999004", "999005" };

	private final DAOTarjeta dao = new DAOTarjeta();
	private final Titular titular = new Titular("T001", "Juan Soto");

	@BeforeEach
	@AfterEach
	void limpiar() throws Exception {
		Connection con = ConexionBD.getInstancia().getConexion();
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM tarjeta WHERE numero = ?")) {
			for (String n : NUMEROS_PRUEBA) {
				ps.setString(1, n);
				ps.executeUpdate();
			}
		}
	}

	/** Criterio: el estado se almacena de forma persistente. */
	@Test
	void create_debito_persisteEstado() throws Exception {
		Debito debito = new Debito("999001", "25/12/2025", Estado.BLOQUEADA, titular, 5000.0);

		String result = dao.create(debito);
		assertTrue(result.contains("guardada"), result);

		Tarjeta t = dao.readone("999001");
		assertNotNull(t);
		assertTrue(t instanceof Debito);
		assertEquals(Estado.BLOQUEADA, t.getEstado());
		assertEquals(5000.0, ((Debito) t).getSaldo());
	}

	@Test
	void create_credito_persisteEstado() throws Exception {
		Credito credito = new Credito("999002", "25/12/2025", Estado.CANCELADA, titular, 10000.0);

		String result = dao.create(credito);
		assertTrue(result.contains("guardada"), result);

		Tarjeta t = dao.readone("999002");
		assertNotNull(t);
		assertTrue(t instanceof Credito);
		assertEquals(Estado.CANCELADA, t.getEstado());
		assertEquals(10000.0, ((Credito) t).getLimite());
	}

	/** HU-01: asignar un estado a una tarjeta existente. */
	@Test
	void actualizarEstado_persisteElNuevoEstado() throws Exception {
		dao.create(new Debito("999003", "25/12/2025", Estado.ACTIVA, titular, 0));

		String result = dao.actualizarEstado("999003", Estado.BLOQUEADA);

		assertTrue(result.startsWith("✔"), result);
		assertEquals(Estado.BLOQUEADA, dao.readone("999003").getEstado());
	}

	@Test
	void actualizarEstado_tarjetaInexistente_informaError() throws Exception {
		String result = dao.actualizarEstado("000000", Estado.ACTIVA);
		assertTrue(result.contains("No existe"));
	}

	/** Criterio: la base de datos no acepta una tarjeta sin estado. */
	@Test
	void baseDeDatos_rechazaTarjetaSinEstado() throws Exception {
		Connection con = ConexionBD.getInstancia().getConexion();
		try (PreparedStatement ps = con.prepareStatement(
				"INSERT INTO tarjeta (numero, fecha_exp, titular_id) VALUES ('999004', '25/12/2025', 'T001')")) {
			assertThrows(SQLException.class, ps::executeUpdate);
		}
	}

	/** Criterio: la base de datos no acepta un estado inválido. */
	@Test
	void baseDeDatos_rechazaEstadoInvalido() throws Exception {
		Connection con = ConexionBD.getInstancia().getConexion();
		try (PreparedStatement ps = con.prepareStatement("INSERT INTO tarjeta (numero, fecha_exp, estado, titular_id) "
				+ "VALUES ('999005', '25/12/2025', 'SUSPENDIDA', 'T001')")) {
			assertThrows(SQLException.class, ps::executeUpdate);
		}
	}

	/** Criterio: el estado se visualiza en el listado de todas las tarjetas. */
	@Test
	void readall_incluyeTarjetasConSuEstadoYTipo() throws Exception {
		dao.create(new Debito("999001", "25/12/2025", Estado.BLOQUEADA, titular, 700.0));
		dao.create(new Credito("999002", "25/12/2025", Estado.ACTIVA, titular, 9000.0));

		java.util.List<Tarjeta> todas = dao.readall();

		Tarjeta d = todas.stream().filter(t -> t.getNumero().equals("999001")).findFirst().orElse(null);
		Tarjeta c = todas.stream().filter(t -> t.getNumero().equals("999002")).findFirst().orElse(null);
		assertNotNull(d);
		assertNotNull(c);
		assertTrue(d instanceof Debito);
		assertTrue(c instanceof Credito);
		assertEquals(Estado.BLOQUEADA, d.getEstado());
		assertEquals(Estado.ACTIVA, c.getEstado());
		assertEquals(700.0, ((Debito) d).getSaldo());
		assertEquals("T001", c.getTitular().getId());
	}

	@Test
	void readone_noExiste() throws Exception {
		assertNull(dao.readone("000000"));
	}
}
