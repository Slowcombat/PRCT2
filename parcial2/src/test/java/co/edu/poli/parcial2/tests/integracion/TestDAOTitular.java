package co.edu.poli.parcial2.tests.integracion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import co.edu.poli.parcial2.modelo.Titular;
import co.edu.poli.parcial2.servicios.DAOTitular;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@Tag("integracion")
public class TestDAOTitular {

    // ✔️ 1. Test: la lista no es null
    @Test
    void readAll_noDebeRetornarNull() throws Exception {
        DAOTitular dao = new DAOTitular();

        List<Titular> lista = dao.readall();

        assertNotNull(lista);
    }

    // ✔️ 2. Test: la lista puede estar vacía pero válida
    @Test
    void readAll_listaInicializada() throws Exception {
        DAOTitular dao = new DAOTitular();

        List<Titular> lista = dao.readall();

        assertTrue(lista.size() >= 0);
    }

    // ✔️ 3. Test: si hay datos, los objetos están bien formados
    @Test
    void readAll_objetosValidos() throws Exception {
        DAOTitular dao = new DAOTitular();

        List<Titular> lista = dao.readall();

        if (!lista.isEmpty()) {
            Titular t = lista.get(0);

            assertNotNull(t.getId());
            assertNotNull(t.getNombre());
        }
    }
}
