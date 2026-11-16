package monedero.estructuras;

import java.util.HashMap;

//Grafo Dirigido y Ponderado con Lista de Adyacencia
public class Grafo<T> {

    private static final int EN_CAMINO = 1;   // usado por la búsqueda de ciclos
    private static final int TERMINADO = 2;

    private final HashMap<T, Vertice<T>> vertices = new HashMap<>();
    private int cantidadAristas;


    public Vertice<T> agregarVertice(T dato) {
        Vertice<T> vertice = vertices.get(dato);
        if (vertice == null) {
            vertice = new Vertice<>(dato);
            vertices.put(dato, vertice);
        }
        return vertice;
    }

    public void agregarArista(T origen, T destino, double peso) {
        //Si no existe el origen y el destino, se crean. Si existen, se retornan
        Vertice<T> verticeOrigen = agregarVertice(origen);
        Vertice<T> verticeDestino = agregarVertice(destino);

        Arista<T> arista = verticeOrigen.buscarAristaHacia(verticeDestino);
        if (arista == null) {
            arista = new Arista<>(verticeDestino);
            //Añado la Arista a la Lista Correspondiente
            verticeOrigen.getAdyacentes().addLast(arista);
            cantidadAristas++;
        }
        //Valor de la Arista y Cantidad de Veces
        arista.sumar(peso);
        //Total entrante y Cantidad de Veces del Vértice
        verticeDestino.registrarEntrada(peso);
    }

    public Vertice<T> obtenerVertice(T dato) {
        return vertices.get(dato);
    }

    public boolean existeVertice(T dato) {
        return vertices.containsKey(dato);
    }

    public LinkedList<Vertice<T>> obtenerVertices() {
        LinkedList<Vertice<T>> lista = new LinkedList<>();
        for (Vertice<T> vertice : vertices.values()) {
            lista.addLast(vertice);
        }
        return lista;
    }

    public int cantidadVertices() {
        return vertices.size();
    }

    public int cantidadAristas() {
        return cantidadAristas;
    }

    // ===================== RECORRIDO EN ANCHURA (BFS) =====================

    /**
     * Devuelve todos los vértices a los que se puede llegar desde "inicio"
     * siguiendo las flechas, ordenados por cercanía (primero los vecinos directos,
     * luego los vecinos de los vecinos...). Usa una COLA.
     */
    public LinkedList<T> recorridoAnchura(T inicio) {
        LinkedList<T> alcanzados = new LinkedList<>();
        Vertice<T> origen = vertices.get(inicio);
        if (origen == null) {
            return alcanzados;
        }

        HashMap<T, Boolean> visitados = new HashMap<>();   // para no visitar dos veces
        LinkedList<Vertice<T>> cola = new LinkedList<>();

        visitados.put(inicio, true);
        cola.addLast(origen);

        while (!cola.isEmpty()) {
            Vertice<T> actual = cola.removeFirst();
            for (Arista<T> arista : actual.getAdyacentes()) {
                T vecino = arista.getDestino().getDato();
                if (!visitados.containsKey(vecino)) {
                    visitados.put(vecino, true);
                    alcanzados.addLast(vecino);
                    cola.addLast(arista.getDestino());
                }
            }
        }
        return alcanzados;
    }

    // ===================== DETECCIÓN DE CICLOS (DFS RECURSIVO) =====================

    /**
     * ¿Existe algún camino que salga de un vértice y vuelva a él? (A -> B -> C -> A)
     * Recorrido en profundidad recursivo: si mientras bajo por un camino me encuentro
     * con un vértice que todavía está "en el camino actual", hay un ciclo.
     */
    public boolean tieneCiclos() {
        HashMap<T, Integer> estado = new HashMap<>();
        for (Vertice<T> vertice : vertices.values()) {
            if (!estado.containsKey(vertice.getDato()) && hayCicloDesde(vertice, estado)) {
                return true;
            }
        }
        return false;
    }

    private boolean hayCicloDesde(Vertice<T> vertice, HashMap<T, Integer> estado) {
        estado.put(vertice.getDato(), EN_CAMINO);
        for (Arista<T> arista : vertice.getAdyacentes()) {
            Integer estadoVecino = estado.get(arista.getDestino().getDato());
            if (estadoVecino == null) {
                if (hayCicloDesde(arista.getDestino(), estado)) {   // llamada recursiva
                    return true;
                }
            } else if (estadoVecino == EN_CAMINO) {
                return true;                                        // volví a mi propio camino
            }
        }
        estado.put(vertice.getDato(), TERMINADO);
        return false;
    }
}