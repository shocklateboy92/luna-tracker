package it.danieleverducci.lunatracker

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.danieleverducci.lunatracker.adapters.LunaEventRecyclerAdapter
import it.danieleverducci.lunatracker.entities.Logbook
import it.danieleverducci.lunatracker.entities.LunaEvent
import it.danieleverducci.lunatracker.entities.LunaEventType

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    lateinit var logbook: Logbook
    lateinit var adapter: LunaEventRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load data
        logbook = Logbook.load(this)

        // Show view
        setContentView(R.layout.activity_main)

        // Show logbook
        val recyclerView = findViewById<RecyclerView>(R.id.list_events)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        adapter = LunaEventRecyclerAdapter(this)
        adapter.items.addAll(logbook.logs)
        recyclerView.adapter = adapter

        // Set listeners
        findViewById<View>(R.id.button_bottle).setOnClickListener { askBabyBottleContent() }
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
        d.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            logEvent(LunaEvent(LunaEventType.BABY_BOTTLE, numberPicker.value * 10))
        }
        d.setNegativeButton(android.R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = d.create()
        alertDialog.show()
    }

    fun logEvent(event: LunaEvent) {
        adapter.items.add(0, event)
        adapter.notifyItemInserted(0)
        logbook.logs.add(0, event)
        logbook.save(this)
    }

}
