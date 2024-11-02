package it.danieleverducci.lunatracker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import it.danieleverducci.lunatracker.ui.theme.LunaTrackerTheme

class MainActivity : AppCompatActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.button_bottle).setOnClickListener { askBabyBottleContent() }
        findViewById<View>(R.id.button_nipple_left).setOnClickListener { logEvent(LunaEvent(LunaEventType.BREASTFEEDING_LEFT_NIPPLE)) }
        findViewById<View>(R.id.button_nipple_both).setOnClickListener { logEvent(LunaEvent(LunaEventType.BREASTFEEDING_BOTH_NIPPLE)) }
        findViewById<View>(R.id.button_nipple_right).setOnClickListener { logEvent(LunaEvent(LunaEventType.BREASTFEEDING_RIGHT_NIPPLE)) }
        findViewById<View>(R.id.button_change_poo).setOnClickListener { logEvent(LunaEvent(LunaEventType.DIAPERCHANGE_POO)) }
        findViewById<View>(R.id.button_change_pee).setOnClickListener { logEvent(LunaEvent(LunaEventType.DIAPERCHANGE_PEE)) }
    }

    fun askBabyBottleContent() {
        // Show number picker dialog
        val d = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.number_picker_dialog, null)
        d.setTitle(R.string.log_bottle_dialog_title)
        d.setMessage(R.string.log_bottle_dialog_description)
        d.setView(dialogView)
        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.dialog_number_picker)
        numberPicker.minValue = 10
        numberPicker.maxValue = 250
        numberPicker.displayedValues = ((numberPicker.minValue..numberPicker.maxValue step 10).map { it.toString() }.toTypedArray())
        numberPicker.wrapSelectorWheel = false
        d.setPositiveButton(android.R.string.ok) { dialogInterface, i ->
            logEvent(LunaEvent(LunaEventType.BABY_BOTTLE, numberPicker.value))
        }
        d.setNegativeButton(android.R.string.cancel) { dialogInterface, i -> dialogInterface.dismiss() }
        val alertDialog = d.create()
        alertDialog.show()
    }

    fun logEvent(event: LunaEvent) {
        Log.d(TAG, event.toString())
    }

}
