package monedero.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import monedero.modelos.Cliente;
import monedero.modelos.Notificacion;
import monedero.modelos.SessionManager;
import monedero.servicios.facade.NotificacionFacade;

import java.time.format.DateTimeFormatter;

public class NotificacionesController {

    @FXML private TableView<Notificacion> tablaNotificaciones;
    @FXML private TableColumn<Notificacion, String> colFecha;
    @FXML private TableColumn<Notificacion, String> colTipo;
    @FXML private TableColumn<Notificacion, String> colMensaje;
    @FXML private TableColumn<Notificacion, String> colEstado;
    @FXML private Label lblNoLeidas;

    private final NotificacionFacade fachada = new NotificacionFacade();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Cliente cliente;

    @FXML
    private void initialize() {
        cliente = SessionManager.getInstancia().getClienteLogueado();

        //Le digo qué va en cada  campo
        colFecha.setCellValueFactory(f -> new SimpleStringProperty(FECHA.format(f.getValue().getFecha())));
        colTipo.setCellValueFactory(f -> new SimpleStringProperty(String.valueOf(f.getValue().getTipoNotificacion())));
        colMensaje.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getMensaje()));
        colEstado.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().isLeida() ? "Leída" : "Nueva"));
        tablaNotificaciones.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        cargar();
    }

    private void cargar() {
        if (cliente == null) return;
        ObservableList<Notificacion> datos = FXCollections.observableArrayList();
        for (Notificacion n : fachada.obtenerNotificaciones(cliente.getIdCliente())) {
            datos.add(n);
        }
        tablaNotificaciones.setItems(datos);
        tablaNotificaciones.refresh();
        lblNoLeidas.setText("Sin leer: " + fachada.contarNoLeidas(cliente.getIdCliente()));
    }

    @FXML
    private void marcarLeidas() {
        if (cliente == null) return;
        fachada.marcarTodasLeidas(cliente.getIdCliente());
        cargar();
    }

    @FXML
    private void volver(ActionEvent evento) {
        ((Stage) ((Node) evento.getSource()).getScene().getWindow()).close();
    }
}