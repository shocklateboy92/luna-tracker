package it.danieleverducci.lunatracker.repository

import android.content.Context
import it.danieleverducci.lunatracker.entities.Logbook
import android.util.Log
import it.danieleverducci.lunatracker.entities.LunaEvent
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class FileLogbookRepository: LogbookRepository {
    companion object {
        val TAG = "FileLogbookRepository"
    }

    override fun loadLogbook(context: Context, listener: LogbookLoadedListener) {
        try {
            listener.onLogbookLoaded(loadLogbook(context))
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "No logbook file found, create one")
            val newLogbook = Logbook()
            saveLogbook(context, newLogbook)
            listener.onLogbookLoaded(newLogbook)
        }
    }

    fun loadLogbook(context: Context): Logbook {
        val logbook = Logbook()
        val file = File(context.getFilesDir(), "data.json")
        val json = FileInputStream(file).bufferedReader().use { it.readText() }
        val ja = JSONArray(json)
        for (i in 0 until ja.length()) {
            val jo = ja.getJSONObject(i)
            val evt = LunaEvent.fromJson(jo)
            logbook.logs.add(evt)
        }
        return logbook
    }

    override fun saveLogbook(
        context: Context,
        logbook: Logbook,
        listener: LogbookSavedListener
    ) {
        saveLogbook(context, logbook)
        listener.onLogbookSaved()
    }

    fun saveLogbook(context: Context, logbook: Logbook) {
        val file = File(context.getFilesDir(), "data.json")
        val ja = JSONArray()
        for (l in logbook.logs) {
            ja.put(l.toJson())
        }
        file.writeText(ja.toString())
    }
}