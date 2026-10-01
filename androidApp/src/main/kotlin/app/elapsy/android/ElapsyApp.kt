package app.elapsy.android

import android.app.Application
import androidx.glance.appwidget.updateAll
import app.elapsy.android.widget.EventWidget
import app.elapsy.core.EventRepository
import java.io.File
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ElapsyApp : Application() {
    private val scope = MainScope()

    val repository: EventRepository by lazy {
        EventRepository(
            storage = FileDataStorage(File(filesDir, "data.json")),
            onChanged = { scope.launch { EventWidget().updateAll(this@ElapsyApp) } },
        )
    }
}
