package monedero.estructuras;

public class ListaEnlazada<T> {

    private static class Node<T> {
        private T data;
        private Node<T> next;

        Node(T data) {
            this.data = data;
        }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        public Node<T> getNext() { return next; }
        public void setNext(Node<T> next) { this.next = next; }
    }

    private Node<T> first;
    private Node<T> last;
    private int size;

    public void addLast(T data) {  /// agregar al final
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            first = newNode;
            last = newNode;
        } else {
            last.setNext(newNode);
            last = newNode;
        }
        size++;
    }

    public void addFirst(T data) {  /// agregar al inicio
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            first = newNode;
            last = newNode;
        } else {
            newNode.setNext(first);
            first = newNode;
        }
        size++;
    }

    public boolean contains(T data) {  /// el nodo esta en la lista
        Node<T> current = first;
        while (current != null) {
            if (current.getData().equals(data)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    public T get(int index) {  /// indice del nodo (recorre nodo por nodo O(n))
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> current = first;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    public boolean remove(T data) {  /// Eliminar
        Node<T> previous = null;
        Node<T> current = first;
        while (current != null) {
            if (current.getData().equals(data)) {
                if (previous == null) {
                    first = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }
                if (current == last) {
                    last = previous;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.getNext();
        }
        return false;
    }

    public int getSize() { return size; } /// Tamaño

    public boolean isEmpty() { return size == 0; }  /// Esta vacia?
}