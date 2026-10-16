package co.edu.poli.examen2_Soto.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import co.edu.poli.examen2_Soto.modelo.Credito;
import co.edu.poli.examen2_Soto.modelo.Debito;
import co.edu.poli.examen2_Soto.modelo.Tarjeta;
import co.edu.poli.examen2_Soto.modelo.Titular;
import co.edu.poli.examen2_Soto.servicios.DAOTarjeta;
import co.edu.poli.examen2_Soto.servicios.DAOTitular;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.application.Platform;

public class ControlFormCard {

	@FXML
	private Button bttConsulta;
	@FXML
	private TextField txtTarjeta1;
	@FXML
	private TextArea txtAreaResultado;
	@FXML
	private Button bttCreacion;
	@FXML
	private TextField txtTarjeta2;
	@FXML
	private DatePicker datepk1;
	@FXML
	private ComboBox<Titular> cmbTitular;
	@FXML
	private RadioButton radio1; // Débito
	@FXML
	private RadioButton radio2; // Crédito
	@FXML
	private ToggleGroup tipo;

	private DAOTarjeta daoTarjeta;

	private DAOTitular daoTitular;

	@FXML
	private void initialize() {
		daoTarjeta = new DAOTarjeta();
		daoTitular = new DAOTitular();

		datepk1.setValue(LocalDate.now());

		List<Titular> lista = null;
		try {
			lista = daoTitular.readall();
		} catch (Exception e) {
			mostrarAlerta(e.getMessage());
		}
		cmbTitular.getItems().setAll(lista);

		txtTarjeta1.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal)
				validarSoloNumeros(txtTarjeta1);
		});

		txtTarjeta2.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal)
				validarSoloNumeros(txtTarjeta2);
		});

	}

	@FXML
	private void pressConsulta(ActionEvent event) {
		txtAreaResultado.setText("");
		if (!txtTarjeta1.getText().isBlank() || !txtTarjeta1.getText().isEmpty()) {
			try {
				Tarjeta t = daoTarjeta.readone(txtTarjeta1.getText());

				if (t != null)
					txtAreaResultado.setText(t.toString());
				else
					mostrarAlerta("No existe el numero de tarjeta");
			} catch (Exception e) {
				mostrarAlerta(e.getMessage());
			}
		} else
			mostrarAlerta("Ingrese número de tarjeta");
	}

	@FXML
	private void pressCreacion(ActionEvent event) {

		String numero = txtTarjeta2.getText().trim();
		if (numero.isEmpty()) {
			mostrarAlerta("⚠ Ingrese el número de tarjeta.");
			return;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String fechaExp = datepk1.getValue().format(formatter);
		if (fechaExp == null || fechaExp.isEmpty()) {
			mostrarAlerta("⚠ Seleccione la fecha de expedición.");
			return;
		}

		Titular titular = cmbTitular.getValue();
		if (titular == null) {
			mostrarAlerta("⚠ Seleccione un titular.");
			return;
		}

		Tarjeta nueva;

		if (radio1.isSelected()) {
			nueva = new Debito(numero, fechaExp, true, titular, 0.0);
		} else {
			nueva = new Credito(numero, fechaExp, true, titular, 1000000);
		}

		try {
			String resultado = daoTarjeta.create(nueva);
			mostrarAlerta(resultado);
			if (resultado.startsWith("✔")) {
				limpiarFormCrear();
			}
		} catch (Exception e) {
			mostrarAlerta(e.getMessage());
		}

	}

	private void mostrarAlerta(String mensaje) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Resultado");
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}

	private void limpiarFormCrear() {
		txtTarjeta2.clear();
		datepk1.setValue(null);
		cmbTitular.setValue(null);
		radio1.setSelected(true);
	}

	private void validarSoloNumeros(TextField txt) {
		String texto = txt.getText().trim();
		if (!texto.isBlank()) {
			if (!texto.matches("\\d+")) {
				txtAreaResultado.setText("");
				txt.setStyle("-fx-border-color: red;");
				mostrarAlerta("Solo números son permitidos!");
				txt.setText("");
				Platform.runLater(() -> txt.requestFocus());
			} else {
				txt.setStyle("");
			}
		} else
			txt.setStyle("");
	}

}