package it.danieleverducci.lunatracker.entities

import android.content.Context
import android.util.Log
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class Logbook {
    companion object {
        val TAG = "Logbook"

        fun load(context: Context): Logbook {
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
            }
            return logbook
        }
    }

    val logs = ArrayList<LunaEvent>()

    fun save(context: Context) {
        val file = File(context.getFilesDir(), "data.json")
        val ja = JSONArray()
        for (l in logs) {
            ja.put(l.toJson())
        }
        file.writeText(ja.toString())
    }
}