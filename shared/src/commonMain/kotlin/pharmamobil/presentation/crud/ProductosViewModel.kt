package pharmamobil.presentation.crud
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import pharmamobil.domain.error.*
import pharmamobil.domain.model.ProductoRest
import pharmamobil.domain.usecase.*
import pharmamobil.presentation.crud.ProductoUiState.*
class ProductosViewModel(private val listar: ListarProductosUseCase,private val obtener: ObtenerProductoUseCase,private val crear: RegistrarProductoUseCase,private val actualizar: ActualizarProductoUseCase,private val eliminar: EliminarProductoUseCase) : ViewModel() {
    private val _estado=MutableStateFlow(ProductoUiState())
    val estado=_estado.asStateFlow()
    init { cargar() }
    fun cerrar() { viewModelScope.cancel() }
    fun formulario(f: FormularioProducto) { _estado.update { it.copy(formulario=f.copy(errores=emptyMap()),mensajeExito=null) } }
    fun nuevo() { formulario(FormularioProducto()) }
    private suspend fun refrescar(pagina: Int): Boolean {
        val r=listar(pagina)
        r.onSuccess { datos -> _estado.update { it.copy(fase=if(datos.contenido.isEmpty()) Fase.SinProductos else Fase.ConProductos(datos.contenido),pagina=datos.pagina,totalPaginas=datos.totalPaginas,totalElementos=datos.totalElementos) } }
        r.onFailure { fallo -> manejar(fallo) }
        return r.isSuccess
    }
    fun cargar(pagina: Int=0) {
        if (_estado.value.operacion is Operacion.EnCurso) return
        viewModelScope.launch {
            _estado.update { it.copy(fase=Fase.Cargando,operacion=Operacion.Inactiva) }
            listar(pagina).onSuccess { datos -> _estado.update { it.copy(fase=if(datos.contenido.isEmpty()) Fase.SinProductos else Fase.ConProductos(datos.contenido),pagina=datos.pagina,totalPaginas=datos.totalPaginas,totalElementos=datos.totalElementos) } }
                .onFailure { fallo -> _estado.update { it.copy(fase=Fase.Error(mensaje(fallo))) } }
        }
    }
    fun editar(id: Long) {
        if (_estado.value.operacion is Operacion.EnCurso) return
        viewModelScope.launch {
            _estado.update { it.copy(operacion=Operacion.EnCurso(Operacion.Tipo.Obtener,id),mensajeExito=null) }
            obtener(id).onSuccess { p -> _estado.update { it.copy(formulario=FormularioProducto(p.id,p.nombre,p.precio.toString(),p.stock.toString(),p.categoriaId?.toString().orEmpty(),p.estado),operacion=Operacion.Inactiva) } }.onFailure { manejar(it) }
        }
    }
    fun guardar() {
        if (_estado.value.operacion is Operacion.EnCurso) return
        val f=_estado.value.formulario
        val precio=f.precio.toDoubleOrNull();val stock=f.stock.toIntOrNull();val categoria=f.categoriaId.toLongOrNull()
        val errores=buildMap {
            if(precio==null || !precio.isFinite()) put("precio","Introduce un número válido.")
            if(stock==null) put("stock","Introduce un número entero.")
            if(categoria==null) put("categoriaId","Introduce el ID numérico de una categoría existente.")
        }
        if(errores.isNotEmpty()) { _estado.update { it.copy(formulario=f.copy(errores=errores)) }; return }
        val p=ProductoRest(f.id?:0,f.nombre,precio!!,stock!!,f.estado,categoria)
        viewModelScope.launch {
            _estado.update { it.copy(operacion=Operacion.EnCurso(if(f.id==null) Operacion.Tipo.Crear else Operacion.Tipo.Actualizar,f.id),mensajeExito=null,formulario=f.copy(errores=emptyMap())) }
            val r=if(f.id==null) crear(p) else actualizar(p)
            r.onSuccess {
                _estado.update { it.copy(formulario=FormularioProducto(),mensajeExito=if(f.id==null) "Producto creado" else "Producto actualizado") }
                if(refrescar(_estado.value.pagina)) _estado.update { it.copy(operacion=Operacion.Inactiva) }
            }.onFailure { manejar(it) }
        }
    }
    fun borrar(id: Long) {
        if (_estado.value.operacion is Operacion.EnCurso) return
        viewModelScope.launch {
            _estado.update { it.copy(operacion=Operacion.EnCurso(Operacion.Tipo.Eliminar,id),mensajeExito=null) }
            eliminar(id).onSuccess {
                _estado.update { it.copy(mensajeExito="Producto eliminado",formulario=if(it.formulario.id==id) FormularioProducto() else it.formulario) }
                val productos=(_estado.value.fase as? Fase.ConProductos)?.productos.orEmpty()
                val pagina=if(productos.size==1 && _estado.value.pagina>0) _estado.value.pagina-1 else _estado.value.pagina
                if(refrescar(pagina)) _estado.update { it.copy(operacion=Operacion.Inactiva) }
            }.onFailure { manejar(it) }
        }
    }
    private fun mensaje(t: Throwable) = (t as? ErrorApiException)?.error?.mensaje() ?: "No se pudo completar la operación."
    private fun manejar(t: Throwable) {
        val e=(t as? ErrorApiException)?.error
        _estado.update { it.copy(operacion=Operacion.Fallida(mensaje(t)),formulario=if(e is ErrorApi.Validacion) it.formulario.copy(errores=e.porCampo) else it.formulario) }
    }
}
