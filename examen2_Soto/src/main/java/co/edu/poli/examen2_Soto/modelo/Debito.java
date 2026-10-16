package co.edu.poli.examen2_Soto.modelo;

/**
 * Tarjeta débito: agrega a {@link Tarjeta} el saldo disponible.
 * Se persiste en la tabla {@code tarjeta_debito}.
 *
 * @author Julian Larrarte
 * @version 2.0
 */
public class Debito extends Tarjeta {

	/** Saldo disponible en la cuenta asociada. */
	private double saldo;

	/**
	 * Crea una tarjeta débito.
	 *
	 * @param numero   número de la tarjeta
	 * @param fechaExp fecha de expedición
	 * @param estado   estado inicial (obligatorio)
	 * @param titular  titular de la tarjeta
	 * @param saldo    saldo disponible
	 * @throws IllegalArgumentException si {@code estado} es {@code null}
	 */
	public Debito(String numero, String fechaExp, EstadoTarjeta estado, Titular titular, double saldo) {
		super(numero, fechaExp, estado, titular);
		this.saldo = saldo;
	}

	/**
	 * @return saldo disponible
	 */
	public double getSaldo() {
		return saldo;
	}

	/**
	 * @param saldo nuevo saldo disponible
	 */
	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

	/**
	 * @return representación textual de la tarjeta débito
	 */
	@Override
	public String toString() {
		return "Debito [" + super.toString() + ", saldo=" + saldo + "]";
	}
}
