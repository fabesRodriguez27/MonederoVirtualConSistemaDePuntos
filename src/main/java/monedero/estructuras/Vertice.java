package monedero.estructuras;

//Un punto del Grafo (como un nodo)
public class Vertice<T> {

    private final T dato;
    private final LinkedList<Arista<T>> adyacentes = new LinkedList<>();

    // Lo que ENTRA se va sumando cada vez que alguien apunta a este vértice,
    // así consultar "cuánto recibió" es O(1) en vez de recorrer todo el grafo.
    private double totalEntrante;
    private int cantidadEntrante;

    public Vertice(T dato) {
        this.dato = dato;
    }

    //Buscamos si desde este vértice ya se tiene una salida (arista) hacia el destino
    public Arista<T> buscarAristaHacia(Vertice<T> destino) {
        for (Arista<T> arista : adyacentes) {
            if (arista.getDestino() == destino) {
                return arista;
            }
        }
        return null;
    }

    public void registrarEntrada(double peso) {
        totalEntrante += peso;
        cantidadEntrante++;
    }

    public T getDato() {
        return dato;
    }

    public LinkedList<Arista<T>> getAdyacentes() {
        return adyacentes;
    }

    public double getTotalEntrante() {
        return totalEntrante;
    }

    public int getCantidadEntrante() {
        return cantidadEntrante;
    }

    //Suma de los pesos de las flechas que salen
    public double getTotalSaliente() {
        double total = 0;
        for (Arista<T> arista : adyacentes) {
            total += arista.getPeso();
        }
        return total;
    }
}