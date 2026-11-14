package monedero.modelos;

import monedero.utilidades.TipoNotificacion;

import java.time.LocalDateTime;
import java.util.Random;

public class Notificacion {
    private String id;
    private String mensaje;
    private LocalDateTime fecha;
    private boolean leida;
    private TipoNotificacion tipoNotificacion;

    public Notificacion(TipoNotificacion tipoNotificacion, String mensaje) {
        this.id = generarIdNotificacion();
        this.fecha = LocalDateTime.now();
        this.leida = false;
        this.tipoNotificacion = tipoNotificacion;
        this.mensaje = mensaje;
    }

    public String getId() {
        return id;
    }

    public String generarIdNotificacion() {
        Random random = new Random();
        int numero = random.nextInt(999) + 1;

        return String.format("NOT%03d", numero);
    }

    public TipoNotificacion getTipoNotificacion() {
        return this.tipoNotificacion;
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
