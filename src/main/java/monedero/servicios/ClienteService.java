package monedero.servicios;
import monedero.application.MonederoVirtual;
import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.utilidades.Rango;

import java.util.HashMap;

public class ClienteService {

    private final HashMap<String, Cliente> clientes = new HashMap<>();

    public ClienteService() {
    }

    // ===================== CLIENTES =====================

    public Cliente buscarCliente(String idCliente) {
        Cliente c = clientes.get(idCliente);
        if (c == null) {
            throw new IllegalArgumentException("No Existe un Cliente con Id: " + idCliente);
        }
        return c;
    }

    public boolean registrarCliente(String idCliente, String nombre) {
        if (clientes.containsKey(idCliente)) {
            return false;                          // ya existe ese usuario
        }
        Cliente clienteNuevo = new Cliente(idCliente, nombre);
        clientes.put(idCliente, clienteNuevo);
        MonederoVirtual.getInstancia().getPuntosService().registrarCliente(clienteNuevo);
        return true;
    }

    public boolean eliminarCliente(String idCliente){
        Cliente clienteHallado = clientes.get(idCliente);
        if(clienteHallado == null){
            return false;
        }else if(!clienteHallado.getTransaccionesProgramadas().isEmpty()){
            return false;
        }else{
            clientes.remove(idCliente);
            MonederoVirtual.getInstancia().getPuntosService().eliminarCliente(clienteHallado);
            return true;
        }
    }

    public Rango consultarRango(Cliente cliente) {
        return cliente.getRango();
    }

    public boolean existeCliente(String idCliente) {
        return clientes.get(idCliente) != null;
    }

    public LinkedList<Cliente> obtenerListaClientes() {
        LinkedList<Cliente> resultado = new LinkedList<>();
        for (Cliente cliente : clientes.values()) {
            resultado.addLast(cliente);
        }
        return resultado;
    }

    public boolean crearMonedero(String idCliente, String nombre, double saldo) {
        Cliente cliente = buscarCliente(idCliente);
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Escribe un nombre para el monedero.");
        }
        return cliente.addMonedero(new Monedero(nombre.trim(), saldo));
    }

    public boolean eliminarMonedero(String idCliente, String idMonedero) {
        return buscarCliente(idCliente).eliminarMonedero(idMonedero);
    }

    public LinkedList<Monedero> obtenerMonederos(String idCliente) {
        LinkedList<Monedero> resultado = new LinkedList<>();
        for (Monedero monedero : buscarCliente(idCliente).getMonederos()) {
            resultado.addLast(monedero);
        }
        return resultado;
    }
}
