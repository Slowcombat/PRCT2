package co.edu.poli.examen2_Soto.tests.unitaria;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.poli.examen2_Soto.modelo.EstadoTarjeta;

/**
 * Pruebas unitarias del enumerado {@link EstadoTarjeta} (HU-01).
 * No requieren base de datos.
 *
 * @author Julian Larrarte
 */
public class TestEstadoTarjeta {

	/** Criterio: la tarjeta solo puede estar Activa, Bloqueada o Cancelada. */
	@Test
	void existenExactamenteLosTresEstadosDeLaHU() {
		assertArrayEquals(
				new EstadoTarjeta[] { EstadoTarjeta.ACTIVA, EstadoTarjeta.BLOQUEADA, EstadoTarjeta.CANCELADA },
				EstadoTarjeta.values());
	}

	@Test
	void etiquetas_sonLegiblesParaElFormulario() {
		assertEquals("Activa", EstadoTarjeta.ACTIVA.getEtiqueta());
		assertEquals("Bloqueada", EstadoTarjeta.BLOQUEADA.getEtiqueta());
		assertEquals("Cancelada", EstadoTarjeta.CANCELADA.getEtiqueta());
		assertEquals("Activa", EstadoTarjeta.ACTIVA.toString());
	}

	@Test
	void soloLaTarjetaActivaPermiteUso() {
		assertTrue(EstadoTarjeta.ACTIVA.permiteUso());
		assertFalse(EstadoTarjeta.BLOQUEADA.permiteUso());
		assertFalse(EstadoTarjeta.CANCELADA.permiteUso());
	}

	@Test
	void desdeTexto_convierteValoresDeLaBaseDeDatos() {
		assertEquals(EstadoTarjeta.ACTIVA, EstadoTarjeta.desdeTexto("ACTIVA"));
		assertEquals(EstadoTarjeta.BLOQUEADA, EstadoTarjeta.desdeTexto("bloqueada"));
		assertEquals(EstadoTarjeta.CANCELADA, EstadoTarjeta.desdeTexto("  Cancelada "));
	}

	/** Criterio: no es posible usar un estado inexistente. */
	@ParameterizedTest
	@ValueSource(strings = { "SUSPENDIDA", "1", "0", "ACTIV" })
	void desdeTexto_rechazaEstadosInvalidos(String valor) {
		assertThrows(IllegalArgumentException.class, () -> EstadoTarjeta.desdeTexto(valor));
	}

	/** Criterio: el estado es obligatorio. */
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   " })
	void desdeTexto_rechazaEstadoVacio(String valor) {
		assertThrows(IllegalArgumentException.class, () -> EstadoTarjeta.desdeTexto(valor));
	}
}
