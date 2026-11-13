package monedero.servicios.facade;

import monedero.application.MonederoVirtual;
import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.Transaccion;

public class PanelControlFacade {
    private final MonederoVirtual monederoVirtual = MonederoVirtual.getInstancia();

    public LinkedList<Cliente> obtenerClientes() {
        return monederoVirtual.getClienteService().obtenerListaClientes();
    }

    public LinkedList<Cliente> obtenerClientesPorPuntos() {
        return monederoVirtual.getPuntosService().obtenerClientesPorPuntos();
    }

    public LinkedList<Transaccion> obtenerHistorialPorMonto() {
        return monederoVirtual.getTransaccionService().obtenerHistorialPorMonto();
    }

    public LinkedList<Monedero> obtenerMonederos(String idCliente) {
        return monederoVirtual.getClienteService().obtenerMonederos(idCliente);
    }

    public boolean eliminarCliente(String id) {
        return monederoVirtual.getClienteService().eliminarCliente(id);
    }

    public float tarifaRetiro() {
        return monedero.servicios.TarifaService.getTarifaRetiro();
    }

    public float tarifaTransferencia() {
        return monedero.servicios.TarifaService.getTarifaTransferencia();
    }
    public boolean actualizarTarifas(float retiro, float transferencia) {
        return monederoVirtual.getTarifaService().actualizarTarifas(retiro, transferencia);
    }
}
