package pharmamobil.presentation.producto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeFarmacia = Color(0xFF006C4C)
private val Fondo = Color(0xFFF5FBF7)

@Composable
fun ProductoScreen() {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var intentoRegistrar by remember { mutableStateOf(false) }
    var resultadoActual by remember { mutableStateOf<ResultadoRegistro?>(null) }

    MaterialTheme(colorScheme = lightColorScheme(primary = VerdeFarmacia)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Fondo).padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("PharmaMobil", color = VerdeFarmacia, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    Text("Registro de productos", style = MaterialTheme.typography.titleMedium)
                    Text("Complete los datos del medicamento", color = Color.Gray)

                    CampoTexto(
                        valor = nombre,
                        alCambiar = { nombre = it; resultadoActual = null; mensaje = "" },
                        etiqueta = "Nombre del producto",
                        error = intentoRegistrar && (resultadoActual as? ResultadoRegistro.Error)?.campo == Campo.NOMBRE,
                        mensajeError = (resultadoActual as? ResultadoRegistro.Error)?.takeIf { it.campo == Campo.NOMBRE }?.mensaje
                    )
                    CampoTexto(
                        valor = precio,
                        alCambiar = { precio = it; resultadoActual = null; mensaje = "" },
                        etiqueta = "Precio (S/)",
                        error = intentoRegistrar && (resultadoActual as? ResultadoRegistro.Error)?.campo == Campo.PRECIO,
                        mensajeError = (resultadoActual as? ResultadoRegistro.Error)?.takeIf { it.campo == Campo.PRECIO }?.mensaje
                    )
                    CampoTexto(
                        valor = stock,
                        alCambiar = { stock = it; resultadoActual = null; mensaje = "" },
                        etiqueta = "Stock",
                        error = intentoRegistrar && (resultadoActual as? ResultadoRegistro.Error)?.campo == Campo.STOCK,
                        mensajeError = (resultadoActual as? ResultadoRegistro.Error)?.takeIf { it.campo == Campo.STOCK }?.mensaje
                    )

                    Button(
                        onClick = {
                            intentoRegistrar = true
                            when (val resultado = ProductoValidator.validar(nombre, precio, stock)) {
                                is ResultadoRegistro.Error -> {
                                    resultadoActual = resultado
                                    mensaje = resultado.mensaje
                                }
                                is ResultadoRegistro.Exito -> {
                                    resultadoActual = resultado
                                    mensaje = "Producto ${resultado.producto.nombre} registrado correctamente."
                                    nombre = ""
                                    precio = ""
                                    stock = ""
                                    intentoRegistrar = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeFarmacia)
                    ) {
                        Text("Registrar producto", fontWeight = FontWeight.Bold)
                    }

                    if (mensaje.isNotEmpty()) {
                        val exito = resultadoActual is ResultadoRegistro.Exito
                        Row(
                            modifier = Modifier.fillMaxWidth().background(
                                if (exito) Color(0xFFE4F7EC) else Color(0xFFFFEDEA),
                                RoundedCornerShape(12.dp)
                            ).padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (exito) "✓" else "!", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(10.dp))
                            Text(mensaje, color = if (exito) Color(0xFF146C43) else Color(0xFFB3261E))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: Boolean,
    mensajeError: String?
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = error,
        supportingText = if (error && mensajeError != null) {
            { Text(mensajeError) }
        } else null,
        shape = RoundedCornerShape(14.dp)
    )
}
