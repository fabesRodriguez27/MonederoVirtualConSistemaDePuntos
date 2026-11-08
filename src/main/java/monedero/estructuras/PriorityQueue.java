package monedero.estructuras;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PriorityQueue<T> implements Iterable<T>{

    private NodePriorityQueue<T> nodoPrimero;
    private NodePriorityQueue<T> nodoUltimo;
    private int size;
    //Recibe Cualquier Comparador (desde que implemente Comparator y tenga el método compare
    private final Comparator<T> comparador;

    public PriorityQueue(Comparator<T> comparador) {
        this.comparador = comparador;
    }

    public void encolar(T dato){
        NodePriorityQueue<T> nuevoNodo =  new NodePriorityQueue<>(dato);
        if(isEmpty()){
            nodoPrimero = nuevoNodo;
            nodoUltimo = nuevoNodo;
        }else if(comparador.compare(dato, nodoPrimero.getDato()) < 0){ //Cuando Va de Primero
            nuevoNodo.setSiguiente(nodoPrimero);
            nodoPrimero = nuevoNodo;
        }else if(comparador.compare(dato, nodoUltimo.getDato()) > 0){  //Cuando Va de último
            nodoUltimo.setSiguiente(nuevoNodo);
            nodoUltimo = nuevoNodo;
        }else{ //Agregar dentro de un Rango - 6 - 7 - 8 - 10  (Agregar 9)
            NodePriorityQueue<T> actual = nodoPrimero;
            while (comparador.compare(actual.getSiguiente().getDato(), dato) <= 0) {
                actual = actual.getSiguiente();
            }
            nuevoNodo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevoNodo);
        }
        size ++;
    }

    public T desencolar(){
        if (isEmpty()) {
            throw new NoSuchElementException("No hay elementos en la cola");
        }
        T dato = nodoPrimero.getDato();
        nodoPrimero = nodoPrimero.getSiguiente();
        if (nodoPrimero == null){
            nodoUltimo = null;
        }
        size --;
        return dato;
    }

    //Mirar el Primero sin Sacarlo
    public T verPrimero(){
        if(isEmpty()){
            throw new NoSuchElementException("No hay elementos en la cola");
        }else{
            return nodoPrimero.getDato();
        }
    }

    public boolean eliminar(T dato){
        if(isEmpty()){
            throw new NoSuchElementException("No hay elementos en la cola");
        }else{
            NodePriorityQueue<T> actual = nodoPrimero;
            NodePriorityQueue<T> anterior = null;
            while(actual != null){
                if(actual.getDato().equals(dato)){
                    if(anterior == null){
                        nodoPrimero = actual.getSiguiente();
                    }else{
                        anterior.setSiguiente(actual.getSiguiente());
                    }
                    if(actual.getSiguiente() == null){
                        nodoUltimo = anterior;
                    }
                    size --;
                    return true;
                }
                anterior = actual;
                actual = actual.getSiguiente();
            }
            return false;
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isEmpty(){
        return this.size == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodePriorityQueue<T> actual = nodoPrimero;

            public boolean hasNext() {
                return actual != null;
            }

            public T next() {
                T dato = actual.getDato();
                actual = actual.getSiguiente();
                return dato;
            }
        };
    }
}
