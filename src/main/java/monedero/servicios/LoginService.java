package monedero.servicios;

import monedero.application.MonederoVirtual;
import monedero.modelos.Usuario;
import monedero.utilidades.FormatoCedula;
import monedero.utilidades.Rol;

import java.util.HashMap;

public class LoginService {
    private final HashMap<String, Usuario> usuarios = new HashMap<>();

    public LoginService() {
    }

    public Usuario validarIngreso(String idCliente, String passwordCliente) {
        idCliente = FormatoCedula.normalizar(idCliente);
        Usuario usuarioHallado = usuarios.get(idCliente);
        if (usuarioHallado != null && usuarioHallado.getPassword().equals(passwordCliente)) {
            System.out.println("Cliente Hallado y Clave Correcta");
            return usuarioHallado;
        }else{
            System.out.println("Credenciales Invalidas");
            return null;
        }
    }

    public boolean crearNuevoUsuario(String idCliente, String password, String nombre, Rol rol) {
        idCliente = FormatoCedula.normalizar(idCliente);
        if (usuarios.containsKey(idCliente)) {
            return false;
        }
        Usuario usuarioNuevo = new Usuario(idCliente,password,rol);

        if (rol == Rol.ADMINISTRADOR) {
            usuarios.put(idCliente, usuarioNuevo);
            return true;
        }

        if (MonederoVirtual.getInstancia().getClienteService().registrarCliente(idCliente,nombre)) {
            usuarios.put(idCliente, usuarioNuevo);
            return true;
        }else{
            return false;
        }
    }
    void eliminarAccesoCliente(String id) { usuarios.remove(id); }
}
