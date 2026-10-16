package co.edu.poli.parcial2.modelo;

/**
 * Persona dueña de una o varias tarjetas. Se persiste en la tabla {@code titular}.
 *
 * @author Julian Larrarte
 * @version 1.0
 */
public class Titular {

	/** Identificador del titular (llave primaria). */
	private String id;

	/** Nombre completo del titular. */
	private String nombre;

	/**
	 * Crea un titular.
	 *
	 * @param id     identificador del titular
	 * @param nombre nombre completo
	 */
	public Titular(String id, String nombre) {
		this.id = id;
		this.nombre = nombre;
	}

	/**
	 * @return identificador del titular
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id nuevo identificador
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return nombre del titular
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * @param nombre nuevo nombre
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/**
	 * @return texto {@code "id, nombre"}, usado en el ComboBox de titulares
	 */
	@Override
	public String toString() {
		return id + ", " + nombre;
	}
}
