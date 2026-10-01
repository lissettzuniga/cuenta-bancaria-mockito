package com.academia.banco;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MP-2 · Stubs: el mock responde lo que tú decides")
class StubsTest {

    private RepositorioCuentas repositorio;
    private ServicioAntifraude antifraude;
    private Notificador notificador;
    private CajeroAutomatico cajero;
    private CuentaBancaria cuenta;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioCuentas.class);      // un objeto falso que «implementa» la interfaz
        antifraude = mock(ServicioAntifraude.class);
        notificador = mock(Notificador.class);
        cajero = new CajeroAutomatico(repositorio, antifraude, notificador, Clock.systemDefaultZone());
        cuenta = new CuentaBancaria("Ana");                // la cuenta es REAL: no se mockea
        cuenta.depositar(new BigDecimal("2000.00"));
    }

    // ── EJEMPLO 1 (completo): when(...).thenReturn(...) ────────────────
    @Test
    @DisplayName("consultar el saldo devuelve el de la cuenta que da el repositorio")
    void consultarSaldo() {
        when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));

        assertEquals(new BigDecimal("2000.00"), cajero.consultarSaldo("001"));
    }

    // ── EJEMPLO 2 (completo): when(...).thenThrow(...) ─────────────────
    @Test
    @DisplayName("si el repositorio no responde, su excepción sale tal cual")
    void repositorioCaido() {
        when(repositorio.buscar("001")).thenThrow(new ServicioNoDisponibleException("repositorio"));

        assertThrows(ServicioNoDisponibleException.class, () -> cajero.consultarSaldo("001"));
    }

    // ── HUECO 1 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("si el repositorio no la encuentra, CuentaNoEncontradaException")
    void cuentaInexistente() {
        when(repositorio.buscar("999")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> cajero.consultarSaldo("999"));
    }

    // ── HUECO 2: lo que devuelve un mock que nadie configuró ──────────
    @Test
    @DisplayName("un mock sin configurar devuelve «vacío»: Optional.empty")
    void sinConfigurar() {
        assertThrows(CuentaNoEncontradaException.class, () -> cajero.consultarSaldo("001"));
    }

    // ── HUECO 3: dos stubs, uno con matchers ───────────────────────────
    @Test
    @DisplayName("un retiro normal baja el saldo")
    void retiroNormal() {
        when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));
        when(repositorio.totalRetiradoEn(eq("001"), any(LocalDate.class))).thenReturn(new BigDecimal("0.00"));

        cajero.retirar("001", new BigDecimal("500.00"));

        assertEquals(new BigDecimal("1500.00"), cajero.consultarSaldo("001"));
    }

    // ── HUECO 4 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("una operación sospechosa se rechaza y la cuenta no cambia")
    void sospechosa() {
        when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));
        when(antifraude.esSospechosa("001", new BigDecimal("500.00"))).thenReturn(true);

        assertThrows(OperacionRechazadaException.class, () -> cajero.retirar("001", new BigDecimal("500.00")));
        assertEquals(new BigDecimal("2000.00"), cajero.consultarSaldo("001"));
    }

    // ── HUECO 5 ────────────────────────────────────────────────────────
    @Test
    @DisplayName("pasar el límite diario se rechaza y la cuenta no cambia")
    void limiteDiario() {
        when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));
        when(repositorio.totalRetiradoEn(eq("001"), any(LocalDate.class))).thenReturn(new BigDecimal("7600.00"));

        LimiteDiarioExcedidoException ex = assertThrows(LimiteDiarioExcedidoException.class,
                () -> cajero.retirar("001", new BigDecimal("500.00")));

        assertAll(
                () -> assertEquals("Hoy ya retiraste $7600.00; con $500.00 pasarías el límite diario de $8000.00", ex.getMessage()),
                () -> assertEquals(new BigDecimal("2000.00"), cajero.consultarSaldo("001"))
        );
    }
}
