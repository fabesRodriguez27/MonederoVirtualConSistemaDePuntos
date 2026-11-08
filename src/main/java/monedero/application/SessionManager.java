package monedero.application;
import monedero.modelos.Cliente;

public class SessionManager {
    private static SessionManager instancia;
    private Cliente clienteLogueado;

    private SessionManager() {
    }

    public static SessionManager getInstancia() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    public void iniciarSesion(Cliente cliente) {
        this.clienteLogueado = cliente;
        System.out.println("Sesión iniciada: " + cliente.getIdCliente());
    }

    public void cerrarSesion() {
        System.out.println("Cerrando Sesión para Cliente: " + clienteLogueado.getIdCliente());
        this.clienteLogueado = null;
    }

    public Cliente getClienteLogueado() {
        return clienteLogueado;
    }
}

