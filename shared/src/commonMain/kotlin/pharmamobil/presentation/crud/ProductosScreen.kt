package pharmamobil.presentation.crud

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.mp.KoinPlatform
import pharmamobil.di.ConfiguracionServidor
import pharmamobil.domain.model.ProductoRest
import pharmamobil.presentation.crud.ProductoUiState.Fase
import pharmamobil.presentation.crud.ProductoUiState.Operacion

@Composable
fun ProductosScreen() {
    val vm = remember {
        KoinPlatform.getKoin().get<ProductosViewModel>()
    }

    DisposableEffect(vm) {
        onDispose {
            vm.cerrar()
        }
    }

    val s by vm.estado.collectAsState()

    var url by remember {
        mutableStateOf(ConfiguracionServidor.url)
    }
    var avisoUrl by remember {
        mutableStateOf<String?>(null)
    }
    var productoAEliminar by remember {
        mutableStateOf<ProductoRest?>(null)
    }

    val operacionActual = s.operacion as? Operacion.EnCurso
    val ocupado = operacionActual != null
    val guardando =
        operacionActual?.tipo == Operacion.Tipo.Crear ||
                operacionActual?.tipo == Operacion.Tipo.Actualizar

    val verde = Color(0xFF006C4C)

    MaterialTheme(
        colorScheme = lightColorScheme(primary = verde)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "PharmaMobil · CRUD REST",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Conecta con tu servidor PharmaSoft",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = {
                    Text("URL base (termina en /api/v1/)")
                },
                singleLine = true,
                enabled = !ocupado,
                modifier = Modifier.fillMaxWidth()
            )

            avisoUrl?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val limpia = url.trim()
                    val valida =
                        (limpia.startsWith("http://") ||
                                limpia.startsWith("https://")) &&
                                limpia.substringAfter("://")
                                    .substringBefore('/')
                                    .isNotBlank()

                    if (valida) {
                        ConfiguracionServidor.url =
                            limpia.trimEnd('/') + "/"
                        avisoUrl = null
                        vm.nuevo()
                        vm.cargar()
                    } else {
                        avisoUrl =
                            "Introduce una URL http:// o https:// válida."
                    }
                },
                enabled = !ocupado
            ) {
                Text("Conectar / Recargar")
            }

            s.mensajeExito?.let { mensaje ->
                Text(text = mensaje, color = verde)
            }

            when (val operacion = s.operacion) {
                is Operacion.EnCurso -> {
                    val mensaje = when (operacion.tipo) {
                        Operacion.Tipo.Crear -> "Creando producto..."
                        Operacion.Tipo.Actualizar -> "Actualizando producto..."
                        Operacion.Tipo.Eliminar -> "Dando de baja al producto..."
                        Operacion.Tipo.Obtener -> "Obteniendo producto..."
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                        Text(mensaje)
                    }
                }

                is Operacion.Fallida -> {
                    Text(
                        text = operacion.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(
                        onClick = { vm.cargar(s.pagina) },
                        enabled = !ocupado
                    ) {
                        Text("Actualizar listado")
                    }
                }

                Operacion.Inactiva -> Unit
            }

            val f = s.formulario

            Text(
                text = if (f.id == null) {
                    "Crear producto"
                } else {
                    "Editar #${f.id}"
                },
                style = MaterialTheme.typography.titleMedium
            )

            Campo(
                nombre = "Nombre",
                valor = f.nombre,
                error = f.errores["nombre"],
                habilitado = !ocupado
            ) {
                vm.formulario(f.copy(nombre = it))
            }

            Campo(
                nombre = "Precio",
                valor = f.precio,
                error = f.errores["precio"],
                habilitado = !ocupado
            ) {
                vm.formulario(f.copy(precio = it))
            }

            Campo(
                nombre = "Stock",
                valor = f.stock,
                error = f.errores["stock"],
                habilitado = !ocupado
            ) {
                vm.formulario(f.copy(stock = it))
            }

            Campo(
                nombre = "ID categoría existente",
                valor = f.categoriaId,
                error = f.errores["categoriaId"],
                habilitado = !ocupado
            ) {
                vm.formulario(f.copy(categoriaId = it))
            }

            Row {
                Checkbox(
                    checked = f.estado,
                    onCheckedChange = {
                        vm.formulario(f.copy(estado = it))
                    },
                    enabled = !ocupado
                )

                Text(
                    text = "Producto activo",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            f.errores["estado"]?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { vm.guardar() },
                    enabled = !ocupado
                ) {
                    Text(
                        when {
                            guardando -> "Guardando..."
                            f.id == null -> "Crear"
                            else -> "Guardar cambios"
                        }
                    )
                }

                TextButton(
                    onClick = { vm.nuevo() },
                    enabled = !ocupado
                ) {
                    Text("Limpiar / Nuevo")
                }
            }

            HorizontalDivider()

            Text(
                text = "Productos del servidor",
                style = MaterialTheme.typography.titleMedium
            )

            when (val fase = s.fase) {
                Fase.Cargando -> {
                    CircularProgressIndicator()
                }

                Fase.SinProductos -> {
                    Text("No hay productos en esta página.")
                }

                is Fase.Error -> {
                    Text(
                        text = fase.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = { vm.cargar(s.pagina) },
                        enabled = !ocupado
                    ) {
                        Text("Reintentar")
                    }
                }

                is Fase.ConProductos -> {
                    fase.productos.forEach { producto ->
                        val obteniendoEste =
                            operacionActual?.tipo == Operacion.Tipo.Obtener &&
                                    operacionActual?.id == producto.id

                        val eliminandoEste =
                            operacionActual?.tipo == Operacion.Tipo.Eliminar &&
                                    operacionActual?.id == producto.id

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = "#${producto.id} · ${producto.nombre}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Precio: ${producto.precio} · " +
                                            "Stock: ${producto.stock}"
                                )

                                val categoria =
                                    producto.categoriaNombre
                                        ?: producto.categoriaId?.toString()
                                        ?: "Sin información"

                                val estadoTexto =
                                    if (producto.estado) "Activo" else "Inactivo"

                                Text(
                                    text = "$estadoTexto · Categoría: $categoria"
                                )

                                Row {
                                    TextButton(
                                        onClick = {
                                            vm.editar(producto.id)
                                        },
                                        enabled = !ocupado
                                    ) {
                                        Text(
                                            if (obteniendoEste) {
                                                "Obteniendo..."
                                            } else {
                                                "Obtener / Editar"
                                            }
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            productoAEliminar = producto
                                        },
                                        enabled = !ocupado
                                    ) {
                                        Text(
                                            if (eliminandoEste) {
                                                "Eliminando..."
                                            } else {
                                                "Eliminar"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (s.fase !is Fase.Cargando && s.fase !is Fase.Error) {
                Text(
                    text = "Página ${s.pagina + 1} de " +
                            "${s.totalPaginas.coerceAtLeast(1)} · " +
                            "${s.totalElementos} productos"
                )

                Row {
                    TextButton(
                        onClick = { vm.cargar(s.pagina - 1) },
                        enabled = !ocupado && s.pagina > 0
                    ) {
                        Text("Anterior")
                    }

                    TextButton(
                        onClick = { vm.cargar(s.pagina + 1) },
                        enabled = !ocupado &&
                                s.pagina + 1 < s.totalPaginas
                    ) {
                        Text("Siguiente")
                    }
                }
            }
        }

        productoAEliminar?.let { producto ->
            AlertDialog(
                onDismissRequest = {
                    productoAEliminar = null
                },
                title = {
                    Text("Eliminar producto")
                },
                text = {
                    Text(
                        "¿Dar de baja a ${producto.nombre} " +
                                "(#${producto.id})? El producto quedará inactivo."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            productoAEliminar = null
                            vm.borrar(producto.id)
                        },
                        enabled = !ocupado
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            productoAEliminar = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun Campo(
    nombre: String,
    valor: String,
    error: String?,
    habilitado: Boolean,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        label = { Text(nombre) },
        singleLine = true,
        isError = error != null,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            error?.let { mensaje ->
                Text(mensaje)
            }
        }
    )
}