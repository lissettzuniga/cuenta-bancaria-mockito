package com.academia.banco;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

public class CajeroAutomatico {

    public static final BigDecimal LIMITE_DIARIO = new BigDecimal("8000.00");

    private final RepositorioCuentas repositorio;
    private final ServicioAntifraude antifraude;
    private final Notificador notificador;
    private final Clock reloj;

    public CajeroAutomatico(RepositorioCuentas repositorio, ServicioAntifraude antifraude,
                            Notificador notificador, Clock reloj) {
        this.repositorio = repositorio;
        this.antifraude = antifraude;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    public BigDecimal consultarSaldo(String numeroCuenta) {
        return buscar(numeroCuenta).getSaldo();
    }

    public void retirar(String numeroCuenta, BigDecimal monto) {
        CuentaBancaria cuenta = buscar(numeroCuenta);
        validarAntifraude(numeroCuenta, monto);
        validarLimiteDiario(numeroCuenta, monto);
        cuenta.retirar(monto);
        repositorio.guardar(numeroCuenta, cuenta);
        avisar(numeroCuenta, "Retiro de $" + monto + ". Saldo: $" + cuenta.getSaldo());
    }

    private CuentaBancaria buscar(String numeroCuenta) {
        return repositorio.buscar(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(numeroCuenta));
    }

    private void validarAntifraude(String numeroCuenta, BigDecimal monto) {
        boolean sospechosa;
        try {
            sospechosa = antifraude.esSospechosa(numeroCuenta, monto);
        } catch (ServicioNoDisponibleException e) {
            throw new OperacionRechazadaException("Servicio de antifraude no disponible");
        }
        if (sospechosa) {
            throw new OperacionRechazadaException("operación sospechosa");
        }
    }

    private void validarLimiteDiario(String numeroCuenta, BigDecimal monto) {
        LocalDate hoy = LocalDate.now(reloj);
        BigDecimal yaRetirado = repositorio.totalRetiradoEn(numeroCuenta, hoy);
        if (yaRetirado.add(monto).compareTo(LIMITE_DIARIO) > 0) {
            throw new LimiteDiarioExcedidoException(yaRetirado, monto, LIMITE_DIARIO);
        }
    }
    

    private void avisar(String numeroCuenta, String mensaje) {
        try {
            notificador.enviarSms(numeroCuenta, mensaje);
        } catch (RuntimeException e) {
            // C7: el SMS es un aviso, no parte de la operación. El retiro ya se hizo y no se deshace.
        }
    }
}
