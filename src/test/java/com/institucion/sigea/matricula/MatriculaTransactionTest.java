package com.institucion.sigea.matricula;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Suite de pruebas de la transacción de matrícula.
 * TODO: transacción atómica exitosa, rollback ante fallo intermedio (ej. pago no
 *       registrado), validación del claim "2fa" del JWT, optimistic lock ante
 *       actualizaciones concurrentes sobre la misma aula/concepto.
 */
class MatriculaTransactionTest {

    @Test
    @Disabled("Pendiente: implementar MatriculaService antes de escribir el caso de prueba")
    void placeholder() {
    }
}
