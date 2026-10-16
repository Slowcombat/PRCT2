package co.edu.poli.examen2_Soto.modelo;

/**
 * Tarjeta crédito: agrega a {@link Tarjeta} el límite de crédito.
 * Se persiste en la tabla {@code tarjeta_credito}.
 *
 * @author Julian Larrarte
 * @version 2.0
 */
public class Credito extends Tarjeta {

	/** Cupo máximo de crédito aprobado. */
	private double limite;

	/**
	 * Crea una tarjeta crédito.
	 *
	 * @param numero   número de la tarjeta
	 * @param fechaExp fecha de expedición
	 * @param estado   estado inicial (obligatorio)
	 * @param titular  titular de la tarjeta
	 * @param limite   límite de crédito
	 * @throws IllegalArgumentException si {@code estado} es {@code null}
	 */
	public Credito(String numero, String fechaExp, EstadoTarjeta estado, Titular titular, double limite) {
		super(numero, fechaExp, estado, titular);
		this.limite = limite;
	}

	/**
	 * @return límite de crédito
	 */
	public double getLimite() {
		return limite;
	}

	/**
	 * @param limite nuevo límite de crédito
	 */
	public void setLimite(double limite) {
		this.limite = limite;
	}

	/**
	 * @return representación textual de la tarjeta crédito
	 */
	@Override
	public String toString() {
		return "Credito [" + super.toString() + ", limite=" + limite + "]";
	}
}
