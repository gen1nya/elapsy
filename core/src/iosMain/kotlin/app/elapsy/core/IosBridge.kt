@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package app.elapsy.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfURL
import platform.Foundation.writeToURL

/**
 * Keeps the document in the App Group container so the app, its widget and its intents extension
 * all read the same file.
 */
class AppGroupStorage(appGroupId: String) : DataStorage {
    private val file: NSURL = run {
        val manager = NSFileManager.defaultManager
        // Without the App Group entitlement there is no shared container; fall back to a private
        // directory so the app itself keeps working.
        val directory = manager.containerURLForSecurityApplicationGroupIdentifier(appGroupId)
            ?: manager.URLForDirectory(NSApplicationSupportDirectory, NSUserDomainMask, null, true, null)
            ?: error("No writable directory")
        manager.createDirectoryAtURL(directory, withIntermediateDirectories = true, attributes = null, error = null)
        directory.URLByAppendingPathComponent("data.json") ?: error("Invalid storage path")
    }

    override fun read(): String? = NSString.stringWithContentsOfURL(file, NSUTF8StringEncoding, null)

    override fun write(text: String) {
        val written = NSString.create(string = text).writeToURL(file, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        check(written) { "Could not write ${file.path}" }
    }
}

fun createRepository(appGroupId: String, onChanged: () -> Unit): EventRepository =
    EventRepository(storage = AppGroupStorage(appGroupId), onChanged = onChanged)

/** One-shot read for the widget and the intents extension, which never modify the data. */
fun loadAppData(appGroupId: String): AppData =
    AppGroupStorage(appGroupId).read()?.let(AppDataCodec::decodeOrNull) ?: AppData()

class Subscription internal constructor(private val scope: CoroutineScope) {
    fun cancel() = scope.cancel()
}

/** Delivers the current data and every later change on the main thread. */
fun EventRepository.observe(onEach: (AppData) -> Unit): Subscription {
    val scope = CoroutineScope(Dispatchers.Main.immediate)
    scope.launch { data.collect { onEach(it) } }
    return Subscription(scope)
}

fun localDate(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)

/** kotlinx-datetime's Month type is awkward from Swift; this exposes the plain 1...12 number. */
fun monthNumber(date: LocalDate): Int = date.month.number
