package monedero.modelos;

import java.util.Random;

public class Monedero {
    private String id;
    private String nombre;
    private double saldo;

    public Monedero(String nombre, double saldo) {
        this.id = generarNumeroMonedero();
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public String getId() {
        return id;
    }

    public String generarNumeroMonedero() {
        Random random = new Random();
        String monedero = "";

        for (int i = 0; i < 10; i++) {
            monedero += random.nextInt(10);
        }
        return monedero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }


    @Override
    public String toString() {
        return nombre;
    }
}
