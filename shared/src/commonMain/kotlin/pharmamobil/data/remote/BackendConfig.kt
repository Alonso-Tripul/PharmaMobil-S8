package pharmamobil.data.remote
expect fun urlInicial(): String
object BackendConfig { var baseUrl: String = urlInicial() }
