package monedero.servicios;

import monedero.application.MonederoVirtual;
import monedero.modelos.Cliente;
import monedero.modelos.Monedero;
import monedero.modelos.TransaccionProgramada;
import monedero.utilidades.Categoria;
import monedero.utilidades.Frecuencia;
import monedero.utilidades.TipoTransaccion;

import java.time.LocalDateTime;

public class ProgramacionService {

    private ClienteService clientes() {
        return MonederoVirtual.getInstancia().getClienteService();
    }

    private TransaccionService transacciones() {
        return MonederoVirtual.getInstancia().getTransaccionService();
    }

    public ProgramacionService() {
    }

    // Programar transacción
    public boolean programarTransaccion(
            String idCliente,
            TipoTransaccion tipoTransaccion,
            String origen,
            String idClienteDestino,
            String destino,
            double monto,
            Categoria categoria,
            LocalDateTime proximaEjecucion,
            Frecuencia frecuencia) {

        // Validar monto
        if (!Double.isFinite(monto) || monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor que cero"
            );
        }

        // Validar fecha
        if (proximaEjecucion == null ||
                !proximaEjecucion.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "La fecha de ejecución debe ser futura"
            );
        }

        // Validar frecuencia
        if (frecuencia == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una frecuencia"
            );
        }

        // Buscar cliente de origen
        Cliente cliente = clientes().buscarCliente(idCliente);

        // Buscar cliente destinatario
        Cliente clienteDestino = clientes().buscarCliente(idClienteDestino);

        // Obtener monedero de origen
        Monedero monederoOrigen = null;

        if (tipoTransaccion == TipoTransaccion.TRANSFERENCIA) {

            monederoOrigen = cliente.buscarMonedero(origen);

            if (monederoOrigen == null) {
                throw new IllegalArgumentException(
                        "El monedero de origen no existe"
                );
            }
        }

        // Obtener monedero de destino
        Monedero monederoDestino =
                clienteDestino.buscarMonedero(destino);

        if (monederoDestino == null) {
            throw new IllegalArgumentException(
                    "El monedero de destino no existe"
            );
        }

        // Crear transacción programada
        TransaccionProgramada programada =
                new TransaccionProgramada(
                        tipoTransaccion,
                        monederoOrigen,
                        monederoDestino,
                        monto,
                        categoria,
                        frecuencia,
                        clienteDestino
                );

        // Establecer primera fecha
        programada.setProximaEjecucion(proximaEjecucion);

        // Insertar en la cola de prioridad
        cliente.getTransaccionesProgramadas().encolar(programada);

        // Verificar registro en consola
        System.out.println("PROGRAMACIÓN CREADA");
        System.out.println("Cliente: " + cliente.getIdCliente());
        System.out.println("ID: " + programada.getId());
        System.out.println("Tipo: " + programada.getTipoTransaccion());
        System.out.println("Monto: " + programada.getMonto());
        System.out.println("Fecha: " + programada.getProximaEjecucion());
        System.out.println("Frecuencia: " + programada.getFrecuencia());

        System.out.println(
                "Cantidad en cola: "
                        + cliente.getTransaccionesProgramadas().getSize()
        );

        return true;
    }

    // Cancelar transacción programada
    public boolean eliminarTransaccion(
            String idCliente,
            String idTransaccion) {

        Cliente cliente = clientes().buscarCliente(idCliente);

        TransaccionProgramada transaccion =
                cliente.buscarTransaccionProgramada(idTransaccion);

        if (transaccion == null) {
            throw new IllegalArgumentException(
                    "La transacción programada no existe"
            );
        }

        return cliente.getTransaccionesProgramadas().eliminar(transaccion);
    }

    // Procesar programaciones de todos los clientes
    public boolean procesarTodasLasProgramaciones() {

        for (Cliente cliente : clientes().obtenerListaClientes()) {

            procesarTransaccionesProgramadas(
                    cliente.getIdCliente()
            );
        }

        return true;
    }

    // Procesar transacciones programadas de un cliente
    public boolean procesarTransaccionesProgramadas(String idCliente) {

        Cliente cliente = clientes().buscarCliente(idCliente);

        // Verificar que el scheduler llegue al cliente
        System.out.println(
                "Revisando programaciones del cliente: "
                        + cliente.getIdCliente()
        );

        System.out.println(
                "Cantidad de programaciones pendientes: "
                        + cliente.getTransaccionesProgramadas().getSize()
        );

        while (!cliente.getTransaccionesProgramadas().isEmpty()) {

            // Consultar primera transacción sin retirarla
            TransaccionProgramada primeraTransaccion =
                    cliente.getTransaccionesProgramadas().verPrimero();

            // Obtener fecha actual
            LocalDateTime ahora = LocalDateTime.now();

            // Mostrar fechas para comprobar la comparación
            System.out.println(
                    "Fecha programada: "
                            + primeraTransaccion.getProximaEjecucion()
            );

            System.out.println(
                    "Fecha actual: " + ahora
            );

            // Si todavía no llega la fecha, terminar
            if (primeraTransaccion.getProximaEjecucion().isAfter(ahora)) {

                System.out.println(
                        "La programación todavía no debe ejecutarse"
                );

                break;
            }

            System.out.println(
                    "Ejecutando programación: "
                            + primeraTransaccion.getId()
            );

            // Retirar de la cola
            cliente.getTransaccionesProgramadas().desencolar();

            // Obtener los monederos
            Monedero monederoOrigen = primeraTransaccion.getOrigen();
            Monedero monederoDestino = primeraTransaccion.getDestino();

            boolean realizada = false;

            // Ejecutar transacción
            try {

                switch (primeraTransaccion.getTipoTransaccion()) {

                    case DEPOSITO -> realizada =
                            transacciones().depositar(
                                    idCliente,
                                    monederoDestino.getId(),
                                    primeraTransaccion.getMonto()
                            );

                    case TRANSFERENCIA -> realizada =
                            transacciones().transferir(
                                    idCliente,
                                    monederoOrigen.getId(),
                                    primeraTransaccion
                                            .getClienteDestino()
                                            .getIdCliente(),
                                    monederoDestino.getId(),
                                    primeraTransaccion.getMonto(),
                                    primeraTransaccion.getCategoria()
                            );

                    default -> throw new IllegalArgumentException(
                            "Tipo de transacción no permitido"
                    );
                }

            } catch (Exception e) {

                System.err.println(
                        "Error al ejecutar la transacción programada "
                                + primeraTransaccion.getId()
                                + ": " + e.getMessage()
                );

                e.printStackTrace();
            }

            // Mostrar resultado de la operación
            System.out.println(
                    "Transacción ejecutada correctamente: " + realizada
            );

            // Si falló, dejarla inactiva
            if (!realizada) {

                System.out.println(
                        "No se pudo ejecutar la transacción programada: "
                                + primeraTransaccion.getId()
                );

                primeraTransaccion.setEstado(false);

                continue;
            }

            // Calcular próxima ejecución
            LocalDateTime siguienteFecha =
                    primeraTransaccion.calcularProximaEjecucion();

            if (siguienteFecha != null) {

                // Actualizar fecha
                primeraTransaccion.setProximaEjecucion(siguienteFecha);

                // Volver a insertar en la cola
                cliente.getTransaccionesProgramadas().encolar(
                        primeraTransaccion
                );

                System.out.println(
                        "Programación recurrente. Próxima fecha: "
                                + siguienteFecha
                );

            } else {

                // Programación de frecuencia única
                primeraTransaccion.setEstado(false);

                System.out.println(
                        "Programación única completada: "
                                + primeraTransaccion.getId()
                );
            }
        }

        return true;
    }
}