package it.danieleverducci.lunatracker.repository

import android.content.Context
import android.util.Log
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import it.danieleverducci.lunatracker.entities.Logbook
import it.danieleverducci.lunatracker.entities.LunaEvent
import kotlinx.coroutines.Runnable
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.io.bufferedReader
import kotlin.text.replace

class WebDAVLogbookRepository(val webDavURL: String, val username: String, val password: String): LogbookRepository {
    companion object {
        val TAG = "LogbookRepository"
        val FILE_NAME_START = "lunatracker_logbook"
        val FILE_NAME_END = ".json"
    }
    val sardine: OkHttpSardine = OkHttpSardine()

    init {
        sardine.setCredentials(
            username,
            password
        )
    }

    override fun loadLogbook(context: Context, name: String, listener: LogbookLoadedListener) {
        Thread(Runnable {
            try {
                val logbook = loadLogbook(name)
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

    private fun loadLogbook(name: String,): Logbook {
        val inputStream = sardine.get(getUrl(name))
        val json = inputStream.bufferedReader().use(BufferedReader::readText)
        inputStream.close()
        val ja = JSONArray(json)
        val logbook = Logbook(name)
        for (i in 0 until ja.length()) {
            try {
                val evt: LunaEvent = LunaEvent(ja.getJSONObject(i))
                logbook.logs.add(evt)
            } catch (e: IllegalArgumentException) {
                // Mangled JSON?
                throw JSONException(e.toString())
            }
        }
        Log.d(TAG, "Loaded ${logbook.logs.size} events into logbook")
        return logbook
    }

    override fun saveLogbook(context: Context, logbook: Logbook, listener: LogbookSavedListener) {
        Thread(Runnable {
            try {
                saveLogbook(context, logbook)
                listener.onLogbookSaved()
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

    override fun listLogbooks(
        context: Context,
        listener: LogbookListObtainedListener
    ) {
        Thread(Runnable {
            val logbooksNames = arrayListOf<String>()
            for (dr: DavResource in sardine.list(webDavURL)){
                if(!dr.name.startsWith(FILE_NAME_START))
                    continue
                if(!dr.name.endsWith(FILE_NAME_END))
                    continue
                logbooksNames.add(
                    dr.name.replace("${FILE_NAME_START}_", "")
                        .replace(FILE_NAME_START, "")
                        .replace(FILE_NAME_END, "")
                )
            }
            listener.onLogbookListObtained(logbooksNames)
        }).start()
    }

    private fun saveLogbook(context: Context, logbook: Logbook) {
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
        sardine.put(getUrl(logbook.name), ja.toString().toByteArray())
    }

    /**
     * Connect to server and check if a logbook already exists.
     * If it does not exist, try to upload the local one.
     * @return error, or null if no error
     */
    fun uploadLogbookIfNotExists(context: Context, name: String): String? {
        val flr = FileLogbookRepository()
        try {
            loadLogbook(name)
            Log.d(TAG, "Logbook file $name already exist on the webDav share: will not overwrite it")
            return null
        } catch (e: SardineException) {
            if (e.toString().contains("404")) {
                // Connection successful, but logbook does not exist. Upload the local one.
                try {
                    val logbook = flr.loadLogbook(context, name)
                    saveLogbook(context, logbook)
                    Log.d(TAG, "Local logbook file $name found, uploaded")
                    return null
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "No local logbook file found, this should not happen!")
                    return "No local logbook file found, app is in inconsistent state, please delete and reinstall it"
                } catch (e: SardineException) {
                    Log.e(TAG, "Unable to upload logbook: $e")
                    return e.toString()
                }
            } else {
                Log.e(TAG, e.toString())
                return e.toString()
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            return e.toString()
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, e.toString())
            return e.toString()
        } catch (e: JSONException) {
            Log.e(TAG, e.toString())
            return e.toString()
        } catch (e: Exception) {
            return e.toString()
        }
    }

    private fun getUrl(name: String): String {
        val fileName = "${FILE_NAME_START}${if (name.isNotEmpty()) "_" else ""}${name}${FILE_NAME_END}"
        Log.d(TAG, fileName)
        return "$webDavURL/$fileName"
    }
}