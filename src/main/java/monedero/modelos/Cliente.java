package monedero.modelos;
import monedero.comparadores.ComparadorProximaEjecucion;
import monedero.estructuras.LinkedList;
import monedero.estructuras.PriorityQueue;
import monedero.utilidades.Rango;

import java.util.Iterator;

public class Cliente {
    private String idCliente;
    private String password;
    private String nombre;
    private int puntos;
    private Rango rango;
    private LinkedList<Monedero> monederos;
    private PriorityQueue<TransaccionProgramada> transaccionesProgramadas;

    public Cliente(String idCliente, String password, String nombre) {
        this.idCliente = idCliente;
        this.password = password;
        this.nombre = nombre;
        this.puntos = 0;
        this.rango = Rango.BRONCE; //Cuando Inicia No Tiene Puntos. Ya Luego se Recalcula
        this.monederos = new LinkedList<>();
        this.transaccionesProgramadas = new PriorityQueue<>(new ComparadorProximaEjecucion());
        //Modero Principal
        this.monederos.addLast(new Monedero("Principal", 0));
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public double getSaldoTotal() {
        double total = 0;
        Iterator<Monedero> iterador = monederos.iterator();
        while (iterador.hasNext()) {
            Monedero monedero = iterador.next();
            total += monedero.getSaldo();
        }
        return total;
    }

    public PriorityQueue<TransaccionProgramada> getTransaccionesProgramadas() {
        return transaccionesProgramadas;
    }

}
