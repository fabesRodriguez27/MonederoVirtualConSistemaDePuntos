package monedero.comparadores;

import monedero.modelos.RelacionReporte;
import java.util.Comparator;

public class ComparadorPorTotal implements Comparator<RelacionReporte> {

    @Override
    public int compare(RelacionReporte r1, RelacionReporte r2) {
        int comparacion = Double.compare(r2.getTotal(), r1.getTotal());   // r2 primero = descendente
        if (comparacion != 0) {
            return comparacion;
        }
        return r1.getClave().compareTo(r2.getClave());
    }
}