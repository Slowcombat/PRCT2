package co.edu.poli.examen2_Soto.modelo;

/**
 * Tarjeta bancaria asociada a un {@link Titular}.
 * <p>
 * Clase abstracta base de la jerarquía {@link Debito} / {@link Credito}. En la base
 * de datos se mapea con estrategia JOINED: los campos comunes viven en la tabla
 * {@code tarjeta} y los propios de cada subclase en {@code tarjeta_debito} y
 * {@code tarjeta_credito}.
 * </p>
 * <p>
 * <b>HU-01:</b> toda tarjeta tiene un {@link EstadoTarjeta} obligatorio
 * (Activa, Bloqueada o Cancelada). No es posible construir una tarjeta ni
 * asignarle un estado nulo, de modo que nunca se llega a guardar una tarjeta
 * sin un estado válido.
 * </p>
 *
 * @author Julian Larrarte
 * @version 2.0
 * @see EstadoTarjeta
 */
public abstract class Tarjeta {

	/** Número de la tarjeta; es la llave primaria en la base de datos. */
	private String numero;

	/** Fecha de expedición en formato {@code dd/MM/yyyy}. */
	private String fechaExp;

	/** Estado actual de la tarjeta; nunca es {@code null}. */
	private EstadoTarjeta estado;

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
	public Tarjeta(String numero, String fechaExp, EstadoTarjeta estado, Titular titular) {
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
	public EstadoTarjeta getEstado() {
		return estado;
	}

	/**
	 * Asigna el estado de la tarjeta.
	 *
	 * @param estado nuevo estado (obligatorio)
	 * @throws IllegalArgumentException si {@code estado} es {@code null}
	 */
	public void setEstado(EstadoTarjeta estado) {
		if (estado == null) {
			throw new IllegalArgumentException("El estado de la tarjeta es obligatorio.");
		}
		this.estado = estado;
	}

	/**
	 * Indica si la tarjeta puede ser utilizada en este momento.
	 *
	 * @return {@code true} si el estado es {@link EstadoTarjeta#ACTIVA}
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
	 * Bloquea la tarjeta (estado {@link EstadoTarjeta#BLOQUEADA}).
	 *
	 * @return mensaje de confirmación
	 * @throws IllegalStateException si la tarjeta está cancelada
	 */
	public String bloquear() {
		validarNoCancelada();
		this.estado = EstadoTarjeta.BLOQUEADA;
		return "Tarjeta " + numero + " BLOQUEADA.";
	}

	/**
	 * Activa la tarjeta (estado {@link EstadoTarjeta#ACTIVA}).
	 *
	 * @return mensaje de confirmación
	 * @throws IllegalStateException si la tarjeta está cancelada
	 */
	public String activar() {
		validarNoCancelada();
		this.estado = EstadoTarjeta.ACTIVA;
		return "Tarjeta " + numero + " ACTIVADA.";
	}

	/**
	 * Cancela la tarjeta de forma definitiva (estado {@link EstadoTarjeta#CANCELADA}).
	 *
	 * @return mensaje de confirmación
	 */
	public String cancelar() {
		this.estado = EstadoTarjeta.CANCELADA;
		return "Tarjeta " + numero + " CANCELADA.";
	}

	/**
	 * Regla de negocio: una tarjeta cancelada no puede volver a activarse ni bloquearse.
	 *
	 * @throws IllegalStateException si la tarjeta está cancelada
	 */
	private void validarNoCancelada() {
		if (estado == EstadoTarjeta.CANCELADA) {
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
