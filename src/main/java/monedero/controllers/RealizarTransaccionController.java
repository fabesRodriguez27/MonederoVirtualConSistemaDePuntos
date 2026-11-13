package monedero.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class RealizarTransaccionController {
    @FXML private void depositar() {
        // Pendiente: conectar con la fachada.
    }

    @FXML private void retirar() {
        // Pendiente: conectar con la fachada.
    }

    @FXML private void transferir() {
        // Pendiente: conectar con la fachada.
    }

    @FXML private void revertirUltimaTransaccion() {
        // Pendiente: conectar con la fachada.
    }

    @FXML private void volver(ActionEvent evento) {
        Stage ventana = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        ventana.close();
    }
}
