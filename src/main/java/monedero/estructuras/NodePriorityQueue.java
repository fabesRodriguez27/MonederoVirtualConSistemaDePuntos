package monedero.estructuras;

public class NodePriorityQueue<T>{
    private T dato;
    private NodePriorityQueue<T> siguiente;

    //Nodo siguiente creado en el constructor como null
    public NodePriorityQueue(T dato) {
        this.dato = dato;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public NodePriorityQueue<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodePriorityQueue<T> siguiente) {
        this.siguiente = siguiente;
    }

}
