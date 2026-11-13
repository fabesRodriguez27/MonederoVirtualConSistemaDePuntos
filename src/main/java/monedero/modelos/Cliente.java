package monedero.modelos;
import monedero.comparadores.ComparadorProximaEjecucion;
import monedero.estructuras.LinkedList;
import monedero.estructuras.PriorityQueue;
import monedero.estructuras.Stack;
import monedero.utilidades.Rango;

import java.util.Iterator;
import java.util.Objects;

public class Cliente {
    private String idCliente;
    private String nombre;
    private int puntos;
    private Rango rango;

    private LinkedList<Monedero> monederos;
    private PriorityQueue<TransaccionProgramada> transaccionesProgramadas;
    private final LinkedList<Transaccion> historialTransacciones = new LinkedList<>(); // Historial de transacciones
    private final Stack<Transaccion> transaccionesReversibles = new Stack<>(); // Pila de transacciones que se pueden revertir

    public Cliente(String idCliente, String nombre) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.puntos = 0;
        this.rango = Rango.BRONCE; //Cuando Inicia No Tiene Puntos. Ya Luego se Recalcula
        this.monederos = new LinkedList<>();
        this.transaccionesProgramadas = new PriorityQueue<>(new ComparadorProximaEjecucion());
        //Monedero Principal
        this.monederos.addLast(new Monedero("Principal", 0));
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public Rango getRango() {
        return rango;
    }

    public void setRango(Rango rango) {
        this.rango = rango;
    }

    public LinkedList<Monedero> getMonederos() {
        return monederos;
    }

    public boolean addMonedero(Monedero monedero) {
        Monedero principal = monederos.get(0);
        double saldoInicial = monedero.getSaldo();

        if (!Double.isFinite(saldoInicial) || saldoInicial < 0) {
            throw new IllegalArgumentException("El saldo inicial debe ser válido y no negativo.");
        }

        if (saldoInicial > principal.getSaldo()) {
            throw new IllegalArgumentException("Saldo insuficiente en el monedero principal.");
        }

        monederos.addLast(monedero);
        principal.setSaldo(principal.getSaldo() - saldoInicial);
        return true;
    }

    public boolean eliminarMonedero(String idMonedero) {
        Monedero principal = monederos.get(0);

        if (principal.getId().equals(idMonedero)) {
            throw new IllegalArgumentException("No se puede eliminar el monedero principal.");
        }

        Monedero encontrado = null;
        for (Monedero actual : monederos) {
            if (actual.getId().equals(idMonedero)) {
                encontrado = actual;
                break;
            }
        }

        if (encontrado == null || !monederos.remove(encontrado)) {
            return false;
        }

        principal.setSaldo(principal.getSaldo() + encontrado.getSaldo());
        encontrado.setSaldo(0);
        return true;
    }

    public double getSaldoTotal() {
        double total = 0;
        Iterator<Monedero> iterador = monederos.iterator();
        while (iterador.hasNext()) {
            Monedero monedero = iterador.next();
            total += monedero.getSaldo();
        }
        return total;
    }

    public double getSaldoMonederoPrincipal(){
        return monederos.get(0).getSaldo();
    }

    public LinkedList<Transaccion> getHistorialTransacciones(){
        return historialTransacciones;
    }

    public PriorityQueue<TransaccionProgramada> getTransaccionesProgramadas() {
        return transaccionesProgramadas;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return puntos == cliente.puntos && Objects.equals(idCliente, cliente.idCliente) && Objects.equals(nombre, cliente.nombre) && rango == cliente.rango && Objects.equals(monederos, cliente.monederos) && Objects.equals(transaccionesProgramadas, cliente.transaccionesProgramadas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCliente, nombre, puntos, rango, monederos, transaccionesProgramadas);
    }


}
