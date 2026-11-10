package monedero.application;
import monedero.servicios.ClienteService;
import monedero.servicios.PuntosService;
import monedero.servicios.TransaccionService;

public class Sistema {

    private static Sistema instancia;

    //Implementamos el Patrón Singleton
    private final ClienteService clienteService;
    private final PuntosService puntosService;
    private final TransaccionService transaccionService;


    private Sistema() {
        clienteService = new ClienteService();
        puntosService = new PuntosService();
        transaccionService = new TransaccionService();
    }

    public static Sistema getInstancia() {
        if (instancia == null) {
            instancia = new Sistema();
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
}