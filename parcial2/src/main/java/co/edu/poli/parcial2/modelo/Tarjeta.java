package co.edu.poli.parcial2.modelo;

/**
 * Tarjeta bancaria asociada a un {@link Titular}.
 * <p>
 * Clase abstracta base de la jerarquía {@link Debito} / {@link Credito}. En la base
 * de datos se mapea con estrategia JOINED: los campos comunes viven en la tabla
 * {@code tarjeta} y los propios de cada subclase en {@code tarjeta_debito} y
 * {@code tarjeta_credito}.
 * </p>
 * <p>
 * <b>HU-01:</b> toda tarjeta tiene un {@link Estado} obligatorio
 * (Activa, Bloqueada o Cancelada). No es posible construir una tarjeta ni
 * asignarle un estado nulo, de modo que nunca se llega a guardar una tarjeta
 * sin un estado válido.
 * </p>
 *
 * @author Julian Larrarte
 * @version 2.0
 * @see Tarjeta.Estado
 */
public abstract class Tarjeta {

	/**
	 * Estados posibles de una tarjeta (HU-01). Se define como enum anidado
	 * porque el estado solo tiene sentido dentro de una {@code Tarjeta}.
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
	 */
	public enum Estado {

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
		Estado(String etiqueta) {
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
		 * Convierte el valor almacenado en la base de datos en un {@code Estado}.
		 * La comparación ignora mayúsculas/minúsculas y espacios en los extremos.
		 *
		 * @param valor texto leído de la columna {@code estado}
		 * @return el estado correspondiente
		 * @throws IllegalArgumentException si el valor es nulo, vacío o no corresponde a un estado válido
		 */
		public static Estado desdeTexto(String valor) {
			if (valor == null || valor.isBlank()) {
				throw new IllegalArgumentException("El estado de la tarjeta es obligatorio.");
			}
			String normalizado = valor.trim().toUpperCase();
			for (Estado e : values()) {
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

	/** Número de la tarjeta; es la llave primaria en la base de datos. */
	private String numero;

	/** Fecha de expedición en formato {@code dd/MM/yyyy}. */
	private String fechaExp;

	/** Estado actual de la tarjeta; nunca es {@code null}. */
	private Estado estado;

	/** Titular dueño de la tarjeta. */
	private Titular titular;

	/**
	 * Crea una tarjeta con todos sus datos comunes.
	 *
	 * @param numero   número de la tarjeta
	 * @param fechaExp fecha de expedición
	 * @param estado   estado inicial de la tarjeta (obligatorio)
	 * @param titular  titular de la tarjeta
	 * @throws IllegalArgumentException si {@code estado} es {@code null}
	 */
	public Tarjeta(String numero, String fechaExp, Estado estado, Titular titular) {
		this.numero = numero;
		this.fechaExp = fechaExp;
		setEstado(estado);
		this.titular = titular;
	}

	/**
	 * @return número de la tarjeta
	 */
	public String getNumero() {
		return numero;
	}

	/**
	 * @param numero nuevo número de la tarjeta
	 */
	public void setNumero(String numero) {
		this.numero = numero;
	}

	/**
	 * @return fecha de expedición
	 */
	public String getFechaExp() {
		return fechaExp;
	}

	/**
	 * @param fechaExp nueva fecha de expedición
	 */
	public void setFechaExp(String fechaExp) {
		this.fechaExp = fechaExp;
	}

	/**
	 * Devuelve el estado actual de la tarjeta.
	 *
	 * @return estado de la tarjeta (nunca {@code null})
	 */
	public Estado getEstado() {
		return estado;
	}

	/**
	 * Asigna el estado de la tarjeta.
	 *
	 * @param estado nuevo estado (obligatorio)
	 * @throws IllegalArgumentException si {@code estado} es {@code null}
	 */
	public void setEstado(Estado estado) {
		if (estado == null) {
			throw new IllegalArgumentException("El estado de la tarjeta es obligatorio.");
		}
		this.estado = estado;
	}

	/**
	 * Indica si la tarjeta puede ser utilizada en este momento.
	 *
	 * @return {@code true} si el estado es {@link Estado#ACTIVA}
	 */
	public boolean puedeUsarse() {
		return estado.permiteUso();
	}

	/**
	 * @return titular de la tarjeta
	 */
	public Titular getTitular() {
		return titular;
	}

	/**
	 * @param titular nuevo titular de la tarjeta
	 */
	public void setTitular(Titular titular) {
		this.titular = titular;
	}

	/**
	 * Bloquea la tarjeta (estado {@link Estado#BLOQUEADA}).
	 *
	 * @return mensaje de confirmación
	 * @throws IllegalStateException si la tarjeta está cancelada
	 */
	public String bloquear() {
		validarNoCancelada();
		this.estado = Estado.BLOQUEADA;
		return "Tarjeta " + numero + " BLOQUEADA.";
	}

	/**
	 * Activa la tarjeta (estado {@link Estado#ACTIVA}).
	 *
	 * @return mensaje de confirmación
	 * @throws IllegalStateException si la tarjeta está cancelada
	 */
	public String activar() {
		validarNoCancelada();
		this.estado = Estado.ACTIVA;
		return "Tarjeta " + numero + " ACTIVADA.";
	}

	/**
	 * Cancela la tarjeta de forma definitiva (estado {@link Estado#CANCELADA}).
	 *
	 * @return mensaje de confirmación
	 */
	public String cancelar() {
		this.estado = Estado.CANCELADA;
		return "Tarjeta " + numero + " CANCELADA.";
	}

	/**
	 * Regla de negocio: una tarjeta cancelada no puede volver a activarse ni bloquearse.
	 *
	 * @throws IllegalStateException si la tarjeta está cancelada
	 */
	private void validarNoCancelada() {
		if (estado == Estado.CANCELADA) {
			throw new IllegalStateException("La tarjeta " + numero + " está CANCELADA y no puede cambiar de estado.");
		}
	}

	/**
	 * Representación textual con los datos comunes, incluido el estado.
	 *
	 * @return texto con número, fecha, estado y titular
	 */
	@Override
	public String toString() {
		return "numero=" + numero + ", fechaExp=" + fechaExp + ", estado=" + estado.getEtiqueta() + ", titular="
				+ titular;
	}
}
