package monedero.utilidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import monedero.application.MonederoVirtual;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.Transaccion;
import monedero.servicios.PuntosService;

public class AppSetup {
    private static boolean datosInicializados;
    private final MonederoVirtual sistema = MonederoVirtual.getInstancia();

    public AppSetup() {
        if (datosInicializados) return;
        sistema.getTarifaService().actualizarTarifas(2_700, 5_800);
        if (!sistema.getLoginService().crearNuevoUsuario("admin", "admin123",
                "Administrador Base", Rol.ADMINISTRADOR)) {
            throw new IllegalStateException("Ya existe la cuenta admin");
        }
        cargarEjemplos();
        datosInicializados = true;
    }

    private void cargarEjemplos() {
        Cliente base = cliente("1097404946", "Cliente Base", "cliente123", 100_000);
        Monedero ahorrosBase = agregarMonedero(base, "Ahorros", 50_000);

        Cliente ana = cliente("1032456101", "Ana Torres", "Cliente123", 350_000);
        Monedero viajeAna = agregarMonedero(ana, "Viaje", 280_000);
        Cliente luis = cliente("1032456102", "Luis Martínez", "Cliente123", 810_000);
        Cliente maria = cliente("1032456103", "María López", "Cliente123", 740_000);
        Monedero hogarMaria = agregarMonedero(maria, "Hogar", 410_000);
        agregarMonedero(maria, "Emergencias", 180_000);
        Cliente carlos = cliente("1032456104", "Carlos Gómez", "Cliente123", 250_000);
        Cliente laura = cliente("1032456105", "Laura Rodríguez", "Cliente123", 980_000);
        Monedero estudiosLaura = agregarMonedero(laura, "Estudios", 650_000);
        Cliente juan = cliente("1032456106", "Juan Pérez", "Cliente123", 430_000);
        Cliente sofia = cliente("1032456107", "Sofía Ramírez", "Cliente123", 500_000);
        Monedero negocioSofia = agregarMonedero(sofia, "Negocio", 820_000);
        agregarMonedero(sofia, "Impuestos", 300_000);
        Cliente andres = cliente("1032456108", "Andrés García", "Cliente123", 600_000);
        Cliente vale = cliente("1032456109", "Valentina Castro", "Cliente123", 330_000);
        Monedero saludVale = agregarMonedero(vale, "Salud", 90_000);

        LocalDate lunes = LocalDate.now().with(java.time.DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate martes = lunes.plusDays(1);
        LocalDate miercoles = lunes.plusDays(2);
        LocalDate jueves = lunes.plusDays(3);
        LocalDate viernes = lunes.plusDays(4);

        deposito(base, ahorrosBase, 50_000, lunes.atTime(8, 10));
        deposito(ana, viajeAna, 460_000, lunes.atTime(9, 25));
        retiro(luis, principal(luis), 180_000, Categoria.TRANSPORTE, lunes.atTime(12, 5));
        transferencia(maria, hogarMaria, carlos, principal(carlos), 235_000, Categoria.HOGAR, lunes.atTime(18, 40));

        deposito(laura, estudiosLaura, 1_200_000, martes.atTime(7, 50));
        retiro(juan, principal(juan), 95_000, Categoria.ALIMENTACION, martes.atTime(11, 15));
        transferencia(sofia, negocioSofia, andres, principal(andres), 510_000, Categoria.OTROS, martes.atTime(14, 30));
        deposito(vale, saludVale, 320_000, martes.atTime(17, 20));

        deposito(carlos, principal(carlos), 2_000_000, miercoles.atTime(8, 45));
        transferencia(carlos, principal(carlos), base, principal(base), 1_350_000, Categoria.HOGAR, miercoles.atTime(10, 0));
        retiro(ana, viajeAna, 150_000, Categoria.ENTRETENIMIENTO, miercoles.atTime(16, 20));
        deposito(luis, principal(luis), 875_000, miercoles.atTime(19, 5));

        transferencia(laura, estudiosLaura, juan, principal(juan), 725_000, Categoria.EDUCACION, jueves.atTime(9, 30));
        retiro(vale, saludVale, 215_000, Categoria.SALUD, jueves.atTime(11, 40));
        deposito(sofia, negocioSofia, 1_650_000, jueves.atTime(13, 15));
        transferencia(andres, principal(andres), maria, hogarMaria, 380_000, Categoria.HOGAR, jueves.atTime(17, 10));

        retiro(base, principal(base), 250_000, Categoria.ALIMENTACION, viernes.atTime(8, 35));
        deposito(maria, hogarMaria, 640_000, viernes.atTime(10, 55));
        transferencia(juan, principal(juan), base, ahorrosBase, 300_000, Categoria.OTROS, viernes.atTime(15, 5));
        retiro(sofia, negocioSofia, 900_000, Categoria.IMPUESTOS, viernes.atTime(18, 25));
    }

    private Cliente cliente(String id, String nombre, String clave, double saldo) {
        if (!sistema.getLoginService().crearNuevoUsuario(id, clave, nombre, Rol.USUARIO)) {
            throw new IllegalStateException("Ya existe la cuenta " + id);
        }
        Cliente cliente = sistema.getClienteService().buscarCliente(id);
        principal(cliente).setSaldo(saldo);
        return cliente;
    }

    private Monedero principal(Cliente cliente) {
        return cliente.getMonederos().get(0);
    }

    private Monedero agregarMonedero(Cliente cliente, String nombre, double saldo) {
        Monedero monedero = new Monedero(nombre, saldo);
        cliente.getMonederos().addLast(monedero);
        return monedero;
    }

    private void deposito(Cliente cliente, Monedero destino, double monto, LocalDateTime fecha) {
        registrar(cliente, new Transaccion(TipoTransaccion.DEPOSITO, null, destino, monto, Categoria.OTROS), fecha);
    }

    private void retiro(Cliente cliente, Monedero origen, double monto, Categoria categoria, LocalDateTime fecha) {
        registrar(cliente, new Transaccion(TipoTransaccion.RETIRO, origen, null, monto, categoria), fecha);
    }

    private void transferencia(Cliente cliente, Monedero origen, Cliente receptor, Monedero destino,
                               double monto, Categoria categoria, LocalDateTime fecha) {
        Transaccion movimiento = new Transaccion(TipoTransaccion.TRANSFERENCIA, origen, destino, monto, categoria);
        registrar(cliente, movimiento, fecha);
        if (!cliente.getIdCliente().equals(receptor.getIdCliente())) {
            receptor.getHistorialTransacciones().addLast(movimiento);
        }
    }

    private void registrar(Cliente cliente, Transaccion movimiento, LocalDateTime fecha) {
        movimiento.setFecha(fecha);
        movimiento.setIdCliente(cliente.getIdCliente());
        Monedero origen = movimiento.getOrigen();
        Monedero destino = movimiento.getDestino();
        double cargo = movimiento.getMonto() + movimiento.getComision();
        if (origen != null && origen.getSaldo() < cargo) {
            throw new IllegalStateException("Saldo insuficiente en " + origen.getNombre());
        }
        if (!sistema.getTransaccionService().registrarTransaccion(movimiento)) {
            throw new IllegalStateException("No se pudo registrar la transacción de ejemplo");
        }
        cliente.getHistorialTransacciones().addLast(movimiento);
        if (origen != null) origen.setSaldo(origen.getSaldo() - cargo);
        if (destino != null) destino.setSaldo(destino.getSaldo() + movimiento.getMonto());
        acreditarPuntos(cliente, movimiento.getPuntos());
    }

    private void acreditarPuntos(Cliente cliente, int puntos) {
        sistema.getPuntosService().actualizarPuntos(cliente, puntos);
        cliente.setRango(PuntosService.calcularRango(cliente));
    }
}
