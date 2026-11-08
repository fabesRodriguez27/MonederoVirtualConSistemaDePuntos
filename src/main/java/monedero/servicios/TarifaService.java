package monedero.servicios;

public class TarifaService {
    private static float tarifaRetiro;
    private static float tarifaTransferencia;

    public TarifaService(float tarifaRetiro, float tarifaTransferencia) {
        actualizarTarifas(tarifaRetiro, tarifaTransferencia);
    }

    public boolean actualizarTarifas(float retiro, float transferencia) {
        tarifaRetiro = retiro;
        tarifaTransferencia = transferencia;
        return true;
    }

    public static float getTarifaRetiro() {
        return tarifaRetiro;
    }

    public void setTarifaRetiro(float tarifaRetiro) {
        TarifaService.tarifaRetiro = tarifaRetiro;
    }

    public static float getTarifaTransferencia() {
        return tarifaTransferencia;
    }

    public void setTarifaTransferencia(float tarifaTransferencia) {
        TarifaService.tarifaTransferencia = tarifaTransferencia;
    }
}
