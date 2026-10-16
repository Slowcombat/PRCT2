package co.edu.poli.examen2_Soto.tests.integracion;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import co.edu.poli.examen2_Soto.modelo.Credito;
import co.edu.poli.examen2_Soto.modelo.Debito;
import co.edu.poli.examen2_Soto.modelo.EstadoTarjeta;
import co.edu.poli.examen2_Soto.modelo.Tarjeta;
import co.edu.poli.examen2_Soto.modelo.Titular;
import co.edu.poli.examen2_Soto.servicios.ConexionBD;
import co.edu.poli.examen2_Soto.servicios.DAOTarjeta;

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
		Debito debito = new Debito("999001", "25/12/2025", EstadoTarjeta.BLOQUEADA, titular, 5000.0);

		String result = dao.create(debito);
		assertTrue(result.contains("guardada"), result);

		Tarjeta t = dao.readone("999001");
		assertNotNull(t);
		assertTrue(t instanceof Debito);
		assertEquals(EstadoTarjeta.BLOQUEADA, t.getEstado());
		assertEquals(5000.0, ((Debito) t).getSaldo());
	}

	@Test
	void create_credito_persisteEstado() throws Exception {
		Credito credito = new Credito("999002", "25/12/2025", EstadoTarjeta.CANCELADA, titular, 10000.0);

		String result = dao.create(credito);
		assertTrue(result.contains("guardada"), result);

		Tarjeta t = dao.readone("999002");
		assertNotNull(t);
		assertTrue(t instanceof Credito);
		assertEquals(EstadoTarjeta.CANCELADA, t.getEstado());
		assertEquals(10000.0, ((Credito) t).getLimite());
	}

	/** HU-01: asignar un estado a una tarjeta existente. */
	@Test
	void actualizarEstado_persisteElNuevoEstado() throws Exception {
		dao.create(new Debito("999003", "25/12/2025", EstadoTarjeta.ACTIVA, titular, 0));

		String result = dao.actualizarEstado("999003", EstadoTarjeta.BLOQUEADA);

		assertTrue(result.startsWith("✔"), result);
		assertEquals(EstadoTarjeta.BLOQUEADA, dao.readone("999003").getEstado());
	}

	@Test
	void actualizarEstado_tarjetaInexistente_informaError() throws Exception {
		String result = dao.actualizarEstado("000000", EstadoTarjeta.ACTIVA);
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

	@Test
	void readone_noExiste() throws Exception {
		assertNull(dao.readone("000000"));
	}
}
