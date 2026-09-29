package pharmamobil.data.remote.dto
import kotlinx.serialization.Serializable
@Serializable data class ProductoRequestDto(val nombre: String, val precio: Double, val stock: Int, val estado: Boolean, val categoriaId: Long)
@Serializable data class ProductoResponseDto(val id: Long, val nombre: String, val precio: Double, val stock: Int, val estado: Boolean = true, val categoriaId: Long? = null, val categoriaNombre: String? = null)
@Serializable data class PaginaResponseDto<T>(val contenido: List<T>, val pagina: Int, val tamanio: Int, val totalElementos: Long, val totalPaginas: Int, val ultima: Boolean)
@Serializable data class ErrorResponseDto(val message: String = "Revisa los datos enviados.", val validationErrors: Map<String,String> = emptyMap())
