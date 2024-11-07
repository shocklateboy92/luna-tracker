package it.danieleverducci.lunatracker.repository

import android.content.Context
import it.danieleverducci.lunatracker.entities.Logbook

interface LogbookRepository {
    fun loadLogbook(context: Context, listener: LogbookLoadedListener)
    fun saveLogbook(context: Context, logbook: Logbook, listener: LogbookSavedListener)
}

interface LogbookLoadedListener {
    fun onLogbookLoaded(logbook: Logbook)
    fun onError(error: String)
}

interface LogbookSavedListener {
    fun onLogbookSaved()
    fun onError(error: String)
}