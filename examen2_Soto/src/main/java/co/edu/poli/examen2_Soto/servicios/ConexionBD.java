package co.edu.poli.examen2_Soto.servicios;

import java.sql.Connection;
import java.sql.DriverManager;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Conexión única (patrón Singleton) a la base de datos MySQL.
 * <p>
 * Lee {@code DB_URL}, {@code DB_USER} y {@code DB_PASSWORD} del archivo
 * {@code .env} ubicado en la raíz del módulo {@code examen2_Soto}.
 * </p>
 *
 * @author Julian Larrarte
 * @version 1.0
 */
public class ConexionBD {

	private static ConexionBD instancia;
	private Connection conexion;

	/**
	 * Abre la conexión con los datos del archivo {@code .env}.
	 *
	 * @throws Exception si faltan variables o no es posible conectarse
	 */
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

	/**
	 * Devuelve la instancia única, creándola si aún no existe.
	 *
	 * @return instancia de {@code ConexionBD}
	 * @throws Exception si no es posible conectarse
	 */
	public static ConexionBD getInstancia() throws Exception {
		if (instancia == null) {
			instancia = new ConexionBD();
		}
		return instancia;
	}

	/**
	 * Devuelve la conexión activa; si se cerró, la vuelve a abrir.
	 *
	 * @return conexión JDBC
	 * @throws Exception si no es posible conectarse
	 */
	public Connection getConexion() throws Exception {
		if (conexion == null || conexion.isClosed()) {
			instancia = new ConexionBD();
			return instancia.conexion;
		}
		return conexion;
	}
}
