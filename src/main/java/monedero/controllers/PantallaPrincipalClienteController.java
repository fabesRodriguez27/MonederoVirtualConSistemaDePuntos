package monedero.controllers;

import java.io.IOException;
import java.util.Iterator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import monedero.application.SessionManager;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    private Cliente cliente;

    /** JavaFX lo llama solo cuando se abre la pantalla. */
    @FXML
    public void initialize() {
        configurarTablaMonederos();
        cliente = SessionManager.getInstancia().getClienteLogueado();
        if (cliente == null) {
            nombreClientelbl.setText("No hay una sesión iniciada");
            return;
        }
        cargarInfoCliente();
        cargarMonederos();
        //Metemos Demás Info
    }

    private void cargarInfoCliente() {
        nombreClientelbl.setText(cliente.getNombre());
        idClientelbl.setText(cliente.getIdCliente());
        puntosAcumuladoslbl.setText(String.valueOf(cliente.getPuntos()));
        rangoClientelbl.setText(String.valueOf(cliente.getRango()));
        saldoClientelbl.setText(dinero(cliente.getSaldoTotal()));
    }

    //Establecemos dentro de cada columna de la tabla de monederos lo que debe mostrar
    private void configurarTablaMonederos() {
        colIdMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(String.valueOf(fila.getValue().getId())));
        colNombreMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(fila.getValue().getNombre()));
        colSaldoMonedero.setCellValueFactory(fila ->
                new SimpleStringProperty(dinero(fila.getValue().getSaldo())));
    }

    //Copiamos los datos de la lista que tiene el cliente a la lista que entiende la tabla
    private void cargarMonederos() {
        ObservableList<Monedero> datos = FXCollections.observableArrayList();

        Iterator<Monedero> it = cliente.getMonederos().iterator();
        while (it.hasNext()) {
            datos.add(it.next());
        }

        tablaMonederos.setItems(datos);
    }

    private String dinero(double valor) {
        return String.format("$%,.2f", valor);
    }

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

    //Pendientes-------
    @FXML
    void realizarTransaccion(ActionEvent event) {
        // pendiente
    }

    @FXML
    void revertirUltimaTransaccion(ActionEvent event) {
        // pendiente
    }

    @FXML
    void verBeneficios(ActionEvent event) {
        // pendiente
    }

    @FXML
    void verNotificaciones(ActionEvent event) {
        // pendiente
    }

    @FXML
    void verHistorial(ActionEvent event) {
        // pendiente
    }
}
