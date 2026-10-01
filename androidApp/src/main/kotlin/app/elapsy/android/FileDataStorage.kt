package app.elapsy.android

import android.util.AtomicFile
import app.elapsy.core.DataStorage
import java.io.File
import java.io.FileNotFoundException

/** Stores the document in a single file, replaced atomically on every write. */
class FileDataStorage(file: File) : DataStorage {
    private val file = AtomicFile(file)

    override fun read(): String? =
        try {
            file.readFully().decodeToString()
        } catch (_: FileNotFoundException) {
            null
        }

    override fun write(text: String) {
        val stream = file.startWrite()
        try {
            stream.write(text.encodeToByteArray())
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
            throw e
        }
    }
}
