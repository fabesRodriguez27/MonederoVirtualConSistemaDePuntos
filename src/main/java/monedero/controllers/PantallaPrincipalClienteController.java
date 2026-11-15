package monedero.controllers;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import monedero.modelos.SessionManager;
import monedero.modelos.Cliente;
import monedero.modelos.Transaccion;
import monedero.modelos.TransaccionProgramada;
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

    @FXML
    private TableView<Transaccion> tablaHistorialCliente;

    @FXML
    private TableColumn<Transaccion, String> id;

    @FXML
    private TableColumn<Transaccion, String> tipo;

    @FXML
    private TableColumn<Transaccion, String> origen;

    @FXML
    private TableColumn<Transaccion, String> destino;

    @FXML
    private TableColumn<Transaccion, String> comision;

    @FXML
    private TableColumn<Transaccion, String> puntos;

    @FXML
    private TableColumn<Transaccion, String> estado;

    // Tabla de transacciones programadas
    @FXML
    private TableView<TransaccionProgramada> tablaProgramadas;

    @FXML
    private TableColumn<TransaccionProgramada, String> colProximaEjecucion;

    @FXML
    private TableColumn<TransaccionProgramada, String> colTipoProgramada;

    @FXML
    private TableColumn<TransaccionProgramada, String> colMontoProgramado;

    @FXML
    private TableColumn<TransaccionProgramada, String> colFrecuenciaProgramada;

    @FXML
    private TableColumn<TransaccionProgramada, String> colDestinoProgramado;

    private final ClienteFacade clienteFacade = new ClienteFacade();

    private Cliente cliente;

    private Timeline actualizador;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Inicializar pantalla
    @FXML
    public void initialize() {

        configurarTablaMonederos();
        configurarTablaProgramadas();

        cliente = SessionManager.getInstancia().getClienteLogueado();

        if (cliente == null) {
            nombreClientelbl.setText("No Hay una Sesión Iniciada");
            return;
        }

        cargarHistorial();
        cargarInfoCliente();
        cargarMonederos();
        cargarProgramadas();

        iniciarActualizacionAutomatica();

        tablaMonederos.sceneProperty().addListener(
                (observable, escenaAnterior, escenaNueva) -> {

                    if (escenaNueva == null) {
                        detenerActualizacionAutomatica();
                    }
                }
        );
    }

    // Cargar información del cliente
    private void cargarInfoCliente() {

        nombreClientelbl.setText(cliente.getNombre());

        idClientelbl.setText(
                FormatoCedula.mostrar(cliente.getIdCliente())
        );

        puntosAcumuladoslbl.setText(
                String.valueOf(cliente.getPuntos())
        );

        rangoClientelbl.setText(
                String.valueOf(cliente.getRango())
        );

        saldoClientelbl.setText(
                dinero(cliente.getSaldoTotal())
        );
    }

    // Configurar tabla de monederos
    private void configurarTablaMonederos() {

        colIdMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        String.valueOf(fila.getValue().getId())
                )
        );

        colNombreMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        fila.getValue().getNombre()
                )
        );

        colSaldoMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        dinero(fila.getValue().getSaldo())
                )
        );
    }

    // Cargar monederos del cliente
    private void cargarMonederos() {

        ObservableList<Monedero> datos =
                FXCollections.observableArrayList();

        Iterator<Monedero> it =
                cliente.getMonederos().iterator();

        while (it.hasNext()) {
            datos.add(it.next());
        }

        tablaMonederos.setItems(datos);
        tablaMonederos.refresh();
    }

    // Cargar historial de transacciones
    private void cargarHistorial() {

        configurarColumna(
                id,
                t -> t.getId()
        );

        configurarColumna(
                tipo,
                t -> t.getTipoTransaccion().toString()
        );

        configurarColumna(
                origen,
                t -> idMonedero(t.getOrigen())
        );

        configurarColumna(
                destino,
                t -> idMonedero(t.getDestino())
        );

        configurarColumna(
                comision,
                t -> dinero(t.getComision())
        );

        configurarColumna(
                puntos,
                t -> String.valueOf(t.getPuntos())
        );

        configurarColumna(
                estado,
                t -> t.isRevertida() ? "Revertida" : "OK"
        );

        ObservableList<Transaccion> historial =
                FXCollections.observableArrayList();

        for (Transaccion transaccion :
                cliente.getHistorialTransacciones()) {

            historial.add(transaccion);
        }

        historial.sort(
                Comparator.comparing(Transaccion::getFecha).reversed()
        );

        tablaHistorialCliente.setItems(historial);

        tablaHistorialCliente.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    // Configurar columnas de transacciones programadas
    private void configurarTablaProgramadas() {

        colProximaEjecucion.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        fila.getValue()
                                .getProximaEjecucion()
                                .format(formatoFecha)
                )
        );

        colTipoProgramada.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        fila.getValue()
                                .getTipoTransaccion()
                                .toString()
                )
        );

        colMontoProgramado.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        dinero(fila.getValue().getMonto())
                )
        );

        colFrecuenciaProgramada.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        fila.getValue()
                                .getFrecuencia()
                                .toString()
                )
        );

        colDestinoProgramado.setCellValueFactory(fila -> {

            Monedero destino = fila.getValue().getDestino();

            String informacion = destino == null
                    ? "—"
                    : destino.getNombre() + " (" + destino.getId() + ")";

            return new SimpleStringProperty(informacion);
        });

        tablaProgramadas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    // Cargar transacciones programadas pendientes
    private void cargarProgramadas() {

        ObservableList<TransaccionProgramada> programadas =
                FXCollections.observableArrayList();

        for (TransaccionProgramada transaccion :
                cliente.getTransaccionesProgramadas()) {

            if (transaccion.isEstado()) {
                programadas.add(transaccion);
            }
        }

        programadas.sort(
                Comparator.comparing(
                        TransaccionProgramada::getProximaEjecucion
                )
        );

        tablaProgramadas.setItems(programadas);
        tablaProgramadas.refresh();
    }

    // Actualizar información cada 5 segundos
    private void iniciarActualizacionAutomatica() {
        if (actualizador != null) {
            actualizador.stop();
        }
        actualizador = new Timeline(new KeyFrame(Duration.seconds(5), event -> refrescar()));
        actualizador.setCycleCount(Timeline.INDEFINITE);
        actualizador.play();
    }

    // Detener actualización automática
    private void detenerActualizacionAutomatica() {
        if (actualizador != null) {
            actualizador.stop();
            actualizador = null;
        }
    }

    // Refrescar información de la pantalla
    private void refrescar() {
        if (cliente == null) {
            return;
        }
        cargarInfoCliente();
        cargarMonederos();
        cargarHistorial();
        cargarProgramadas();
        tablaHistorialCliente.refresh();
        tablaProgramadas.refresh();
    }

    // Formatear dinero
    private String dinero(double valor) {

        return String.format("$%,.2f", valor);
    }

    // Obtener ID del monedero
    private String idMonedero(Monedero monedero) {

        return monedero == null
                ? "—"
                : monedero.getId();
    }

    // Configurar columnas de transacciones
    private void configurarColumna(
            TableColumn<Transaccion, String> columna,
            Function<Transaccion, String> valor) {

        columna.setCellValueFactory(fila ->
                new SimpleStringProperty(
                        valor.apply(fila.getValue())
                )
        );
    }

    // Mostrar mensaje
    private void mostrarMensaje(String mensaje) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Transacciones");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        alert.showAndWait();
    }

    // Abrir gestión de monederos y programaciones
    @FXML
    private void abrirGestion() throws IOException {

        abrirVentana(
                PathsFxml.PATH_GESTION_MONEDEROS,
                "Gestión de Monederos y Programadas"
        );

        refrescar();
    }

    // Abrir ventana de transacciones
    @FXML
    private void abrirTransacciones() throws IOException {

        abrirVentana(PathsFxml.PATH_REALIZAR_TRANSACCION, "Realizar Transacción");
        refrescar();
    }

    // Abrir ventana de notificaciones
    @FXML
    private void verNotificaciones() throws IOException {

        abrirVentana(PathsFxml.PATH_NOTIFICACIONES_CLIENTE, "Centro de Notificaciones");
        refrescar();
    }

    // Abrir ventana
    private void abrirVentana(String recurso, String titulo) throws IOException {

        Parent contenido = FXMLLoader.load(getClass().getResource(recurso));
        Stage ventana = new Stage();
        ventana.initOwner(tablaMonederos.getScene().getWindow());
        ventana.initModality(javafx.stage.Modality.WINDOW_MODAL);
        ventana.setTitle(titulo);
        ventana.setScene(new Scene(contenido));
        ventana.showAndWait();
    }

    // Cerrar sesión
    @FXML
    void cerrarSesion(ActionEvent event) {

        detenerActualizacionAutomatica();

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

    // Abrir ventana de notificaciones
    @FXML
    private void verBeneficios() throws IOException {
        abrirVentana(PathsFxml.PATH_BENEFICIOS_CLIENTE, "Mis Beneficios");
        refrescar();
    }

    // Ver historial
    @FXML
    void verHistorial(ActionEvent event) {
        // Pendiente
    }
}