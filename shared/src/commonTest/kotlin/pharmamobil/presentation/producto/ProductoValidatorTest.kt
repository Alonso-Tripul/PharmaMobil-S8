package pharmamobil.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProductoValidatorTest {
    @Test fun paracetamolValido() = assertIs<ResultadoRegistro.Exito>(validar("Paracetamol 500 mg", "8.50", "100"))
    @Test fun nombreVacio() = verificarError("", "8.50", "100", "El nombre es obligatorio.")
    @Test fun precioNoNumerico() = verificarError("Ibuprofeno", "abc", "50", "Ingrese un precio numérico.")
    @Test fun precioCero() = verificarError("Ibuprofeno", "0", "50", "El precio debe ser mayor que cero.")
    @Test fun stockNoEntero() = verificarError("Amoxicilina", "18.50", "abc", "Ingrese un stock entero.")
    @Test fun stockNegativo() = verificarError("Amoxicilina", "18.50", "-1", "El stock no puede ser negativo.")
    @Test fun stockCeroValido() = assertIs<ResultadoRegistro.Exito>(validar("Loratadina", "10", "0"))

    private fun validar(nombre: String, precio: String, stock: String) =
        ProductoValidator.validar(nombre, precio, stock)

    private fun verificarError(nombre: String, precio: String, stock: String, mensaje: String) {
        val resultado = assertIs<ResultadoRegistro.Error>(validar(nombre, precio, stock))
        assertEquals(mensaje, resultado.mensaje)
    }
}
