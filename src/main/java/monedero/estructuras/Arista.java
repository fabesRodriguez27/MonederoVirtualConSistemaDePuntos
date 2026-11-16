package monedero.estructuras;

public class Arista<T> {

    private final Vertice<T> destino;   // a dónde apunta la flecha (Arista)
    private double peso;                // valor acumulado
    private int cantidad;               // cuántas veces se recorrió (ej. número de transferencias)

    public Arista(Vertice<T> destino) {
        this.destino = destino;
        this.peso = 0;
        this.cantidad = 0;
    }

    //Si la arista ya existe, le sumo a su valor y aumento la cantidad (cuantas veces he hecho la operación de un vértice a otro)
    public void sumar(double valor) {
        this.peso += valor;
        this.cantidad++;
    }

    public Vertice<T> getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public int getCantidad() {
        return cantidad;
    }
}