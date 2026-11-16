package monedero.servicios;

import monedero.application.MonederoVirtual;
import monedero.comparadores.ComparadorPorTotal;
import monedero.estructuras.AVLTree;
import monedero.estructuras.Arista;
import monedero.estructuras.Grafo;
import monedero.estructuras.LinkedList;
import monedero.estructuras.Vertice;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.RelacionReporte;
import monedero.modelos.Transaccion;
import monedero.utilidades.Categoria;
import monedero.utilidades.FormatoCedula;
import monedero.utilidades.TipoTransaccion;

import java.util.HashMap;

/**
 * Los grafos se construyen a partir del historial global de transacciones (el AVL de
 * TransaccionService) cada vez que se pide un informe, ignorando las revertidas
 *
 *  - Grafo de transferencias: cliente emisor -> cliente receptor (peso = total transferido)
 *  - Grafo de gastos: cliente -> categoría               (peso = total gastado)
 *  - Grafo de monederos: monedero origen -> monedero destino (peso = total movido)
 */
public class ReporteService {

    public ReporteService() {
    }

    private ClienteService clientes() {
        return MonederoVirtual.getInstancia().getClienteService();
    }

    private TransaccionService transacciones() {
        return MonederoVirtual.getInstancia().getTransaccionService();
    }

    //Obtener Todos los Monederos de Los Clientes
    private HashMap<String, Cliente> indiceDuenos() {
        HashMap<String, Cliente> indice = new HashMap<>();
        for (Cliente cliente : clientes().obtenerListaClientes()) {
            for (Monedero monedero : cliente.getMonederos()) {
                indice.put(monedero.getId(), cliente);
            }
        }
        return indice;
    }

    //Obtener Todas Las Transacciones (Excepto las Revertidas)
    private LinkedList<Transaccion> transaccionesValidas() {
        LinkedList<Transaccion> validas = new LinkedList<>();
        for (Transaccion transaccion : transacciones().obtenerHistorialPorMonto()) {
            if (!transaccion.isRevertida()) {
                validas.addLast(transaccion);
            }
        }
        return validas;
    }

    // Una transferencia es "interna" si el dueño del destino es el mismo que la hizo
    private boolean esInterna(Transaccion transaccion, HashMap<String, Cliente> duenos) {
        Cliente receptor = duenos.get(transaccion.getDestino().getId());
        return receptor != null && receptor.getIdCliente().equals(transaccion.getIdCliente());
    }

    private String nombreCliente(String idCliente) {
        if (clientes().existeCliente(idCliente)) {
            return clientes().buscarCliente(idCliente).getNombre() + " (" + FormatoCedula.mostrar(idCliente) + ")";
        }
        return "Cliente eliminado (" + FormatoCedula.mostrar(idCliente) + ")";
    }

    private String nombreMonedero(Monedero monedero, HashMap<String, Cliente> duenos) {
        Cliente dueno = duenos.get(monedero.getId());
        return monedero.getNombre() + " : " + (dueno == null ? "monedero eliminado" : dueno.getNombre());
    }

    //Grafo de Transferencias
    public Grafo<String> construirGrafoTransferencias() {
        HashMap<String, Cliente> duenos = indiceDuenos();
        Grafo<String> grafo = new Grafo<>();

        for (Transaccion transaccion : transaccionesValidas()) {
            if (transaccion.getTipoTransaccion() == TipoTransaccion.TRANSFERENCIA) {
                //Busco al Monedero del Cliente a Quien se le Envió La Transferencia (Destino)
                Cliente receptor = duenos.get(transaccion.getDestino().getId());
                if (receptor != null) {
                    String emisor = transaccion.getIdCliente();
                    String recibe = receptor.getIdCliente();
                    if (!emisor.equals(recibe)) {
                        grafo.agregarArista(emisor, recibe, transaccion.getMonto());
                    }
                }
            }
        }
        return grafo;
    }

    // Gasto = retiros + transferencias a OTROS clientes (los depósitos no son gasto)
    public Grafo<String> construirGrafoGastos() {
        HashMap<String, Cliente> duenos = indiceDuenos();
        Grafo<String> grafo = new Grafo<>();
        for (Transaccion transaccion : transaccionesValidas()) {
            boolean esGasto = false;
            if (transaccion.getTipoTransaccion() == TipoTransaccion.RETIRO) {
                esGasto = true;
            }
            if (transaccion.getTipoTransaccion() == TipoTransaccion.TRANSFERENCIA) {
                Cliente receptor = duenos.get(transaccion.getDestino().getId());
                //Que no Sea Una Transferencia Interna
                if (receptor != null && !receptor.getIdCliente().equals(transaccion.getIdCliente())) {
                    esGasto = true;
                }
            }
            if (esGasto) {
                Categoria categoria = transaccion.getCategoria();
                if (categoria == null) {
                    categoria = Categoria.OTROS;
                }
                grafo.agregarArista(transaccion.getIdCliente(), categoria.name(), transaccion.getMonto());
            }
        }
        return grafo;
    }

    private Grafo<String> construirGrafoMonederos(HashMap<String, String> etiquetas) {
        HashMap<String, Cliente> duenos = indiceDuenos();
        Grafo<String> grafo = new Grafo<>();

        for (Transaccion transaccion : transaccionesValidas()) {
            if (transaccion.getTipoTransaccion() == TipoTransaccion.TRANSFERENCIA) {
                Monedero origen = transaccion.getOrigen();
                Monedero destino = transaccion.getDestino();

                etiquetas.put(origen.getId(), nombreMonedero(origen, duenos));
                etiquetas.put(destino.getId(), nombreMonedero(destino, duenos));

                grafo.agregarArista(origen.getId(), destino.getId(), transaccion.getMonto());
            }
        }
        return grafo;
    }

    public Grafo<String> construirGrafoMonederos() {
        return construirGrafoMonederos(new HashMap<>());
    }

    // ===================== INFORMES =====================
    // Todos ordenan de mayor a menor insertando las filas en un AVL (ComparadorPorTotal)
    // y leyéndolo en inorden: insertar n filas cuesta O(n log n).

    // Cada flecha emisor -> receptor con su total
    public LinkedList<RelacionReporte> transferenciasEntreClientes() {
        Grafo<String> grafo = construirGrafoTransferencias();
        AVLTree<RelacionReporte> orden = new AVLTree<>(new ComparadorPorTotal());
        for (Vertice<String> emisor : grafo.obtenerVertices()) {
            for (Arista<String> arista : emisor.getAdyacentes()) {
                //Recordar que la Arista tiene La Referencia de un Vértice de A Quien se le Envía, por eso .getDestino (vertice) y Luego .getDato()
                String receptor = arista.getDestino().getDato();
                orden.insertar(new RelacionReporte(emisor.getDato() + ">" + receptor,
                        nombreCliente(emisor.getDato()), nombreCliente(receptor),
                        arista.getPeso(), arista.getCantidad()));
            }
        }
        return orden.inOrden();
    }

    // Grado de ENTRADA ponderado: cuánto recibió cada cliente en total
    public LinkedList<RelacionReporte> clientesQueMasReciben() {
        Grafo<String> grafo = construirGrafoTransferencias();
        AVLTree<RelacionReporte> orden = new AVLTree<>(new ComparadorPorTotal());
        for (Vertice<String> vertice : grafo.obtenerVertices()) {
            if (vertice.getCantidadEntrante() > 0) {
                orden.insertar(new RelacionReporte(vertice.getDato(), nombreCliente(vertice.getDato()), "",
                        vertice.getTotalEntrante(), vertice.getCantidadEntrante()));
            }
        }
        return orden.inOrden();
    }

    // Grado de SALIDA ponderado: cuánto envió cada cliente en total
    public LinkedList<RelacionReporte> clientesQueMasEnvian() {
        Grafo<String> grafo = construirGrafoTransferencias();
        AVLTree<RelacionReporte> orden = new AVLTree<>(new ComparadorPorTotal());

        // Recorro cada cliente (vértice) del grafo
        for (Vertice<String> vertice : grafo.obtenerVertices()) {
            //Aristas del Vertice (a quien envió)
            LinkedList<Arista<String>> flechas = vertice.getAdyacentes();

            // Si no tiene flechas de salida, nunca envió plata: no lo incluyo
            if (!flechas.isEmpty()) {
                double totalEnviado = 0;
                int cantidadEnvios = 0;

                // Sumo todas sus flechas de salida
                for (Arista<String> arista : flechas) {
                    totalEnviado = totalEnviado + arista.getPeso();
                    cantidadEnvios = cantidadEnvios + arista.getCantidad();
                }

                // Armo la fila del informe
                String cedula = vertice.getDato();
                String nombre = nombreCliente(cedula);
                RelacionReporte fila = new RelacionReporte(cedula, nombre, "", totalEnviado, cantidadEnvios);

                // La meto en el AVL para que quede ordenada
                orden.insertar(fila);
            }
        }
        return orden.inOrden();
    }

    // En el grafo de gastos, a las categorías solo les LLEGAN flechas: su grado de
    // entrada es el total gastado en esa categoría por todos los clientes
    public LinkedList<RelacionReporte> totalesPorCategoria() {
        Grafo<String> grafo = construirGrafoGastos();
        AVLTree<RelacionReporte> orden = new AVLTree<>(new ComparadorPorTotal());
        for (Vertice<String> vertice : grafo.obtenerVertices()) {
            if (vertice.getCantidadEntrante() > 0) {
                orden.insertar(new RelacionReporte(vertice.getDato(), vertice.getDato(), "",
                        vertice.getTotalEntrante(), vertice.getCantidadEntrante()));
            }
        }
        return orden.inOrden();
    }

    // Cada flecha cliente -> categoría: en qué gasta cada cliente
    public LinkedList<RelacionReporte> gastosPorClienteYCategoria() {
        Grafo<String> grafo = construirGrafoGastos();
        AVLTree<RelacionReporte> orden = new AVLTree<>(new ComparadorPorTotal());
        for (Vertice<String> cliente : grafo.obtenerVertices()) {
            for (Arista<String> arista : cliente.getAdyacentes()) {
                String categoria = arista.getDestino().getDato();
                orden.insertar(new RelacionReporte(cliente.getDato() + ">" + categoria,
                        nombreCliente(cliente.getDato()), categoria,
                        arista.getPeso(), arista.getCantidad()));
            }
        }
        return orden.inOrden();
    }

    // Multimonedero: cada flecha monedero -> monedero
    public LinkedList<RelacionReporte> movimientosEntreMonederos() {
        HashMap<String, String> etiquetas = new HashMap<>();
        Grafo<String> grafo = construirGrafoMonederos(etiquetas);
        AVLTree<RelacionReporte> orden = new AVLTree<>(new ComparadorPorTotal());
        for (Vertice<String> origen : grafo.obtenerVertices()) {
            for (Arista<String> arista : origen.getAdyacentes()) {
                String destino = arista.getDestino().getDato();
                orden.insertar(new RelacionReporte(origen.getDato() + ">" + destino,
                        etiquetas.get(origen.getDato()), etiquetas.get(destino),
                        arista.getPeso(), arista.getCantidad()));
            }
        }
        return orden.inOrden();
    }

    // BFS: ¿a qué clientes puede llegar (directa o indirectamente) el dinero de este cliente?
    public LinkedList<String> alcanceDelDinero(String idCliente) {
        LinkedList<String> nombres = new LinkedList<>();
        for (String id : construirGrafoTransferencias().recorridoAnchura(idCliente)) {
            nombres.addLast(nombreCliente(id));
        }
        return nombres;
    }

    // DFS recursivo: ¿hay dinero que da la vuelta y regresa? (A -> B -> ... -> A)
    public boolean hayCiclosDeTransferencias() {
        return construirGrafoTransferencias().tieneCiclos();
    }

    public String resumenGrafoTransferencias() {
        Grafo<String> grafo = construirGrafoTransferencias();
        return grafo.cantidadVertices() + " clientes conectados por " + grafo.cantidadAristas()
                + " relaciones de transferencia.";
    }
}