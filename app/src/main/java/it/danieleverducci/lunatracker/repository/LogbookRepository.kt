package it.danieleverducci.lunatracker.repository

import android.content.Context
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import it.danieleverducci.lunatracker.entities.Logbook
import okio.IOException
import org.json.JSONException

interface LogbookRepository {
    companion object {
        val DEFAULT_LOGBOOK_NAME = ""   // For compatibility with older app versions
    }
    fun loadLogbook(context: Context, name: String = "", listener: LogbookLoadedListener)
    fun saveLogbook(context: Context,logbook: Logbook, listener: LogbookSavedListener)
    fun listLogbooks(context: Context, listener: LogbookListObtainedListener)
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
    fun onIOError(error: IOException)
    fun onWebDAVError(error: SardineException)
    fun onJSONError(error: JSONException)
    fun onError(error: Exception)
}

interface LogbookListObtainedListener {
    fun onLogbookListObtained(logbooksNames: ArrayList<String>)
    fun onIOError(error: IOException)
    fun onWebDAVError(error: SardineException)
    fun onError(error: Exception)
}