package co.edu.poli.examen2_Soto.servicios;

import java.sql.Connection;
import java.sql.DriverManager;

import io.github.cdimascio.dotenv.Dotenv;

public class ConexionBD {

	private static ConexionBD instancia;
	private Connection conexion;

	private ConexionBD() throws Exception {

		Dotenv dotenv = Dotenv.load();

		String url = dotenv.get("DB_URL");
		String user = dotenv.get("DB_USER");
		String pass = dotenv.get("DB_PASSWORD");

		if (url == null || user == null || pass == null) {
			throw new RuntimeException("Faltan variables en el .env");
		}

		Class.forName("com.mysql.cj.jdbc.Driver");
		conexion = DriverManager.getConnection(url, user, pass);
	}

	public static ConexionBD getInstancia() throws Exception {
		if (instancia == null) {
			instancia = new ConexionBD();
		}
		return instancia;
	}

	public Connection getConexion() throws Exception {
		if (conexion == null || conexion.isClosed()) {
			instancia = new ConexionBD();
			return instancia.conexion;
		}
		return conexion;
	}
}
