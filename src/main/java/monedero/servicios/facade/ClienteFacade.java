package monedero.servicios.facade;

import monedero.application.MonederoVirtual;
import monedero.modelos.Cliente;
import monedero.modelos.Usuario;
import monedero.utilidades.Rol;

public class ClienteFacade {
    private final MonederoVirtual monederoVirtual = MonederoVirtual.getInstancia();

    public boolean crearMonedero(String idCliente,String nombre, double saldo) {
        return monederoVirtual.getClienteService().crearMonedero(idCliente,nombre,saldo);
    }
    public boolean eliminarMonedero(String idCliente,String idMonedero) {
        return monederoVirtual.getClienteService().eliminarMonedero(idCliente,idMonedero);
    }
}
