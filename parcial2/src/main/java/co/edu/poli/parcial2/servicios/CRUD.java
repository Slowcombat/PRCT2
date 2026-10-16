package co.edu.poli.parcial2.servicios;

import java.util.List;

/**
 * Operaciones básicas de persistencia que implementan los DAO.
 *
 * @param <T> tipo de entidad que se persiste
 * @author Julian Larrarte
 * @version 1.0
 */
public interface CRUD<T> {

	/**
	 * Guarda una entidad nueva.
	 *
	 * @param t entidad a guardar
	 * @return mensaje con el resultado de la operación
	 * @throws Exception si ocurre un error de acceso a datos
	 */
	String create(T t) throws Exception;

	/**
	 * Busca una entidad por su llave.
	 *
	 * @param id  llave de la entidad
	 * @param <K> tipo de la llave
	 * @return la entidad encontrada o {@code null}
	 * @throws Exception si ocurre un error de acceso a datos
	 */
	<K> T readone(K id) throws Exception;

	/**
	 * Lista todas las entidades.
	 *
	 * @return lista de entidades
	 * @throws Exception si ocurre un error de acceso a datos
	 */
	List<T> readall() throws Exception;
}
