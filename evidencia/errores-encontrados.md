# Errores encontrados en CajeroAutomatico (MP-5)

1. **Queja 1:** No se validaba saldo suficiente antes de enviar la notificación SMS.
2. **Queja 2:** Si la operación era marcada como sospechosa por el servicio antifraude, aun así se descontaba el dinero de la cuenta.
3. **Queja 3:** Cada retiro exitoso se guardaba dos veces en el repositorio.
4. **Queja 4:** Si el servicio antifraude lanzaba una excepción o no respondía, la transacción continuaba en lugar de rechazarse.
5. **Queja 5:** La validación del límite diario usaba un operador estricto (`>`) impidiendo retirar exactamente el límite máximo permitido ($8,000.00).
