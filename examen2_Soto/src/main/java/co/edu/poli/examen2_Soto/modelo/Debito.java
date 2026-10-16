package co.edu.poli.examen2_Soto.modelo;

public class Debito extends Tarjeta {

	private double saldo;

	public Debito(String numero, String fechaExp, boolean estado, Titular titular, double saldo) {
		super(numero, fechaExp, estado, titular);
		this.saldo = saldo;
	}

	public double getSaldo() {
		return saldo;
	}

	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}

	@Override
	public String toString() {
		return "Debito [" + super.toString() + ", saldo=" + saldo + "]";
	}
}
