package monedero.servicios;
import monedero.application.MonederoVirtual;
import monedero.estructuras.AVLTree;
import monedero.estructuras.LinkedList;
import monedero.estructuras.Stack;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.Transaccion;
import monedero.comparadores.ComparadorPorTransaccion;
import monedero.modelos.TransaccionProgramada;
import monedero.utilidades.Categoria;
import monedero.utilidades.Rango;
import monedero.utilidades.TipoNotificacion;
import monedero.utilidades.TipoTransaccion;
import java.time.Duration;
import java.time.LocalDateTime;

public class TransaccionService {

    // Historial global de transacciones de todos los clientes
    private AVLTree<Transaccion> historialTransacciones;
    private static final Duration TIEMPO_MAXIMO_REVERSION = Duration.ofMinutes(2);

    public TransaccionService() {
        this.historialTransacciones =  new AVLTree<>(new ComparadorPorTransaccion());
    }

    private ClienteService clientes() {
        return MonederoVirtual.getInstancia().getClienteService();
    }


    public boolean depositar(String idCliente, String idMonedero, double valor) {
        validarMonto(valor);
        Cliente cliente = clientes().buscarCliente(idCliente);
        Monedero destino = obtenerMonedero(cliente, idMonedero);
        Transaccion transaccion = new Transaccion(TipoTransaccion.DEPOSITO, null, destino, valor, Categoria.OTROS);
        transaccion.setIdCliente(idCliente);
        //Le Sumo Valor al Monedero al Que le Estoy Depositando
        destino.setSaldo(destino.getSaldo() + valor);
        finalizar(cliente, transaccion);
        return true;
    }

    public boolean retirar(String idCliente, String idMonedero, double valor, Categoria categoria) {
        validarMonto(valor);
        Cliente cliente = clientes().buscarCliente(idCliente);
        Monedero origen = obtenerMonedero(cliente, idMonedero);
        Transaccion transaccion = new Transaccion(TipoTransaccion.RETIRO, origen, null, valor, categoria);
        transaccion.setIdCliente(idCliente);
        //Le Sumamos al Valor del Retiro el Costo de La Comisión
        double cargo = valor + transaccion.getComision();
        if (origen.getSaldo() < cargo){
            throw new IllegalArgumentException("Saldo Insuficiente: Necesitas " + cargo + " (Monto + Comisión).");
        }else{
            //Modificamos el Saldo del Monedero
            origen.setSaldo(origen.getSaldo() - cargo);
            finalizar(cliente, transaccion);
            return true;
        }
    }

    public boolean transferir(String idClienteOrigen, String idMonederoOrigen, String idClienteDestino, String idMonederoDestino, double valor, Categoria categoria) {
        validarMonto(valor);
        Cliente emisor = clientes().buscarCliente(idClienteOrigen);
        Cliente receptor = clientes().buscarCliente(idClienteDestino);
        Monedero origen = obtenerMonedero(emisor, idMonederoOrigen);
        Monedero destino = obtenerMonedero(receptor, idMonederoDestino);
        if (origen == destino) {
            throw new IllegalArgumentException("El Origen y el Destino NO Pueden Ser el Mismo Monedero");
        }
        Transaccion transaccion = new Transaccion(TipoTransaccion.TRANSFERENCIA, origen, destino, valor, categoria);
        transaccion.setIdCliente(idClienteOrigen);
        boolean interna = emisor == receptor;   // entre mis propios monederos
        //Si la Transacción es de Para uno Mismo (Mover Dinero)
        if (interna) {
            transaccion.setComision(0);
            transaccion.setPuntos(0);
        }
        double cargo = valor + transaccion.getComision();
        if (origen.getSaldo() < cargo){
            throw new IllegalArgumentException("Saldo Insuficiente: Necesitas " + cargo + " (Monto + Comisión)");
        }
        origen.setSaldo(origen.getSaldo() - cargo);
        destino.setSaldo(destino.getSaldo() + valor);
        finalizar(emisor, transaccion);
        //El Receptor También Recibe la Transacción
        if (!interna){
            receptor.getHistorialTransacciones().addLast(transaccion);
            MonederoVirtual.getInstancia().getNotificacionService().notificar(receptor,
                    TipoNotificacion.TRANSFERENCIA_RECIBIDA, "Recibiste " + String.format("$%,.2f", valor) + " de " + emisor.getNombre() + " en tu monedero " + destino.getNombre());
        }
        return true;
    }

    private void validarMonto(double valor) {
        if (valor <= 0)
            throw new IllegalArgumentException("El Monto Debe Ser Mayor que Cero.");
    }

    private Monedero obtenerMonedero(Cliente cliente, String idMonedero) {
        Monedero monedero = cliente.buscarMonedero(idMonedero);
        if (monedero == null){
            throw new IllegalArgumentException("El Monedero " + idMonedero + " No Existe.");
        }
        return monedero;
    }

    //Agregamos la Transacción  en Todo Lado
    private void finalizar(Cliente cliente, Transaccion transaccion) {
        Rango rangoAnterior = cliente.getRango();
        registrarTransaccion(transaccion);                          // AVL (Panel de Control)
        cliente.getHistorialTransacciones().addLast(transaccion);   // Lista del cliente
        cliente.getTransaccionesReversibles().apilar(transaccion);  // Pila del Cliente para Revertir
        //Actualizar Puntos del Cliente, Rango del Cliente y Agregar al Árbol Binario
        MonederoVirtual.getInstancia().getPuntosService().actualizarPuntos(cliente, transaccion.getPuntos());
        MonederoVirtual.getInstancia().getNotificacionService().notificar(cliente, TipoNotificacion.TRANSACCION, describir(transaccion) + "Ganaste " + transaccion.getPuntos() + " puntos");
        MonederoVirtual.getInstancia().getNotificacionService().verificarSaldoBajo(cliente, transaccion.getOrigen());
        avisarCambioRango(cliente, rangoAnterior);
    }

    private String describir(Transaccion transaccion) {
        String monto = String.format("$%,.2f", transaccion.getMonto());
        return switch (transaccion.getTipoTransaccion()) {
            case DEPOSITO -> "Depósito de " + monto + " en " + transaccion.getDestino().getNombre() + "\n";
            case RETIRO -> "Retiro de " + monto + " de \"" + transaccion.getOrigen().getNombre() + "\n";
            case TRANSFERENCIA -> "Transferencia de " + monto + " desde \"" + transaccion.getOrigen().getNombre() + "\n";
        };
    }

    private void avisarCambioRango(Cliente cliente, Rango rangoAnterior) {
        if (cliente.getRango() != rangoAnterior) {
            MonederoVirtual.getInstancia().getNotificacionService().notificar(cliente,
                    TipoNotificacion.CAMBIO_RANGO,
                    "Tu Rango Cambió de " + rangoAnterior + " a " + cliente.getRango());
        }
    }

    // Registrar una transacción en el historial global
    public boolean registrarTransaccion(Transaccion transaccion) {
        if (transaccion == null) {
            return false;
        }
        historialTransacciones.insertar(transaccion);
        return true;
    }

    public LinkedList<Transaccion> obtenerHistorialPorMonto() {
        return historialTransacciones.inOrden();
    }

    public boolean revertirUltimaOperacion(String idCliente) {
        Cliente cliente = clientes().buscarCliente(idCliente);
        Stack<Transaccion> pila = cliente.getTransaccionesReversibles();

        if (pila.isEmpty()) {
            throw new IllegalArgumentException("No Hay Transacciones para Revertir");
        }
        Transaccion transaccionReversible = pila.desapilar();
        Duration transcurrido = Duration.between(transaccionReversible.getFecha(), LocalDateTime.now());
        if (transcurrido.compareTo(TIEMPO_MAXIMO_REVERSION) > 0) {
            pila.limpiarPila();
            throw new IllegalArgumentException("Solo Se Puede Revertir Una Transacción Durante Los 2 Minutos Siguientes a Realizarla");
        }
        Monedero origen = transaccionReversible.getOrigen();
        Monedero destino = transaccionReversible.getDestino();
        try {
            //Así no tenga origen (como el Depósito) el destino si debe ser parte de los monederos del cliente
            if (origen != null && cliente.buscarMonedero(origen.getId()) == null) {
                throw new IllegalArgumentException("No Existe el Monedero");
            }
            if (destino != null && destino.getSaldo() < transaccionReversible.getMonto()) {
                throw new IllegalArgumentException("No Se Puede Revertir: El Monedero Destino Ya No Tiene ese Dinero");
            }
            //Se revierte la Operación
            switch(transaccionReversible.getTipoTransaccion()){
                case DEPOSITO ->
                    destino.setSaldo(destino.getSaldo() - transaccionReversible.getMonto());
                case RETIRO ->
                    origen.setSaldo(origen.getSaldo() + transaccionReversible.getMonto() + transaccionReversible.getComision() );
                case TRANSFERENCIA ->{
                    destino.setSaldo(destino.getSaldo() - transaccionReversible.getMonto());
                    origen.setSaldo(origen.getSaldo() + transaccionReversible.getMonto() + transaccionReversible.getComision());
                }
            }
        } catch (IllegalArgumentException e) {
            pila.apilar(transaccionReversible);
            throw e;
        }
        //Quitamos los puntos (obtenemos el menor)
        Rango rangoAnterior = cliente.getRango();
        int puntosAQuitar = Math.min(transaccionReversible.getPuntos(), cliente.getPuntos());
        MonederoVirtual.getInstancia().getPuntosService().actualizarPuntos(cliente, -puntosAQuitar);
        transaccionReversible.setRevertida(true);
        MonederoVirtual.getInstancia().getNotificacionService().notificar(cliente, TipoNotificacion.TRANSACCION, "Revertiste: " + describir(transaccionReversible));
        avisarCambioRango(cliente, rangoAnterior);
        return true;
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

    // Verificar una transacción
    public boolean verificarTransaccion(Transaccion transaccion) {
        return false;
    }

    // Verificar integridad del historial
    public boolean verificarIntegridadHistorial(String idCliente) {
        return false;
    }

}