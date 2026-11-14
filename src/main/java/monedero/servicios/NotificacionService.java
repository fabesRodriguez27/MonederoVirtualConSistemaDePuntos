package monedero.servicios;

import monedero.application.MonederoVirtual;
import monedero.estructuras.CircularList;
import monedero.estructuras.LinkedList;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.Notificacion;
import monedero.utilidades.TipoNotificacion;

public class NotificacionService {

    public static final int CAPACIDAD_MAXIMA = 10;
    public static final double UMBRAL_SALDO_BAJO = 50_000;

    public NotificacionService() {
    }

    public ClienteService clientes(){
        return MonederoVirtual.getInstancia().getClienteService();
    }

    public void notificar(Cliente cliente, TipoNotificacion tipoNotificacion, String mensaje){
        CircularList<Notificacion> notificaciones = cliente.getNotificaciones();
        if(notificaciones.getSize() >= CAPACIDAD_MAXIMA){
            notificaciones.removeLast();
        }
        Notificacion notificacionNueva = new Notificacion(tipoNotificacion, mensaje);
        notificaciones.addFirst(notificacionNueva);
    }

    public void verificarSaldoBajo(Cliente cliente, Monedero monedero){
        if(monedero != null && monedero.getSaldo() < UMBRAL_SALDO_BAJO){
            notificar(cliente, TipoNotificacion.SALDO_BAJO, "Saldo Bajo. Le Queda " + formatearDinero(monedero.getSaldo()));
        }
    }

    private String formatearDinero(double valor) {
        return String.format("$%,.2f", valor);
    }

    public LinkedList<Notificacion> obtenerNotificaciones(String idCliente){
        CircularList<Notificacion> lista = clientes().buscarCliente(idCliente).getNotificaciones();
        LinkedList<Notificacion> notificaciones = new LinkedList<>();
        for(int i=0; i<lista.getSize(); i++){
            notificaciones.addLast(lista.get(i));
        }
        return notificaciones;
    }

    public int contarNoLeidas(String idCliente){
        CircularList<Notificacion> lista = clientes().buscarCliente(idCliente).getNotificaciones();
        int contador = 0;
        for(int i=0; i<lista.getSize(); i++){
            if(!lista.get(i).isLeida()){
                contador++;
            }
        }
        return contador;
    }

    public void marcarTodasLeidas(String idCliente){
        CircularList<Notificacion> lista = clientes().buscarCliente(idCliente).getNotificaciones();
        for(int i=0; i<lista.getSize(); i++){
            lista.get(i).setLeida(true);
        }
    }
}
