package it.danieleverducci.lunatracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.PopupWindow
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
import it.danieleverducci.lunatracker.repository.FileLogbookRepository
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
        val UPDATE_EVERY_SECS: Long = 30
    }

    lateinit var logbook: Logbook
    lateinit var adapter: LunaEventRecyclerAdapter
    lateinit var progressIndicator: LinearProgressIndicator
    lateinit var buttonsContainer: ViewGroup
    lateinit var recyclerView: RecyclerView
    lateinit var handler: Handler
    var savingEvent = false
    val updateListRunnable: Runnable = Runnable {
        loadLogbook()
        handler.postDelayed(updateListRunnable, 1000*60)
    }
    var logbookRepo: LogbookRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(mainLooper)
        adapter = LunaEventRecyclerAdapter(this)

        // Show view
        setContentView(R.layout.activity_main)

        progressIndicator = findViewById<LinearProgressIndicator>(R.id.progress_indicator)
        buttonsContainer = findViewById<ViewGroup>(R.id.buttons_container)
        recyclerView = findViewById<RecyclerView>(R.id.list_events)
        recyclerView.setLayoutManager(LinearLayoutManager(applicationContext))
        recyclerView.adapter = adapter

        // Set listeners
        findViewById<View>(R.id.button_bottle).setOnClickListener { askBabyBottleContent() }
        findViewById<View>(R.id.button_scale).setOnClickListener { askWeightValue() }
        findViewById<View>(R.id.button_nipple_left).setOnClickListener { logEvent(
            LunaEvent(
                LunaEvent.TYPE_BREASTFEEDING_LEFT_NIPPLE
            )
        ) }
        findViewById<View>(R.id.button_nipple_both).setOnClickListener { logEvent(
            LunaEvent(
                LunaEvent.TYPE_BREASTFEEDING_BOTH_NIPPLE
            )
        ) }
        findViewById<View>(R.id.button_nipple_right).setOnClickListener { logEvent(
            LunaEvent(
                LunaEvent.TYPE_BREASTFEEDING_RIGHT_NIPPLE
            )
        ) }
        findViewById<View>(R.id.button_change_poo).setOnClickListener { logEvent(
            LunaEvent(
                LunaEvent.TYPE_DIAPERCHANGE_POO
            )
        ) }
        findViewById<View>(R.id.button_change_pee).setOnClickListener { logEvent(
            LunaEvent(
                LunaEvent.TYPE_DIAPERCHANGE_PEE
            )
        ) }
        val moreButton = findViewById<View>(R.id.button_more)
        moreButton.setOnClickListener {
            showOverflowPopupWindow(moreButton)
        }
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

        val settingsRepository = LocalSettingsRepository(this)
        if (settingsRepository.loadDataRepository() == LocalSettingsRepository.DATA_REPO.WEBDAV) {
            val webDavCredentials = settingsRepository.loadWebdavCredentials()
            if (webDavCredentials == null) {
                throw IllegalStateException("Corrupted local settings: repo is webdav, but no webdav login data saved")
            }
            logbookRepo = WebDAVLogbookRepository(
                webDavCredentials[0],
                webDavCredentials[1],
                webDavCredentials[2]
            )
        } else {
            logbookRepo = FileLogbookRepository()
        }

        // Update list dates
        adapter.notifyDataSetChanged()

        // Reload data
        loadLogbook()
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
            logEvent(LunaEvent(LunaEvent.TYPE_BABY_BOTTLE, numberPicker.value * 10))
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
        val weightET = dialogView.findViewById<EditText>(R.id.dialog_number_edittext)
        d.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            val weight = weightET.text.toString().toIntOrNull()
            if (weight != null)
                logEvent(LunaEvent(LunaEvent.TYPE_WEIGHT, weight))
            else
                Toast.makeText(this, R.string.toast_integer_error, Toast.LENGTH_SHORT).show()
        }
        d.setNegativeButton(android.R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = d.create()
        alertDialog.show()
    }

    fun askNotes(lunaEvent: LunaEvent) {
        val d = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_notes, null)
        d.setTitle(lunaEvent.getTypeDescription(this))
        d.setMessage(
            when (lunaEvent.type){
                LunaEvent.TYPE_MEDICINE -> R.string.log_medicine_dialog_description
                else -> R.string.log_notes_dialog_description
            }
        )
        d.setView(dialogView)
        val notesET = dialogView.findViewById<EditText>(R.id.notes_edittext)
        val qtyET = dialogView.findViewById<EditText>(R.id.notes_qty_edittext)
        d.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            val qtyStr = qtyET.text.toString()
            if (qtyStr.isNotEmpty()) {
                val qty = qtyStr.toIntOrNull()
                if (qty == null) {
                    Toast.makeText(this, R.string.toast_integer_error, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lunaEvent.quantity = qty
            }
            val notes = notesET.text.toString()
            lunaEvent.notes = notes
            logEvent(lunaEvent)
        }
        d.setNegativeButton(android.R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = d.create()
        alertDialog.show()
    }

    fun askToTrimLogbook() {
        val d = AlertDialog.Builder(this)
        d.setTitle(R.string.trim_logbook_dialog_title)
        d.setMessage(
            when (LocalSettingsRepository(this).loadDataRepository()) {
                LocalSettingsRepository.DATA_REPO.WEBDAV -> R.string.trim_logbook_dialog_message_dav
                else -> R.string.trim_logbook_dialog_message_local
            }
        )
        d.setPositiveButton(R.string.trim_logbook_dialog_button_ok) { dialogInterface, i ->
            logbook.trim()
            saveLogbook()
        }
        d.setNegativeButton(R.string.trim_logbook_dialog_button_cancel) { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        val alertDialog = d.create()
        alertDialog.show()
    }

    fun loadLogbook() {
        if (savingEvent)
            return

        // Reset time counter
        handler.removeCallbacks(updateListRunnable)
        handler.postDelayed(updateListRunnable, UPDATE_EVERY_SECS*1000)

        // Load data
        setLoading(true)
        logbookRepo?.loadLogbook(this, object: LogbookLoadedListener{
            override fun onLogbookLoaded(lb: Logbook) {
                runOnUiThread({
                    setLoading(false)
                    findViewById<View>(R.id.no_connection_screen).visibility = View.GONE
                    logbook = lb
                    showLogbook()
                })
            }

            override fun onIOError(error: IOException) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(getString(R.string.settings_network_error) + error.toString())
                })
            }

            override fun onWebDAVError(error: SardineException) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(
                        if(error.toString().contains("401")) {
                            getString(R.string.settings_webdav_error_denied)
                        } else {
                            getString(R.string.settings_webdav_error_generic) + error.toString()
                        }
                    )
                })
            }

            override fun onJSONError(error: JSONException) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(getString(R.string.settings_json_error) + error.toString())
                })
            }

            override fun onError(error: Exception) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(getString(R.string.settings_generic_error) + error.toString())
                })
            }

        })
    }

    fun onRepoError(message: String){
        runOnUiThread({
            setLoading(false)
            findViewById<View>(R.id.no_connection_screen).visibility = View.VISIBLE
            findViewById<TextView>(R.id.no_connection_screen_message).text = message
        })
    }

    fun logEvent(event: LunaEvent) {
        savingEvent(true)
        adapter.items.add(0, event)
        adapter.notifyItemInserted(0)
        recyclerView.smoothScrollToPosition(0)

        setLoading(true)
        logbook.logs.add(0, event)
        saveLogbook(event)

        // Check logbook size to avoid OOM errors
        if (logbook.isTooBig()) {
            askToTrimLogbook()
        }
    }

    /**
     * Saves the logbook. If saving while adding an event, please specify the event so in case
     * of error can be removed from the list.
     */
    fun saveLogbook(lastEventAdded: LunaEvent? = null) {
        logbookRepo?.saveLogbook(this, logbook, object: LogbookSavedListener{
            override fun onLogbookSaved() {
                Log.d(TAG, "Logbook saved")
                runOnUiThread({
                    setLoading(false)

                    Toast.makeText(
                        this@MainActivity,
                        if (lastEventAdded != null)
                            R.string.toast_event_added
                        else
                            R.string.toast_logbook_saved,
                        Toast.LENGTH_SHORT
                    ).show()
                    savingEvent(false)
                })
            }

            override fun onIOError(error: IOException) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(getString(R.string.settings_network_error) + error.toString())
                    if (lastEventAdded != null)
                        onAddError(lastEventAdded, error.toString())
                })
            }

            override fun onWebDAVError(error: SardineException) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(
                        if(error.toString().contains("401")) {
                            getString(R.string.settings_webdav_error_denied)
                        } else {
                            getString(R.string.settings_webdav_error_generic) + error.toString()
                        }
                    )
                    if (lastEventAdded != null)
                        onAddError(lastEventAdded, error.toString())
                })
            }

            override fun onJSONError(error: JSONException) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(getString(R.string.settings_json_error) + error.toString())
                    if (lastEventAdded != null)
                        onAddError(lastEventAdded, error.toString())
                })
            }

            override fun onError(error: Exception) {
                runOnUiThread({
                    setLoading(false)
                    onRepoError(getString(R.string.settings_generic_error) + error.toString())
                    if (lastEventAdded != null)
                        onAddError(lastEventAdded, error.toString())
                })
            }
        })
    }

    private fun onAddError(event: LunaEvent, error: String) {
        Log.e(TAG, "Logbook was NOT saved! $error")
        runOnUiThread({
            setLoading(false)

            Toast.makeText(this@MainActivity, R.string.toast_event_add_error, Toast.LENGTH_SHORT).show()
            adapter.items.remove(event)
            adapter.notifyDataSetChanged()
            savingEvent(false)
        })
    }

    private fun setLoading(loading: Boolean) {
        if (loading) {
            progressIndicator.visibility = View.VISIBLE
        } else {
            progressIndicator.visibility = View.INVISIBLE
        }
    }

    private fun savingEvent(saving: Boolean) {
        if (saving) {
            savingEvent = true
            buttonsContainer.alpha = 0.2f
        } else {
            savingEvent = false
            buttonsContainer.alpha = 1f
        }
    }

    private fun showOverflowPopupWindow(anchor: View) {
        PopupWindow(anchor.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor.context)
            contentView = inflater.inflate(R.layout.more_events_popup, null)
            contentView.findViewById<View>(R.id.button_medicine).setOnClickListener({
                askNotes(LunaEvent(LunaEvent.TYPE_MEDICINE))
                dismiss()
            })
            contentView.findViewById<View>(R.id.button_enema).setOnClickListener({
                logEvent(LunaEvent(LunaEvent.TYPE_ENEMA))
                dismiss()
            })
            contentView.findViewById<View>(R.id.button_custom).setOnClickListener({
                Toast.makeText(anchor.context, "TODO: Implement custom events", Toast.LENGTH_SHORT).show()
                dismiss()
            })
        }.also { popupWindow ->
            popupWindow.showAsDropDown(anchor)
        }
    }
}
