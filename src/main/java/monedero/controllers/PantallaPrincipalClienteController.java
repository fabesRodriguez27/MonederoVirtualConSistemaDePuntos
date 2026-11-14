package monedero.controllers;
import java.io.IOException;
import java.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import monedero.modelos.SessionManager;
import monedero.modelos.Cliente;
import monedero.modelos.Transaccion;
import java.util.function.Function;
import monedero.modelos.Monedero;
import monedero.servicios.facade.ClienteFacade;
import monedero.utilidades.FormatoCedula;
import monedero.utilidades.PathsFxml;

public class PantallaPrincipalClienteController {

    @FXML
    private Label idClientelbl;

    @FXML
    private Label nombreClientelbl;

    @FXML
    private Label puntosAcumuladoslbl;

    @FXML
    private Label rangoClientelbl;

    @FXML
    private Label saldoClientelbl;

    @FXML
    private TableView<Monedero> tablaMonederos;

    @FXML
    private TableColumn<Monedero, String> colIdMonedero;

    @FXML
    private TableColumn<Monedero, String> colNombreMonedero;

    @FXML
    private TableColumn<Monedero, String> colSaldoMonedero;

    @FXML private TableView<Transaccion> tablaHistorialCliente;
    @FXML private TableColumn<Transaccion, String> id;
    @FXML private TableColumn<Transaccion, String> tipo;
    @FXML private TableColumn<Transaccion, String> origen;
    @FXML private TableColumn<Transaccion, String> destino;
    @FXML private TableColumn<Transaccion, String> comision;
    @FXML private TableColumn<Transaccion, String> puntos;
    @FXML private TableColumn<Transaccion, String> estado;

    private final ClienteFacade clienteFacade = new ClienteFacade();

    private Cliente cliente;

    // INICIALIZAR PANTALLA

    @FXML
    public void initialize() {
        configurarTablaMonederos();
        cliente = SessionManager.getInstancia().getClienteLogueado();
        if (cliente == null) {
            nombreClientelbl.setText("No Hay una Sesión Iniciada");
            return;
        }
        cargarHistorial();
        cargarInfoCliente();
        cargarMonederos();
    }


    // CARGAR INFORMACIÓN DEL CLIENTE
    private void cargarInfoCliente() {
        nombreClientelbl.setText(cliente.getNombre());
        idClientelbl.setText(FormatoCedula.mostrar(cliente.getIdCliente()));
        puntosAcumuladoslbl.setText(String.valueOf(cliente.getPuntos()));
        rangoClientelbl.setText(String.valueOf(cliente.getRango()));
        saldoClientelbl.setText(dinero(cliente.getSaldoTotal()));
    }

    // CONFIGURAR TABLA DE MONEDEROS
    private void configurarTablaMonederos() {
        colIdMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(String.valueOf(fila.getValue().getId())));

        colNombreMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(fila.getValue().getNombre()));

        colSaldoMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(dinero(fila.getValue().getSaldo())));
    }

    // CARGAR MONEDEROS DEL CLIENTE
    private void cargarMonederos() {
        ObservableList<Monedero> datos = FXCollections.observableArrayList();
        Iterator<Monedero> it = cliente.getMonederos().iterator();
        while (it.hasNext()) {
            datos.add(it.next());
        }
        tablaMonederos.setItems(datos);
        tablaMonederos.refresh();
    }

    private void cargarHistorial() {
        configurarColumna(id, t -> t.getId());
        configurarColumna(tipo, t -> t.getTipoTransaccion().toString());
        configurarColumna(origen, t -> idMonedero(t.getOrigen()));
        configurarColumna(destino, t -> idMonedero(t.getDestino()));
        configurarColumna(comision, t -> dinero(t.getComision()));
        configurarColumna(puntos, t -> String.valueOf(t.getPuntos()));
        configurarColumna(estado, t -> t.isRevertida() ? "Revertida" : "OK");
        ObservableList<Transaccion> historial = FXCollections.observableArrayList();
        for (Transaccion transaccion : cliente.getHistorialTransacciones()) {
            historial.add(transaccion);
        }
        historial.sort(Comparator.comparing(Transaccion::getFecha).reversed());
        tablaHistorialCliente.setItems(historial);
        tablaHistorialCliente.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // FORMATEAR DINERO
    private String dinero(double valor) {
        return String.format("$%,.2f", valor);
    }

    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transacciones");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // CERRAR SESIÓN
    @FXML
    void cerrarSesion(ActionEvent event) {
        SessionManager.getInstancia().cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(PathsFxml.PATH_LOGIN));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void verBeneficios(ActionEvent event) {
        // Pendiente
    }

    @FXML private void verNotificaciones() throws IOException {
        abrirVentana(PathsFxml.PATH_NOTIFICACIONES_CLIENTE, "Centro de Notificaciones");
        refrescar();
    }

    @FXML
    void verHistorial(ActionEvent event) {
        // Pendiente
    }

    private String idMonedero(Monedero monedero) {
        return monedero == null ? "—" : monedero.getId();
    }

    private void configurarColumna(TableColumn<Transaccion, String> columna,
                                   Function<Transaccion, String> valor) {
        columna.setCellValueFactory(fila -> new SimpleStringProperty(valor.apply(fila.getValue())));
    }

    @FXML private void abrirGestion() throws IOException {
        abrirVentana(PathsFxml.PATH_GESTION_MONEDEROS, "Gestión de Monederos y Programadas");
        refrescar();
    }


    @FXML private void abrirTransacciones() throws IOException {
        abrirVentana(PathsFxml.PATH_REALIZAR_TRANSACCION, "Realizar Transacción");
        refrescar();
    }

    private void refrescar() {
        cargarInfoCliente();   // puntos, rango y saldo
        cargarMonederos();
        cargarHistorial();
        tablaHistorialCliente.refresh();
    }

    private void abrirVentana(String recurso, String titulo) throws IOException {
        Parent contenido = FXMLLoader.load(getClass().getResource(recurso));
        Stage ventana = new Stage();
        ventana.initOwner(tablaMonederos.getScene().getWindow());
        ventana.initModality(javafx.stage.Modality.WINDOW_MODAL);
        ventana.setTitle(titulo);
        ventana.setScene(new Scene(contenido));
        ventana.showAndWait();
    }
}
