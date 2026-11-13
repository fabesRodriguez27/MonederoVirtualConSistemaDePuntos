package monedero.modelos;

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
        if (clienteLogueado != null) {
            System.out.println("Cerrando sesión para cliente: "
                    + clienteLogueado.getIdCliente());
        }
        clienteLogueado = null;
    }

    public Cliente getClienteLogueado() {
        return clienteLogueado;
    }
}

