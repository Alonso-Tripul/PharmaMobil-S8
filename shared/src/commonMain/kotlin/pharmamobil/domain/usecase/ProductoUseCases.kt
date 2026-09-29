package pharmamobil.domain.usecase
import pharmamobil.domain.repository.ProductoRepository
import pharmamobil.domain.model.ProductoRest
class ListarProductosUseCase(private val repo: ProductoRepository) { suspend operator fun invoke(pagina: Int = 0) = repo.listar(pagina) }
class ObtenerProductoUseCase(private val repo: ProductoRepository) { suspend operator fun invoke(id: Long) = repo.obtener(id) }
class RegistrarProductoUseCase(private val repo: ProductoRepository) { suspend operator fun invoke(p: ProductoRest) = repo.registrar(p) }
class ActualizarProductoUseCase(private val repo: ProductoRepository) { suspend operator fun invoke(p: ProductoRest) = repo.actualizar(p) }
class EliminarProductoUseCase(private val repo: ProductoRepository) { suspend operator fun invoke(id: Long) = repo.eliminar(id) }
