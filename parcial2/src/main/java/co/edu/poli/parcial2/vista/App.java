package co.edu.poli.parcial2.vista;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicación JavaFX. Carga el formulario principal
 * {@code formCard.fxml}.
 *
 * @author Julian Larrarte
 * @version 2.0
 */
public class App extends Application {

	private static Scene scene;

	/**
	 * Construye y muestra la ventana principal.
	 *
	 * @param stage escenario principal
	 * @throws IOException si no se encuentra el FXML
	 */
	@Override
	public void start(Stage stage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("/co/edu/poli/parcial2/formCard.fxml"));
		scene = new Scene(root);
		stage.setTitle("Gestión de tarjetas");
		stage.setMinWidth(980);
		stage.setMinHeight(640);
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Cambia la vista raíz de la escena.
	 *
	 * @param fxml nombre del FXML sin extensión
	 * @throws IOException si no se encuentra el FXML
	 */
	static void setRoot(String fxml) throws IOException {
		scene.setRoot(loadFXML(fxml));
	}

	/**
	 * Carga un archivo FXML.
	 *
	 * @param fxml nombre del FXML sin extensión
	 * @return nodo raíz cargado
	 * @throws IOException si no se encuentra el FXML
	 */
	private static Parent loadFXML(String fxml) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
		return fxmlLoader.load();
	}

	/**
	 * Inicia la aplicación.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		launch();
	}

}