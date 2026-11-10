package monedero.modelos;

import monedero.servicios.TarifaService;
import monedero.utilidades.Categoria;
import monedero.utilidades.TipoTransaccion;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaccion {
    private String id;
    private String idCliente;
    private TipoTransaccion tipoTransaccion;
    private Monedero origen;
    private Monedero destino;
    private double monto;
    private float comision;
    private LocalDateTime fecha;
    private int puntos;
    private Categoria categoria;
    private boolean revertida;

    public Transaccion (TipoTransaccion tipoTransaccion, Monedero origen, Monedero destino, double monto, Categoria categoria) {
        this.id= UUID.randomUUID().toString();
        this.idCliente = idCliente;
        this.tipoTransaccion = tipoTransaccion;
        this.origen = origen;
        this.destino = destino;
        this.monto = monto;
        this.comision = calcularComision();
        this.fecha = LocalDateTime.now();
        this.puntos = calcularPuntos();
        this.categoria = categoria;
        this.revertida = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
    }

    public Monedero getOrigen() {
        return origen;
    }

    public void setOrigen(Monedero origen) {
        this.origen = origen;
    }

    public Monedero getDestino() {
        return destino;
    }

    public void setDestino(Monedero destino) {
        this.destino = destino;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(float monto) {
        this.monto = monto;
    }

    public float calcularComision() {
        return switch (this.tipoTransaccion) {
            case RETIRO -> TarifaService.getTarifaRetiro();
            case TRANSFERENCIA -> TarifaService.getTarifaTransferencia();
            default -> 0;
        };
    }

    public float getComision() {
        return comision;
    }

    public void setComision(float comision) {
        this.comision = comision;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int calcularPuntos() {

        switch (tipoTransaccion) {
            case DEPOSITO:
                return (int) (this.monto/100);

            case RETIRO:
                return (int) (this.monto/100)*2;

            case TRANSFERENCIA:
                return (int) (this.monto/100)*3;

            default:
                return 0;
        }
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isRevertida() {
        return revertida;
    }

    public void setRevertida(boolean revertida) {
        this.revertida = revertida;
    }

    @Override
    public String toString() {
        return "Transaccion{" +
                "id='" + id + '\'' +
                ", idCliente='" + idCliente + '\'' +
                ", tipoTransaccion=" + tipoTransaccion +
                ", origen='" + origen + '\'' +
                ", destino='" + destino + '\'' +
                ", monto=" + monto +
                ", comision=" + comision +
                ", fecha=" + fecha +
                ", puntos=" + puntos +
                ", categoria=" + categoria +
                ", revertida=" + revertida +
                '}';
    }
}
