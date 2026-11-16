package monedero.modelos;


public class RelacionReporte {

    private final String clave;      // identificador único (ids), usado para ordenar sin empates
    private final String origen;
    private final String destino;
    private final double total;
    private final int cantidad;

    public RelacionReporte(String clave, String origen, String destino, double total, int cantidad) {
        this.clave = clave;
        this.origen = origen;
        this.destino = destino;
        this.total = total;
        this.cantidad = cantidad;
    }

    public String getClave() { return clave; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public double getTotal() { return total; }
    public int getCantidad() { return cantidad; }
}