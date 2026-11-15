package monedero.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.SessionManager;
import monedero.modelos.TransaccionProgramada;
import monedero.servicios.facade.ClienteFacade;

import monedero.utilidades.Categoria;
import monedero.utilidades.FormatoCedula;
import monedero.utilidades.Frecuencia;
import monedero.utilidades.TipoTransaccion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MonederosTransferenciasController {

    @FXML private TextField nombre;
    @FXML private TextField saldo;
    @FXML private ComboBox<Monedero> seleccion;
    @FXML private Button eliminar;
    @FXML private Label mensaje;

    private final ClienteFacade fachada = new ClienteFacade();
    private Cliente cliente;

    @FXML
    private void initialize() {

        cliente = SessionManager.getInstancia().getClienteLogueado();

        seleccion.setConverter(new StringConverter<>() {

            public String toString(Monedero m) {
                return m == null ? "" : m.getNombre() + " (" + m.getId() + ")";
            }

            public Monedero fromString(String texto) {
                return null;
            }
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

    @FXML
    private void crearMonedero() {

        if (cliente == null) {
            mensaje.setText("Inicia sesión para crear un monedero.");
            return;
        }

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

    @FXML
    private void eliminarMonedero() {

        Monedero elegido = seleccion.getValue();

        if (elegido == null || cliente == null) return;

        Alert aviso = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Su saldo volverá al monedero principal.",
                ButtonType.YES,
                ButtonType.CANCEL
        );

        aviso.setTitle("Eliminar monedero");
        aviso.setHeaderText("¿Eliminar " + seleccion.getConverter().toString(elegido) + "?");
        aviso.initOwner(seleccion.getScene().getWindow());

        if (aviso.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.YES) return;

        boolean eliminado = fachada.eliminarMonedero(
                cliente.getIdCliente(),
                elegido.getId()
        );

        mensaje.setText(
                eliminado
                        ? "Monedero eliminado. Saldo devuelto al principal."
                        : "El monedero ya no existe."
        );

        actualizarLista();
    }

    @FXML
    private void programarTransaccion() {

        if (cliente == null) {
            mensaje.setText("Debe iniciar sesión.");
            return;
        }

        // Seleccionar tipo de transacción
        ChoiceDialog<TipoTransaccion> dialogoTipo =
                new ChoiceDialog<>(
                        TipoTransaccion.TRANSFERENCIA,
                        TipoTransaccion.DEPOSITO,
                        TipoTransaccion.TRANSFERENCIA
                );

        dialogoTipo.setTitle("Programar transacción");
        dialogoTipo.setHeaderText("Seleccione el tipo de transacción");
        dialogoTipo.setContentText("Tipo:");

        Optional<TipoTransaccion> resultadoTipo = dialogoTipo.showAndWait();

        if (resultadoTipo.isEmpty()) return;

        TipoTransaccion tipoTransaccion = resultadoTipo.get();

        String idCliente = cliente.getIdCliente();
        String idClienteDestino = idCliente;
        String origen = null;
        String destino;

        Categoria categoria = Categoria.OTROS;

        if (tipoTransaccion == TipoTransaccion.TRANSFERENCIA) {

            // Seleccionar monedero de origen
            Monedero monederoOrigen = seleccionarMonedero(
                    "Seleccione el monedero desde el cual desea transferir",
                    null
            );

            if (monederoOrigen == null) return;

            origen = monederoOrigen.getId();

            // Seleccionar destino de transferencia
            ChoiceDialog<String> dialogoDestino =
                    new ChoiceDialog<>(
                            "Entre mis monederos",
                            "Entre mis monederos",
                            "A otro cliente"
                    );

            dialogoDestino.setTitle("Destino");
            dialogoDestino.setHeaderText("¿A dónde desea transferir?");
            dialogoDestino.setContentText("Destino:");

            Optional<String> resultadoDestino = dialogoDestino.showAndWait();

            if (resultadoDestino.isEmpty()) return;

            if (resultadoDestino.get().equals("A otro cliente")) {

                TextInputDialog dialogoCliente = new TextInputDialog();

                dialogoCliente.setTitle("Cliente destino");
                dialogoCliente.setHeaderText("Ingrese la cédula del cliente destinatario");
                dialogoCliente.setContentText("Cédula:");

                Optional<String> resultadoCliente = dialogoCliente.showAndWait();

                if (resultadoCliente.isEmpty() || resultadoCliente.get().isBlank()) return;

                try {

                    idClienteDestino = FormatoCedula.normalizar(
                            resultadoCliente.get().trim()
                    );

                } catch (IllegalArgumentException e) {

                    mostrarError(e.getMessage());
                    return;
                }
            }
        }

        // Obtener los monederos del cliente destinatario
        try {

            LinkedList<Monedero> monederosDestino =
                    fachada.obtenerMonederos(idClienteDestino);

            Monedero excluir = null;

            // Si es transferencia interna, excluir el monedero de origen
            if (idClienteDestino.equals(idCliente) && origen != null) {
                excluir = cliente.buscarMonedero(origen);
            }

            Monedero monederoDestino = seleccionarMonederoDestino(
                    monederosDestino,
                    excluir
            );

            if (monederoDestino == null) return;

            destino = monederoDestino.getId();

        } catch (IllegalArgumentException e) {

            mostrarError("No se encontró el cliente destinatario: " + e.getMessage());
            return;
        }

        // Solicitar monto
        Double monto = solicitarMonto();

        if (monto == null) return;

        // Seleccionar categoría
        if (tipoTransaccion == TipoTransaccion.TRANSFERENCIA) {

            ChoiceDialog<Categoria> dialogoCategoria =
                    new ChoiceDialog<>(
                            Categoria.OTROS,
                            Categoria.values()
                    );

            dialogoCategoria.setTitle("Categoría");
            dialogoCategoria.setHeaderText("Seleccione la categoría de la transferencia");
            dialogoCategoria.setContentText("Categoría:");

            Optional<Categoria> resultadoCategoria = dialogoCategoria.showAndWait();

            if (resultadoCategoria.isEmpty()) return;

            categoria = resultadoCategoria.get();
        }

        // Solicitar fecha de ejecución
        LocalDateTime proximaEjecucion = solicitarFecha();

        if (proximaEjecucion == null) return;

        // Seleccionar frecuencia
        ChoiceDialog<Frecuencia> dialogoFrecuencia =
                new ChoiceDialog<>(
                        Frecuencia.UNICA,
                        Frecuencia.values()
                );

        dialogoFrecuencia.setTitle("Frecuencia");
        dialogoFrecuencia.setHeaderText("Seleccione cada cuánto se repetirá la transacción");
        dialogoFrecuencia.setContentText("Frecuencia:");

        Optional<Frecuencia> resultadoFrecuencia = dialogoFrecuencia.showAndWait();

        if (resultadoFrecuencia.isEmpty()) return;

        Frecuencia frecuencia = resultadoFrecuencia.get();

        // Confirmar programación
        String resumen =
                "Tipo: " + tipoTransaccion
                        + "\nMonto: " + formatearDinero(monto)
                        + "\nFecha: " + proximaEjecucion
                        + "\nFrecuencia: " + frecuencia;

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                resumen,
                ButtonType.YES,
                ButtonType.CANCEL
        );

        confirmacion.setTitle("Confirmar programación");
        confirmacion.setHeaderText("¿Desea programar esta transacción?");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.YES) return;

        // Registrar programación
        try {

            boolean programada = fachada.programarTransaccion(
                    idCliente,
                    tipoTransaccion,
                    origen,
                    idClienteDestino,
                    destino,
                    monto,
                    categoria,
                    proximaEjecucion,
                    frecuencia
            );

            if (programada) {
                mostrarInformacion("Transacción programada correctamente.");
                mensaje.setText("Programación registrada correctamente.");
            } else {
                mostrarError("No se pudo programar la transacción.");
            }

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void eliminarProgramada() {

        if (cliente == null) {
            mensaje.setText("Debe iniciar sesión.");
            return;
        }

        if (cliente.getTransaccionesProgramadas().isEmpty()) {
            mostrarError("No tiene transacciones programadas pendientes.");
            return;
        }

        // Obtener programaciones del cliente
        List<TransaccionProgramada> programaciones = new ArrayList<>();

        for (TransaccionProgramada programada :
                cliente.getTransaccionesProgramadas()) {

            programaciones.add(programada);
        }

        // Seleccionar programación
        ChoiceDialog<TransaccionProgramada> dialogo =
                new ChoiceDialog<>(
                        programaciones.get(0),
                        programaciones
                );

        dialogo.setTitle("Cancelar programación");
        dialogo.setHeaderText("Seleccione la transacción programada que desea cancelar");
        dialogo.setContentText("Programación:");

        Optional<TransaccionProgramada> resultado = dialogo.showAndWait();

        if (resultado.isEmpty()) return;

        TransaccionProgramada seleccionada = resultado.get();

        // Confirmar cancelación
        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Está seguro de cancelar esta programación?",
                ButtonType.YES,
                ButtonType.CANCEL
        );

        confirmacion.setTitle("Cancelar transacción");
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.YES) return;

        // Eliminar programación
        try {

            boolean eliminada = fachada.eliminarTransaccion(
                    cliente.getIdCliente(),
                    seleccionada.getId()
            );

            if (eliminada) {
                mostrarInformacion("Transacción programada cancelada correctamente.");
                mensaje.setText("Programación cancelada.");
            } else {
                mostrarError("No se pudo cancelar la programación.");
            }

        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }

    // Seleccionar monedero del cliente logueado
    private Monedero seleccionarMonedero(
            String texto,
            Monedero excluir) {

        return seleccionarMonederoDestino(
                cliente.getMonederos(),
                excluir
        );
    }

    // Seleccionar monedero de cualquier cliente
    private Monedero seleccionarMonederoDestino(
            Iterable<Monedero> monederosCliente,
            Monedero excluir) {

        List<Monedero> monederos = new ArrayList<>();

        for (Monedero monedero : monederosCliente) {

            if (excluir == null ||
                    !monedero.getId().equals(excluir.getId())) {

                monederos.add(monedero);
            }
        }

        if (monederos.isEmpty()) {
            mostrarError("No hay monederos disponibles.");
            return null;
        }

        ChoiceDialog<Monedero> dialogo =
                new ChoiceDialog<>(
                        monederos.get(0),
                        monederos
                );

        dialogo.setTitle("Seleccionar monedero");
        dialogo.setHeaderText(textoMonedero(excluir));
        dialogo.setContentText("Monedero:");

        return dialogo.showAndWait().orElse(null);
    }

    private String textoMonedero(Monedero excluir) {
        return excluir == null
                ? "Seleccione un monedero"
                : "Seleccione un monedero diferente al de origen";
    }

    private Double solicitarMonto() {

        TextInputDialog dialogo = new TextInputDialog();

        dialogo.setTitle("Monto");
        dialogo.setHeaderText("Ingrese el monto de la transacción");
        dialogo.setContentText("Monto:");

        Optional<String> resultado = dialogo.showAndWait();

        if (resultado.isEmpty()) return null;

        try {

            double monto = Double.parseDouble(
                    resultado.get().trim().replace(',', '.')
            );

            if (!Double.isFinite(monto) || monto <= 0) {
                mostrarError("El monto debe ser mayor que cero.");
                return null;
            }

            return monto;

        } catch (NumberFormatException e) {
            mostrarError("Ingrese un monto numérico válido.");
            return null;
        }
    }

    private LocalDateTime solicitarFecha() {

        TextInputDialog dialogo = new TextInputDialog();

        dialogo.setTitle("Fecha de ejecución");
        dialogo.setHeaderText("Ingrese la fecha y hora de la primera ejecución");
        dialogo.setContentText("DD/MM/AAAA H:mm:");

        Optional<String> resultado = dialogo.showAndWait();

        if (resultado.isEmpty()) return null;

        try {

            DateTimeFormatter formato =
                    DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
                            .withResolverStyle(ResolverStyle.STRICT);

            LocalDateTime fecha = LocalDateTime.parse(
                    resultado.get().trim(),
                    formato
            );

            if (!fecha.isAfter(LocalDateTime.now())) {
                mostrarError("La fecha de ejecución debe ser futura.");
                return null;
            }

            return fecha;

        } catch (DateTimeParseException e) {
            mostrarError("Formato incorrecto. Ejemplo: 15/12/2026 10:30");
            return null;
        }
    }

    private String formatearDinero(double valor) {
        return String.format("$%,.2f", valor);
    }

    private void mostrarInformacion(String texto) {

        Alert alerta = new Alert(
                Alert.AlertType.INFORMATION,
                texto
        );

        alerta.setTitle("Transacciones programadas");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private void mostrarError(String texto) {

        Alert alerta = new Alert(
                Alert.AlertType.ERROR,
                texto
        );

        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.showAndWait();

        mensaje.setText(texto);
    }

    @FXML
    private void volver(ActionEvent evento) {
        Stage ventana = (Stage) ((Node) evento.getSource()).getScene().getWindow();
        ventana.close();
    }
}