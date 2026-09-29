package pe.edu.upeu.pharmamobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pharmamobil.presentation.App
import pharmamobil.di.commonModule
import pharmamobil.di.platformModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (GlobalContext.getOrNull() == null) startKoin { modules(commonModule, platformModule()) }
        setContent { App() }
    }
}
