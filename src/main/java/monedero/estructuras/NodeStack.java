package monedero.estructuras;

public class NodeStack<T> {

    private T dato;
    private NodeStack<T> siguiente;

    //Nodo siguiente creado en el constructor como null
    public NodeStack(T dato) {
        this.dato = dato;
    }

    public T getDato() {
        return dato;
    }

    public void setDato(T dato) {
        this.dato = dato;
    }

    public NodeStack<T> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(NodeStack<T> siguiente) {
        this.siguiente = siguiente;
    }
}
