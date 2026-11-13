package monedero.controllers;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import monedero.modelos.SessionManager;
import monedero.modelos.Usuario;
import monedero.servicios.facade.LoginFacade;
import monedero.utilidades.PathsFxml;
import javafx.scene.input.MouseEvent;
import monedero.utilidades.Rol;

public class LoginController implements Initializable {

    @FXML
    private Button btnLogin;
    @FXML
    private PasswordField inputPassword;
    @FXML
    private TextField inputUsername;
    @FXML
    private Label lblMensajeError;
    private LoginFacade loginFacade;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginFacade=new LoginFacade();
        inputUsername.clear();
        inputPassword.clear();
    }

    @FXML
    void login(ActionEvent event) {
        try {
            String id = inputUsername.getText().trim();
            String password = inputPassword.getText().trim();

            if (id.isEmpty() || password.isEmpty()) {
                mostrarError("Por Favor Ingrese su Usuario y Contraseña");
                return;
            }
            Usuario usuario = loginFacade.validarIngreso(id, password);

            if (usuario.getRol()== Rol.USUARIO) {
                SessionManager.getInstancia().iniciarSesion(loginFacade.retornarCliente(usuario.getId()));
                navegarAPantalla(event, PathsFxml.PATH_PANTALLA_PRINCIPAL_CLIENTE);
            }else if (usuario.getRol()==Rol.ADMINISTRADOR) {
                navegarAPantalla(event, PathsFxml.PATH_PANTALLA_PANEL_CONTROL);
            }else{
                mostrarError("Credenciales Inválidas. Verifique Su ID y Contraseña.");
                inputPassword.clear();
                inputUsername.clear();
            }
        }catch (Exception e) {
            e.printStackTrace(); //Esto hace que el error salga en la consola
            mostrarError("Error Al Iniciar Sesión. Intente Nuevamente.");
        }
    }

    private void navegarAPantalla(ActionEvent event, String pathFxml) {
        try {
            // Cargo la Nueva Pantalla
            FXMLLoader loader = new FXMLLoader(getClass().getResource(pathFxml));
            Parent root = loader.load();

            // Para obtener el stage actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Crear nueva escena
            Scene scene = new Scene(root);

            // Cambiar la escena
            stage.setScene(scene);
            stage.show();
            System.out.println("Navegación Exitosa a: " + pathFxml);

        } catch (Exception e) {
            System.out.println("Error al Navegar a la pantalla: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al Cargar la Pantalla. Intente Nuevamente.");
        }
    }

    @FXML
    void registrarUsuario(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(PathsFxml.PATH_REGISTER));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al Ingresar a Registrar Usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        if (lblMensajeError != null) {
            lblMensajeError.setText(mensaje);
            lblMensajeError.setVisible(true);
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Login");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
