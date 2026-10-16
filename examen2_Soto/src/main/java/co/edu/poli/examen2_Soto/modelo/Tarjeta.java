package co.edu.poli.examen2_Soto.modelo;

public abstract class Tarjeta {

	private String numero;

	private String fechaExp;

	private boolean estado;

	private Titular titular;

	public Tarjeta(String numero, String fechaExp, boolean estado, Titular titular) {
		this.numero = numero;
		this.fechaExp = fechaExp;
		this.estado = estado;
		this.titular = titular;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getFechaExp() {
		return fechaExp;
	}

	public void setFechaExp(String fechaExp) {
		this.fechaExp = fechaExp;
	}

	public boolean isEstado() {
		return estado;
	}

	public void setEstado(boolean estado) {
		this.estado = estado;
	}

	public Titular getTitular() {
		return titular;
	}

	public void setTitular(Titular titular) {
		this.titular = titular;
	}

	public String bloquear() {
		this.estado = false;
		return "Tarjeta " + numero + " BLOQUEADA.";
	}

	public String activar() {
		this.estado = true;
		return "Tarjeta " + numero + " ACTIVADA.";
	}

	@Override
	public String toString() {
		return "numero=" + numero + ", fechaExp=" + fechaExp + ", estado=" + estado + ", titular=" + titular;
	}
}
