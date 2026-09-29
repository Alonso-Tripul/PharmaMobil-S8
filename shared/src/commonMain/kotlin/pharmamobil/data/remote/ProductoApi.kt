package pharmamobil.data.remote
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import pharmamobil.data.remote.dto.*
class ProductoApi(private val client: HttpClient) {
    suspend fun listar(pagina: Int = 0): PaginaResponseDto<ProductoResponseDto> =
        client.get("productos") { parameter("pagina",pagina); parameter("tamanio",20) }.body()
    suspend fun obtener(id: Long): ProductoResponseDto = client.get("productos/$id").body()
    suspend fun crear(request: ProductoRequestDto): ProductoResponseDto = client.post("productos") { setBody(request) }.body()
    suspend fun actualizar(id: Long,request: ProductoRequestDto): ProductoResponseDto = client.put("productos/$id") { setBody(request) }.body()
    suspend fun eliminar(id: Long) { client.delete("productos/$id") }
}
