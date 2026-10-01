# Cajero automático — pruebas con JUnit 5 y Mockito

**Autor:** Lissett Zuñiga Reyes

## Cómo correr las pruebas

```bash
./probar.sh final

| Clase de prueba | Qué prueba | Qué dobles usa | Cuántas pruebas |
|---|---|---|---|
| CajeroConDoblesAManoTest | Comportamiento básico del cajero usando stubs manuales. | Fakes hechos a mano y una lambda | 2 |
| StubsTest | Configuración de respuestas simuladas, excepciones y límites de consulta. | Stubs creados con `mock()` y `when(...).thenReturn()` | 7 |
| InteraccionesTest | Verificación de orden de ejecución, llamadas necesarias y captura de argumentos. | Mocks con `verify()`, `inOrder()`, `ArgumentCaptor` y `doThrow()` | 7 |
| AnotacionesTest | Inicialización limpia del entorno de pruebas mediante anotaciones declarativas. | Mocks con `@ExtendWith(MockitoExtension.class)`, `@Mock` y `@Captor` | 3 |
| QuejasCajeroTest | Reproducción y aislamiento de errores reportados en el comportamiento del cajero. | Mocks de repositorio, antifraude y notificador | 5 |
| RelojTest | Control determinista de fechas y zonas horarias en la validación de límites diarios. | Doble real de `Clock` con `Clock.fixed()` | 3 |



## Boleto de salida

1. **¿Qué diferencia hay entre un stub y un mock? Da un ejemplo de cada uno de hoy.**
   * **Stub:** Es un doble que proporciona respuestas preconfiguradas a llamadas realizadas durante la prueba para alimentar al objeto bajo prueba con datos de control (orientado al estado). Ejemplo: `when(repositorio.buscar("001")).thenReturn(Optional.of(cuenta));`.
   * **Mock:** Es un doble que registra e inspecciona las interacciones que recibe para verificar si ocurrieron según lo esperado (métodos llamados, frecuencia y parámetros; orientado al comportamiento). Ejemplo: `verify(notificador).enviarSms(eq("555-1234"), anyString());`.

2. **¿Por qué la prueba inútil de la MP-6 salía en verde con el cajero roto?**
   * Porque en lugar de usar una entidad `CuentaBancaria` real, se creó un mock de ella (`mock(CuentaBancaria.class)`) y se programó la respuesta de `getSaldo()` de forma estática con `when(...)`. Al llamar a `cajero.consultarSaldo("001")`, la prueba simplemente devolvió el valor prefijado en el stub del mock en lugar de verificar si el método `retirar()` de la cuenta realmente se había ejecutado o si había modificado el estado interno del objeto real.

3. **¿Qué NO mockearías en un proyecto de Spring Boot como el de la Semana 3, y por qué?**
   * No mockearía entidades de dominio (`@Entity` / JPA), DTOs ni Value Objects (como `Empleado` o `BigDecimal`). Tienen lógica de estado interna o reglas puras que deben probarse con sus instancias reales; mockearlos genera falsos positivos que ocultan fallos de negocio. Tampoco mockearía repositorios dentro de pruebas de integración (`@DataJpaTest`), donde el objetivo explícito es validar la interacción y persistencia real contra la base de datos.