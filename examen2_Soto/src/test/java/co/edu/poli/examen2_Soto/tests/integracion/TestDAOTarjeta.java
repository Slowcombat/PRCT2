package co.edu.poli.examen2_Soto.tests.integracion;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import co.edu.poli.examen2_Soto.modelo.Credito;
import co.edu.poli.examen2_Soto.modelo.Debito;
import co.edu.poli.examen2_Soto.modelo.Tarjeta;
import co.edu.poli.examen2_Soto.modelo.Titular;
import co.edu.poli.examen2_Soto.servicios.DAOTarjeta;

public class TestDAOTarjeta {

    DAOTarjeta dao = new DAOTarjeta();

    @Test
    void create_debito_y_readone() throws Exception {

        Titular titular = new Titular("T001", "Test");

        Debito debito = new Debito(
                "999001",
                "2025-12-25",
                true,
                titular,
                5000.0
        );

        String result = dao.create(debito);

        assertTrue(result.contains("guardada"));

        Tarjeta t = dao.readone("999001");

        assertNotNull(t);
        assertTrue(t instanceof Debito);

        Debito d = (Debito) t;
        assertEquals(5000.0, d.getSaldo());
    }

    @Test
    void create_credito_y_readone() throws Exception {

        Titular titular = new Titular("T001", "Test");

        Credito credito = new Credito(
                "999002",
                "2025-12-25",
                true,
                titular,
                10000.0
        );

        String result = dao.create(credito);

        assertTrue(result.contains("guardada"));

        Tarjeta t = dao.readone("999002");

        assertNotNull(t);
        assertTrue(t instanceof Credito);

        Credito c = (Credito) t;
        assertEquals(10000.0, c.getLimite());
    }

    @Test
    void readone_noExiste() throws Exception {

        Tarjeta t = dao.readone("000000");

        assertNull(t);
    }
}
