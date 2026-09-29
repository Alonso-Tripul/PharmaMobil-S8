package pharmamobil.presentation.crud
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.core.context.GlobalContext
import pharmamobil.di.ConfiguracionServidor
import pharmamobil.domain.model.ProductoRest
import pharmamobil.presentation.crud.ProductoUiState.*
@Composable fun ProductosScreen() {
    val vm=remember { GlobalContext.get().get<ProductosViewModel>() }
    DisposableEffect(vm) { onDispose { vm.cerrar() } }
    val s by vm.estado.collectAsState()
    var url by remember { mutableStateOf(ConfiguracionServidor.url) }
    var avisoUrl by remember { mutableStateOf<String?>(null) }
    var eliminar by remember { mutableStateOf<ProductoRest?>(null) }
    val ocupado=s.operacion is Operacion.EnCurso
    MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF006C4C))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text("PharmaMobil · CRUD REST",style=MaterialTheme.typography.headlineSmall)
            Text("Conecta con tu servidor PharmaSoft",style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(url,{url=it},label={Text("URL base (termina en /api/v1/)")},singleLine=true,modifier=Modifier.fillMaxWidth(),enabled=!ocupado)
            avisoUrl?.let { Text(it,color=MaterialTheme.colorScheme.error) }
            Button(onClick={
                val limpia=url.trim()
                if ((limpia.startsWith("http://") || limpia.startsWith("https://")) && limpia.substringAfter("://").substringBefore('/').isNotBlank()) {
                    ConfiguracionServidor.url=limpia.trimEnd('/')+"/";avisoUrl=null;vm.nuevo();vm.cargar()
                } else avisoUrl="Introduce una URL http:// o https:// válida."
            },enabled=!ocupado) { Text("Conectar / Recargar") }
            s.mensajeExito?.let { Text(it,color=Color(0xFF006C4C)) }
            when(val op=s.operacion) {
                is Operacion.EnCurso -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { CircularProgressIndicator(Modifier.size(20.dp)); Text("Operación: ${op.tipo}") }
                is Operacion.Fallida -> { Text(op.mensaje,color=MaterialTheme.colorScheme.error); TextButton(onClick={vm.cargar(s.pagina)}) { Text("Actualizar listado") } }
                Operacion.Inactiva -> Unit
            }
            Text(if(s.formulario.id==null) "Crear producto" else "Editar #${s.formulario.id}",style=MaterialTheme.typography.titleMedium)
            val f=s.formulario
            Campo("Nombre",f.nombre,f.errores["nombre"],!ocupado) { vm.formulario(f.copy(nombre=it)) }
            Campo("Precio",f.precio,f.errores["precio"],!ocupado) { vm.formulario(f.copy(precio=it)) }
            Campo("Stock",f.stock,f.errores["stock"],!ocupado) { vm.formulario(f.copy(stock=it)) }
            Campo("ID categoría existente",f.categoriaId,f.errores["categoriaId"],!ocupado) { vm.formulario(f.copy(categoriaId=it)) }
            Row { Checkbox(f.estado,{vm.formulario(f.copy(estado=it))},enabled=!ocupado);Text("Producto activo",Modifier.padding(top=12.dp)) }
            f.errores["estado"]?.let { Text(it,color=MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Button(onClick=vm::guardar,enabled=!ocupado) { Text(if(f.id==null) "Crear" else "Guardar cambios") }
                TextButton(onClick=vm::nuevo,enabled=!ocupado) { Text("Limpiar / Nuevo") }
            }
            HorizontalDivider()
            Text("Productos del servidor",style=MaterialTheme.typography.titleMedium)
            when(val fase=s.fase) {
                Fase.Cargando -> CircularProgressIndicator()
                Fase.SinProductos -> Text("No hay productos en esta página.")
                is Fase.Error -> { Text(fase.mensaje,color=MaterialTheme.colorScheme.error);Button(onClick={vm.cargar(s.pagina)},enabled=!ocupado) { Text("Reintentar") } }
                is Fase.ConProductos -> fase.productos.forEach { p ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("#${p.id} · ${p.nombre}",style=MaterialTheme.typography.titleMedium)
                            Text("Precio: ${p.precio} · Stock: ${p.stock}")
                            Text("${if(p.estado) "Activo" else "Inactivo"} · Categoría: ${p.categoriaNombre?:p.categoriaId?.toString()?:"Sin información"}")
                            Row {
                                TextButton(onClick={vm.editar(p.id)},enabled=!ocupado) { Text("Obtener / Editar") }
                                TextButton(onClick={eliminar=p},enabled=!ocupado) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
            if(s.fase !is Fase.Cargando && s.fase !is Fase.Error) {
                Text("Página ${s.pagina+1} de ${s.totalPaginas.coerceAtLeast(1)} · ${s.totalElementos} productos")
                Row {
                    TextButton(onClick={vm.cargar(s.pagina-1)},enabled=!ocupado && s.pagina>0) { Text("Anterior") }
                    TextButton(onClick={vm.cargar(s.pagina+1)},enabled=!ocupado && s.pagina+1<s.totalPaginas) { Text("Siguiente") }
                }
            }
        }
        eliminar?.let { p -> AlertDialog(onDismissRequest={eliminar=null},title={Text("Eliminar producto")},text={Text("¿Eliminar ${p.nombre} (#${p.id}) del servidor?")},confirmButton={TextButton(onClick={eliminar=null;vm.borrar(p.id)},enabled=!ocupado) { Text("Eliminar") }},dismissButton={TextButton(onClick={eliminar=null}) { Text("Cancelar") }}) }
    }
}
@Composable private fun Campo(nombre: String,valor: String,error: String?,habilitado: Boolean,onChange:(String)->Unit) {
    OutlinedTextField(valor,onChange,label={Text(nombre)},singleLine=true,isError=error!=null,enabled=habilitado,modifier=Modifier.fillMaxWidth(),supportingText=if(error!=null) { { Text(error) } } else null)
}
