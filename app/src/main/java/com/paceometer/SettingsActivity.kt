package com.paceometer

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.LocaleListCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings)
        }

        prefs = Prefs(this)

        val switchKmh        = findViewById<SwitchCompat>(R.id.switchKmh)
        val switchPace       = findViewById<SwitchCompat>(R.id.switchPace)
        val switchEta        = findViewById<SwitchCompat>(R.id.switchEta)
        val switchSpeedColor = findViewById<SwitchCompat>(R.id.switchSpeedColor)
        val etaDistRow       = findViewById<View>(R.id.etaDistanceRow)
        val etaDistEdit      = findViewById<EditText>(R.id.etaDistanceEdit)
        val languageGroup    = findViewById<RadioGroup>(R.id.languageGroup)
        val aboutVersion     = findViewById<TextView>(R.id.aboutVersion)

        // Load current settings
        switchKmh.isChecked        = prefs.showKmh
        switchPace.isChecked       = prefs.showPace
        switchEta.isChecked        = prefs.showEta
        switchSpeedColor.isChecked = prefs.speedColorArc
        etaDistEdit.setText(prefs.etaDistanceKm.toString())
        etaDistRow.visibility = if (prefs.showEta) View.VISIBLE else View.GONE

        // About: show version
        val version = packageManager.getPackageInfo(packageName, 0).versionName
        aboutVersion.text = "Paceometer v$version"

        // Language: pre-select current locale
        val currentLang = AppCompatDelegate.getApplicationLocales().let {
            if (it.isEmpty) "en" else it[0]!!.language
        }
        languageGroup.check(when (currentLang) {
            "sv" -> R.id.radioSwedish
            "et" -> R.id.radioEstonian
            else -> R.id.radioEnglish
        })

        // Listeners
        switchKmh.setOnCheckedChangeListener { _, checked ->
            prefs.showKmh = checked
        }
        switchPace.setOnCheckedChangeListener { _, checked ->
            prefs.showPace = checked
        }
        switchEta.setOnCheckedChangeListener { _, checked ->
            prefs.showEta = checked
            etaDistRow.visibility = if (checked) View.VISIBLE else View.GONE
        }
        switchSpeedColor.setOnCheckedChangeListener { _, checked ->
            prefs.speedColorArc = checked
        }
        etaDistEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) saveDistance(etaDistEdit)
        }
        etaDistEdit.setOnEditorActionListener { _, _, _ ->
            saveDistance(etaDistEdit)
            etaDistEdit.clearFocus()
            false
        }

        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val tag = when (checkedId) {
                R.id.radioSwedish  -> "sv"
                R.id.radioEstonian -> "et"
                else               -> "en"
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }

    private fun saveDistance(edit: EditText) {
        val km = edit.text.toString().toIntOrNull()
        if (km != null && km > 0) {
            prefs.etaDistanceKm = km
        } else {
            edit.setText(prefs.etaDistanceKm.toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
