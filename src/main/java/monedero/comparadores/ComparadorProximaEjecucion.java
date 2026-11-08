package monedero.comparadores;
import monedero.modelos.TransaccionProgramada;
import java.util.Comparator;

public class ComparadorProximaEjecucion implements Comparator<TransaccionProgramada> {
    @Override
    public int compare(TransaccionProgramada o1, TransaccionProgramada o2) {
        return o1.getProximaEjecucion().compareTo(o2.getProximaEjecucion());
    }
}
