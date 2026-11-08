package monedero.servicios;
import monedero.modelos.Cliente;
import java.util.HashMap;

public class ClienteService {

    private final HashMap<String, Cliente> clientes = new HashMap<>();

    public ClienteService() {
    }

    // ===================== CLIENTES =====================

    public Cliente buscarCliente(String idCliente) {
        Cliente c = clientes.get(idCliente);
        if (c == null) {
            throw new IllegalArgumentException("No existe un cliente con id: " + idCliente);
        }
        return c;
    }

    public boolean registrarCliente(String idCliente, String password, String nombre) {
        if (clientes.containsKey(idCliente)) {
            return false;                          // ya existe ese usuario
        }
        Cliente clienteNuevo = new Cliente(idCliente, password, nombre);
        clientes.put(idCliente, clienteNuevo);
        //Aquí se Registra También en el Árbol creo
        return true;
    }

    public Cliente validarIngreso(String idCliente, String passwordCliente) {
        Cliente clienteHallado = clientes.get(idCliente);
        if (clienteHallado != null && clienteHallado.getPassword().equals(passwordCliente)) {
            System.out.println("Cliente Hallado y Clave Correcta");
            return clienteHallado;
        }else{
            System.out.println("Credenciales Invalidas");
            return null;
        }
    }

    //No Creo que se Deba de Eliminar (es más una función del Admin) pero se crea
    public boolean eliminarCliente(String idCliente){
        Cliente clienteHallado = clientes.get(idCliente);
        if(clienteHallado == null){

            return false;
        }else if(clienteHallado.getSaldoTotal() > 0 || !clienteHallado.getTransaccionesProgramadas().isEmpty()){
            return false;
        }else{
            clientes.remove(idCliente);
            //Eliminar del Árbol
            return true;
        }
    }

    public boolean existeCliente(String idCliente) {
        return clientes.get(idCliente) != null;
    }

    public HashMap<String, Cliente> obtenerClientes() {
        return clientes;
    }


}
