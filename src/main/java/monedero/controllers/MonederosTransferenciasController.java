package monedero.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.SessionManager;
import monedero.servicios.facade.ClienteFacade;

public class MonederosTransferenciasController {
    @FXML private TextField nombre;
    @FXML private TextField saldo;
    @FXML private ComboBox<Monedero> seleccion;
    @FXML private Button eliminar;
    @FXML private Label mensaje;
    private final ClienteFacade fachada = new ClienteFacade();
    private Cliente cliente;

    @FXML private void initialize() {
        cliente = SessionManager.getInstancia().getClienteLogueado();
        seleccion.setConverter(new StringConverter<>() {
            public String toString(Monedero m) {
                return m == null ? "" : m.getNombre() + " (" + m.getId() + ")";
            }
            public Monedero fromString(String texto) { return null; }
        });
        eliminar.disableProperty().bind(seleccion.valueProperty().isNull());
        actualizarLista();
    }

    private void actualizarLista() {
        seleccion.setValue(null);
        seleccion.getItems().clear();
        if (cliente == null) return;
        Monedero principal = cliente.getMonederos().get(0);
        for (Monedero m : cliente.getMonederos()) {
            if (m != principal) seleccion.getItems().add(m);
        }
    }

    @FXML private void crearMonedero() {
        if (cliente == null) { mensaje.setText("Inicia sesión para crear un monedero."); return; }
        String importe = saldo.getText().trim();
        if (!importe.matches("[0-9]+([,.][0-9]{1,2})?")) {
            mensaje.setText("Ingresa un saldo de cero o más pesos, sin separadores de miles.");
            return;
        }
        try {
            double valor = Double.parseDouble(importe.replace(',', '.'));
            if (fachada.crearMonedero(cliente.getIdCliente(), nombre.getText(), valor)) {
                mensaje.setText("Monedero creado: " + nombre.getText().trim());
                nombre.clear();
                saldo.clear();
                actualizarLista();
            }
        } catch (IllegalArgumentException ex) {
            mensaje.setText(ex.getMessage());
        }
    }

    @FXML private void eliminarMonedero() {
        Monedero elegido = seleccion.getValue();
        if (elegido == null || cliente == null) return;
        Alert aviso = new Alert(Alert.AlertType.CONFIRMATION,
                "Su saldo volverá al monedero principal.", ButtonType.YES, ButtonType.CANCEL);
        aviso.setTitle("Eliminar monedero");
        aviso.setHeaderText("¿Eliminar " + seleccion.getConverter().toString(elegido) + "?");
        aviso.initOwner(seleccion.getScene().getWindow());
        if (aviso.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.YES) return;
        boolean eliminado = fachada.eliminarMonedero(cliente.getIdCliente(), elegido.getId());
        mensaje.setText(eliminado ? "Monedero eliminado. Saldo devuelto al principal." : "El monedero ya no existe.");
        actualizarLista();
    }

    @FXML private void programarTransaccion() {
        // Pendiente: conectar con la fachada.
    }

    @FXML private void eliminarProgramada() {
        // Pendiente: conectar con la fachada.
    }

    @FXML private void volver(ActionEvent evento) {
        Stage ventana = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        ventana.close();
    }
}
