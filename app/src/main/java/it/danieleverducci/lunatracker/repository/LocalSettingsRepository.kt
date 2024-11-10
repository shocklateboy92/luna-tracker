package it.danieleverducci.lunatracker.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class LocalSettingsRepository(val context: Context) {
    companion object {
        val SHARED_PREFS_FILE_NAME = "lunasettings"
        val SHARED_PREFS_BB_CONTENT = "bbcontent"
        val SHARED_PREFS_DAV_URL = "webdav_url"
        val SHARED_PREFS_DAV_USER = "webdav_user"
        val SHARED_PREFS_DAV_PASS = "webdav_password"
    }
    val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS_FILE_NAME, MODE_PRIVATE)
    }

    fun saveBabyBottleContent(content: Int) {
        sharedPreferences.edit().putInt(SHARED_PREFS_BB_CONTENT, content).apply()
    }

    fun loadBabyBottleContent(): Int {
        return sharedPreferences.getInt(SHARED_PREFS_BB_CONTENT, 1)
    }

    fun saveWebdavCredentials(url: String, username: String, password: String) {
        val spe = sharedPreferences.edit()
        spe.putString(SHARED_PREFS_DAV_URL, url)
        spe.putString(SHARED_PREFS_DAV_USER, username)
        spe.putString(SHARED_PREFS_DAV_PASS, password)
        spe.commit()
    }

    fun loadWebdavCredentials(): Array<String>? {
        val url = sharedPreferences.getString(SHARED_PREFS_DAV_URL, null)
        val user = sharedPreferences.getString(SHARED_PREFS_DAV_USER, null)
        val pass = sharedPreferences.getString(SHARED_PREFS_DAV_PASS, null)
        if (url == null || user == null || pass == null)
            return null
        return arrayOf(url, user, pass)
    }
}