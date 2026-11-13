package monedero.servicios;

import monedero.comparadores.ComparadorPorPuntos;
import monedero.estructuras.BinaryTree;
import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.utilidades.Rango;

public class PuntosService {

    private final BinaryTree<Cliente> clientesPorPuntos;

    public PuntosService() {
        this.clientesPorPuntos =
                new BinaryTree<>(new ComparadorPorPuntos());

    }

    // CALCULAR RANGO

    public static Rango calcularRango(Cliente cliente) {

        int puntos = cliente.getPuntos();

        if (puntos <= 500) {
            return Rango.BRONCE;
        } else if (puntos <= 1000) {
            return Rango.PLATA;
        } else if (puntos <= 5000) {
            return Rango.ORO;
        } else {
            return Rango.PLATINO;
        }
    }

    //REGISTRAR EN ARBOL

    public void registrarCliente(Cliente cliente) {
        cliente.setRango(calcularRango(cliente));
        clientesPorPuntos.insertar(cliente);
    }

    // ACTUALIZAR PUNTOS Y RANGO
    public void actualizarPuntos(Cliente cliente, int variacion) {

        if (clientesPorPuntos.buscarNodo(cliente) == null) {
            throw new IllegalArgumentException(
                    "El cliente no está registrado en el árbol"
            );
        }

        int nuevosPuntos = Math.addExact(
                cliente.getPuntos(), variacion
        );

        if (nuevosPuntos < 0) {
            throw new IllegalArgumentException(
                    "Los puntos no pueden ser negativos"
            );
        }

        // Eliminar de ambos árboles antes de modificar
        clientesPorPuntos.eliminar(cliente);

        // Actualizar puntos y rango
        cliente.setPuntos(nuevosPuntos);

        // Reinsertar en ambos árboles
        clientesPorPuntos.insertar(cliente);

    }

    //  RECORRIDOS inOrden

    public LinkedList<Cliente> obtenerClientesPorPuntos() {
        return clientesPorPuntos.inOrden();
    }

    // CONSULTAR TAMAÑOS

    public int cantidadClientesPorPuntos() {
        return clientesPorPuntos.obtenerPeso();
    }

    void eliminarCliente(Cliente cliente) {
        clientesPorPuntos.eliminar(cliente);
    }
}
