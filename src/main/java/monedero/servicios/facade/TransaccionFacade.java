package monedero.servicios.facade;
import monedero.application.MonederoVirtual;
import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.utilidades.Categoria;

public class TransaccionFacade {
    private final MonederoVirtual monederoVirtual = MonederoVirtual.getInstancia();

    public boolean depositar(String idCliente, String idMonedero, double monto) {
        return monederoVirtual.getTransaccionService().depositar(idCliente, idMonedero, monto);
    }

    public boolean retirar(String idCliente, String idMonedero, double monto, Categoria categoria) {
        return monederoVirtual.getTransaccionService().retirar(idCliente, idMonedero, monto, categoria);
    }

    public boolean transferir(String idClienteOrigen, String idMonederoOrigen, String idClienteDestino, String idMonederoDestino, double monto, Categoria categoria) {
        return monederoVirtual.getTransaccionService().transferir(idClienteOrigen, idMonederoOrigen, idClienteDestino, idMonederoDestino, monto, categoria);
    }

    public Cliente buscarCliente(String idCliente) {
        return monederoVirtual.getClienteService().buscarCliente(idCliente);
    }

    public LinkedList<Monedero> obtenerMonederos(String idCliente) {
        return monederoVirtual.getClienteService().obtenerMonederos(idCliente);
    }

    public float tarifaRetiro() {
        return monedero.servicios.TarifaService.getTarifaRetiro();
    }

    public float tarifaTransferencia() {
        return monedero.servicios.TarifaService.getTarifaTransferencia();
    }

    public boolean revertirUltimaTransaccion(String idCliente ){
        return monederoVirtual.getTransaccionService().revertirUltimaOperacion(idCliente);
    }
}
