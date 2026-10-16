package co.edu.poli.examen2_Soto.modelo;

/**
 * Estados posibles de una {@link Tarjeta} (HU-01).
 * <p>
 * El estado determina si la tarjeta puede ser utilizada:
 * </p>
 * <ul>
 *   <li>{@link #ACTIVA}: la tarjeta puede usarse con normalidad.</li>
 *   <li>{@link #BLOQUEADA}: uso suspendido temporalmente; puede volver a activarse.</li>
 *   <li>{@link #CANCELADA}: la tarjeta se dio de baja de forma definitiva y no puede reactivarse.</li>
 * </ul>
 * <p>
 * El nombre de cada constante ({@link #name()}) es el valor que se guarda en la
 * columna {@code tarjeta.estado}, que es una llave foránea obligatoria hacia la
 * tabla catálogo {@code estado_tarjeta}.
 * </p>
 *
 * @author Julian Larrarte
 * @version 1.0
 * @see Tarjeta
 */
public enum EstadoTarjeta {

	/** Tarjeta habilitada para su uso. */
	ACTIVA("Activa"),

	/** Tarjeta bloqueada temporalmente; no puede usarse hasta ser activada de nuevo. */
	BLOQUEADA("Bloqueada"),

	/** Tarjeta cancelada de forma definitiva; no puede usarse ni reactivarse. */
	CANCELADA("Cancelada");

	/** Texto legible que se muestra en la interfaz gráfica. */
	private final String etiqueta;

	/**
	 * Crea una constante del enumerado con su etiqueta visible.
	 *
	 * @param etiqueta texto que se muestra al usuario
	 */
	EstadoTarjeta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	/**
	 * Devuelve el texto legible del estado (por ejemplo, "Activa").
	 *
	 * @return etiqueta del estado
	 */
	public String getEtiqueta() {
		return etiqueta;
	}

	/**
	 * Indica si una tarjeta en este estado puede ser utilizada.
	 *
	 * @return {@code true} solo para {@link #ACTIVA}
	 */
	public boolean permiteUso() {
		return this == ACTIVA;
	}

	/**
	 * Convierte el valor almacenado en la base de datos en un {@code EstadoTarjeta}.
	 * La comparación ignora mayúsculas/minúsculas y espacios en los extremos.
	 *
	 * @param valor texto leído de la columna {@code estado}
	 * @return el estado correspondiente
	 * @throws IllegalArgumentException si el valor es nulo, vacío o no corresponde a un estado válido
	 */
	public static EstadoTarjeta desdeTexto(String valor) {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El estado de la tarjeta es obligatorio.");
		}
		String normalizado = valor.trim().toUpperCase();
		for (EstadoTarjeta e : values()) {
			if (e.name().equals(normalizado)) {
				return e;
			}
		}
		throw new IllegalArgumentException("Estado de tarjeta no válido: " + valor);
	}

	/**
	 * Devuelve la etiqueta legible; así se muestra el estado en los {@code ComboBox}.
	 *
	 * @return etiqueta del estado
	 */
	@Override
	public String toString() {
		return etiqueta;
	}
}
