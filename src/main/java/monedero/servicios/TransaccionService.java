package monedero.servicios;

import monedero.estructuras.AVLTree;
import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.Transaccion;
import monedero.comparadores.ComparadorPorTransaccion;
import monedero.estructuras.AVLTree;
import monedero.modelos.TransaccionProgramada;
import monedero.utilidades.Categoria;

import java.time.LocalDateTime;

public class TransaccionService {

    // Historial global de transacciones de todos los clientes
    private AVLTree<Transaccion> historialTransacciones;

    public TransaccionService() {
        this.historialTransacciones =
                new AVLTree<>(new ComparadorPorTransaccion());
    }

    // Registrar una transacción en el historial global
    public boolean registrarTransaccion(Transaccion transaccion) {

        if (transaccion == null) {
            return false;
        }

        historialTransacciones.insertar(transaccion);

        return true;
    }

    // Depositar dinero
    public boolean depositar(
            String idCliente,
            String idMonedero,
            double valor) {

        // Implementar lógica del depósito
        return false;
    }

    // Retirar dinero
    public boolean retirar(
            String idCliente,
            String idMonedero,
            double valor) {

        // Implementar lógica del retiro
        return false;
    }

    // Transferir dinero
    public boolean transferir(
            String idClienteOrigen,
            String idMonederoOrigen,
            String idClienteDestino,
            String idMonederoDestino,
            double valor,
            Categoria categoria) {

        // Implementar lógica de la transferencia
        return false;
    }

    // Consultar historial de un cliente
    public LinkedList<Transaccion> verHistorialTransacciones(String idCliente) {

        // Recorrer el AVL y seleccionar las transacciones del cliente
        return null;
    }

    // Programar una transacción
    public boolean programarTransaccion(
            String idCliente,
            TransaccionProgramada programada) {

        return false;
    }

    // Cancelar una transacción programada
    public boolean cancelarTransaccionProgramada(
            String idCliente,
            String idProgramacion) {

        return false;
    }

    // Procesar transacciones programadas
    public boolean procesarTransaccionesProgramadas(
            LocalDateTime fecha) {

        return false;
    }

    // Revertir última operación de un cliente
    public boolean revertirUltimaOperacion(String idCliente) {

        return false;
    }

    // Verificar una transacción
    public boolean verificarTransaccion(Transaccion transaccion) {
        return false;
    }

    // Verificar integridad del historial
    public boolean verificarIntegridadHistorial(String idCliente) {
        return false;
    }
}