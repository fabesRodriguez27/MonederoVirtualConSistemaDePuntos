package monedero.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.SessionManager;
import monedero.modelos.Transaccion;
import monedero.servicios.facade.PanelControlFacade;
import monedero.utilidades.FormatoCedula;
import monedero.utilidades.PathsFxml;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PanelControlController {
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableView<Cliente> tablaClientesPorPuntos;
    @FXML private TableView<Transaccion> tablaHistorialTransacciones;
    @FXML private ComboBox<Monedero> comboMonederosCliente;
    @FXML private TableColumn<Cliente, String> colIdCliente;
    @FXML private TableColumn<Cliente, String> colNombreCliente;
    @FXML private TableColumn<Cliente, String> colRangoCliente;
    @FXML private TableColumn<Cliente, String> colIdClientePuntos;
    @FXML private TableColumn<Cliente, String> colNombreClientePuntos;
    @FXML private TableColumn<Cliente, String> colRangoClientePuntos;
    @FXML private TableColumn<Cliente, Number> colPuntosCliente;
    @FXML private TableColumn<Transaccion, String> colIdTransaccion;
    @FXML private TableColumn<Transaccion, String> colClienteTransaccion;
    @FXML private TableColumn<Transaccion, String> colTipoTransaccion;
    @FXML private TableColumn<Transaccion, String> colMontoTransaccion;
    @FXML private TableColumn<Transaccion, String> colFechaTransaccion;
    @FXML private TableColumn<Transaccion, String> colOrigenTransaccion;
    @FXML private TableColumn<Transaccion, String> colDestinoTransaccion;
    @FXML private Button btnEliminarCliente;

    private final PanelControlFacade panelControlFacade = new PanelControlFacade();
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        configurarColumnas();
        tablaHistorialTransacciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        btnEliminarCliente.disableProperty().bind(tablaClientes.getSelectionModel().selectedItemProperty().isNull());
        for (TableColumn<Transaccion, ?> columna : tablaHistorialTransacciones.getColumns()) {
            configurarAyuda(columna);
        }
        comboMonederosCliente.setDisable(true);
        comboMonederosCliente.setCellFactory(lista -> crearCeldaMonedero());
        comboMonederosCliente.setButtonCell(crearCeldaMonedero());
        tablaClientes.getSelectionModel().selectedItemProperty()
                .addListener((observable, anterior, actual) -> cargarMonederos(actual));
        cargarDatos();
    }

    private void configurarColumnas() {
        colIdCliente.setCellValueFactory(f -> texto(FormatoCedula.mostrar(f.getValue().getIdCliente())));
        colNombreCliente.setCellValueFactory(f -> texto(f.getValue().getNombre()));
        colRangoCliente.setCellValueFactory(f -> texto(f.getValue().getRango()));
        colIdClientePuntos.setCellValueFactory(f -> texto(FormatoCedula.mostrar(f.getValue().getIdCliente())));
        colNombreClientePuntos.setCellValueFactory(f -> texto(f.getValue().getNombre()));
        colPuntosCliente.setCellValueFactory(f ->
                new SimpleIntegerProperty(f.getValue().getPuntos()));
        colRangoClientePuntos.setCellValueFactory(f -> texto(f.getValue().getRango()));
        colIdTransaccion.setCellValueFactory(f -> texto(f.getValue().getId()));
        colClienteTransaccion.setCellValueFactory(f -> texto(FormatoCedula.mostrar(f.getValue().getIdCliente())));
        colTipoTransaccion.setCellValueFactory(f -> texto(f.getValue().getTipoTransaccion()));
        colMontoTransaccion.setCellValueFactory(f -> texto(dinero(f.getValue().getMonto())));
        colFechaTransaccion.setCellValueFactory(f -> texto(f.getValue().getFecha() == null
                ? null : FECHA.format(f.getValue().getFecha())));
        colOrigenTransaccion.setCellValueFactory(f -> texto(describirMonedero(f.getValue().getOrigen())));
        colDestinoTransaccion.setCellValueFactory(f -> texto(describirMonedero(f.getValue().getDestino())));
    }

    public void cargarDatos() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        tablaClientes.setItems(observable(panelControlFacade.obtenerClientes()));
        tablaClientesPorPuntos.setItems(observable(panelControlFacade.obtenerClientesPorPuntos()));
        tablaHistorialTransacciones.setItems(observable(panelControlFacade.obtenerHistorialPorMonto()));
        tablaClientes.refresh();
        tablaClientesPorPuntos.refresh();
        tablaHistorialTransacciones.refresh();
        if (seleccionado != null) {
            for (Cliente cliente : tablaClientes.getItems()) {
                if (cliente.getIdCliente().equals(seleccionado.getIdCliente())) {
                    tablaClientes.getSelectionModel().select(cliente);
                    cargarMonederos(cliente);
                    return;
                }
            }
        }
        cargarMonederos(null);
    }

    private void cargarMonederos(Cliente cliente) {
        comboMonederosCliente.getSelectionModel().clearSelection();
        comboMonederosCliente.getItems().clear();
        comboMonederosCliente.setDisable(true);
        comboMonederosCliente.setPromptText("Selecciona un cliente");
        if (cliente == null) return;
        comboMonederosCliente.setItems(observable(panelControlFacade.obtenerMonederos(cliente.getIdCliente())));
        boolean vacio = comboMonederosCliente.getItems().isEmpty();
        comboMonederosCliente.setDisable(vacio);
        comboMonederosCliente.setPromptText(vacio ? "Sin monederos" : "Selecciona un monedero");
        if (!vacio) comboMonederosCliente.getSelectionModel().selectFirst();
    }

    private ListCell<Monedero> crearCeldaMonedero() {
        return new ListCell<>() {
            @Override protected void updateItem(Monedero monedero, boolean empty) {
                super.updateItem(monedero, empty);
                setText(empty || monedero == null ? null
                        : describirMonedero(monedero) + " · " + dinero(monedero.getSaldo()));
            }
        };
    }

    private String describirMonedero(Monedero monedero) {
        return monedero == null ? "—" : monedero.getNombre() + " (" + monedero.getId() + ")";
    }

    private String dinero(double valor) {
        return String.format(Locale.forLanguageTag("es-CO"), "$%,.2f", valor);
    }

    private SimpleStringProperty texto(Object valor) {
        return new SimpleStringProperty(valor == null ? "—" : valor.toString());
    }

    public static String normalizar(String id) {
        return id != null && id.matches("[0-9]{1,3}(\\.[0-9]{3})+")
                ? id.replace(".", "") : id;
    }

    public static String mostrar(String id) {
        if (id == null) return "—";
        return normalizar(id).replaceAll("(?<=\\d)(?=(\\d{3})+$)", ".");
    }

    private <T> ObservableList<T> observable(Iterable<T> elementos) {
        ObservableList<T> resultado = FXCollections.observableArrayList();
        for (T elemento : elementos) resultado.add(elemento);
        return resultado;
    }

    private <T> void configurarAyuda(TableColumn<Transaccion,T> columna) {
        columna.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(T dato, boolean vacia) {
                super.updateItem(dato, vacia);
                String valor = vacia || dato == null ? null : dato.toString();
                setText(valor);
                setTooltip(valor == null ? null : new Tooltip(valor));
            }
        });
    }

    @FXML private void eliminarSeleccionado() {
        Cliente cliente = tablaClientes.getSelectionModel().getSelectedItem();
        if (cliente == null) return;
        ButtonType si = new ButtonType("Sí, eliminar", ButtonBar.ButtonData.YES);
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION,
                "Se eliminarán su acceso, sus monederos y sus puntos. El historial de transacciones global se conservará.",
                si, ButtonType.CANCEL);
        aviso.initOwner(tablaClientes.getScene().getWindow());
        aviso.setTitle("Eliminar cliente");
        aviso.setHeaderText("¿Eliminar a " + cliente.getNombre() + " (" +
                FormatoCedula.mostrar(cliente.getIdCliente()) + ")?");
        if (aviso.showAndWait().orElse(ButtonType.CANCEL) == si) {
            if(panelControlFacade.eliminarCliente(cliente.getIdCliente())){
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Eliminación exitosa");
                alerta.setHeaderText(null);
                alerta.setContentText("Usuario eliminado exitosamente: " + cliente.getNombre() +"\n"
                + "Saldo a devolver: " +dinero(cliente.getSaldoTotal()));
                alerta.showAndWait();
            }else{
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("No se pudo eliminar el usuario");
                alerta.setHeaderText(null);
                alerta.setContentText("No debe tener transacciones programadas");
                alerta.showAndWait();
            }
            cargarDatos();
        }
    }

    @FXML private void abrirInformes() {
        Alert aviso = new Alert(Alert.AlertType.INFORMATION);
        aviso.initOwner(tablaClientes.getScene().getWindow());
        aviso.setTitle("Informes avanzados");
        aviso.setHeaderText("Informes avanzados");
        aviso.setContentText("Esta función está pendiente de implementación.");
        aviso.showAndWait();
    }

    @FXML private void abrirTarifas() {
        Dialog<Void> ventana = new Dialog<>();
        ventana.initOwner(tablaClientes.getScene().getWindow());
        ventana.setTitle("Gestión de tarifas");
        ventana.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        javafx.scene.layout.GridPane filas = new javafx.scene.layout.GridPane();
        filas.setHgap(14);
        filas.setVgap(12);
        Label retiroActual = new Label(dinero(panelControlFacade.tarifaRetiro()));
        Label transferenciaActual = new Label(dinero(panelControlFacade.tarifaTransferencia()));
        TextField retiro = new TextField();
        TextField transferencia = new TextField();
        retiro.setPromptText("Ej. 2700");
        transferencia.setPromptText("Ej. 5800");
        filas.addRow(0, new Label("Operación"), new Label("Tarifa actual"), new Label("Nueva tarifa"));
        filas.addRow(1, new Label("Retiro"), retiroActual, retiro);
        filas.addRow(2, new Label("Transferencia"), transferenciaActual, transferencia);
        Button actualizar = new Button("Actualizar");
        filas.add(actualizar, 3, 2);
        Label mensaje = new Label();
        filas.add(mensaje, 0, 3, 4, 1);
        actualizar.setOnAction(evento -> {
            try {
                float a = leerTarifa(retiro.getText());
                float b = leerTarifa(transferencia.getText());
                if (!panelControlFacade.actualizarTarifas(a, b)) throw new NumberFormatException();
                retiroActual.setText(dinero(panelControlFacade.tarifaRetiro()));
                transferenciaActual.setText(dinero(panelControlFacade.tarifaTransferencia()));
                mensaje.setText("Tarifas actualizadas.");
            } catch (NumberFormatException ex) {
                mensaje.setText("Ingresa dos montos positivos o cero, sin puntos de miles.");
            }
        });
        ventana.getDialogPane().setContent(filas);
        ventana.showAndWait();
    }

    private float leerTarifa(String entrada) {
        String valor = entrada.trim();
        if (!valor.matches("[0-9]+(,[0-9]{1,2})?")) throw new NumberFormatException();
        return Float.parseFloat(valor.replace(',', '.'));
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        SessionManager.getInstancia().cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(PathsFxml.PATH_LOGIN)
            );
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
