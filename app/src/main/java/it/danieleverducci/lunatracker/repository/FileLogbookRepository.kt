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
        val logbook = Logbook()
        val file = File(context.getFilesDir(), "data.json")
        try {
            val json = FileInputStream(file).bufferedReader().use { it.readText() }
            val ja = JSONArray(json)
            for (i in 0 until ja.length()) {
                val jo = ja.getJSONObject(i)
                val evt = LunaEvent.fromJson(jo)
                logbook.logs.add(evt)
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "No logbook file found")
            listener.onIOError(e)
        }
        listener.onLogbookLoaded(logbook)
    }

    override fun saveLogbook(
        context: Context,
        logbook: Logbook,
        listener: LogbookSavedListener
    ) {
        val file = File(context.getFilesDir(), "data.json")
        val ja = JSONArray()
        for (l in logbook.logs) {
            ja.put(l.toJson())
        }
        file.writeText(ja.toString())
        listener.onLogbookSaved()
    }
}