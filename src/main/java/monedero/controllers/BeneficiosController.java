package monedero.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import monedero.modelos.Beneficio;
import monedero.modelos.Cliente;
import monedero.modelos.SessionManager;
import monedero.servicios.facade.BeneficioFacade;
import monedero.utilidades.TipoBeneficio;

import java.time.format.DateTimeFormatter;

public class BeneficiosController {

    // Catálogo (lo que se puede canjear)
    @FXML private TableView<TipoBeneficio> tablaCatalogo;
    @FXML private TableColumn<TipoBeneficio, String> colBeneficio;
    @FXML private TableColumn<TipoBeneficio, String> colDescripcion;
    @FXML private TableColumn<TipoBeneficio, String> colCosto;
    @FXML private TableColumn<TipoBeneficio, String> colDisponibilidad;

    @FXML private TableView<Beneficio> tablaMisBeneficios;
    @FXML private TableColumn<Beneficio, String> colMiBeneficio;
    @FXML private TableColumn<Beneficio, String> colFechaCanje;
    @FXML private TableColumn<Beneficio, String> colVencimiento;
    @FXML private TableColumn<Beneficio, String> colEstado;

    @FXML private Label lblPuntos;
    @FXML private Button btnCanjear;

    private final BeneficioFacade fachada = new BeneficioFacade();
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Cliente cliente;

    @FXML
    private void initialize() {
        cliente = SessionManager.getInstancia().getClienteLogueado();
        configurarColumnas();
        //Deshabilitar el botón cuando nada del catálogo esté seleccionado
        btnCanjear.disableProperty().bind(tablaCatalogo.getSelectionModel().selectedItemProperty().isNull());
        cargar();
    }

    private void configurarColumnas() {
        colBeneficio.setCellValueFactory(f -> texto(nombre(f.getValue())));
        colDescripcion.setCellValueFactory(f -> texto(fachada.descripcionDe(f.getValue())));
        colCosto.setCellValueFactory(f -> texto(fachada.costoDe(f.getValue()) + " pts"));
        colDisponibilidad.setCellValueFactory(f -> texto(disponibilidad(f.getValue())));

        colMiBeneficio.setCellValueFactory(f -> texto(nombre(f.getValue().getTipoBeneficio())));
        colFechaCanje.setCellValueFactory(f -> texto(FECHA.format(f.getValue().getFechaCanje())));
        colVencimiento.setCellValueFactory(f -> texto(f.getValue().getFechaVencimiento() == null
                ? "No vence" : FECHA.format(f.getValue().getFechaVencimiento())));
        colEstado.setCellValueFactory(f -> texto(String.valueOf(f.getValue().getEstado())));

        tablaCatalogo.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaMisBeneficios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // Carga las dos tablas y los puntos (al cargar, el servicio revisa vencimientos con now())
    private void cargar() {
        if (cliente == null) return;
        String id = cliente.getIdCliente();

        ObservableList<Beneficio> mios = FXCollections.observableArrayList();
        for (Beneficio b : fachada.obtenerBeneficios(id)) {
            mios.add(0, b);                 // el más reciente arriba
        }
        tablaMisBeneficios.setItems(mios);

        ObservableList<TipoBeneficio> catalogo = FXCollections.observableArrayList(fachada.obtenerCatalogo());
        tablaCatalogo.setItems(catalogo);

        lblPuntos.setText("Puntos disponibles: " + fachada.puntosCliente(id));
        tablaCatalogo.refresh();
        tablaMisBeneficios.refresh();
    }

    //Texto de la columna "Disponibilidad" del catálogo
    private String disponibilidad(TipoBeneficio tipo) {
        Beneficio activo = fachada.buscarActivo(cliente.getIdCliente(), tipo);
        if (activo != null) {
            return "Activo hasta " + FECHA.format(activo.getFechaVencimiento());
        }
        int faltan = fachada.costoDe(tipo) - fachada.puntosCliente(cliente.getIdCliente());
        return faltan > 0 ? "Te faltan " + faltan + " pts" : "Disponible";
    }

    @FXML
    private void canjear() {
        TipoBeneficio tipo = tablaCatalogo.getSelectionModel().getSelectedItem();
        if (tipo == null || cliente == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Canjear " + fachada.costoDe(tipo) + " puntos por:\n" + fachada.descripcionDe(tipo) + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Canjear beneficio");
        confirmacion.setHeaderText(null);
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            fachada.canjear(cliente.getIdCliente(), tipo);
            mostrar(Alert.AlertType.INFORMATION, "¡Beneficio canjeado! Revisa tus notificaciones.");
        } catch (IllegalArgumentException e) {
            mostrar(Alert.AlertType.ERROR, e.getMessage());
        }
        cargar();
    }

    @FXML
    private void volver(ActionEvent evento) {
        ((Stage) ((Node) evento.getSource()).getScene().getWindow()).close();
    }

    private String nombre(TipoBeneficio tipo) {
        return switch (tipo) {
            case DESCUENTO_TRANSFERENCIA -> "Descuento en transferencias";
            case RETIROS_SIN_COMISION -> "Retiros sin comisión";
            case BONO_SALDO -> "Bono de saldo";
        };
    }

    private SimpleStringProperty texto(String valor) {
        return new SimpleStringProperty(valor);
    }

    private void mostrar(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle("Beneficios");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}