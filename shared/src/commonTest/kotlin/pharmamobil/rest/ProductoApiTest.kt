package pharmamobil.rest

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import pharmamobil.data.remote.*
import pharmamobil.data.remote.dto.*
import pharmamobil.domain.error.*

class ProductoApiTest {
    private val producto = """{"id":7,"nombre":"Paracetamol","precio":4.5,"stock":8,"estado":true,"categoriaId":1}"""
    private val request = ProductoRequestDto("Paracetamol",4.5,8,true,1)
    private fun cliente(status: HttpStatusCode,body: String,verificar: (HttpRequestData)->Unit = {}): HttpClient =
        crearHttpClient(MockEngine { req ->
            verificar(req)
            respond(body,status,headersOf(HttpHeaders.ContentType,"application/json"))
        })

    @Test fun listadoDeserializaPaginacion() = runTest {
        val c=cliente(HttpStatusCode.OK,"""{"contenido":[$producto],"pagina":0,"tamanio":20,"totalElementos":1,"totalPaginas":1,"ultima":true}""") {
            assertEquals(HttpMethod.Get,it.method);assertEquals("20",it.url.parameters["tamanio"])
        }
        try { assertEquals(7,ProductoApi(c).listar().contenido.single().id.toInt()) } finally { c.close() }
    }
    @Test fun obtenerUsaId() = runTest {
        val c=cliente(HttpStatusCode.OK,producto) { assertTrue(it.url.encodedPath.endsWith("/productos/7"));assertEquals(HttpMethod.Get,it.method) }
        try { assertEquals("Paracetamol",ProductoApi(c).obtener(7).nombre) } finally { c.close() }
    }
    @Test fun crearUsaPost() = runTest {
        val c=cliente(HttpStatusCode.Created,producto) { assertEquals(HttpMethod.Post,it.method) }
        try { assertEquals(7L,ProductoApi(c).crear(request).id) } finally { c.close() }
    }
    @Test fun actualizarUsaPut() = runTest {
        val c=cliente(HttpStatusCode.OK,producto) { assertEquals(HttpMethod.Put,it.method);assertTrue(it.url.encodedPath.endsWith("/productos/7")) }
        try { assertEquals(7L,ProductoApi(c).actualizar(7,request).id) } finally { c.close() }
    }
    @Test fun eliminarAcepta204SinCuerpo() = runTest {
        val c=cliente(HttpStatusCode.NoContent,"") { assertEquals(HttpMethod.Delete,it.method) }
        try { assertTrue(ejecutarLlamada { ProductoApi(c).eliminar(7) }.isSuccess) } finally { c.close() }
    }
    @Test fun validacion400PreservaCampo() = runTest {
        val c=cliente(HttpStatusCode.BadRequest,"""{"message":"Datos inválidos","validationErrors":{"nombre":"Debe tener al menos 3 caracteres"}}""")
        try {
            val r=ejecutarLlamada { ProductoApi(c).crear(request.copy(nombre="ab")) }
            val e=(r.exceptionOrNull() as ErrorApiException).error as ErrorApi.Validacion
            assertEquals("Debe tener al menos 3 caracteres",e.porCampo["nombre"])
        } finally { c.close() }
    }
    @Test fun cancelacionNoSeConvierteEnError() = runTest {
        assertFailsWith<CancellationException> { ejecutarLlamada<Unit> { throw CancellationException("cancelado") } }
    }
}
