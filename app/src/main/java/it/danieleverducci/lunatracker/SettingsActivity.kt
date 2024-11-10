package it.danieleverducci.lunatracker

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.settings_save).setOnClickListener({
            Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show()
        })
        findViewById<View>(R.id.settings_cancel).setOnClickListener({
            finish()
        })
    }

}