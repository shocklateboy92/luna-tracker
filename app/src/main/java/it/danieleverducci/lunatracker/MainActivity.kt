package it.danieleverducci.lunatracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import it.danieleverducci.lunatracker.adapters.LunaEventRecyclerAdapter
import it.danieleverducci.lunatracker.entities.Logbook
import it.danieleverducci.lunatracker.entities.LunaEvent
import it.danieleverducci.lunatracker.entities.LunaEventType
import it.danieleverducci.lunatracker.repository.LocalSettingsRepository
import it.danieleverducci.lunatracker.repository.LogbookLoadedListener
import it.danieleverducci.lunatracker.repository.LogbookRepository
import it.danieleverducci.lunatracker.repository.LogbookSavedListener
import it.danieleverducci.lunatracker.repository.WebDAVLogbookRepository
import kotlinx.coroutines.Runnable
import okio.IOException
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    lateinit var logbook: Logbook
    lateinit var adapter: LunaEventRecyclerAdapter
    lateinit var progressIndicator: LinearProgressIndicator
    lateinit var recyclerView: RecyclerView
    lateinit var handler: Handler
    var savingEvent = false
    val updateListRunnable: Runnable = Runnable {
        loadLogbook()
        handler.postDelayed(updateListRunnable, 1000*60)
    }
    lateinit var logbookRepo: LogbookRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val localSettings = LocalSettingsRepository(this)
        val webDavCredentials = localSettings.loadWebdavCredentials()
        if (webDavCredentials == null) {
            TODO("Not supported ATM (TODO: apply settings)")
        }
        logbookRepo = WebDAVLogbookRepository(   // TODO: support also FileLogbookRepository
            webDavCredentials[0],
            webDavCredentials[1],
            webDavCredentials[2]
        )

        handler = Handler(mainLooper)
        adapter = LunaEventRecyclerAdapter(this)

        // Show view
        setContentView(R.layout.activity_main)

        progressIndicator = findViewById<LinearProgressIndicator>(R.id.progress_indicator)
        recyclerView = findViewById<RecyclerView>(R.id.list_events)
        recyclerView.setLayoutManager(LinearLayoutManager(applicationContext))
        recyclerView.adapter = adapter

        // Set listeners
        findViewById<View>(R.id.button_bottle).setOnClickListener { askBabyBottleContent() }
        findViewById<View>(R.id.button_scale).setOnClickListener { askWeightValue() }
        findViewById<View>(R.id.button_nipple_left).setOnClickListener { logEvent(
            LunaEvent(
                LunaEventType.BREASTFEEDING_LEFT_NIPPLE
            )
        ) }
        findViewById<View>(R.id.button_nipple_both).setOnClickListener { logEvent(
            LunaEvent(
                LunaEventType.BREASTFEEDING_BOTH_NIPPLE
            )
        ) }
        findViewById<View>(R.id.button_nipple_right).setOnClickListener { logEvent(
            LunaEvent(
                LunaEventType.BREASTFEEDING_RIGHT_NIPPLE
            )
        ) }
        findViewById<View>(R.id.button_change_poo).setOnClickListener { logEvent(
            LunaEvent(
                LunaEventType.DIAPERCHANGE_POO
            )
        ) }
        findViewById<View>(R.id.button_change_pee).setOnClickListener { logEvent(
            LunaEvent(
                LunaEventType.DIAPERCHANGE_PEE
            )
        ) }
        findViewById<View>(R.id.button_no_connection_settings).setOnClickListener({
            showSettings()
        })
        findViewById<View>(R.id.button_settings).setOnClickListener({
            showSettings()
        })
        findViewById<View>(R.id.button_no_connection_retry).setOnClickListener({
            loadLogbook()
        })
        findViewById<View>(R.id.button_sync).setOnClickListener({
            loadLogbook()
        })
    }

    fun showSettings() {
        val i = Intent(this, SettingsActivity::class.java)
        startActivity(i)

    }

    fun showLogbook() {
        // Show logbook
        adapter.items.clear()
        adapter.items.addAll(logbook.logs)
        adapter.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()

        // Update list dates
        adapter.notifyDataSetChanged()

        // Reload data
        loadLogbook()
        handler.postDelayed(updateListRunnable, 1000*30)
    }

    override fun onStop() {
        handler.removeCallbacks(updateListRunnable)

        super.onStop()
    }

    fun askBabyBottleContent() {
        // Show number picker dialog
        val localSettings = LocalSettingsRepository(this)
        val d = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.number_picker_dialog, null)
        d.setTitle(R.string.log_bottle_dialog_title)
        d.setMessage(R.string.log_bottle_dialog_description)
        d.setView(dialogView)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialog_number_picker)
        numberPicker.minValue = 1 // "10"
        numberPicker.maxValue = 25 // "250
        numberPicker.displayedValues = ((10..250 step 10).map { it.toString() }.toTypedArray())
        numberPicker.wrapSelectorWheel = false
        numberPicker.value = localSettings.loadBabyBottleContent()
        d.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            logEvent(LunaEvent(LunaEventType.BABY_BOTTLE, numberPicker.value * 10))
            localSettings.saveBabyBottleContent(numberPicker.value)
        }
        d.setNegativeButton(android.R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = d.create()
        alertDialog.show()
    }

    fun askWeightValue() {
        // Show number picker dialog
        val d = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.number_edit_dialog, null)
        d.setTitle(R.string.log_weight_dialog_title)
        d.setMessage(R.string.log_weight_dialog_description)
        d.setView(dialogView)
        val weightET = dialogView.findViewById<TextView>(R.id.dialog_number_edittext)
        d.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            val weight = weightET.text.toString().toIntOrNull()
            if (weight != null)
                logEvent(LunaEvent(LunaEventType.WEIGHT, weight))
            else
                Toast.makeText(this, R.string.toast_integer_error, Toast.LENGTH_SHORT).show()
        }
        d.setNegativeButton(android.R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = d.create()
        alertDialog.show()
    }

    fun loadLogbook() {
        if (savingEvent)
            return

        // Load data
        progressIndicator.visibility = View.VISIBLE
        logbookRepo.loadLogbook(this, object: LogbookLoadedListener{
            override fun onLogbookLoaded(lb: Logbook) {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    findViewById<View>(R.id.no_connection_screen).visibility = View.GONE
                    logbook = lb
                    showLogbook()
                })
            }

            override fun onIOError(error: IOException) {
                onRepoError(error) // TODO: Meaningful message
            }

            override fun onWebDAVError(error: SardineException) {
                onRepoError(error) // TODO: Meaningful message
            }

            override fun onJSONError(error: JSONException) {
                onRepoError(error) // TODO: Meaningful message
            }
        })
    }

    fun onRepoError(e: Exception){
        runOnUiThread({
            progressIndicator.visibility = View.INVISIBLE
            findViewById<View>(R.id.no_connection_screen).visibility = View.VISIBLE

            Log.e(TAG, "Unable to load logbook: ${e.toString()} . Created a new one.")
            logbook = Logbook()
            showLogbook()
        })
    }

    fun logEvent(event: LunaEvent) {
        savingEvent = true
        adapter.items.add(0, event)
        adapter.notifyItemInserted(0)
        recyclerView.smoothScrollToPosition(0)

        progressIndicator.visibility = View.VISIBLE
        logbook.logs.add(0, event)
        logbookRepo.saveLogbook(this, logbook, object: LogbookSavedListener{
            override fun onLogbookSaved() {
                Log.d(TAG, "Logbook saved")
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE

                    Toast.makeText(this@MainActivity, R.string.toast_event_added, Toast.LENGTH_SHORT).show()
                    savingEvent = false
                })
            }

            override fun onError(error: String) {
                Log.e(TAG, "ERROR: Logbook was NOT saved!")
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE

                    Toast.makeText(this@MainActivity, R.string.toast_event_add_error, Toast.LENGTH_SHORT).show()
                    adapter.items.remove(event)
                    adapter.notifyDataSetChanged()
                    savingEvent = false
                })
            }

        })
    }

}
