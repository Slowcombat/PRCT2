package co.edu.poli.examen2_Soto.tests.unitaria;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import co.edu.poli.examen2_Soto.modelo.Debito;
import co.edu.poli.examen2_Soto.modelo.Tarjeta;
import co.edu.poli.examen2_Soto.modelo.Titular;

public class TestTarjeta {

	@Test
	void bloquear_cambiaEstadoAFalso() {
		Titular titular = new Titular("1", "Test");

		Tarjeta t = new Debito("123", "2025-12-25", true, titular, 1000);

		String mensaje = t.bloquear();

		assertFalse(t.isEstado());
		assertTrue(mensaje.contains("BLOQUEADA"));
	}

	@Test
	void activar_cambiaEstadoAVerdadero() {
		Titular titular = new Titular("1", "Test");

		Tarjeta t = new Debito("123", "2025-12-25", false, titular, 1000);

		String mensaje = t.activar();

		assertTrue(t.isEstado());
		assertTrue(mensaje.contains("ACTIVADA"));
	}

	@Test
	void getters_retornaValoresCorrectos() {
		Titular titular = new Titular("1", "Test");

		Tarjeta t = new Debito("123", "2025-12-25", true, titular, 1000);

		assertEquals("123", t.getNumero());
		assertEquals("2025-12-25", t.getFechaExp());
		assertTrue(t.isEstado());
		assertEquals(titular, t.getTitular());
	}

	@Test
	void setters_modificanValores() {
		Titular titular = new Titular("1", "Test");
		Titular nuevo = new Titular("2", "Nuevo");

		Tarjeta t = new Debito("123", "2025-12-25", true, titular, 1000);

		t.setNumero("999");
		t.setFechaExp("2030-01-01");
		t.setEstado(false);
		t.setTitular(nuevo);

		assertEquals("999", t.getNumero());
		assertEquals("2030-01-01", t.getFechaExp());
		assertFalse(t.isEstado());
		assertEquals(nuevo, t.getTitular());
	}

	@Test
	void toString_contieneDatos() {
		Titular titular = new Titular("1", "Test");

		Tarjeta t = new Debito("123", "2025-12-25", true, titular, 1000);

		String texto = t.toString();

		assertTrue(texto.contains("123"));
		assertTrue(texto.contains("2025-12-25"));
	}
}
