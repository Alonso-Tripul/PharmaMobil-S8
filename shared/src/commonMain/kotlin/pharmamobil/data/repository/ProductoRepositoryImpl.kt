package pharmamobil.data.repository
import pharmamobil.data.remote.*
import pharmamobil.data.mapper.*
import pharmamobil.domain.model.*
import pharmamobil.domain.repository.ProductoRepository
class ProductoRepositoryImpl(private val api: ProductoApi) : ProductoRepository {
    override suspend fun listar(pagina: Int) = ejecutarLlamada { val r=api.listar(pagina); PaginaProductos(r.contenido.map { it.toDomain() },r.pagina,r.totalPaginas,r.totalElementos) }
    override suspend fun obtener(id: Long) = ejecutarLlamada { api.obtener(id).toDomain() }
    override suspend fun registrar(producto: ProductoRest) = ejecutarLlamada { api.crear(producto.toRequest()).toDomain() }
    override suspend fun actualizar(producto: ProductoRest) = ejecutarLlamada { api.actualizar(producto.id,producto.toRequest()).toDomain() }
    override suspend fun eliminar(id: Long) = ejecutarLlamada { api.eliminar(id) }
}
