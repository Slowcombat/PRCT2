package co.edu.poli.parcial2.tests.unitaria;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.poli.parcial2.modelo.Tarjeta.Estado;

/**
 * Pruebas unitarias del enum anidado {@link Estado} de Tarjeta (HU-01).
 * No requieren base de datos.
 *
 * @author Julian Larrarte
 */
public class TestEstado {

	/** Criterio: la tarjeta solo puede estar Activa, Bloqueada o Cancelada. */
	@Test
	void existenExactamenteLosTresEstadosDeLaHU() {
		assertArrayEquals(
				new Estado[] { Estado.ACTIVA, Estado.BLOQUEADA, Estado.CANCELADA },
				Estado.values());
	}

	@Test
	void etiquetas_sonLegiblesParaElFormulario() {
		assertEquals("Activa", Estado.ACTIVA.getEtiqueta());
		assertEquals("Bloqueada", Estado.BLOQUEADA.getEtiqueta());
		assertEquals("Cancelada", Estado.CANCELADA.getEtiqueta());
		assertEquals("Activa", Estado.ACTIVA.toString());
	}

	@Test
	void soloLaTarjetaActivaPermiteUso() {
		assertTrue(Estado.ACTIVA.permiteUso());
		assertFalse(Estado.BLOQUEADA.permiteUso());
		assertFalse(Estado.CANCELADA.permiteUso());
	}

	@Test
	void desdeTexto_convierteValoresDeLaBaseDeDatos() {
		assertEquals(Estado.ACTIVA, Estado.desdeTexto("ACTIVA"));
		assertEquals(Estado.BLOQUEADA, Estado.desdeTexto("bloqueada"));
		assertEquals(Estado.CANCELADA, Estado.desdeTexto("  Cancelada "));
	}

	/** Criterio: no es posible usar un estado inexistente. */
	@ParameterizedTest
	@ValueSource(strings = { "SUSPENDIDA", "1", "0", "ACTIV" })
	void desdeTexto_rechazaEstadosInvalidos(String valor) {
		assertThrows(IllegalArgumentException.class, () -> Estado.desdeTexto(valor));
	}

	/** Criterio: el estado es obligatorio. */
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   " })
	void desdeTexto_rechazaEstadoVacio(String valor) {
		assertThrows(IllegalArgumentException.class, () -> Estado.desdeTexto(valor));
	}
}
