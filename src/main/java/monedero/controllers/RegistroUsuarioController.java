package monedero.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import monedero.application.Sistema;
import monedero.servicios.ClienteService;
import monedero.utilidades.PathsFxml;

public class RegistroUsuarioController {

    @FXML
    private Label lblMensaje;
    @FXML
    private TextField txtIdUsuario;
    @FXML
    private TextField txtNombre;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtPassword2;
    private ClienteService clienteService;

    @FXML
    void CrearUsuario(ActionEvent event) {
        try {
            if (!validarCampos()) {
                return;
            }
            if (clienteService.registrarCliente(txtIdUsuario.getText().trim(), txtPassword.getText().trim(), txtNombre.getText().trim())){
                Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                alerta.setTitle("Registro exitoso");
                alerta.setHeaderText(null);
                alerta.setContentText("Usuario creado exitosamente: " + txtNombre.getText().trim());
                alerta.showAndWait();
                limpiarCampos();
                regresarALogin(event);
            } else {
                mostrarError("Error: Ya existe un Cliente con la Cédula " + txtIdUsuario.getText().trim());
            }
        } catch (Exception e) {
            mostrarError("Error al Crear al Cliente: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        if (txtIdUsuario.getText().trim().isEmpty()) {
            mostrarError("La Cédula es Obligatoria");
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El Nombre es Obligatorio");
            return false;
        }
        if (txtPassword.getText().trim().isEmpty() ||  txtPassword2.getText().trim().isEmpty()) {
            mostrarError("La Contraseña es Obligatoria");
            return false;
        }
        if (!txtPassword.getText().equals(txtPassword2.getText())) {
            mostrarError("La contraseña debe coincidir en los dos campos");
            return false;
        }
        return true;
    }

    private void mostrarError(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Login");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    void regresarALogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(PathsFxml.PATH_LOGIN));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al Cerrar Sesión: " + e.getMessage());
        }
    }

    void limpiarCampos() {
        txtIdUsuario.clear();
        txtNombre.clear();
        txtPassword.clear();
        txtPassword2.clear();
    }

    public void initialize() {
        clienteService = Sistema.getInstancia().getClienteService();
        limpiarCampos();
    }
}
