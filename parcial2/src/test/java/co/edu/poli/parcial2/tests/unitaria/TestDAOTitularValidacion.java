package co.edu.poli.parcial2.tests.unitaria;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import co.edu.poli.parcial2.modelo.Titular;
import co.edu.poli.parcial2.servicios.DAOTitular;

/**
 * Pruebas unitarias de las validaciones de {@link DAOTitular}. No requieren
 * conexión a la base de datos.
 *
 * @author Julian Larrarte
 */
public class TestDAOTitularValidacion {

	private final DAOTitular dao = new DAOTitular();

	@Test
	void titularValido_retornaNull() {
		assertNull(dao.validarParaGuardar(new Titular("1023456789", "Ana Gómez")));
	}

	@Test
	void titularNulo_retornaError() {
		assertNotNull(dao.validarParaGuardar(null));
	}

	@Test
	void sinIdentificacion_retornaError() {
		assertTrue(dao.validarParaGuardar(new Titular("  ", "Ana Gómez")).contains("identificación"));
	}

	@Test
	void sinNombre_retornaError() {
		assertTrue(dao.validarParaGuardar(new Titular("123", "")).contains("nombre"));
	}

	@Test
	void textosDemasiadoLargos_retornaError() {
		assertNotNull(dao.validarParaGuardar(new Titular("1".repeat(51), "Ana")));
		assertNotNull(dao.validarParaGuardar(new Titular("1", "a".repeat(101))));
	}

	@Test
	void toString_muestraNombreEIdentificacion() {
		assertEquals("Ana Gómez (123)", new Titular("123", "Ana Gómez").toString());
	}
}
