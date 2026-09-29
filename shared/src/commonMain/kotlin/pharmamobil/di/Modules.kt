package pharmamobil.di
import org.koin.core.module.Module
import org.koin.dsl.module
import pharmamobil.data.remote.*
import pharmamobil.data.repository.ProductoRepositoryImpl
import pharmamobil.domain.repository.ProductoRepository
import pharmamobil.domain.usecase.*
import pharmamobil.presentation.crud.ProductosViewModel
val commonModule = module {
    single { crearHttpClient(get()) }
    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositoryImpl(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { ObtenerProductoUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
    factory { ProductosViewModel(get(),get(),get(),get(),get()) }
}
expect fun platformModule(): Module

object ConfiguracionServidor {
    var url: String
        get() = BackendConfig.baseUrl
        set(value) { BackendConfig.baseUrl = value }
}
