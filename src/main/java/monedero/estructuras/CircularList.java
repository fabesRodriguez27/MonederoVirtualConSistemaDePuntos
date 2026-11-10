package monedero.estructuras;

import java.util.NoSuchElementException;

public class CircularList<T> {

    // Clase interna nodo
    private class Node {
        private T element;
        private Node next;

        public Node(T element) {
            this.element = element;
            this.next = null;
        }
    }

    private Node first;
    private Node last;
    private int size;

    public CircularList() {
        this.first = null;
        this.last = null;
        this.size = 0;
    }

    // está vacía
    public boolean isEmpty() {
        return size == 0;
    }

    // cantidad de elementos
    public int getSize() {
        return size;
    }

    // Inserta un elemento al inicio
    public void addFirst(T element) {
        Node newNode = new Node(element);

        if (isEmpty()) {
            first = newNode;
            last = newNode;
            newNode.next = newNode;
        } else {
            newNode.next = first;
            first = newNode;
            last.next = first;
        }

        size++;
    }

    // Inserta un elemento al final
    public void addLast(T element) {
        Node newNode = new Node(element);

        if (isEmpty()) {
            first = newNode;
            last = newNode;
            newNode.next = newNode;
        } else {
            last.next = newNode;
            last = newNode;
            last.next = first;
        }

        size++;
    }

    // Elimina y devuelve el primer elemento
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("La lista está vacía");
        }

        T removedElement = first.element;

        if (size == 1) {
            first = null;
            last = null;
        } else {
            first = first.next;
            last.next = first;
        }

        size--;
        return removedElement;
    }

    // Elimina y devuelve el último elemento
    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("La lista está vacía");
        }

        T removedElement = last.element;

        if (size == 1) {
            first = null;
            last = null;
        } else {
            Node current = first;

            // Busca el nodo anterior al último
            while (current.next != last) {
                current = current.next;
            }

            last = current;
            last.next = first;
        }

        size--;
        return removedElement;
    }

    // Busca un elemento en la lista
    public boolean contains(T element) {
        if (isEmpty()) {
            return false;
        }

        Node current = first;

        do {
            if (element == null) {
                if (current.element == null) {
                    return true;
                }
            } else if (element.equals(current.element)) {
                return true;
            }

            current = current.next;
        } while (current != first);

        return false;
    }

    // Elimina la primera aparición de un elemento
    public boolean remove(T element) {
        if (isEmpty()) {
            return false;
        }

        Node current = first;
        Node previous = last;

        do {
            boolean matches = element == null
                    ? current.element == null
                    : element.equals(current.element);

            if (matches) {
                if (size == 1) {
                    first = null;
                    last = null;
                } else {
                    previous.next = current.next;

                    if (current == first) {
                        first = current.next;
                        last.next = first;
                    }

                    if (current == last) {
                        last = previous;
                        last.next = first;
                    }
                }

                size--;
                return true;
            }

            previous = current;
            current = current.next;

        } while (current != first);

        return false;
    }

    // Devuelve el elemento ubicado en una posición
    public T get(int index) {
        checkIndex(index);

        Node current = first;

        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        return current.element;
    }

    // Reemplaza el elemento de una posición
    public void set(int index, T element) {
        checkIndex(index);

        Node current = first;

        for (int i = 0; i < index; i++) {
            current = current.next;
        }

        current.element = element;
    }

    // Verifica que el índice sea válido
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "Índice fuera de los límites de la lista: " + index
            );
        }
    }

    // Elimina todos los elementos
    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    // Muestra todos los elementos de la lista
    public void printList() {
        if (isEmpty()) {
            System.out.println("La lista está vacía");
            return;
        }

        Node current = first;

        do {
            System.out.print(current.element + " -> ");
            current = current.next;
        } while (current != first);

        System.out.println("(vuelve al inicio)");
    }
}
