package co.edu.poli.examen2_Soto.servicios;

import co.edu.poli.examen2_Soto.modelo.Credito;
import co.edu.poli.examen2_Soto.modelo.Debito;
import co.edu.poli.examen2_Soto.modelo.Tarjeta;
import co.edu.poli.examen2_Soto.modelo.Titular;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class DAOTarjeta implements CRUD<Tarjeta> {

	@Override
	public String create(Tarjeta t) throws Exception {

		Connection con = ConexionBD.getInstancia().getConexion();

		con.setAutoCommit(false);

		String SQL_INSERT_TARJETA = "INSERT INTO tarjeta (numero, fecha_exp, estado, titular_id) "
				+ "VALUES (?, ?, ?, ?)";

		PreparedStatement ps = con.prepareStatement(SQL_INSERT_TARJETA);
		ps.setString(1, t.getNumero());
		ps.setString(2, t.getFechaExp());
		ps.setBoolean(3, t.isEstado());
		ps.setString(4, t.getTitular().getId());
		ps.executeUpdate();

		String SQL_INSERT_DEBITO = "INSERT INTO tarjeta_debito (numero, saldo) VALUES (?, ?)";
		String SQL_INSERT_CREDITO = "INSERT INTO tarjeta_credito (numero, limite) VALUES (?, ?)";

		String sql = (t instanceof Debito) ? SQL_INSERT_DEBITO : SQL_INSERT_CREDITO;
		ps = con.prepareStatement(sql);
		ps.setString(1, t.getNumero());
		if (t instanceof Debito)
			ps.setDouble(2, ((Debito) t).getSaldo());
		else
			ps.setDouble(2, ((Credito) t).getLimite());
		
		try {
			ps.executeUpdate();
			con.commit();
			return "✔ " + t.getClass().getSimpleName() + " [" + t.getNumero() + "] guardada correctamente.";
		} catch (Exception e) {
			con.rollback();
			return e.getMessage();
		} finally {
			con.setAutoCommit(true);
		}
	}

	@Override
	public <K> Tarjeta readone(K num) throws Exception {

		Connection con = ConexionBD.getInstancia().getConexion();

		String SQL_SELECT_DEBITO = "SELECT  t.numero, t.fecha_exp, t.estado, "
				+ "        ti.id AS titular_id, ti.nombre AS titular_nombre, " + "        d.saldo "
				+ "FROM    tarjeta_debito d " + "INNER JOIN tarjeta  t  ON d.numero     = t.numero "
				+ "INNER JOIN titular  ti ON t.titular_id = ti.id " + "WHERE   d.numero = ?";

		PreparedStatement ps = con.prepareStatement(SQL_SELECT_DEBITO);
		ps.setString(1, (String) num);
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return new Debito(rs.getString("numero"), rs.getString("fecha_exp"), rs.getBoolean("estado"),
					new Titular(rs.getString("titular_id"), rs.getString("titular_nombre")), rs.getDouble("saldo"));
		}

		String SQL_SELECT_CREDITO = "SELECT  t.numero, t.fecha_exp, t.estado, "
				+ "        ti.id AS titular_id, ti.nombre AS titular_nombre, " + "        c.limite "
				+ "FROM    tarjeta_credito c " + "INNER JOIN tarjeta  t  ON c.numero     = t.numero "
				+ "INNER JOIN titular  ti ON t.titular_id = ti.id " + "WHERE   c.numero = ?";

		ps = con.prepareStatement(SQL_SELECT_CREDITO);
		ps.setString(1, (String) num);
		rs = ps.executeQuery();
		if (rs.next()) {
			return new Credito(rs.getString("numero"), rs.getString("fecha_exp"), rs.getBoolean("estado"),
					new Titular(rs.getString("titular_id"), rs.getString("titular_nombre")), rs.getDouble("limite"));
		}

		return null;
	}

	@Override
	public List<Tarjeta> readall() {
		return null;
	}
}
