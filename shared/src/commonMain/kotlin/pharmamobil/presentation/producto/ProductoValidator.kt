package pharmamobil.presentation.producto

import pharmamobil.model.Producto

sealed interface ResultadoRegistro {
    data class Exito(val producto: Producto) : ResultadoRegistro
    data class Error(val campo: Campo, val mensaje: String) : ResultadoRegistro
}

enum class Campo { NOMBRE, PRECIO, STOCK }

object ProductoValidator {
    fun validar(nombre: String, precioTexto: String, stockTexto: String): ResultadoRegistro {
        if (nombre.isBlank()) {
            return ResultadoRegistro.Error(Campo.NOMBRE, "El nombre es obligatorio.")
        }

        val precio = precioTexto.toDoubleOrNull()
            ?: return ResultadoRegistro.Error(Campo.PRECIO, "Ingrese un precio numérico.")
        if (precio <= 0.0) {
            return ResultadoRegistro.Error(Campo.PRECIO, "El precio debe ser mayor que cero.")
        }

        val stock = stockTexto.toIntOrNull()
            ?: return ResultadoRegistro.Error(Campo.STOCK, "Ingrese un stock entero.")
        if (stock < 0) {
            return ResultadoRegistro.Error(Campo.STOCK, "El stock no puede ser negativo.")
        }

        return ResultadoRegistro.Exito(Producto(nombre.trim(), precio, stock))
    }
}
