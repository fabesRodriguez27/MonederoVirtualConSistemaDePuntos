package monedero.comparadores;

import monedero.modelos.Transaccion;
import java.util.Comparator;

public class ComparadorPorTransaccion implements Comparator<Transaccion> {

    @Override
    public int compare(Transaccion t1, Transaccion t2) {

        int comparacionMonto =
                Double.compare(t1.getMonto(), t2.getMonto());

        if (comparacionMonto != 0) {
            return comparacionMonto;
        }

        return t1.getId().compareTo(t2.getId());
    }
}
