package monedero.modelos;

import monedero.utilidades.TipoBeneficio;

import java.time.LocalDateTime;
import java.util.Random;

public class Beneficio {
    private String id;
    private TipoBeneficio tipoBeneficio;
    private int costoPuntos;
    private LocalDateTime fechaCanje;
    private LocalDateTime fechaVencimiento;

    public Beneficio(TipoBeneficio tipoBeneficio, int costoPuntos) {
        this.id = generarIdBeneficio();
        this.tipoBeneficio = tipoBeneficio;
        this.costoPuntos = costoPuntos;
        this.fechaCanje = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String generarIdBeneficio() {
        Random random = new Random();
        int numero = random.nextInt(999) + 1;

        return String.format("BEN%03d", numero);
    }

    public TipoBeneficio getTipoBeneficio() {
        return tipoBeneficio;
    }

    public void setTipoBeneficio(TipoBeneficio tipoBeneficio) {
        this.tipoBeneficio = tipoBeneficio;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public void setCostoPuntos(int costoPuntos) {
        this.costoPuntos = costoPuntos;
    }

    public LocalDateTime getFechaCanje() {
        return fechaCanje;
    }

    public void setFechaCanje(LocalDateTime fechaCanje) {
        this.fechaCanje = fechaCanje;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
