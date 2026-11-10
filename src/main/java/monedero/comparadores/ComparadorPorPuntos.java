package monedero.comparadores;

import monedero.modelos.Cliente;
import java.util.Comparator;

public class ComparadorPorPuntos implements Comparator<Cliente> {

    @Override
    public int compare(Cliente o1, Cliente o2) {

        int cmp = Integer.compare(o1.getPuntos(), o2.getPuntos());

        if (cmp != 0) {
            return cmp;
        }

        return o1.getIdCliente().compareTo(o2.getIdCliente());
    }
}