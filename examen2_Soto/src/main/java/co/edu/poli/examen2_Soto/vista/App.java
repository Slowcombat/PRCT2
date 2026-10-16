package co.edu.poli.examen2_Soto.vista;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

	private static Scene scene;

	@Override
	public void start(Stage stage) throws IOException {
		TabPane root = (TabPane) FXMLLoader.load(getClass().getResource("/co/edu/poli/examen2_Soto/formCard.fxml"));
		scene = new Scene(root);
		stage.setTitle("Tarjetas");
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}

	static void setRoot(String fxml) throws IOException {
		scene.setRoot(loadFXML(fxml));
	}

	private static Parent loadFXML(String fxml) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
		return fxmlLoader.load();
	}

	public static void main(String[] args) {
		launch();
	}

}