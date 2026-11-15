package monedero.servicios.facade;

import monedero.application.MonederoVirtual;
import monedero.estructuras.LinkedList;
import monedero.modelos.Beneficio;
import monedero.modelos.Cliente;
import monedero.utilidades.TipoBeneficio;

public class BeneficioFacade {
    private final MonederoVirtual monederoVirtual = MonederoVirtual.getInstancia();

    public TipoBeneficio[] obtenerCatalogo() {
        return TipoBeneficio.values();
    }

    public int costoDe(TipoBeneficio tipo) {
        return monederoVirtual.getBeneficioService().obtenerCostoBeneficio(tipo);
    }

    public String descripcionDe(TipoBeneficio tipo) {
        return monederoVirtual.getBeneficioService().obtenerDescripcionBeneficio(tipo);
    }

    public Beneficio canjear(String idCliente, TipoBeneficio tipo) {
        return monederoVirtual.getBeneficioService().canjear(idCliente, tipo);
    }

    public LinkedList<Beneficio> obtenerBeneficios(String idCliente) {
        return monederoVirtual.getBeneficioService().obtenerBeneficios(idCliente);
    }

    public Beneficio buscarActivo(String idCliente, TipoBeneficio tipo) {
        Cliente cliente = monederoVirtual.getClienteService().buscarCliente(idCliente);
        return monederoVirtual.getBeneficioService().buscarActivo(cliente, tipo);
    }

    public void actualizarEstados(String idCliente) {
        Cliente cliente = monederoVirtual.getClienteService().buscarCliente(idCliente);
        monederoVirtual.getBeneficioService().actualizarEstados(cliente);
    }

    public int puntosCliente(String idCliente) {
        return monederoVirtual.getClienteService().buscarCliente(idCliente).getPuntos();
    }
}
