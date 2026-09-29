package pharmamobil.domain.model

data class ProductoRest(val id: Long = 0, val nombre: String, val precio: Double, val stock: Int,
                        val estado: Boolean = true, val categoriaId: Long? = null,
                        val categoriaNombre: String? = null)
data class PaginaProductos(val contenido: List<ProductoRest>, val pagina: Int, val totalPaginas: Int, val totalElementos: Long)
