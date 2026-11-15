package monedero.application;

import monedero.servicios.*;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MonederoVirtual {

    private static MonederoVirtual instancia;

    private final ClienteService clienteService;
    private final PuntosService puntosService;
    private final TransaccionService transaccionService;
    private final LoginService loginService;
    private final NotificacionService notificacionService;
    private final ProgramacionService programacionService;
    private final ReporteService reporteService;
    private final TarifaService tarifaService;

    private ScheduledExecutorService planificador;


    private MonederoVirtual() {
        clienteService = new ClienteService();
        transaccionService = new TransaccionService();
        loginService = new LoginService();
        notificacionService = new NotificacionService();
        programacionService = new ProgramacionService();
        reporteService = new ReporteService();
        puntosService = new PuntosService();
        tarifaService = new TarifaService();
    }

    public static MonederoVirtual getInstancia() {
        if (instancia == null) {
            instancia = new MonederoVirtual();
        }
        return instancia;
    }

    public ClienteService getClienteService() {
        return clienteService;
    }
    public PuntosService getPuntosService() {
        return puntosService;
    }
    public TransaccionService getTransaccionService() {
        return transaccionService;
    }
    public LoginService getLoginService() {
        return loginService;
    }
    public NotificacionService getNotificacionService() {
        return notificacionService;
    }
    public ProgramacionService getProgramacionService() {
        return programacionService;
    }
    public ReporteService getReporteService() {
        return reporteService;
    }
    public TarifaService getTarifaService() {
        return tarifaService;
    }


    /// Iniciar revisión automática de transacciones programadas
    public void iniciarProgramaciones() {

        if (planificador != null && !planificador.isShutdown()) {
            return;
        }

        planificador = Executors.newSingleThreadScheduledExecutor();

        planificador.scheduleWithFixedDelay(() -> {

            try {

                System.out.println(
                        "Planificador ejecutándose: " + LocalDateTime.now()
                );

                programacionService.procesarTodasLasProgramaciones();

            } catch (Exception e) {

                System.err.println(
                        "Error al procesar programaciones: " + e.getMessage()
                );

                e.printStackTrace();
            }

        }, 0, 1, TimeUnit.MINUTES);
    }

    // Detener revisión automática
    public void detenerProgramaciones() {

        if (planificador != null) {
            planificador.shutdown();
        }
    }


}