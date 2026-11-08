package monedero.estructuras;

public class Stack<T> {

    private int size;
    private NodeStack<T> cima;

    public Stack() {
        this.size = 0;
        this.cima = null;
    }

    public void apilar(T dato){
        NodeStack<T> nuevoNodo = new NodeStack<T>(dato);
        if(isEmpty()){
            cima = nuevoNodo;
        }else{
            nuevoNodo.setSiguiente(cima);
            cima = nuevoNodo;
        }
        size++;
    }

    public T desapilar(){
        if(cima == null){
            throw new RuntimeException("No Hay Elementos en la Pila");
        }else {
            T data = cima.getDato();
            cima = cima.getSiguiente();
            size--;
            return data;
        }
    }

    public void addPila(Stack<T> otra) {
        if (otra.isEmpty()) {
            return;
        }
        // Recorro la Otra Pila Pasando Sus Datos a Una Auxiliar para Invertir El Orden
        Stack<T> aux = new Stack<>();
        NodeStack<T> actual = otra.cima;
        while (actual != null) {
            aux.apilar(actual.getDato());
            actual = actual.getSiguiente();
        }

        // Paso los datos de la Pila Auxiliar que Creé a mi Pila
        while (!aux.isEmpty()) {
            this.apilar(aux.desapilar());
        }
    }

    public Stack<T> clonar(){
        Stack<T> pilaClonada = new Stack<>();
        // Retorno Vacía
        if(isEmpty()){
            return pilaClonada;
        }
        // Recorro mi pila pasando los datos a una auxiliar (los invierto)
        Stack<T> aux = new Stack<>();
        NodeStack<T> actual = cima;
        while(actual != null){
            aux.apilar(actual.getDato());
            actual = actual.getSiguiente();
        }
        // Paso los datos de la auxiliar a la clonada (quedan en el orden original)
        while(!aux.isEmpty()){
            pilaClonada.apilar(aux.desapilar());
        }
        return pilaClonada;
    }

    public void imprimirPila(){
        if(isEmpty()){
            System.out.println("La Pila Está Vacía");
            return;
        }
        NodeStack<T> actual = cima;
        while(actual != null){
            System.out.print(actual.getDato());
            if(actual.getSiguiente() != null){
                System.out.print(" -> ");
            }
            actual = actual.getSiguiente();
        }
        System.out.println();
    }

    public boolean isEmpty(){
        return cima == null;
    }

    public void limpiarPila(){
        cima = null;
        size = 0;
    }
}

