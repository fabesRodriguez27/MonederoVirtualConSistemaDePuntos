package monedero.servicios;
import monedero.application.MonederoVirtual;
import monedero.estructuras.LinkedList;
import monedero.modelos.Beneficio;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.utilidades.EstadoBeneficio;
import monedero.utilidades.Rango;
import monedero.utilidades.TipoBeneficio;
import monedero.utilidades.TipoNotificacion;
import monedero.utilidades.TipoTransaccion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BeneficioService {

    public static final int COSTO_DESCUENTO_TRANSFERENCIA = 100;
    public static final int COSTO_RETIROS_SIN_COMISION = 500;
    public static final int COSTO_BONO_SALDO = 1000;

    public static final int DIAS_VIGENCIA = 30;
    public static final double VALOR_BONO = 50_000;
    public static final float PORCENTAJE_DESCUENTO_TRANSFERENCIA = 0.10f;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BeneficioService() {
    }

    //ClienteService
    private ClienteService clientes() {
        return MonederoVirtual.getInstancia().getClienteService();
    }

    //NotificacionService
    private NotificacionService notificaciones() {
        return MonederoVirtual.getInstancia().getNotificacionService();
    }

    public int obtenerCostoBeneficio(TipoBeneficio tipo) {
        return switch (tipo) {
            case DESCUENTO_TRANSFERENCIA -> COSTO_DESCUENTO_TRANSFERENCIA;
            case RETIROS_SIN_COMISION -> COSTO_RETIROS_SIN_COMISION;
            case BONO_SALDO -> COSTO_BONO_SALDO;
        };
    }

    public String obtenerDescripcionBeneficio(TipoBeneficio tipo) {
        return switch (tipo) {
            case DESCUENTO_TRANSFERENCIA -> "10% de descuento en la comisión de transferencias por " + DIAS_VIGENCIA + " días";
            case RETIROS_SIN_COMISION -> "Retiros sin comisión por " + DIAS_VIGENCIA + " días";
            case BONO_SALDO -> "Bono de " + String.format("$%,.2f", VALOR_BONO) + " en tu monedero principal";
        };
    }

    private LocalDateTime calcularVencimiento(TipoBeneficio tipo, LocalDateTime fechaInicio) {
        return switch (tipo) {
            case DESCUENTO_TRANSFERENCIA, RETIROS_SIN_COMISION -> fechaInicio.plusDays(DIAS_VIGENCIA);
            case BONO_SALDO -> null;
        };
    }

    //Actualizamos los Beneficios del Cliente (Estados)
    public void actualizarEstados(Cliente cliente) {
        LocalDateTime ahora = LocalDateTime.now();
        for (Beneficio beneficio : cliente.getBeneficios()) {
            if (beneficio.getEstado() == EstadoBeneficio.ACTIVO && !beneficio.estaVigente(ahora)) {
                beneficio.setEstado(EstadoBeneficio.VENCIDO);
                notificaciones().notificar(cliente, TipoNotificacion.BENEFICIO_VENCIDO,
                        "Se venció tu beneficio: " + obtenerDescripcionBeneficio(beneficio.getTipoBeneficio()) + ".");
            }
        }
    }

    //Devolvemos el beneficio activo según la transacción
    public Beneficio buscarActivo(Cliente cliente, TipoBeneficio tipo) {
        actualizarEstados(cliente);
        for (Beneficio beneficio : cliente.getBeneficios()) {
            if (beneficio.getTipoBeneficio() == tipo && beneficio.getEstado() == EstadoBeneficio.ACTIVO) {
                return beneficio;
            }
        }
        return null;
    }

    public Beneficio canjear(String idCliente, TipoBeneficio tipo) {
        Cliente cliente = clientes().buscarCliente(idCliente);
        int costo = obtenerCostoBeneficio(tipo);

        //No Puede Tener dos Beneficios de lo Mismo Activos al Mismo Tiempo
        Beneficio activo = buscarActivo(cliente, tipo);
        if (activo != null) {
            throw new IllegalArgumentException("Ya tienes este beneficio activo hasta " + FECHA.format(activo.getFechaVencimiento()));
        }

        //Verificamos los Puntos
        if (cliente.getPuntos() < costo) {
            throw new IllegalArgumentException("Puntos insuficientes: Necesitas " + costo + " y tienes " + cliente.getPuntos() + "Puntos");
        }

        //Descontar puntos (reordena el árbol y recalcula el rango)
        Rango rangoAnterior = cliente.getRango();
        MonederoVirtual.getInstancia().getPuntosService().actualizarPuntos(cliente, -costo);

        //Creamos el Beneficio
        Beneficio nuevoBeneficio = new Beneficio(tipo, costo, calcularVencimiento(tipo, LocalDateTime.now()));
        if (tipo == TipoBeneficio.BONO_SALDO) {
            //Agrego la Suma al Monedero Principal
            Monedero principal = cliente.getMonederos().get(0);
            principal.setSaldo(principal.getSaldo() + VALOR_BONO);
            //El Bono de Saldo Es Inmediatamente Usado
            nuevoBeneficio.setEstado(EstadoBeneficio.USADO);
        }
        cliente.getBeneficios().addLast(nuevoBeneficio);

        //Le Notificamos al Cliente
        String vigencia = nuevoBeneficio.getFechaVencimiento() == null ? "" : "Vigente hasta " + FECHA.format(nuevoBeneficio.getFechaVencimiento()) + ".";
        notificaciones().notificar(cliente, TipoNotificacion.BENEFICIO_CANJEADO,
                "Canjeaste " + costo + " Puntos Por: " + obtenerDescripcionBeneficio(tipo) + "\n" + vigencia);
        if (cliente.getRango() != rangoAnterior) {
            notificaciones().notificar(cliente, TipoNotificacion.CAMBIO_RANGO, "Tu rango cambió de " + rangoAnterior + " a " + cliente.getRango());
        }
        return nuevoBeneficio;
    }

    public LinkedList<Beneficio> obtenerBeneficios(String idCliente) {
        Cliente cliente = clientes().buscarCliente(idCliente);
        actualizarEstados(cliente);
        return cliente.getBeneficios();
    }

    //Comisión que se Cobra al Cliente
    public float calcularComision(Cliente cliente, TipoTransaccion tipo) {
        switch (tipo) {
            case RETIRO:
                if (buscarActivo(cliente, TipoBeneficio.RETIROS_SIN_COMISION) != null) {
                    return 0; //No se cobra comisión
                }
                return TarifaService.getTarifaRetiro();

            case TRANSFERENCIA:
                float tarifa = TarifaService.getTarifaTransferencia();
                if (buscarActivo(cliente, TipoBeneficio.DESCUENTO_TRANSFERENCIA) != null) {
                    return tarifa * (1 - PORCENTAJE_DESCUENTO_TRANSFERENCIA);
                }
                return tarifa;
            default:
                return 0;
        }
    }
}
