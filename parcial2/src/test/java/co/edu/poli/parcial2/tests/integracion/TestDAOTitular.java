package co.edu.poli.parcial2.tests.integracion;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import co.edu.poli.parcial2.modelo.Titular;
import co.edu.poli.parcial2.servicios.ConexionBD;
import co.edu.poli.parcial2.servicios.DAOTitular;

/**
 * Pruebas de integración de {@link DAOTitular} contra MySQL.
 * Se ejecutan con {@code mvn test -Pintegracion}.
 *
 * @author Julian Larrarte
 */
@Tag("integracion")
public class TestDAOTitular {

	private static final String ID_PRUEBA = "TEST-999";

	private final DAOTitular dao = new DAOTitular();

	@BeforeEach
	@AfterEach
	void limpiar() throws Exception {
		Connection con = ConexionBD.getInstancia().getConexion();
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM titular WHERE id = ?")) {
			ps.setString(1, ID_PRUEBA);
			ps.executeUpdate();
		}
	}

	@Test
	void readAll_retornaLosTitularesDelScript() throws Exception {
		List<Titular> lista = dao.readall();

		assertNotNull(lista);
		assertTrue(lista.stream().anyMatch(t -> t.getId().equals("T001")));
		lista.forEach(t -> {
			assertNotNull(t.getId());
			assertNotNull(t.getNombre());
		});
	}

	@Test
	void create_guardaYSePuedeLeer() throws Exception {
		String resultado = dao.create(new Titular(ID_PRUEBA, "Titular de Prueba"));

		assertTrue(resultado.startsWith("✔"), resultado);
		Titular leido = dao.readone(ID_PRUEBA);
		assertNotNull(leido);
		assertEquals("Titular de Prueba", leido.getNombre());
		assertTrue(dao.readall().stream().anyMatch(t -> t.getId().equals(ID_PRUEBA)));
	}

	@Test
	void create_idRepetido_informaError() throws Exception {
		dao.create(new Titular(ID_PRUEBA, "Primero"));

		String resultado = dao.create(new Titular(ID_PRUEBA, "Segundo"));

		assertTrue(resultado.contains("Ya existe"), resultado);
		assertEquals("Primero", dao.readone(ID_PRUEBA).getNombre());
	}

	@Test
	void readone_noExiste() throws Exception {
		assertNull(dao.readone("NO-EXISTE"));
	}
}
