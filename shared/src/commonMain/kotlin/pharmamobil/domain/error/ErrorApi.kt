package pharmamobil.domain.error

sealed interface ErrorApi {
    data class Validacion(val porCampo: Map<String, String>, val mensaje: String) : ErrorApi
    data object NoEncontrado : ErrorApi
    data class Conflicto(val mensaje: String) : ErrorApi
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
    data object RespuestaInvalida : ErrorApi
}
class ErrorApiException(val error: ErrorApi) : Exception()
fun ErrorApi.mensaje(): String = when (this) {
    is ErrorApi.Validacion -> mensaje
    ErrorApi.NoEncontrado -> "Producto no encontrado (404)."
    is ErrorApi.Conflicto -> mensaje
    ErrorApi.Servidor -> "El servidor no pudo completar la operación."
    ErrorApi.SinConexion -> "Sin conexión con PharmaSoft. Verifica la URL, Internet y que el backend esté encendido."
    ErrorApi.TiempoAgotado -> "La solicitud tardó demasiado. Inténtalo otra vez."
    ErrorApi.RespuestaInvalida -> "La respuesta no coincide con el contrato de la guía. Revisa Swagger y los DTO."
}
