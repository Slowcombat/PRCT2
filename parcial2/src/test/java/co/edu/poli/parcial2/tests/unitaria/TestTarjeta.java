package co.edu.poli.parcial2.tests.unitaria;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.poli.parcial2.modelo.Credito;
import co.edu.poli.parcial2.modelo.Debito;
import co.edu.poli.parcial2.modelo.Tarjeta.Estado;
import co.edu.poli.parcial2.modelo.Tarjeta;
import co.edu.poli.parcial2.modelo.Titular;

/**
 * Pruebas unitarias de {@link Tarjeta} y el manejo de su estado (HU-01).
 * No requieren base de datos.
 *
 * @author Julian Larrarte
 */
public class TestTarjeta {

	private Titular titular;

	@BeforeEach
	void setUp() {
		titular = new Titular("1", "Test");
	}

	/** Criterio: el estado es obligatorio al crear una tarjeta. */
	@Test
	void constructor_sinEstado_lanzaExcepcion() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new Debito("123", "25/12/2025", null, titular, 1000));
		assertTrue(ex.getMessage().contains("obligatorio"));

		assertThrows(IllegalArgumentException.class, () -> new Credito("124", "25/12/2025", null, titular, 1000));
	}

	@Test
	void constructor_guardaElEstadoRecibido() {
		for (Estado estado : Estado.values()) {
			Tarjeta t = new Credito("123", "25/12/2025", estado, titular, 5000);
			assertEquals(estado, t.getEstado());
		}
	}

	@Test
	void setEstado_null_lanzaExcepcionYConservaElEstadoAnterior() {
		Tarjeta t = new Debito("123", "25/12/2025", Estado.ACTIVA, titular, 1000);

		assertThrows(IllegalArgumentException.class, () -> t.setEstado(null));
		assertEquals(Estado.ACTIVA, t.getEstado());
	}

	@Test
	void bloquear_cambiaEstadoABloqueada() {
		Tarjeta t = new Debito("123", "25/12/2025", Estado.ACTIVA, titular, 1000);

		String mensaje = t.bloquear();

		assertEquals(Estado.BLOQUEADA, t.getEstado());
		assertFalse(t.puedeUsarse());
		assertTrue(mensaje.contains("BLOQUEADA"));
	}

	@Test
	void activar_cambiaEstadoAActiva() {
		Tarjeta t = new Debito("123", "25/12/2025", Estado.BLOQUEADA, titular, 1000);

		String mensaje = t.activar();

		assertEquals(Estado.ACTIVA, t.getEstado());
		assertTrue(t.puedeUsarse());
		assertTrue(mensaje.contains("ACTIVADA"));
	}

	@Test
	void cancelar_cambiaEstadoACancelada() {
		Tarjeta t = new Credito("123", "25/12/2025", Estado.ACTIVA, titular, 1000);

		String mensaje = t.cancelar();

		assertEquals(Estado.CANCELADA, t.getEstado());
		assertFalse(t.puedeUsarse());
		assertTrue(mensaje.contains("CANCELADA"));
	}

	@Test
	void tarjetaCancelada_noPuedeActivarseNiBloquearse() {
		Tarjeta t = new Credito("123", "25/12/2025", Estado.CANCELADA, titular, 1000);

		assertThrows(IllegalStateException.class, t::activar);
		assertThrows(IllegalStateException.class, t::bloquear);
		assertEquals(Estado.CANCELADA, t.getEstado());
	}

	@Test
	void getters_retornanValoresCorrectos() {
		Tarjeta t = new Debito("123", "25/12/2025", Estado.ACTIVA, titular, 1000);

		assertEquals("123", t.getNumero());
		assertEquals("25/12/2025", t.getFechaExp());
		assertEquals(Estado.ACTIVA, t.getEstado());
		assertEquals(titular, t.getTitular());
	}

	@Test
	void setters_modificanValores() {
		Titular nuevo = new Titular("2", "Nuevo");
		Tarjeta t = new Debito("123", "25/12/2025", Estado.ACTIVA, titular, 1000);

		t.setNumero("999");
		t.setFechaExp("01/01/2030");
		t.setEstado(Estado.BLOQUEADA);
		t.setTitular(nuevo);

		assertEquals("999", t.getNumero());
		assertEquals("01/01/2030", t.getFechaExp());
		assertEquals(Estado.BLOQUEADA, t.getEstado());
		assertEquals(nuevo, t.getTitular());
	}

	/** Criterio: el estado se visualiza (el formulario muestra toString()). */
	@Test
	void toString_contieneDatosYEstado() {
		Tarjeta t = new Debito("123", "25/12/2025", Estado.BLOQUEADA, titular, 1000);

		String texto = t.toString();

		assertTrue(texto.contains("123"));
		assertTrue(texto.contains("25/12/2025"));
		assertTrue(texto.contains("estado=Bloqueada"));
	}
}
