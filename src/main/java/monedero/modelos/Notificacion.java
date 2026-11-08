package monedero.modelos;

import java.time.LocalDateTime;
import java.util.Random;

public class Notificacion {
    private String id;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;

    public Notificacion(String mensaje) {
        this.id = generarIdNotificacion();
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    public String getId() {
        return id;
    }

    public String generarIdNotificacion() {
        Random random = new Random();
        int numero = random.nextInt(999) + 1;

        return String.format("NOT%03d", numero);
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
