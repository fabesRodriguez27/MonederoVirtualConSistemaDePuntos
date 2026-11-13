package monedero.modelos;

import monedero.utilidades.Rol;

public class Usuario {
    private String id;
    private String password;
    private Rol rol;

    public Usuario(String id, String password, Rol rol) {
        this.id = id;
        this.password = password;
        this.rol = rol;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Rol getRol() {
        return rol;
    }
    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
