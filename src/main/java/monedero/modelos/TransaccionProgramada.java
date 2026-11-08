package monedero.modelos;

import monedero.utilidades.Categoria;
import monedero.utilidades.Frecuencia;
import monedero.utilidades.TipoTransaccion;

import java.time.LocalDateTime;

public class TransaccionProgramada extends Transaccion {

    private LocalDateTime proximaEjecucion;
    private Frecuencia frecuencia;
    private boolean estado;

    public TransaccionProgramada(String idCliente, TipoTransaccion tipoTransaccion, String origen, String destino, float monto, Categoria categoria, Frecuencia frecuencia) {
        super(idCliente, tipoTransaccion, origen, destino, monto, categoria);
        this.frecuencia = frecuencia;
        this.proximaEjecucion = calcularProximaEjecucion();
        this.estado = true;
    }

    public LocalDateTime calcularProximaEjecucion() {
        switch (frecuencia) {
            case QUINCENAL:
                return proximaEjecucion.plusDays(15);

            case MENSUAL:
                return proximaEjecucion.plusMonths(1);

            case BIMENSUAL:
                return proximaEjecucion.plusMonths(2);

            case TRIMESTRAL:
                return proximaEjecucion.plusMonths(3);

            case SEMESTRAL:
                return proximaEjecucion.plusMonths(6);

            case ANUAL:
                return proximaEjecucion.plusYears(1);

            default:
                throw new IllegalArgumentException("Frecuencia no válida");
        }
    }

    public LocalDateTime getProximaEjecucion() {
        return proximaEjecucion;
    }

    public void setProximaEjecucion(LocalDateTime proximaEjecucion) {
        this.proximaEjecucion = proximaEjecucion;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Frecuencia frecuencia) {
        this.frecuencia = frecuencia;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
