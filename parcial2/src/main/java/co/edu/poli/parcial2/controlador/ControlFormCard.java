package co.edu.poli.parcial2.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import co.edu.poli.parcial2.modelo.Credito;
import co.edu.poli.parcial2.modelo.Debito;
import co.edu.poli.parcial2.modelo.Tarjeta.Estado;
import co.edu.poli.parcial2.modelo.Tarjeta;
import co.edu.poli.parcial2.modelo.Titular;
import co.edu.poli.parcial2.servicios.DAOTarjeta;
import co.edu.poli.parcial2.servicios.DAOTitular;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * Controlador del formulario principal {@code formCard.fxml}.
 * <p>
 * Pestaña <b>Consultar</b>: busca una tarjeta por número, muestra sus datos y su
 * estado, y permite asignarle un nuevo estado.<br>
 * Pestaña <b>Crear</b>: registra una tarjeta débito o crédito; el estado es un
 * campo obligatorio (HU-01).
 * </p>
 *
 * @author Julian Larrarte
 * @version 2.0
 */
public class ControlFormCard {

	/** Formato con el que se guarda la fecha de expedición. */
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	@FXML
	private Button bttConsulta;
	@FXML
	private TextField txtTarjeta1;
	@FXML
	private TextArea txtAreaResultado;
	/** Muestra el estado de la tarjeta consultada (HU-01). */
	@FXML
	private Label lblEstado;
	/** Estado que se quiere asignar a la tarjeta consultada (HU-01). */
	@FXML
	private ComboBox<Estado> cmbNuevoEstado;
	@FXML
	private Button bttCambiarEstado;
	@FXML
	private Button bttCreacion;
	@FXML
	private TextField txtTarjeta2;
	@FXML
	private DatePicker datepk1;
	@FXML
	private ComboBox<Titular> cmbTitular;
	/** Estado obligatorio de la tarjeta a crear (HU-01). */
	@FXML
	private ComboBox<Estado> cmbEstado;
	@FXML
	private RadioButton radio1; // Débito
	@FXML
	private RadioButton radio2; // Crédito
	@FXML
	private ToggleGroup tipo;

	/** Acceso a datos de tarjetas. */
	private DAOTarjeta daoTarjeta;

	/** Acceso a datos de titulares. */
	private DAOTitular daoTitular;

	/** Última tarjeta consultada; es a la que se le cambia el estado. */
	private Tarjeta tarjetaConsultada;

	/**
	 * Inicializa los DAO, carga titulares y estados en los ComboBox y registra
	 * las validaciones de los campos numéricos. Lo invoca JavaFX al cargar el FXML.
	 */
	@FXML
	private void initialize() {
		daoTarjeta = new DAOTarjeta();
		daoTitular = new DAOTitular();

		datepk1.setValue(LocalDate.now());

		try {
			List<Titular> lista = daoTitular.readall();
			cmbTitular.getItems().setAll(lista);
		} catch (Exception e) {
			mostrarAlerta(e.getMessage());
		}

		// HU-01: opciones de estado. No se preselecciona ninguna en "Crear"
		// para obligar al analista a elegir el estado de forma explícita.
		cmbEstado.getItems().setAll(Estado.values());
		cmbNuevoEstado.getItems().setAll(Estado.values());

		txtTarjeta1.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal)
				validarSoloNumeros(txtTarjeta1);
		});

		txtTarjeta2.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal)
				validarSoloNumeros(txtTarjeta2);
		});
	}

	/**
	 * Consulta la tarjeta digitada y muestra sus datos y su estado.
	 *
	 * @param event evento del botón "Consulta"
	 */
	@FXML
	private void pressConsulta(ActionEvent event) {
		txtAreaResultado.setText("");
		mostrarEstado(null);
		if (!txtTarjeta1.getText().isBlank()) {
			try {
				Tarjeta t = daoTarjeta.readone(txtTarjeta1.getText().trim());

				if (t != null) {
					txtAreaResultado.setText(t.toString());
					mostrarEstado(t);
				} else
					mostrarAlerta("No existe el numero de tarjeta");
			} catch (Exception e) {
				mostrarAlerta(e.getMessage());
			}
		} else
			mostrarAlerta("Ingrese número de tarjeta");
	}

	/**
	 * Asigna el estado elegido a la tarjeta consultada y lo persiste.
	 * Aplica la regla de negocio del modelo: una tarjeta cancelada no puede
	 * cambiar de estado.
	 *
	 * @param event evento del botón "Cambiar estado"
	 */
	@FXML
	private void pressCambiarEstado(ActionEvent event) {
		if (tarjetaConsultada == null) {
			mostrarAlerta("⚠ Primero consulte una tarjeta.");
			return;
		}
		Estado nuevo = cmbNuevoEstado.getValue();
		if (nuevo == null) {
			mostrarAlerta("⚠ Seleccione el nuevo estado.");
			return;
		}
		try {
			switch (nuevo) {
			case ACTIVA:
				tarjetaConsultada.activar();
				break;
			case BLOQUEADA:
				tarjetaConsultada.bloquear();
				break;
			case CANCELADA:
				tarjetaConsultada.cancelar();
				break;
			}
			String resultado = daoTarjeta.actualizarEstado(tarjetaConsultada.getNumero(), tarjetaConsultada.getEstado());
			mostrarAlerta(resultado);
			txtAreaResultado.setText(tarjetaConsultada.toString());
			mostrarEstado(tarjetaConsultada);
		} catch (IllegalStateException e) {
			mostrarAlerta("⚠ " + e.getMessage());
		} catch (Exception e) {
			mostrarAlerta(e.getMessage());
		}
	}

	/**
	 * Crea una tarjeta débito o crédito con los datos del formulario.
	 * Valida que número, fecha, titular y estado estén diligenciados.
	 *
	 * @param event evento del botón "Creación"
	 */
	@FXML
	private void pressCreacion(ActionEvent event) {

		String numero = txtTarjeta2.getText().trim();
		if (numero.isEmpty()) {
			mostrarAlerta("⚠ Ingrese el número de tarjeta.");
			return;
		}

		if (datepk1.getValue() == null) {
			mostrarAlerta("⚠ Seleccione la fecha de expedición.");
			return;
		}
		String fechaExp = datepk1.getValue().format(FORMATO_FECHA);

		Titular titular = cmbTitular.getValue();
		if (titular == null) {
			mostrarAlerta("⚠ Seleccione un titular.");
			return;
		}

		// HU-01: el estado es obligatorio.
		Estado estado = cmbEstado.getValue();
		if (estado == null) {
			cmbEstado.setStyle("-fx-border-color: red;");
			mostrarAlerta("⚠ Seleccione el estado de la tarjeta.");
			return;
		}
		cmbEstado.setStyle("");

		Tarjeta nueva;
		if (radio1.isSelected()) {
			nueva = new Debito(numero, fechaExp, estado, titular, 0.0);
		} else {
			nueva = new Credito(numero, fechaExp, estado, titular, 1000000);
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

	/**
	 * Actualiza la etiqueta de estado (con color) y habilita o deshabilita el
	 * cambio de estado según la tarjeta consultada.
	 *
	 * @param t tarjeta consultada, o {@code null} para limpiar
	 */
	private void mostrarEstado(Tarjeta t) {
		tarjetaConsultada = t;
		boolean hayTarjeta = t != null;
		cmbNuevoEstado.setDisable(!hayTarjeta);
		bttCambiarEstado.setDisable(!hayTarjeta);
		cmbNuevoEstado.setValue(null);

		if (!hayTarjeta) {
			lblEstado.setText("Estado: -");
			lblEstado.setStyle("-fx-font-weight: bold;");
			return;
		}
		lblEstado.setText("Estado: " + t.getEstado().getEtiqueta());
		lblEstado.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorEstado(t.getEstado()) + ";");
	}

	/**
	 * Color con el que se pinta cada estado en el formulario.
	 *
	 * @param estado estado de la tarjeta
	 * @return color CSS
	 */
	private String colorEstado(Estado estado) {
		switch (estado) {
		case ACTIVA:
			return "#1b7f3b";
		case BLOQUEADA:
			return "#c77700";
		default:
			return "#b3261e";
		}
	}

	/**
	 * Muestra un mensaje informativo al usuario.
	 *
	 * @param mensaje texto a mostrar
	 */
	private void mostrarAlerta(String mensaje) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Resultado");
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}

	/**
	 * Deja el formulario de creación listo para registrar otra tarjeta.
	 */
	private void limpiarFormCrear() {
		txtTarjeta2.clear();
		datepk1.setValue(LocalDate.now());
		cmbTitular.setValue(null);
		cmbEstado.setValue(null);
		radio1.setSelected(true);
	}

	/**
	 * Verifica que el campo solo contenga dígitos; si no, lo limpia y lo marca en rojo.
	 *
	 * @param txt campo de texto a validar
	 */
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
