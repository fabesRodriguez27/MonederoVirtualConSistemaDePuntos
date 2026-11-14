package monedero.servicios.facade;

import monedero.application.MonederoVirtual;
import monedero.estructuras.LinkedList;
import monedero.modelos.Notificacion;

public class NotificacionFacade {
    private final MonederoVirtual monederoVirtual = MonederoVirtual.getInstancia();

    public LinkedList<Notificacion> obtenerNotificaciones(String idCliente) {
        return monederoVirtual.getNotificacionService().obtenerNotificaciones(idCliente);
    }

    public int contarNoLeidas(String idCliente) {
        return monederoVirtual.getNotificacionService().contarNoLeidas(idCliente);
    }

    public void marcarTodasLeidas(String idCliente) {
        monederoVirtual.getNotificacionService().marcarTodasLeidas(idCliente);
    }
}