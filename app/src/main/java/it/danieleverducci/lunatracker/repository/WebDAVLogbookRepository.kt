package it.danieleverducci.lunatracker.repository

import android.content.Context
import android.util.Log
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import it.danieleverducci.lunatracker.entities.Logbook
import it.danieleverducci.lunatracker.entities.LunaEvent
import kotlinx.coroutines.Runnable
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.io.bufferedReader

class WebDAVLogbookRepository(val webDavURL: String, val username: String, val password: String): LogbookRepository {
    companion object {
        val TAG = "LogbookRepository"
        val FILE_NAME = "lunatracker_logbook.json"
    }
    val sardine: OkHttpSardine = OkHttpSardine()

    init {
        sardine.setCredentials(
            username,
            password
        )
    }

    override fun loadLogbook(context: Context, listener: LogbookLoadedListener) {
        Thread(Runnable {
            try {
                val inputStream = sardine.get("$webDavURL/$FILE_NAME")
                val json = inputStream.bufferedReader().use(BufferedReader::readText)
                inputStream.close()
                val ja = JSONArray(json)
                val logbook = Logbook()
                for (i in 0 until ja.length()) {
                    val jo = ja.getJSONObject(i)
                    val evt = LunaEvent.fromJson(jo)
                    logbook.logs.add(evt)
                }
                listener.onLogbookLoaded(logbook)
            } catch (e: SardineException) {
                Log.e(TAG, e.toString())
                listener.onWebDAVError(e)
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
                listener.onIOError(e)
            } catch (e: SocketTimeoutException) {
                Log.e(TAG, e.toString())
                listener.onIOError(e)
            } catch (e: JSONException) {
                Log.e(TAG, e.toString())
                listener.onJSONError(e)
            } catch (e: Exception) {
                listener.onError(e)
            }
        }).start()
    }

    override fun saveLogbook(context: Context, logbook: Logbook, listener: LogbookSavedListener) {
        Thread(Runnable {
            // Lock logbook on WebDAV to avoid concurrent changes
            //sardine.lock(getUrl())
            // Reload logbook from WebDAV
            // Merge logbooks (based on time)
            // Write logbook
            // Unlock logbook on WebDAV
            //sardine.unlock(getUrl())

            val ja = JSONArray()
            for (l in logbook.logs) {
                ja.put(l.toJson())
            }
            try {
                sardine.put(getUrl(), ja.toString().toByteArray())
                listener.onLogbookSaved()
            } catch (e: SardineException) {
                listener.onError(e.toString())
            }

        }).start()
    }

    private fun getUrl(): String {
        return "$webDavURL/$FILE_NAME"
    }
}