package co.edu.poli.examen2_Soto.tests.unitaria;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import co.edu.poli.examen2_Soto.modelo.Debito;
import co.edu.poli.examen2_Soto.modelo.EstadoTarjeta;
import co.edu.poli.examen2_Soto.modelo.Titular;
import co.edu.poli.examen2_Soto.servicios.DAOTarjeta;

/**
 * Pruebas unitarias de las validaciones de {@link DAOTarjeta} que se ejecutan
 * antes de tocar la base de datos (HU-01). No requieren conexión.
 *
 * @author Julian Larrarte
 */
public class TestDAOTarjetaValidacion {

	private final DAOTarjeta dao = new DAOTarjeta();

	@Test
	void validarParaGuardar_tarjetaValida_retornaNull() {
		Debito d = new Debito("1", "25/12/2025", EstadoTarjeta.ACTIVA, new Titular("T1", "Test"), 0);
		assertNull(dao.validarParaGuardar(d));
	}

	@Test
	void validarParaGuardar_tarjetaNula_retornaError() {
		assertNotNull(dao.validarParaGuardar(null));
	}

	@Test
	void validarParaGuardar_sinTitular_retornaError() {
		Debito d = new Debito("1", "25/12/2025", EstadoTarjeta.ACTIVA, null, 0);
		assertTrue(dao.validarParaGuardar(d).contains("titular"));
	}

	/** Criterio: no se puede asignar un estado vacío a una tarjeta existente. */
	@Test
	void actualizarEstado_sinEstado_noConsultaLaBaseYRetornaError() throws Exception {
		String resultado = dao.actualizarEstado("123", null);
		assertTrue(resultado.contains("obligatorio"));
	}
}
