package pharmamobil.data.remote
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
fun crearHttpClient(engine: HttpClientEngine) = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
    install(Logging) {
        logger = object : Logger { override fun log(message: String) { println("KTOR: $message") } }
        level = LogLevel.HEADERS
    }
    install(HttpTimeout) { requestTimeoutMillis = 15_000; connectTimeoutMillis = 10_000 }
    defaultRequest { url(BackendConfig.baseUrl); contentType(ContentType.Application.Json) }
}
