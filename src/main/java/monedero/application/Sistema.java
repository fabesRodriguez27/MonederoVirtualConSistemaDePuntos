package monedero.application;
import monedero.servicios.ClienteService;

public class Sistema {

    private static Sistema instancia;

    //Implementamos el Patrón Singleton
    private final ClienteService clienteService;


    private Sistema() {
        clienteService = new ClienteService();
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
}