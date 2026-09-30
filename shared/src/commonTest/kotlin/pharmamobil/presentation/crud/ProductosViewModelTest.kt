package pharmamobil.presentation.crud

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*
import pharmamobil.domain.error.*
import pharmamobil.domain.model.*
import pharmamobil.domain.repository.ProductoRepository
import pharmamobil.domain.usecase.*
import pharmamobil.presentation.crud.ProductoUiState.Fase
import pharmamobil.presentation.crud.ProductoUiState.Operacion

@OptIn(ExperimentalCoroutinesApi::class)
class ProductosViewModelTest {
    private val producto = ProductoRest(1, "Paracetamol", 6.0, 25, true, 1)

    private fun vm(repo: ProductoRepository) = ProductosViewModel(
        ListarProductosUseCase(repo), ObtenerProductoUseCase(repo),
        RegistrarProductoUseCase(repo), ActualizarProductoUseCase(repo),
        EliminarProductoUseCase(repo)
    )

    @Test
    fun cargaExitosaPasaDeCargandoAConProductos() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeProductoRepository(listOf(producto))
        val modelo = vm(repo)
        try {
            assertIs<Fase.Cargando>(modelo.estado.value.fase)
            advanceUntilIdle()
            val fase = assertIs<Fase.ConProductos>(modelo.estado.value.fase)
            assertEquals(listOf(producto), fase.productos)
        } finally {
            modelo.cerrar()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun listadoVacioTerminaEnSinProductos() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val modelo = vm(FakeProductoRepository(emptyList()))
        try {
            assertIs<Fase.Cargando>(modelo.estado.value.fase)
            advanceUntilIdle()
            assertIs<Fase.SinProductos>(modelo.estado.value.fase)
        } finally {
            modelo.cerrar()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun validacionConservaListadoYAsignaErroresPorCampo() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeProductoRepository(listOf(producto))
        repo.errorRegistro = ErrorApi.Validacion(
            mapOf("nombre" to "Nombre demasiado corto", "precio" to "Precio inválido"),
            "Datos inválidos"
        )
        val modelo = vm(repo)
        try {
            advanceUntilIdle()
            modelo.formulario(FormularioProducto(nombre = "ab", precio = "0", stock = "1", categoriaId = "1"))
            modelo.guardar()
            advanceUntilIdle()
            assertIs<Fase.ConProductos>(modelo.estado.value.fase)
            assertIs<Operacion.Fallida>(modelo.estado.value.operacion)
            assertEquals("Nombre demasiado corto", modelo.estado.value.formulario.errores["nombre"])
            assertEquals("Precio inválido", modelo.estado.value.formulario.errores["precio"])
            assertEquals(listOf(producto), (modelo.estado.value.fase as Fase.ConProductos).productos)
        } finally {
            modelo.cerrar()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun eliminarPasaPorEnCursoYRecargaListado() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repo = FakeProductoRepository(listOf(producto))
        val permiso = CompletableDeferred<Unit>()
        repo.permisoEliminar = permiso
        val modelo = vm(repo)
        try {
            advanceUntilIdle()
            modelo.borrar(producto.id)
            runCurrent()
            val op = assertIs<Operacion.EnCurso>(modelo.estado.value.operacion)
            assertEquals(Operacion.Tipo.Eliminar, op.tipo)
            assertEquals(producto.id, op.id)
            assertIs<Fase.ConProductos>(modelo.estado.value.fase)
            permiso.complete(Unit)
            advanceUntilIdle()
            assertIs<Operacion.Inactiva>(modelo.estado.value.operacion)
            val fase = assertIs<Fase.ConProductos>(modelo.estado.value.fase)
            assertFalse(fase.productos.single().estado)
            assertEquals(2, repo.llamadasListar)
            assertEquals("Producto eliminado", modelo.estado.value.mensajeExito)
        } finally {
            modelo.cerrar()
            Dispatchers.resetMain()
        }
    }
}

private class FakeProductoRepository(productos: List<ProductoRest>) : ProductoRepository {
    private val datos = productos.toMutableList()
    var llamadasListar = 0
    var errorRegistro: ErrorApi? = null
    var permisoEliminar: CompletableDeferred<Unit>? = null

    override suspend fun listar(pagina: Int): Result<PaginaProductos> {
        llamadasListar++
        return Result.success(PaginaProductos(datos.toList(), pagina, if (datos.isEmpty()) 0 else 1, datos.size.toLong()))
    }
    override suspend fun obtener(id: Long): Result<ProductoRest> =
        datos.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(ErrorApiException(ErrorApi.NoEncontrado))

    override suspend fun registrar(producto: ProductoRest): Result<ProductoRest> {
        errorRegistro?.let { return Result.failure(ErrorApiException(it)) }
        val nuevo = producto.copy(id = (datos.maxOfOrNull { it.id } ?: 0L) + 1)
        datos.add(nuevo)
        return Result.success(nuevo)
    }
    override suspend fun actualizar(producto: ProductoRest): Result<ProductoRest> {
        val indice = datos.indexOfFirst { it.id == producto.id }
        if (indice < 0) return Result.failure(ErrorApiException(ErrorApi.NoEncontrado))
        datos[indice] = producto
        return Result.success(producto)
    }
    override suspend fun eliminar(id: Long): Result<Unit> {
        permisoEliminar?.await()
        val indice = datos.indexOfFirst { it.id == id }
        if (indice < 0) return Result.failure(ErrorApiException(ErrorApi.NoEncontrado))
        datos[indice] = datos[indice].copy(estado = false)
        return Result.success(Unit)
    }
}
