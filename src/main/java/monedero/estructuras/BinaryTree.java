package monedero.estructuras;

import java.util.Comparator;
import java.util.Objects;

public class BinaryTree<T> {
    private TreeNode<T> root;
    private int peso;
    private final Comparator<T> comparador;

    public BinaryTree(Comparator<T> comparador) {
        this.peso = 0;
        this.comparador = comparador;
    }

    public boolean isEmpty() {
        return this.root == null;
    }

    /// INORDEN
    public void inOrden() {
        inOrden(root);
    }

    private void inOrden(TreeNode<T> rootAux) {
        if (rootAux == null) return;
        inOrden(rootAux.getLeftChild());
        System.out.println(rootAux.getElement());
        inOrden(rootAux.getRightChild());
    }

    /// PREORDEN
    public void preOrden() {
        preOrden(root);
    }

    private void preOrden(TreeNode<T> rootAux) {
        if (rootAux == null) return;
        System.out.println(rootAux.getElement());
        preOrden(rootAux.getLeftChild());
        preOrden(rootAux.getRightChild());
    }

    /// POSTORDEN
    public void postOrden() {
        postOrden(root);
    }

    private void postOrden(TreeNode<T> rootAux) {
        if (rootAux == null) return;
        postOrden(rootAux.getLeftChild());
        postOrden(rootAux.getRightChild());
        System.out.println(rootAux.getElement());
    }

    /// OBTENER PESO
    public int obtenerPeso() {
        return this.peso; // O(1) usando el contador interno
    }

    /// CALCULAR ALTURA
    public int calcularAltura() {
        return calcularAltura(root);
    }

    private int calcularAltura(TreeNode<T> rootAux) {
        if (rootAux == null) return 0;
        int alturaIzq = calcularAltura(rootAux.getLeftChild());
        int alturaDer = calcularAltura(rootAux.getRightChild());
        return 1 + Math.max(alturaIzq, alturaDer);
    }

    /// BUSCAR NODO
    public TreeNode<T> buscarNodo(T buscado) {
        return buscarNodo(root, buscado);
    }

    private TreeNode<T> buscarNodo(TreeNode<T> rootAux, T buscado) {
        if (rootAux == null) return null;

        int cmp = comparador.compare(rootAux.getElement(), buscado);

        if (cmp == 0) return rootAux;
        if (cmp > 0) return buscarNodo(rootAux.getLeftChild(), buscado);
        return buscarNodo(rootAux.getRightChild(), buscado);
    }

    /// INSERTAR NODO
    public void insertar(T element) {
        root = insertar(root, element, null);
    }

    private TreeNode<T> insertar(TreeNode<T> rootAux, T element, TreeNode<T> fatherAux) {
        if (rootAux == null) {
            peso++;
            return new TreeNode<>(element, fatherAux);
        }

        int cmp = comparador.compare(rootAux.getElement(), element);

        if (cmp == 0) {
            throw new IllegalArgumentException("El elemento ya existe en el arbol");
        }

        if (cmp > 0) {
            rootAux.setLeftChild(insertar(rootAux.getLeftChild(), element, rootAux));
        } else {
            rootAux.setRightChild(insertar(rootAux.getRightChild(), element, rootAux));
        }
        return rootAux;
    }

    /// ELIMINAR NODO
    public void eliminar(T element) {
        TreeNode<T> encontrado = buscarNodo(root, element);

        if (encontrado == null) {
            System.out.println("El elemento no existe en el arbol");
            return;
        }

        // Caso 1: Es la Raíz y tiene 0 o 1 hijos
        if (encontrado == root && (encontrado.getLeftChild() == null || encontrado.getRightChild() == null)) {
            if (encontrado.getLeftChild() == null && encontrado.getRightChild() == null) {
                root = null;
            } else if (encontrado.getLeftChild() != null) {
                root = encontrado.getLeftChild();
                root.setFather(null);
            } else {
                root = encontrado.getRightChild();
                root.setFather(null);
            }
            peso--;
            System.out.println("Nodo eliminado");
            return;
        }

        // Determinar la relación respecto al padre
        boolean esHijoIzquierdo = (encontrado.getFather() != null)
                && (encontrado.getFather().getLeftChild() == encontrado);

        // Caso 2: NODO HOJA
        if (encontrado.getLeftChild() == null && encontrado.getRightChild() == null) {
            if (esHijoIzquierdo) {
                encontrado.getFather().setLeftChild(null);
            } else {
                encontrado.getFather().setRightChild(null);
            }
        }
        // Caso 3: TIENE UN SOLO HIJO
        else if (encontrado.getLeftChild() == null || encontrado.getRightChild() == null) {
            TreeNode<T> hijo = (encontrado.getLeftChild() != null) ? encontrado.getLeftChild() : encontrado.getRightChild();
            hijo.setFather(encontrado.getFather());

            if (esHijoIzquierdo) {
                encontrado.getFather().setLeftChild(hijo);
            } else {
                encontrado.getFather().setRightChild(hijo);
            }
        }
        // Caso 4: TIENE DOS HIJOS
        else {
            TreeNode<T> nodoAux = encontrarNodoAux(encontrado.getLeftChild()); // Máximo del subárbol izquierdo
            encontrado.setElement(nodoAux.getElement());

            if (nodoAux.getFather() == encontrado) {
                encontrado.setLeftChild(nodoAux.getLeftChild());
            } else {
                nodoAux.getFather().setRightChild(nodoAux.getLeftChild());
            }

            if (nodoAux.getLeftChild() != null) {
                nodoAux.getLeftChild().setFather(nodoAux.getFather());
            }
        }

        peso--;
        System.out.println("Nodo eliminado");
    }

    private TreeNode<T> encontrarNodoAux(TreeNode<T> rootAux) {
        if (rootAux.getRightChild() == null) return rootAux;
        return encontrarNodoAux(rootAux.getRightChild());
    }

    // TreeNode Interno
    public class TreeNode<T> {
        private T element;
        private TreeNode<T> leftChild;
        private TreeNode<T> rightChild;
        private TreeNode<T> father;

        public TreeNode(T element) {
            this.element = element;
        }

        public TreeNode(T element, TreeNode<T> father) {
            this.element = element;
            this.father = father;
        }

        public T getElement() { return this.element; }
        public void setElement(T element) { this.element = element; }
        public TreeNode<T> getLeftChild() { return this.leftChild; }
        public void setLeftChild(TreeNode<T> leftChild) { this.leftChild = leftChild; }
        public TreeNode<T> getRightChild() { return this.rightChild; }
        public void setRightChild(TreeNode<T> rightChild) { this.rightChild = rightChild; }
        public TreeNode<T> getFather() { return this.father; }
        public void setFather(TreeNode<T> father) { this.father = father; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TreeNode<?> treeNode = (TreeNode<?>) o;
            return Objects.equals(element, treeNode.element);
        }

        @Override
        public int hashCode() {
            return Objects.hash(element);
        }

        @Override
        public String toString() {
            T fatherElement = (father != null) ? father.element : null;
            T leftElement = (leftChild != null) ? leftChild.element : null;
            T rightElement = (rightChild != null) ? rightChild.element : null;

            return "TreeNode{" +
                    "element=" + element +
                    ", leftChild=" + leftElement +
                    ", rightChild=" + rightElement +
                    ", father=" + fatherElement +
                    '}';
        }
    }
}