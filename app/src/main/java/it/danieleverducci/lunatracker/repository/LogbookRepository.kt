package it.danieleverducci.lunatracker.repository

import android.content.Context
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import it.danieleverducci.lunatracker.entities.Logbook
import okio.IOException
import org.json.JSONException

interface LogbookRepository {
    fun loadLogbook(context: Context, listener: LogbookLoadedListener)
    fun saveLogbook(context: Context, logbook: Logbook, listener: LogbookSavedListener)
}

interface LogbookLoadedListener {
    fun onLogbookLoaded(logbook: Logbook)
    fun onIOError(error: IOException)
    fun onWebDAVError(error: SardineException)
    fun onJSONError(error: JSONException)
    fun onError(error: Exception)
}

interface LogbookSavedListener {
    fun onLogbookSaved()
    fun onError(error: String)
}