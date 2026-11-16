package monedero.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import monedero.modelos.Cliente;
import monedero.modelos.RelacionReporte;
import monedero.servicios.facade.PanelControlFacade;
import monedero.utilidades.FormatoCedula;

import java.util.Locale;
import java.util.function.Function;

public class InformesController {

    @FXML private TableView<RelacionReporte> tablaTransferencias;
    @FXML private TableView<RelacionReporte> tablaReceptores;
    @FXML private TableView<RelacionReporte> tablaEmisores;
    @FXML private TableView<RelacionReporte> tablaCategorias;
    @FXML private TableView<RelacionReporte> tablaGastosCliente;
    @FXML private TableView<RelacionReporte> tablaMonederos;
    @FXML private Label lblResumen;
    @FXML private Label lblCiclos;
    @FXML private ComboBox<Cliente> comboClienteAlcance;
    @FXML private ListView<String> listaAlcance;
    @FXML private Label lblAlcance;

    private final PanelControlFacade fachada = new PanelControlFacade();

    @FXML
    private void initialize() {
        // Las columnas se arman desde código porque todas las tablas muestran el mismo tipo de fila
        configurar(tablaTransferencias, "Emisor", "Receptor", "Total transferido", "Transferencias");
        configurar(tablaReceptores, "Cliente", null, "Total recibido", "Recibidas");
        configurar(tablaEmisores, "Cliente", null, "Total enviado", "Enviadas");
        configurar(tablaCategorias, "Categoría", null, "Total gastado", "Movimientos");
        configurar(tablaGastosCliente, "Cliente", "Categoría", "Total gastado", "Movimientos");
        configurar(tablaMonederos, "Monedero origen", "Monedero destino", "Total movido", "Movimientos");

        comboClienteAlcance.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) {
                return c == null ? "" : c.getNombre() + " (" + FormatoCedula.mostrar(c.getIdCliente()) + ")";
            }
            @Override public Cliente fromString(String texto) { return null; }
        });

        cargar();
    }

    private void cargar() {
        tablaTransferencias.setItems(observable(fachada.transferenciasEntreClientes()));
        tablaReceptores.setItems(observable(fachada.clientesQueMasReciben()));
        tablaEmisores.setItems(observable(fachada.clientesQueMasEnvian()));
        tablaCategorias.setItems(observable(fachada.totalesPorCategoria()));
        tablaGastosCliente.setItems(observable(fachada.gastosPorClienteYCategoria()));
        tablaMonederos.setItems(observable(fachada.movimientosEntreMonederos()));

        lblResumen.setText(fachada.resumenGrafoTransferencias());
        lblCiclos.setText(fachada.hayCiclosDeTransferencias()
                ? "Se detectaron ciclos: hay dinero que sale de un cliente y vuelve a él por otros clientes."
                : "Sin ciclos: ningún dinero regresa a quien lo envió a través de otros clientes.");

        comboClienteAlcance.setItems(observable(fachada.obtenerClientes()));
        listaAlcance.getItems().clear();
        lblAlcance.setText("Elige un cliente y pulsa Analizar.");
    }

    // BFS sobre el grafo de transferencias
    @FXML
    private void analizarAlcance() {
        Cliente cliente = comboClienteAlcance.getValue();
        if (cliente == null) {
            lblAlcance.setText("Primero elige un cliente.");
            return;
        }
        ObservableList<String> alcanzados = observable(fachada.alcanceDelDinero(cliente.getIdCliente()));
        listaAlcance.setItems(alcanzados);
        lblAlcance.setText(alcanzados.isEmpty()
                ? cliente.getNombre() + " no ha transferido dinero a otros clientes."
                : "El dinero de " + cliente.getNombre() + " llega a " + alcanzados.size()
                + " cliente(s), de los más cercanos a los más lejanos:");
    }

    @FXML
    private void volver(ActionEvent evento) {
        ((Stage) ((Node) evento.getSource()).getScene().getWindow()).close();
    }

    // ===================== AYUDAS =====================

    private void configurar(TableView<RelacionReporte> tabla, String tituloOrigen, String tituloDestino,
                            String tituloTotal, String tituloCantidad) {
        tabla.getColumns().clear();
        tabla.getColumns().add(columna(tituloOrigen, RelacionReporte::getOrigen));
        if (tituloDestino != null) {
            tabla.getColumns().add(columna(tituloDestino, RelacionReporte::getDestino));
        }
        tabla.getColumns().add(columna(tituloTotal, r -> dinero(r.getTotal())));
        tabla.getColumns().add(columna(tituloCantidad, r -> String.valueOf(r.getCantidad())));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label("Aún no hay datos para este informe."));
    }

    private TableColumn<RelacionReporte, String> columna(String titulo, Function<RelacionReporte, String> valor) {
        TableColumn<RelacionReporte, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(fila -> new SimpleStringProperty(valor.apply(fila.getValue())));
        return columna;
    }

    private <T> ObservableList<T> observable(Iterable<T> elementos) {
        ObservableList<T> lista = FXCollections.observableArrayList();
        for (T elemento : elementos) lista.add(elemento);
        return lista;
    }

    private String dinero(double valor) {
        return String.format(Locale.forLanguageTag("es-CO"), "$%,.2f", valor);
    }
}