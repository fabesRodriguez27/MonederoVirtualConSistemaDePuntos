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
    private String origen;
    private String destino;
    private float monto;
    private float comision;
    private LocalDateTime fecha;
    private int puntos;
    private Categoria categoria;
    private boolean revertida;

    public Transaccion (String idCliente, TipoTransaccion tipoTransaccion, String origen, String destino, float monto, Categoria categoria) {
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

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public float getMonto() {
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
}
