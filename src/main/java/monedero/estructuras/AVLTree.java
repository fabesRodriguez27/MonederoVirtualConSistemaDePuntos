package monedero.estructuras;

import java.util.Comparator;
import java.util.Objects;

public class AVLTree<T> {

    private TreeNode<T> root;
    private int peso;
    private final Comparator<T> comparador;

    public AVLTree(Comparator<T> comparador) {
        this.root = null;
        this.peso = 0;
        this.comparador = comparador;  // definir el orden
    }

    public boolean isEmpty() {
        return this.root == null;
    }

    public int obtenerPeso() {
        return this.peso;
    }

    public int calcularAltura() {
        return getAltura(root);
    }

    // MÉTODOS AUXILIARES DE ALTURA Y FE

    private int getAltura(TreeNode<T> nodo) {
        return (nodo == null) ? 0 : nodo.altura;
    }


    // Factor de Equilibrio (FE) = Altura Subárbol Izquierdo - Altura Subárbol Derecho

    public int getFE(TreeNode<T> nodo) {
        if (nodo == null) return 0;
        return getAltura(nodo.leftChild) - getAltura(nodo.rightChild);
    }

    private void actualizarAltura(TreeNode<T> nodo) {
        if (nodo != null) {
            nodo.altura = 1 + Math.max(getAltura(nodo.leftChild), getAltura(nodo.rightChild));
        }
    }

    // ROTACIONES AVL


    // Rotación Simple a la Derecha (LL)

    private TreeNode<T> rotacionDerecha(TreeNode<T> y) {
        TreeNode<T> x = y.leftChild;
        TreeNode<T> T2 = x.rightChild;

        // Rotar
        x.rightChild = y;
        y.leftChild = T2;

        // Actualizar padres
        x.father = y.father;
        y.father = x;
        if (T2 != null) {
            T2.father = y;
        }

        // Actualizar alturas
        actualizarAltura(y);
        actualizarAltura(x);

        return x; // Nueva raíz del subárbol
    }

    // Rotación Simple a la Izquierda (RR)

    private TreeNode<T> rotacionIzquierda(TreeNode<T> x) {
        TreeNode<T> y = x.rightChild;
        TreeNode<T> T2 = y.leftChild;

        // Rotar
        y.leftChild = x;
        x.rightChild = T2;

        // Actualizar padres
        y.father = x.father;
        x.father = y;
        if (T2 != null) {
            T2.father = x;
        }

        // Actualizar alturas
        actualizarAltura(x);
        actualizarAltura(y);

        return y; // Nueva raíz del subárbol
    }

    //  REBALANCEO

    private TreeNode<T> rebalancear(TreeNode<T> nodo) {
        actualizarAltura(nodo);
        int fe = getFE(nodo);

        // Caso Izquierda - Izquierda (LL) -> Rotación Simple Derecha
        if (fe > 1 && getFE(nodo.leftChild) >= 0) {
            return rotacionDerecha(nodo);
        }

        // Caso Izquierda - Derecha (LR) -> Rotación Doble (Izq + Der)
        if (fe > 1 && getFE(nodo.leftChild) < 0) {
            nodo.leftChild = rotacionIzquierda(nodo.leftChild);
            return rotacionDerecha(nodo);
        }

        // Caso Derecha - Derecha (RR) -> Rotación Simple Izquierda
        if (fe < -1 && getFE(nodo.rightChild) <= 0) {
            return rotacionIzquierda(nodo);
        }

        // Caso Derecha - Izquierda (RL) -> Rotación Doble (Der + Izq)
        if (fe < -1 && getFE(nodo.rightChild) > 0) {
            nodo.rightChild = rotacionDerecha(nodo.rightChild);
            return rotacionIzquierda(nodo);
        }

        return nodo; // subárbol balanceado
    }

    // INSERTAR NODO

    public void insertar(T element) {
        root = insertar(root, element, null);
        root.father = null;
    }

    private TreeNode<T> insertar(TreeNode<T> nodo, T element, TreeNode<T> fatherAux) {
        if (nodo == null) {
            peso++;
            return new TreeNode<>(element, fatherAux);
        }

        int cmp = comparador.compare(element, nodo.element);

        if (cmp < 0) {
            nodo.leftChild = insertar(nodo.leftChild, element, nodo);
        } else if (cmp > 0) {
            nodo.rightChild = insertar(nodo.rightChild, element, nodo);
        } else {
            throw new IllegalArgumentException("El elemento ya existe en el árbol");
        }

        return rebalancear(nodo);
    }

    // ELIMINAR NODO

    public void eliminar(T element) {
        if (buscarNodo(element) == null) {
            System.out.println("El elemento no existe en el árbol");
            return;
        }

        root = eliminar(root, element);

        if (root != null) {
            root.father = null;
        }

        peso--;
        System.out.println("Nodo eliminado");
    }

    private TreeNode<T> eliminar(TreeNode<T> nodo, T element) {
        if (nodo == null) return null;

        int cmp = comparador.compare(element, nodo.element);

        if (cmp < 0) {
            nodo.leftChild = eliminar(nodo.leftChild, element);
            if (nodo.leftChild != null) nodo.leftChild.father = nodo;
        } else if (cmp > 0) {
            nodo.rightChild = eliminar(nodo.rightChild, element);
            if (nodo.rightChild != null) nodo.rightChild.father = nodo;
        } else {
            // Nodo encontrado

            // Caso 1 o 2: 0 o 1 hijo
            if (nodo.leftChild == null || nodo.rightChild == null) {
                TreeNode<T> temp = (nodo.leftChild != null) ? nodo.leftChild : nodo.rightChild;

                if (temp == null) { // Sin hijos
                    nodo = null;
                } else { // Un hijo
                    temp.father = nodo.father;
                    nodo = temp;
                }
            } else {
                // Caso 3: 2 hijos -> Buscar el Máximo del subárbol izquierdo (Predecesor)
                TreeNode<T> temp = obtenerMaximo(nodo.leftChild);
                nodo.element = temp.element;
                nodo.leftChild = eliminar(nodo.leftChild, temp.element);
                if (nodo.leftChild != null) nodo.leftChild.father = nodo;
            }
        }

        if (nodo == null) return null;

        return rebalancear(nodo);
    }

    private TreeNode<T> obtenerMaximo(TreeNode<T> nodo) {
        TreeNode<T> actual = nodo;
        while (actual.rightChild != null) {
            actual = actual.rightChild;
        }
        return actual;
    }

    // BÚSQUEDA

    public TreeNode<T> buscarNodo(T buscado) {
        return buscarNodo(root, buscado);
    }

    private TreeNode<T> buscarNodo(TreeNode<T> rootAux, T buscado) {
        if (rootAux == null) return null;

        int cmp = comparador.compare(buscado, rootAux.getElement());

        if (cmp == 0) return rootAux;
        if (cmp < 0) return buscarNodo(rootAux.getLeftChild(), buscado);
        return buscarNodo(rootAux.getRightChild(), buscado);
    }

    // RECORRIDOS

    public LinkedList<T> inOrden() {
        LinkedList<T> elementos = new LinkedList<>();
        inOrden(root,elementos);
        return elementos;
    }

    private void inOrden(TreeNode<T> rootAux,LinkedList<T> elementos) {
        if (rootAux == null) return;
        inOrden(rootAux.getLeftChild(),elementos);
        elementos.addLast(rootAux.getElement());
        inOrden(rootAux.getRightChild(),elementos);
    }

    public void preOrden() {
        preOrden(root);
        System.out.println();
    }

    private void preOrden(TreeNode<T> rootAux) {
        if (rootAux == null) return;
        System.out.print(rootAux.getElement() + " (FE: " + getFE(rootAux) + ") ");
        preOrden(rootAux.getLeftChild());
        preOrden(rootAux.getRightChild());
    }

    public void postOrden() {
        postOrden(root);
        System.out.println();
    }

    private void postOrden(TreeNode<T> rootAux) {
        if (rootAux == null) return;
        postOrden(rootAux.getLeftChild());
        postOrden(rootAux.getRightChild());
        System.out.print(rootAux.getElement() + " (FE: " + getFE(rootAux) + ") ");
    }

    // NODO

    public class TreeNode<T> {
        private T element;
        private int altura;
        private TreeNode<T> leftChild;
        private TreeNode<T> rightChild;
        private TreeNode<T> father;

        public TreeNode(T element) {
            this(element, null);
        }

        public TreeNode(T element, TreeNode<T> father) {
            this.element = element;
            this.father = father;
            this.altura = 1; // Un nodo nuevo hoja inicia con altura 1
        }

        public T getElement() { return element; }
        public void setElement(T element) { this.element = element; }

        public TreeNode<T> getLeftChild() { return leftChild; }
        public void setLeftChild(TreeNode<T> leftChild) { this.leftChild = leftChild; }

        public TreeNode<T> getRightChild() { return rightChild; }
        public void setRightChild(TreeNode<T> rightChild) { this.rightChild = rightChild; }

        public TreeNode<T> getFather() { return father; }
        public void setFather(TreeNode<T> father) { this.father = father; }

        public int getAltura() { return altura; }

        public int getFactorEquilibrio() {
            int altIzq = (leftChild != null) ? leftChild.getAltura() : 0;
            int altDer = (rightChild != null) ? rightChild.getAltura() : 0;
            return altIzq - altDer;
        }

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
                    ", altura=" + altura +
                    ", FE=" + getFactorEquilibrio() +
                    ", leftChild=" + leftElement +
                    ", rightChild=" + rightElement +
                    ", father=" + fatherElement +
                    '}';
        }
    }
}