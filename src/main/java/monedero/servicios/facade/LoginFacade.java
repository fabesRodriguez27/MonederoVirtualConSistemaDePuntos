package monedero.servicios.facade;


import monedero.modelos.Cliente;
import monedero.modelos.Usuario;
import monedero.application.MonederoVirtual;
import monedero.utilidades.Rol;


public class LoginFacade {
    private final MonederoVirtual monederoVirtual = MonederoVirtual.getInstancia();

    public Usuario validarIngreso(String id, String password) {
        return monederoVirtual.getLoginService().validarIngreso(id, password);
    }

    public Cliente retornarCliente(String id) {
        return monederoVirtual.getClienteService().buscarCliente(id);
    }

    public boolean registrarCliente(String idCliente, String password, String nombre) {
        return monederoVirtual.getLoginService().crearNuevoUsuario(idCliente,password,nombre,Rol.USUARIO);
    }
}