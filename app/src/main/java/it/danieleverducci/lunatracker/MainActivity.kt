package it.danieleverducci.lunatracker

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
import it.danieleverducci.lunatracker.adapters.LunaEventRecyclerAdapter
import it.danieleverducci.lunatracker.entities.Logbook
import it.danieleverducci.lunatracker.entities.LunaEvent
import it.danieleverducci.lunatracker.entities.LunaEventType
import it.danieleverducci.lunatracker.repository.LogbookLoadedListener
import it.danieleverducci.lunatracker.repository.LogbookRepository
import it.danieleverducci.lunatracker.repository.LogbookSavedListener
import it.danieleverducci.lunatracker.repository.WebDAVLogbookRepository
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity"
        val SHARED_PREFS_FILE_NAME = "lunasettings"
        val SHARED_PREFS_BB_CONTENT = "bbcontent"
    }

    lateinit var logbook: Logbook
    lateinit var adapter: LunaEventRecyclerAdapter
    lateinit var progressIndicator: LinearProgressIndicator
    lateinit var recyclerView: RecyclerView
    lateinit var handler: Handler
    val updateListRunnable: Runnable = Runnable {
        loadLogbook()
        handler.postDelayed(updateListRunnable, 1000*60)
    }
    val logbookRepo: LogbookRepository = WebDAVLogbookRepository(   // TODO: support also FileLogbookRepository
        TemporaryHardcodedCredentials.URL,
        TemporaryHardcodedCredentials.USERNAME,
        TemporaryHardcodedCredentials.PASSWORD
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        loadLogbook()
        handler.postDelayed(updateListRunnable, 1000*30)
    }

    override fun onStop() {
        handler.removeCallbacks(updateListRunnable)

        super.onStop()
    }

    fun askBabyBottleContent() {
        // Show number picker dialog
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
        numberPicker.value = getSharedPreferences(SHARED_PREFS_FILE_NAME, MODE_PRIVATE).getInt(SHARED_PREFS_BB_CONTENT, 1)
        d.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            logEvent(LunaEvent(LunaEventType.BABY_BOTTLE, numberPicker.value * 10))
            getSharedPreferences(SHARED_PREFS_FILE_NAME, MODE_PRIVATE).edit().putInt(SHARED_PREFS_BB_CONTENT, numberPicker.value).commit()
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
        // Load data
        progressIndicator.visibility = View.VISIBLE
        logbookRepo.loadLogbook(this, object: LogbookLoadedListener{
            override fun onLogbookLoaded(lb: Logbook) {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    logbook = lb
                    showLogbook()
                })
            }

            override fun onError(error: String) {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    Log.e(TAG, "Unable to load logbook. Create a new one.")
                    logbook = Logbook()
                    showLogbook()
                })
            }
        })
    }

    fun logEvent(event: LunaEvent) {
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
                })
            }

            override fun onError(error: String) {
                Log.e(TAG, "ERROR: Logbook was NOT saved!")
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                })
            }

        })

        Toast.makeText(this, R.string.toast_event_added, Toast.LENGTH_SHORT).show()
    }

}
