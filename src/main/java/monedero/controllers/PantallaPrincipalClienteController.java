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
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import monedero.application.SessionManager;
import monedero.application.Sistema;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.servicios.TransaccionService;
import monedero.utilidades.Categoria;
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

    private TransaccionService transaccionService;


    // INICIALIZAR PANTALLA

    @FXML
    public void initialize() {

        configurarTablaMonederos();

        cliente = SessionManager.getInstancia().getClienteLogueado();

        if (cliente == null) {
            nombreClientelbl.setText("No hay una sesión iniciada");
            return;
        }

        // Obtener el servicio de transacciones desde Sistema
        transaccionService = Sistema.getInstancia().getTransaccionService();

        cargarInfoCliente();
        cargarMonederos();
    }


    // CARGAR INFORMACIÓN DEL CLIENTE

    private void cargarInfoCliente() {

        nombreClientelbl.setText(cliente.getNombre());

        idClientelbl.setText(cliente.getIdCliente());

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


    // CONFIGURAR TABLA DE MONEDEROS

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


    // CARGAR MONEDEROS DEL CLIENTE

    private void cargarMonederos() {

        ObservableList<Monedero> datos =
                FXCollections.observableArrayList();

        Iterator<Monedero> it = cliente.getMonederos().iterator();

        while (it.hasNext()) {
            datos.add(it.next());
        }

        tablaMonederos.setItems(datos);
    }


    // FORMATEAR DINERO

    private String dinero(double valor) {
        return String.format("$%,.2f", valor);
    }


    // REALIZAR TRANSACCIÓN

    @FXML
    void realizarTransaccion(ActionEvent event) {

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "Depositar",
                Arrays.asList("Depositar", "Retirar", "Transferir")
        );

        dialog.setTitle("Realizar transacción");
        dialog.setHeaderText("Seleccione el tipo de transacción");
        dialog.setContentText("Operación:");

        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(opcion -> {

            switch (opcion) {

                case "Depositar":
                    realizarDeposito();
                    break;

                case "Retirar":
                    realizarRetiro();
                    break;

                case "Transferir":
                    realizarTransferencia();
                    break;
            }
        });
    }


    // SELECCIONAR MONEDERO

    private Monedero seleccionarMonedero(
            String mensaje,
            Monedero monederoExcluir) {

        LinkedList<Monedero> monederos = new LinkedList<>();

        Iterator<Monedero> it = cliente.getMonederos().iterator();

        while (it.hasNext()) {

            Monedero monedero = it.next();

            // Excluir el monedero de origen cuando se está transfiriendo
            if (monedero != monederoExcluir) {
                monederos.add(monedero);
            }
        }

        if (monederos.isEmpty()) {
            mostrarMensaje("No hay monederos disponibles.");
            return null;
        }

        ChoiceDialog<Monedero> dialog =
                new ChoiceDialog<>(monederos.get(0), monederos);

        dialog.setTitle("Seleccionar monedero");
        dialog.setHeaderText(mensaje);
        dialog.setContentText("Monedero:");

        Optional<Monedero> resultado = dialog.showAndWait();

        return resultado.orElse(null);
    }


    // SOLICITAR MONTO

    private Double solicitarMonto() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Monto de la transacción");
        dialog.setHeaderText("Ingrese el monto de la operación");
        dialog.setContentText("Monto:");

        Optional<String> resultado = dialog.showAndWait();

        if (!resultado.isPresent()) {
            return null;
        }

        try {

            double monto = Double.parseDouble(
                    resultado.get().trim().replace(',', '.')
            );

            if (!Double.isFinite(monto) || monto <= 0) {
                mostrarMensaje("El monto debe ser mayor que cero.");
                return null;
            }

            return monto;

        } catch (NumberFormatException e) {

            mostrarMensaje("Ingrese un monto numérico válido.");
            return null;
        }
    }


    // REALIZAR DEPÓSITO

    private void realizarDeposito() {

        Monedero monedero = seleccionarMonedero(
                "Seleccione el monedero donde desea depositar",
                null
        );

        if (monedero == null) {
            return;
        }

        Double monto = solicitarMonto();

        if (monto == null) {
            return;
        }

        try {

            transaccionService.depositar(cliente.getIdCliente(),monedero.getId(), monto);

            mostrarMensaje("Depósito realizado correctamente.");

            cargarMonederos();
            cargarInfoCliente();

        } catch (Exception e) {

            mostrarMensaje("Error al realizar el depósito: " + e.getMessage());
        }
    }


    // REALIZAR RETIRO

    private void realizarRetiro() {

        Monedero monedero = seleccionarMonedero(
                "Seleccione el monedero del cual desea retirar",
                null
        );

        if (monedero == null) {
            return;
        }

        Double monto = solicitarMonto();

        if (monto == null) {
            return;
        }

        try {

            transaccionService.retirar(cliente.getIdCliente(), monedero.getId(), monto);

            mostrarMensaje("Retiro realizado correctamente.");

            cargarMonederos();
            cargarInfoCliente();

        } catch (Exception e) {

            mostrarMensaje("Error al realizar el retiro: " + e.getMessage());
        }
    }


    // REALIZAR TRANSFERENCIA

    private void realizarTransferencia() {

        // Seleccionar monedero de origen
        Monedero origen = seleccionarMonedero(
                "Seleccione el monedero desde el cual desea transferir",
                null
        );

        if (origen == null) {
            return;
        }

        // Seleccionar monedero de destino, excluyendo el de origen
        Monedero destino = seleccionarMonedero(
                "Seleccione el monedero al cual desea transferir",
                origen
        );

        if (destino == null) {
            return;
        }

        Double monto = solicitarMonto();

        if (monto == null) {
            return;
        }

        try {

            // llamada de metodo de transferir con sus respectivos parametros

            mostrarMensaje("Transferencia realizada correctamente.");

            cargarMonederos();
            cargarInfoCliente();

        } catch (Exception e) {

            mostrarMensaje("Error al realizar la transferencia: " + e.getMessage());
        }
    }


    // MOSTRAR MENSAJES

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


    // MÉTODOS PENDIENTES

    @FXML
    void revertirUltimaTransaccion(ActionEvent event) {
        // Pendiente
    }

    @FXML
    void verBeneficios(ActionEvent event) {
        // Pendiente
    }

    @FXML
    void verNotificaciones(ActionEvent event) {
        // Pendiente
    }

    @FXML
    void verHistorial(ActionEvent event) {
        // Pendiente
    }
}