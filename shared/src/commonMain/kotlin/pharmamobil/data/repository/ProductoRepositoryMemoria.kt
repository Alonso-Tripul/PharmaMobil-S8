package pharmamobil.data.repository
import pharmamobil.domain.model.*
import pharmamobil.domain.repository.ProductoRepository
class ProductoRepositoryMemoria : ProductoRepository {
    private val datos=mutableMapOf<Long,ProductoRest>()
    private var siguiente=1L
    override suspend fun listar(pagina: Int) = Result.success(PaginaProductos(datos.values.drop(pagina*20).take(20),pagina,((datos.size+19)/20).coerceAtLeast(1),datos.size.toLong()))
    override suspend fun obtener(id: Long) = runCatching { requireNotNull(datos[id]) }
    override suspend fun registrar(producto: ProductoRest) = Result.success(producto.copy(id=siguiente++).also { datos[it.id]=it })
    override suspend fun actualizar(producto: ProductoRest) = runCatching { require(datos.containsKey(producto.id)); datos[producto.id]=producto; producto }
    override suspend fun eliminar(id: Long) = runCatching { requireNotNull(datos.remove(id)); Unit }
}
