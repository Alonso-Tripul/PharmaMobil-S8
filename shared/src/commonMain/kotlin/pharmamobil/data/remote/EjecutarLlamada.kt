package pharmamobil.data.remote
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import pharmamobil.data.remote.dto.ErrorResponseDto
import pharmamobil.domain.error.*
suspend fun <T> ejecutarLlamada(bloque: suspend () -> T): Result<T> = try {
    Result.success(bloque())
} catch (e: CancellationException) { throw e
} catch (e: ClientRequestException) {
    val cuerpo = try { e.response.body<ErrorResponseDto>() } catch (c: CancellationException) { throw c } catch (_: Exception) { null }
    val error = when (e.response.status.value) {
        400 -> ErrorApi.Validacion(cuerpo?.validationErrors.orEmpty(),cuerpo?.message ?: "Revisa los datos enviados (400).")
        404 -> ErrorApi.NoEncontrado
        409 -> ErrorApi.Conflicto(cuerpo?.message ?: "Operación no permitida (409).")
        else -> ErrorApi.Servidor
    }
    Result.failure(ErrorApiException(error))
} catch (_: ServerResponseException) { Result.failure(ErrorApiException(ErrorApi.Servidor))
} catch (_: HttpRequestTimeoutException) { Result.failure(ErrorApiException(ErrorApi.TiempoAgotado))
} catch (_: ConnectTimeoutException) { Result.failure(ErrorApiException(ErrorApi.TiempoAgotado))
} catch (_: SocketTimeoutException) { Result.failure(ErrorApiException(ErrorApi.TiempoAgotado))
} catch (_: IOException) { Result.failure(ErrorApiException(ErrorApi.SinConexion))
} catch (_: SerializationException) { Result.failure(ErrorApiException(ErrorApi.RespuestaInvalida))
} catch (_: Exception) { Result.failure(ErrorApiException(ErrorApi.RespuestaInvalida)) }
