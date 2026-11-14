package monedero.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.SessionManager;
import monedero.servicios.facade.TransaccionFacade;
import monedero.utilidades.Categoria;
import monedero.utilidades.FormatoCedula;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RealizarTransaccionController {

    private final TransaccionFacade fachada = new TransaccionFacade();
    private Cliente cliente;

    @FXML
    private void initialize() {
        //Traigo al Cliente de la Sesión
        cliente = SessionManager.getInstancia().getClienteLogueado();
    }

    @FXML
    private void depositar() {
        //De los Monederos que Muestro, no Exluyo Ninguno porque Todos son Míos
        Monedero destino = elegirMonedero(misMonederos(), null, "Seleccione el Monedero donde Desea Depositar");
        if (destino == null) {
            return;
        }
        Double monto = pedirMonto("Monto a Depositar");
        if (monto == null){
            return;
        }
        try {
            fachada.depositar(cliente.getIdCliente(), destino.getId(), monto);
            mostrarInformacion("Depósito realizado. Ganaste puntos por esta operación.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void retirar() {
        Monedero origen = elegirMonedero(misMonederos(), null, "Seleccione el Monedero del Cual Desea Retirar");
        if (origen == null){
            return;
        }
        Double monto = pedirMonto("Monto a Retirar");
        if (monto == null){
            return;
        }
        Categoria categoria = pedirCategoria();
        if (categoria == null){
            return;
        }
        //Informo de la Tarifa
        float comision = fachada.tarifaRetiro();
        if (!confirmar("Se Descontarán " + formatearDinero(monto + comision) + " (Monto " + formatearDinero(monto) + " + Comisión " + formatearDinero(comision) + ")")){
            return;
        }
        try {
            fachada.retirar(cliente.getIdCliente(), origen.getId(), monto, categoria);
            mostrarInformacion("Retiro Realizado Correctamente.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private String formatearDinero(double valor) {
        return String.format("$%,.2f", valor);
    }

    private boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        alerta.setTitle("Confirmar Transacción");
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private Categoria pedirCategoria() {
        ChoiceDialog<Categoria> dialogo = new ChoiceDialog<>(Categoria.OTROS, Categoria.values());
        dialogo.setTitle("Categoría");
        dialogo.setHeaderText("¿En Qué Categoría Clasifica este Gasto?");
        dialogo.setContentText("Categoría:");
        return dialogo.showAndWait().orElse(null);
    }

    private Double pedirMonto(String titulo) {
        //Ventana Emergente para que el Usuario Escriba
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(titulo + " (Sin Puntos de Miles)");
        dialogo.setContentText("Monto:");
        Optional<String> texto = dialogo.showAndWait();
        if (texto.isEmpty()) return null;
        try {
            double monto = Double.parseDouble(texto.get().trim().replace(',', '.'));
            if (!Double.isFinite(monto) || monto <= 0) {
                mostrarError("El Monto Debe ser Mayor que Cero.");
                return null;
            }
            return monto;
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un Monto Numérico Válido.");
            return null;
        }
    }

    private Monedero elegirMonedero(List<Monedero> monederos, Monedero excluir, String mensaje) {
        //Configurar Monederos en Lista
        List<Monedero> opciones = new ArrayList<>();
        //Cargo todos Mis Monederos (El Cliente Logueado
        for (Monedero m : monederos) {
            if (excluir == null || !m.getId().equals(excluir.getId())){
                opciones.add(m);
            }
        }
        if (opciones.isEmpty()) {
            mostrarError("No hay Monederos Disponibles.");
            return null;
        }
        //ChoiceDialog es un diálogo de JavaFX (Ventana Emergente) que trae un ComboBox con botones Aceptar y Cancelar
        ChoiceDialog<Monedero> dialogo = new ChoiceDialog<>(opciones.get(0), opciones);
        dialogo.setTitle("Seleccionar Monedero");
        dialogo.setHeaderText(mensaje);
        dialogo.setContentText("Monedero:");
        //Si el Usuario Selecciona y Acepta, Retorna el Monedero, de lo Contrario - Null
        return dialogo.showAndWait().orElse(null);
    }

    private List<Monedero> misMonederos() {
        return aLista(fachada.obtenerMonederos(cliente.getIdCliente()));
    }

    //Algunos Elementos de JafaFx Necesitan de las Colecciones Propias de Java.util
    private List<Monedero> aLista(Iterable<Monedero> monederos) {
        List<Monedero> lista = new ArrayList<>();
        for (Monedero monedero : monederos){
            lista.add(monedero);
        }
        return lista;
    }

    @FXML
    private void transferir() {
        Monedero origen = elegirMonedero(misMonederos(), null, "Seleccione el Monedero desde el Cual Desea Transferir");
        if (origen == null){
            return;
        }
        // A quien se le transfiere
        ChoiceDialog<String> tipo = new ChoiceDialog<>("A otro cliente", Arrays.asList("A otro cliente", "Entre mis monederos"));
        tipo.setTitle("Transferir");
        tipo.setHeaderText("¿A Dónde Desea Transferir?");
        Optional<String> opcion = tipo.showAndWait();

        if (opcion.isEmpty()){
            return;
        }
        String idDestino;
        if (opcion.get().equals("Entre mis monederos")) {
            idDestino = cliente.getIdCliente();
        } else {
            idDestino = pedirCedulaDestino();
            if (idDestino == null){
                return;
            }
        }

        List<Monedero> monederosDestino;
        try {
            //Busco los Monederos del Destino
            monederosDestino = aLista(fachada.obtenerMonederos(idDestino));
        } catch (IllegalArgumentException e) {
            mostrarError("No existe un Cliente con Esa Cédula.");
            return;
        }

        Monedero destino = elegirMonedero(monederosDestino, origen, "Seleccione el Monedero de Destino");
        if (destino == null){
            return;
        }

        Double monto = pedirMonto("Monto a Transferir");
        if (monto == null){
            return;
        }

        Categoria categoria = pedirCategoria();
        if (categoria == null){
            return;
        }

        boolean interna = idDestino.equals(cliente.getIdCliente());
        float comision;
        if(interna){
            comision = 0;
        }else{
            comision = fachada.tarifaTransferencia();
        }

        if (!confirmar("Se Descontarán " + formatearDinero(monto + comision)
                + " (monto " + formatearDinero(monto) + " + comisión " + formatearDinero(comision) + ").")) return;

        try {
            fachada.transferir(cliente.getIdCliente(), origen.getId(), idDestino, destino.getId(), monto, categoria);
            mostrarInformacion("Transferencia Realizada Correctamente.");
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    private String pedirCedulaDestino() {
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Cliente Destino");
        dialogo.setHeaderText("Ingrese La Cédula del Cliente que Recibirá el Dinero");
        dialogo.setContentText("Cédula:");
        Optional<String> texto = dialogo.showAndWait();
        if (texto.isEmpty() || texto.get().isBlank()){
            return null;
        }
        return FormatoCedula.normalizar(texto.get().trim());
    }

    @FXML
    private void revertirUltimaTransaccion() {
        if(!confirmar("¿Está Seguro de Revertir la última Transacción?")){
            return;
        }
        try{
            fachada.revertirUltimaTransaccion(cliente.getIdCliente());
            mostrarInformacion("Última Transacción Revertida Correctamente.");
        }catch (IllegalArgumentException e){
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void volver(ActionEvent evento) {
        ((Stage) ((Node) evento.getSource()).getScene().getWindow()).close();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alerta.setTitle("Transacción");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setTitle("Transacción");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
