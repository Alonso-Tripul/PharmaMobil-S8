package pharmamobil.domain.repository
import pharmamobil.domain.model.ProductoRest
import pharmamobil.domain.model.PaginaProductos
interface ProductoRepository {
    suspend fun listar(pagina: Int = 0): Result<PaginaProductos>
    suspend fun obtener(id: Long): Result<ProductoRest>
    suspend fun registrar(producto: ProductoRest): Result<ProductoRest>
    suspend fun actualizar(producto: ProductoRest): Result<ProductoRest>
    suspend fun eliminar(id: Long): Result<Unit>
}
