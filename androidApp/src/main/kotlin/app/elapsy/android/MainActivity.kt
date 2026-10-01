package app.elapsy.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as ElapsyApp).repository
        setContent {
            val factory = remember { viewModelFactory { initializer { ElapsyViewModel(repository) } } }
            val model: ElapsyViewModel = viewModel(factory = factory)
            val data by model.data.collectAsStateWithLifecycle()
            val dark = isDarkTheme(data.settings.theme)
            SideEffect {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            ElapsyTheme(dark) { ElapsyRoot(model, data) }
        }
    }
}
