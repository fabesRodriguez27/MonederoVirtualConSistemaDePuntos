package monedero.servicios.facade;

import monedero.application.MonederoVirtual;
import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.Usuario;
import monedero.utilidades.Categoria;
import monedero.utilidades.Frecuencia;
import monedero.utilidades.Rol;
import monedero.utilidades.TipoTransaccion;

import java.time.LocalDateTime;

public class ClienteFacade {
    private final MonederoVirtual monederoVirtual = MonederoVirtual.getInstancia();

    public LinkedList<Monedero> obtenerMonederos(String idCliente) {
        return monederoVirtual.getClienteService()
                .obtenerMonederos(idCliente);
    }

    public boolean crearMonedero(String idCliente,String nombre, double saldo) {
        return monederoVirtual.getClienteService().crearMonedero(idCliente,nombre,saldo);
    }
    public boolean eliminarMonedero(String idCliente,String idMonedero) {
        return monederoVirtual.getClienteService().eliminarMonedero(idCliente,idMonedero);
    }

    public boolean programarTransaccion(String idCliente, TipoTransaccion tipoTransaccion, String origen, String idClienteDestino ,String destino, double monto, Categoria categoria,
                                        LocalDateTime proximaEjecucion, Frecuencia frecuencia){

        return monederoVirtual.getProgramacionService().programarTransaccion(idCliente, tipoTransaccion, origen, idClienteDestino,destino, monto, categoria, proximaEjecucion,frecuencia);
    }

    public boolean eliminarTransaccion(String idCliente, String idTransaccion) {
        return monederoVirtual.getProgramacionService().eliminarTransaccion(idCliente,idTransaccion);
    }
}
