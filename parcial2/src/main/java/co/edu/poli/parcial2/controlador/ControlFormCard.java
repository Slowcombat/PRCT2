package co.edu.poli.parcial2.controlador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import co.edu.poli.parcial2.modelo.Credito;
import co.edu.poli.parcial2.modelo.Debito;
import co.edu.poli.parcial2.modelo.Tarjeta;
import co.edu.poli.parcial2.modelo.Tarjeta.Estado;
import co.edu.poli.parcial2.modelo.Titular;
import co.edu.poli.parcial2.servicios.DAOTarjeta;
import co.edu.poli.parcial2.servicios.DAOTitular;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Controlador de la ventana principal {@code formCard.fxml}.
 * <p>
 * La ventana tiene tres pestañas:
 * </p>
 * <ul>
 *   <li><b>Tarjetas</b>: lista todas las tarjetas guardadas con su estado, permite
 *       filtrarlas y, al seleccionar una, ver su detalle y cambiarle el estado
 *       (HU-01).</li>
 *   <li><b>Nueva tarjeta</b>: registra una tarjeta débito o crédito; el estado y
 *       el titular son obligatorios. Desde aquí también se puede crear un titular.</li>
 *   <li><b>Titulares</b>: registra titulares y muestra cuántas tarjetas tiene cada uno.</li>
 * </ul>
 *
 * @author Julian Larrarte
 * @version 3.0
 */
public class ControlFormCard {

	/** Formato con el que se guarda y se muestra la fecha de expedición. */
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	/** Texto de la opción del filtro que muestra todos los estados. */
	private static final String TODOS_LOS_ESTADOS = "Todos los estados";


	// ---------- Encabezado y mensajes ----------
	@FXML
	private Label lblTotal;
	@FXML
	private Label lblActivas;
	@FXML
	private Label lblBloqueadas;
	@FXML
	private Label lblCanceladas;
	@FXML
	private Label lblMensaje;
	@FXML
	private TabPane pestanas;
	@FXML
	private Tab tabTarjetas;
	@FXML
	private Tab tabNueva;
	@FXML
	private Tab tabTitulares;

	// ---------- Pestaña Tarjetas ----------
	@FXML
	private TextField txtBuscar;
	@FXML
	private ComboBox<String> cmbFiltroEstado;
	@FXML
	private TableView<Tarjeta> tablaTarjetas;
	@FXML
	private TableColumn<Tarjeta, String> colNumero;
	@FXML
	private TableColumn<Tarjeta, String> colTipo;
	@FXML
	private TableColumn<Tarjeta, String> colTitular;
	@FXML
	private TableColumn<Tarjeta, String> colFecha;
	@FXML
	private TableColumn<Tarjeta, Estado> colEstado;
	@FXML
	private Label lblSinSeleccion;
	@FXML
	private VBox panelDetalle;
	@FXML
	private VBox tarjetaVisual;
	@FXML
	private Label lblTipoVisual;
	@FXML
	private Label lblNumeroVisual;
	@FXML
	private Label lblTitularVisual;
	@FXML
	private Label lblFechaVisual;
	@FXML
	private Label lblEstadoDetalle;
	/** Estado que se asignará a la tarjeta seleccionada (HU-01). */
	@FXML
	private ComboBox<Estado> cmbNuevoEstado;

	// ---------- Pestaña Nueva tarjeta ----------
	@FXML
	private TextField txtNumero;
	@FXML
	private DatePicker datepk1;
	@FXML
	private RadioButton radio1; // Débito
	@FXML
	private RadioButton radio2; // Crédito
	@FXML
	private ToggleGroup tipo;
	@FXML
	private ComboBox<Titular> cmbTitular;
	/** Estado obligatorio de la tarjeta a crear (HU-01). */
	@FXML
	private ComboBox<Estado> cmbEstado;

	// ---------- Pestaña Titulares ----------
	@FXML
	private TextField txtIdTitular;
	@FXML
	private TextField txtNombreTitular;
	@FXML
	private TableView<Titular> tablaTitulares;
	@FXML
	private TableColumn<Titular, String> colIdTitular;
	@FXML
	private TableColumn<Titular, String> colNombreTitular;
	@FXML
	private TableColumn<Titular, Number> colCantTarjetas;

	/** Acceso a datos de tarjetas. */
	private DAOTarjeta daoTarjeta;

	/** Acceso a datos de titulares. */
	private DAOTitular daoTitular;

	/** Todas las tarjetas leídas de la base de datos. */
	private final ObservableList<Tarjeta> tarjetas = FXCollections.observableArrayList();

	/** Tarjetas visibles según la búsqueda y el filtro de estado. */
	private FilteredList<Tarjeta> tarjetasFiltradas;

	/** Todos los titulares leídos de la base de datos. */
	private final ObservableList<Titular> titulares = FXCollections.observableArrayList();

	/** Cantidad de tarjetas por identificación de titular. */
	private final Map<String, Integer> tarjetasPorTitular = new HashMap<>();

	/**
	 * Configura tablas, filtros, combos y validaciones de los campos, y carga
	 * los datos. Lo invoca JavaFX al cargar el FXML.
	 */
	@FXML
	private void initialize() {
		daoTarjeta = new DAOTarjeta();
		daoTitular = new DAOTitular();

		configurarTablaTarjetas();
		configurarTablaTitulares();
		configurarFormularios();

		cargarDatos(null);
		mostrarDetalle(null);
	}

	// =====================================================================
	// Configuración
	// =====================================================================

	/**
	 * Define cómo se muestra cada columna de la tabla de tarjetas, el filtro de
	 * búsqueda y la reacción al seleccionar una fila.
	 */
	private void configurarTablaTarjetas() {
		colNumero.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNumero()));
		colNumero.getStyleClass().add("celda-numero");
		colTipo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTipo()));
		colTitular.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTitular().getNombre()));
		colFecha.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getFechaExp()));

		// HU-01: el estado se muestra como una insignia de color
		colEstado.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getEstado()));
		colEstado.setCellFactory(col -> new TableCell<Tarjeta, Estado>() {
			private final Label insignia = new Label();

			{
				insignia.setMinWidth(Region.USE_PREF_SIZE);
			}

			@Override
			protected void updateItem(Estado estado, boolean vacio) {
				super.updateItem(estado, vacio);
				if (vacio || estado == null) {
					setGraphic(null);
				} else {
					pintarInsignia(insignia, estado);
					setGraphic(insignia);
				}
				setText(null);
			}
		});


		tarjetasFiltradas = new FilteredList<>(tarjetas, t -> true);
		SortedList<Tarjeta> ordenadas = new SortedList<>(tarjetasFiltradas);
		ordenadas.comparatorProperty().bind(tablaTarjetas.comparatorProperty());
		tablaTarjetas.setItems(ordenadas);
		tablaTarjetas.setPlaceholder(new Label("No hay tarjetas que coincidan."));

		cmbFiltroEstado.getItems().add(TODOS_LOS_ESTADOS);
		for (Estado e : Estado.values()) {
			cmbFiltroEstado.getItems().add(e.getEtiqueta());
		}
		cmbFiltroEstado.setValue(TODOS_LOS_ESTADOS);

		txtBuscar.textProperty().addListener((obs, a, b) -> aplicarFiltro());
		cmbFiltroEstado.valueProperty().addListener((obs, a, b) -> aplicarFiltro());
		tablaTarjetas.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> mostrarDetalle(b));

		cmbNuevoEstado.getItems().setAll(Estado.values());
	}

	/**
	 * Define las columnas de la tabla de titulares.
	 */
	private void configurarTablaTitulares() {
		colIdTitular.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getId()));
		colNombreTitular.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));
		colCantTarjetas.setCellValueFactory(
				c -> new ReadOnlyObjectWrapper<>(tarjetasPorTitular.getOrDefault(c.getValue().getId(), 0)));
		tablaTitulares.setItems(titulares);
		tablaTitulares.setPlaceholder(new Label("Aún no hay titulares."));
	}

	/**
	 * Prepara los campos del formulario de creación y de titulares.
	 */
	private void configurarFormularios() {
		datepk1.setConverter(new StringConverter<LocalDate>() {
			@Override
			public String toString(LocalDate fecha) {
				return fecha == null ? "" : fecha.format(FORMATO_FECHA);
			}

			@Override
			public LocalDate fromString(String texto) {
				try {
					return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), FORMATO_FECHA);
				} catch (DateTimeParseException e) {
					return null;
				}
			}
		});
		datepk1.setValue(LocalDate.now());

		txtNumero.setTextFormatter(new TextFormatter<>(soloDigitos(20)));
		txtIdTitular.setTextFormatter(new TextFormatter<>(soloAlfanumerico(50)));


		// HU-01: no se preselecciona estado para obligar a elegirlo
		cmbEstado.getItems().setAll(Estado.values());
		cmbTitular.setItems(titulares);
	}

	// =====================================================================
	// Carga de datos
	// =====================================================================

	/**
	 * Lee titulares y tarjetas de la base de datos y refresca la vista.
	 *
	 * @param numeroASeleccionar número de tarjeta a dejar seleccionado, o
	 *                           {@code null} para conservar la selección actual
	 */
	private void cargarDatos(String numeroASeleccionar) {
		Tarjeta actual = tablaTarjetas.getSelectionModel().getSelectedItem();
		String seleccion = numeroASeleccionar != null ? numeroASeleccionar : actual != null ? actual.getNumero() : null;
		try {
			List<Titular> listaTitulares = daoTitular.readall();
			List<Tarjeta> listaTarjetas = daoTarjeta.readall();

			tarjetasPorTitular.clear();
			for (Tarjeta t : listaTarjetas) {
				tarjetasPorTitular.merge(t.getTitular().getId(), 1, Integer::sum);
			}
			titulares.setAll(listaTitulares);
			tarjetas.setAll(listaTarjetas);
			tablaTitulares.refresh();
			actualizarResumen();
		} catch (Exception e) {
			mostrarError("No fue posible leer la base de datos: " + e.getMessage());
			return;
		}

		if (seleccion != null) {
			for (Tarjeta t : tablaTarjetas.getItems()) {
				if (t.getNumero().equals(seleccion)) {
					tablaTarjetas.getSelectionModel().select(t);
					tablaTarjetas.scrollTo(t);
					return;
				}
			}
		}
		mostrarDetalle(tablaTarjetas.getSelectionModel().getSelectedItem());
	}

	/**
	 * Actualiza los contadores por estado del encabezado.
	 */
	private void actualizarResumen() {
		int activas = 0, bloqueadas = 0, canceladas = 0;
		for (Tarjeta t : tarjetas) {
			switch (t.getEstado()) {
			case ACTIVA:
				activas++;
				break;
			case BLOQUEADA:
				bloqueadas++;
				break;
			default:
				canceladas++;
			}
		}
		lblTotal.setText(tarjetas.size() + (tarjetas.size() == 1 ? " tarjeta" : " tarjetas"));
		lblActivas.setText(activas + (activas == 1 ? " activa" : " activas"));
		lblBloqueadas.setText(bloqueadas + (bloqueadas == 1 ? " bloqueada" : " bloqueadas"));
		lblCanceladas.setText(canceladas + (canceladas == 1 ? " cancelada" : " canceladas"));
	}

	/**
	 * Filtra la tabla por el texto de búsqueda (número o titular) y por estado.
	 */
	private void aplicarFiltro() {
		String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
		String estado = cmbFiltroEstado.getValue();
		tarjetasFiltradas.setPredicate(t -> {
			boolean coincideTexto = texto.isEmpty() || t.getNumero().contains(texto)
					|| t.getTitular().getNombre().toLowerCase().contains(texto)
					|| t.getTitular().getId().toLowerCase().contains(texto);
			boolean coincideEstado = estado == null || TODOS_LOS_ESTADOS.equals(estado)
					|| t.getEstado().getEtiqueta().equals(estado);
			return coincideTexto && coincideEstado;
		});
	}

	// =====================================================================
	// Pestaña Tarjetas
	// =====================================================================

	/**
	 * Muestra el detalle de la tarjeta seleccionada (o lo oculta si no hay).
	 *
	 * @param t tarjeta seleccionada, o {@code null}
	 */
	private void mostrarDetalle(Tarjeta t) {
		boolean hay = t != null;
		panelDetalle.setVisible(hay);
		panelDetalle.setManaged(hay);
		lblSinSeleccion.setVisible(!hay);
		lblSinSeleccion.setManaged(!hay);
		cmbNuevoEstado.setValue(null);
		if (!hay) {
			return;
		}

		tarjetaVisual.getStyleClass().removeAll("tv-debito", "tv-credito", "tv-bloqueada", "tv-cancelada");
		tarjetaVisual.getStyleClass().add(t instanceof Debito ? "tv-debito" : "tv-credito");
		if (t.getEstado() == Estado.BLOQUEADA) {
			tarjetaVisual.getStyleClass().add("tv-bloqueada");
		} else if (t.getEstado() == Estado.CANCELADA) {
			tarjetaVisual.getStyleClass().add("tv-cancelada");
		}
		lblTipoVisual.setText(t.getTipo().toUpperCase());
		lblNumeroVisual.setText(agruparNumero(t.getNumero()));
		lblTitularVisual.setText(t.getTitular().getNombre());
		lblFechaVisual.setText(t.getFechaExp());

		pintarInsignia(lblEstadoDetalle, t.getEstado());
	}

	/**
	 * Asigna el estado elegido a la tarjeta seleccionada y lo persiste (HU-01).
	 * Respeta la regla de negocio: una tarjeta cancelada no cambia de estado.
	 *
	 * @param event evento del botón "Aplicar"
	 */
	@FXML
	private void pressCambiarEstado(ActionEvent event) {
		Tarjeta t = tablaTarjetas.getSelectionModel().getSelectedItem();
		if (t == null) {
			mostrarAviso("Seleccione una tarjeta de la lista.");
			return;
		}
		Estado nuevo = cmbNuevoEstado.getValue();
		if (nuevo == null) {
			mostrarAviso("Seleccione el nuevo estado.");
			return;
		}
		if (nuevo == t.getEstado()) {
			mostrarAviso("La tarjeta ya está " + nuevo.getEtiqueta().toLowerCase() + ".");
			return;
		}
		try {
			switch (nuevo) {
			case ACTIVA:
				t.activar();
				break;
			case BLOQUEADA:
				t.bloquear();
				break;
			case CANCELADA:
				t.cancelar();
				break;
			}
			String resultado = daoTarjeta.actualizarEstado(t.getNumero(), t.getEstado());
			cargarDatos(t.getNumero());
			informar(resultado);
		} catch (IllegalStateException e) {
			mostrarAviso(e.getMessage());
		} catch (Exception e) {
			mostrarError(e.getMessage());
			cargarDatos(t.getNumero());
		}
	}

	/**
	 * Vuelve a leer los datos de la base de datos.
	 *
	 * @param event evento del botón "Actualizar"
	 */
	@FXML
	private void pressRefrescar(ActionEvent event) {
		cargarDatos(null);
		informar("Datos actualizados.");
	}

	// =====================================================================
	// Pestaña Nueva tarjeta
	// =====================================================================

	/**
	 * Crea una tarjeta débito o crédito con los datos del formulario.
	 * Valida número, fecha, titular y estado (HU-01).
	 *
	 * @param event evento del botón "Guardar tarjeta"
	 */
	@FXML
	private void pressCreacion(ActionEvent event) {
		limpiarErrores();

		String numero = txtNumero.getText() == null ? "" : txtNumero.getText().trim();
		if (numero.isEmpty()) {
			marcarError(txtNumero, "Ingrese el número de tarjeta.");
			return;
		}
		if (datepk1.getValue() == null) {
			marcarError(datepk1, "Seleccione la fecha de expedición.");
			return;
		}
		Titular titular = cmbTitular.getValue();
		if (titular == null) {
			marcarError(cmbTitular, "Seleccione un titular o cree uno con \"+ Nuevo\".");
			return;
		}
		Estado estado = cmbEstado.getValue();
		if (estado == null) {
			marcarError(cmbEstado, "Seleccione el estado de la tarjeta.");
			return;
		}

		String fechaExp = datepk1.getValue().format(FORMATO_FECHA);
		// Saldo y límite no hacen parte de la HU-01: se usan los valores por
		// defecto del proyecto base (débito sin saldo, crédito con límite fijo).
		Tarjeta nueva = radio1.isSelected() ? new Debito(numero, fechaExp, estado, titular, 0.0)
				: new Credito(numero, fechaExp, estado, titular, 1000000);

		try {
			String resultado = daoTarjeta.create(nueva);
			if (resultado.startsWith("✔")) {
				limpiarFormCrear();
				txtBuscar.clear();
				cmbFiltroEstado.setValue(TODOS_LOS_ESTADOS);
				cargarDatos(numero);
				pestanas.getSelectionModel().select(tabTarjetas);
				informar(resultado);
			} else if (resultado.toLowerCase().contains("duplicate")) {
				marcarError(txtNumero, "Ya existe una tarjeta con el número " + numero + ".");
			} else {
				mostrarError(resultado);
			}
		} catch (Exception e) {
			mostrarError(e.getMessage());
		}
	}

	/**
	 * Limpia el formulario de creación.
	 *
	 * @param event evento del botón "Limpiar"
	 */
	@FXML
	private void pressLimpiar(ActionEvent event) {
		limpiarFormCrear();
	}

	/**
	 * Abre un diálogo para registrar un titular sin salir del formulario de
	 * creación; al guardarlo queda seleccionado.
	 *
	 * @param event evento del botón "+ Nuevo"
	 */
	@FXML
	private void pressNuevoTitular(ActionEvent event) {
		Dialog<Titular> dialogo = new Dialog<>();
		dialogo.setTitle("Nuevo titular");
		dialogo.setHeaderText("Registre la persona dueña de la tarjeta");
		dialogo.getDialogPane().getStylesheets()
				.add(getClass().getResource("/co/edu/poli/parcial2/estilos.css").toExternalForm());

		ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
		dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, guardar);
		dialogo.getDialogPane().lookupButton(guardar).getStyleClass().add("boton-primario");

		TextField id = new TextField();
		id.setPromptText("Ej. 1023456789");
		id.setTextFormatter(new TextFormatter<>(soloAlfanumerico(50)));
		TextField nombre = new TextField();
		nombre.setPromptText("Ej. Ana María Gómez");
		nombre.setPrefWidth(280);

		GridPane grid = new GridPane();
		grid.setHgap(12);
		grid.setVgap(10);
		grid.setPadding(new Insets(6, 0, 0, 0));
		Label lId = new Label("Identificación *");
		Label lNombre = new Label("Nombre completo *");
		lId.getStyleClass().add("etiqueta-campo");
		lNombre.getStyleClass().add("etiqueta-campo");
		grid.addRow(0, lId, id);
		grid.addRow(1, lNombre, nombre);
		dialogo.getDialogPane().setContent(grid);
		Platform.runLater(id::requestFocus);

		dialogo.setResultConverter(b -> b == guardar ? new Titular(id.getText(), nombre.getText()) : null);
		dialogo.showAndWait().ifPresent(t -> {
			if (guardarTitular(t)) {
				seleccionarTitularEnCombo(t.getId().trim());
			}
		});
	}

	// =====================================================================
	// Pestaña Titulares
	// =====================================================================

	/**
	 * Registra el titular escrito en el formulario de la pestaña Titulares.
	 *
	 * @param event evento del botón "Guardar titular"
	 */
	@FXML
	private void pressGuardarTitular(ActionEvent event) {
		Titular t = new Titular(txtIdTitular.getText(), txtNombreTitular.getText());
		if (guardarTitular(t)) {
			txtIdTitular.clear();
			txtNombreTitular.clear();
		}
	}

	/**
	 * Valida y guarda un titular, y recarga los datos.
	 *
	 * @param t titular a guardar
	 * @return {@code true} si se guardó
	 */
	private boolean guardarTitular(Titular t) {
		try {
			String resultado = daoTitular.create(t);
			if (resultado.startsWith("✔")) {
				cargarDatos(null);
				informar(resultado);
				return true;
			}
			mostrarAviso(resultado.replace("⚠ ", ""));
		} catch (Exception e) {
			mostrarError(e.getMessage());
		}
		return false;
	}

	/**
	 * Deja seleccionado en el combo de creación el titular indicado.
	 *
	 * @param id identificación del titular
	 */
	private void seleccionarTitularEnCombo(String id) {
		for (Titular t : titulares) {
			if (t.getId().equals(id)) {
				cmbTitular.setValue(t);
				return;
			}
		}
	}

	// =====================================================================
	// Utilidades de interfaz
	// =====================================================================

	/**
	 * Aplica a una etiqueta el texto y el color del estado.
	 *
	 * @param etiqueta etiqueta a pintar
	 * @param estado   estado de la tarjeta
	 */
	private void pintarInsignia(Label etiqueta, Estado estado) {
		etiqueta.setText(estado.getEtiqueta());
		etiqueta.getStyleClass().removeAll("badge-activa", "badge-bloqueada", "badge-cancelada");
		if (!etiqueta.getStyleClass().contains("badge")) {
			etiqueta.getStyleClass().add("badge");
		}
		etiqueta.getStyleClass().add("badge-" + estado.name().toLowerCase());
	}

	/**
	 * Separa el número en grupos de cuatro dígitos, como en una tarjeta física.
	 *
	 * @param numero número de la tarjeta
	 * @return número agrupado, p. ej. {@code 4111 1111 1111 1111}
	 */
	private String agruparNumero(String numero) {
		return numero.replaceAll("(.{4})(?!$)", "$1 ");
	}

	/**
	 * Filtro de entrada que solo acepta dígitos, con una longitud máxima.
	 *
	 * @param max cantidad máxima de caracteres
	 * @return filtro para un {@link TextFormatter}
	 */
	private UnaryOperator<TextFormatter.Change> soloDigitos(int max) {
		return c -> c.getControlNewText().matches("\\d{0," + max + "}") ? c : null;
	}

	/**
	 * Filtro de entrada que acepta letras sin tilde, dígitos y guiones.
	 *
	 * @param max cantidad máxima de caracteres
	 * @return filtro para un {@link TextFormatter}
	 */
	private UnaryOperator<TextFormatter.Change> soloAlfanumerico(int max) {
		return c -> c.getControlNewText().matches("[A-Za-z0-9-]{0," + max + "}") ? c : null;
	}

	/**
	 * Marca un campo en rojo y muestra el motivo.
	 *
	 * @param campo   control con el error
	 * @param mensaje motivo
	 */
	private void marcarError(javafx.scene.control.Control campo, String mensaje) {
		campo.getStyleClass().add("campo-error");
		campo.requestFocus();
		mostrarAviso(mensaje);
	}

	/**
	 * Quita las marcas de error del formulario de creación.
	 */
	private void limpiarErrores() {
		txtNumero.getStyleClass().remove("campo-error");
		datepk1.getStyleClass().remove("campo-error");
		cmbTitular.getStyleClass().remove("campo-error");
		cmbEstado.getStyleClass().remove("campo-error");
	}

	/**
	 * Deja el formulario de creación listo para registrar otra tarjeta.
	 */
	private void limpiarFormCrear() {
		limpiarErrores();
		txtNumero.clear();
		datepk1.setValue(LocalDate.now());
		cmbTitular.setValue(null);
		cmbEstado.setValue(null);
		radio1.setSelected(true);
	}

	/**
	 * Muestra un mensaje de éxito en la barra inferior.
	 *
	 * @param mensaje texto a mostrar
	 */
	private void informar(String mensaje) {
		lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-aviso", "mensaje-error");
		lblMensaje.getStyleClass().add(mensaje.startsWith("✔") ? "mensaje-ok" : "ayuda");
		lblMensaje.setText(mensaje);
	}

	/**
	 * Muestra una advertencia (validación o regla de negocio).
	 *
	 * @param mensaje texto a mostrar
	 */
	private void mostrarAviso(String mensaje) {
		lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-error");
		lblMensaje.getStyleClass().add("mensaje-aviso");
		lblMensaje.setText("⚠ " + mensaje);
		dialogo(AlertType.WARNING, "Atención", mensaje);
	}

	/**
	 * Muestra un error inesperado (por ejemplo, de base de datos).
	 *
	 * @param mensaje texto a mostrar
	 */
	private void mostrarError(String mensaje) {
		lblMensaje.getStyleClass().removeAll("mensaje-ok", "mensaje-aviso");
		lblMensaje.getStyleClass().add("mensaje-error");
		lblMensaje.setText("✖ " + mensaje);
		dialogo(AlertType.ERROR, "Error", mensaje);
	}

	/**
	 * Abre una ventana de mensaje con los estilos de la aplicación.
	 *
	 * @param tipoAlerta tipo de alerta
	 * @param titulo     título de la ventana
	 * @param mensaje    texto a mostrar
	 */
	private void dialogo(AlertType tipoAlerta, String titulo, String mensaje) {
		Alert alert = new Alert(tipoAlerta);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.getDialogPane().getStylesheets()
				.add(getClass().getResource("/co/edu/poli/parcial2/estilos.css").toExternalForm());
		alert.showAndWait();
	}
}
