package monedero.modelos;

import java.util.Random;

public class Monedero {
    private String id;
    private String nombre;
    private float saldo;

    public Monedero(String nombre, float saldo) {
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

    public float getSaldo() {
        return saldo;
    }

    public void setSaldo(float saldo) {
        this.saldo = saldo;
    }
}
