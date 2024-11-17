package it.danieleverducci.lunatracker

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import it.danieleverducci.lunatracker.repository.LocalSettingsRepository
import it.danieleverducci.lunatracker.repository.WebDAVLogbookRepository
import okio.IOException
import org.json.JSONException

open class SettingsActivity : AppCompatActivity() {
    protected lateinit var settingsRepository: LocalSettingsRepository
    protected lateinit var radioDataLocal: RadioButton
    protected lateinit var radioDataWebDAV: RadioButton
    protected lateinit var textViewWebDAVUrl: TextView
    protected lateinit var textViewWebDAVUser: TextView
    protected lateinit var textViewWebDAVPass: TextView
    protected lateinit var progressIndicator: LinearProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
        radioDataLocal = findViewById(R.id.settings_data_local)
        radioDataWebDAV = findViewById(R.id.settings_data_webdav)
        textViewWebDAVUrl = findViewById(R.id.settings_data_webdav_url)
        textViewWebDAVUser = findViewById(R.id.settings_data_webdav_user)
        textViewWebDAVPass = findViewById(R.id.settings_data_webdav_pass)
        progressIndicator = findViewById(R.id.progress_indicator)
        findViewById<View>(R.id.settings_save).setOnClickListener({
            validateAndSave()
        })
        findViewById<View>(R.id.settings_cancel).setOnClickListener({
            finish()
        })

        settingsRepository = LocalSettingsRepository(this)
        loadSettings()
    }

    fun loadSettings() {
        val dataRepo = settingsRepository.loadDataRepository()
        val webDavCredentials = settingsRepository.loadWebdavCredentials()

        when (dataRepo) {
            LocalSettingsRepository.DATA_REPO.LOCAL_FILE -> radioDataLocal.isChecked = true
            LocalSettingsRepository.DATA_REPO.WEBDAV -> radioDataWebDAV.isChecked = true
        }
        if (webDavCredentials != null) {
            textViewWebDAVUrl.setText(webDavCredentials[0])
            textViewWebDAVUser.setText(webDavCredentials[1])
            textViewWebDAVPass.setText(webDavCredentials[2])
        }
    }

    fun validateAndSave() {
        if (radioDataLocal.isChecked) {
            // No validation required, just save
            saveSettings()
            return
        }

        // Try to connect to WebDAV and check if the save file already exists
        val webDAVLogbookRepo = WebDAVLogbookRepository(
            textViewWebDAVUrl.text.toString(),
            textViewWebDAVUser.text.toString(),
            textViewWebDAVPass.text.toString()
        )
        progressIndicator.visibility = View.VISIBLE
        webDAVLogbookRepo.createLogbook(this, object: WebDAVLogbookRepository.LogbookCreatedListener{
            override fun onLogbookCreated() {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    saveSettings()
                    Toast.makeText(this@SettingsActivity, R.string.settings_webdav_creation_ok, Toast.LENGTH_SHORT).show()
                })
            }

            override fun onIOError(error: IOException) {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    Toast.makeText(this@SettingsActivity, getString(R.string.settings_network_error) + error.toString(), Toast.LENGTH_SHORT).show()
                })
            }

            override fun onWebDAVError(error: SardineException) {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    if(error.toString().contains("401")) {
                        Toast.makeText(this@SettingsActivity, getString(R.string.settings_webdav_error_denied), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SettingsActivity, getString(R.string.settings_webdav_error_generic) + error.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onJSONError(error: JSONException) {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    Toast.makeText(this@SettingsActivity, getString(R.string.settings_json_error) + error.toString(), Toast.LENGTH_SHORT).show()
                })
            }

            override fun onError(error: Exception) {
                runOnUiThread({
                    progressIndicator.visibility = View.INVISIBLE
                    Toast.makeText(this@SettingsActivity, getString(R.string.settings_generic_error) + error.toString(), Toast.LENGTH_SHORT).show()
                })
            }

        })
    }

    fun saveSettings() {
        settingsRepository.saveDataRepository(
            if (radioDataWebDAV.isChecked) LocalSettingsRepository.DATA_REPO.WEBDAV
            else LocalSettingsRepository.DATA_REPO.LOCAL_FILE
        )
        settingsRepository.saveWebdavCredentials(
            textViewWebDAVUrl.text.toString(),
            textViewWebDAVUser.text.toString(),
            textViewWebDAVPass.text.toString()
        )
        finish()
    }

}