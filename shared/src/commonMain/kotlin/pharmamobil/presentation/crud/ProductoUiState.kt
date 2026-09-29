package pharmamobil.presentation.crud
import pharmamobil.domain.model.ProductoRest
data class FormularioProducto(val id: Long?=null,val nombre: String="",val precio: String="",val stock: String="",val categoriaId: String="",val estado: Boolean=true,val errores: Map<String,String> = emptyMap())
data class ProductoUiState(val fase: Fase=Fase.Cargando,val formulario: FormularioProducto=FormularioProducto(),val operacion: Operacion=Operacion.Inactiva,val mensajeExito: String?=null,val pagina: Int=0,val totalPaginas: Int=0,val totalElementos: Long=0) {
    sealed interface Fase {
        data object Cargando : Fase
        data object SinProductos : Fase
        data class ConProductos(val productos: List<ProductoRest>) : Fase
        data class Error(val mensaje: String) : Fase
    }
    sealed interface Operacion {
        data object Inactiva : Operacion
        data class EnCurso(val tipo: Tipo,val id: Long?=null) : Operacion
        data class Fallida(val mensaje: String) : Operacion
        enum class Tipo { Crear, Actualizar, Eliminar, Obtener }
    }
}
