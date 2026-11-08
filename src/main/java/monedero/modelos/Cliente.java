package monedero.modelos;

import monedero.utilidades.Rango;

public class Cliente {
    private String idCliente;
    private String password;
    private String nombre;
    private int puntos;
    private Rango rango;

    public Cliente(String idCliente, String password, String nombre, Rango rango) {
        this.idCliente = idCliente;
        this.password = password;
        this.nombre = nombre;
        this.puntos = 0;
        this.rango = rango;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
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
}
