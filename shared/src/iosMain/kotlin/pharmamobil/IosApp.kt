package pharmamobil

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import pharmamobil.di.commonModule
import pharmamobil.di.platformModule
import pharmamobil.presentation.App

fun MainViewController() = ComposeUIViewController {
    if (GlobalContext.getOrNull() == null) startKoin { modules(commonModule, platformModule()) }
    App()
}
