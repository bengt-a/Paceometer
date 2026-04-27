package com.paceometer

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

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

        // Load current settings
        switchKmh.isChecked        = prefs.showKmh
        switchPace.isChecked       = prefs.showPace
        switchEta.isChecked        = prefs.showEta
        switchSpeedColor.isChecked = prefs.speedColorArc
        etaDistEdit.setText(prefs.etaDistanceKm.toString())
        etaDistRow.visibility = if (prefs.showEta) View.VISIBLE else View.GONE

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
        // Also save when user taps away via IME
        etaDistEdit.setOnEditorActionListener { _, _, _ ->
            saveDistance(etaDistEdit)
            etaDistEdit.clearFocus()
            false
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
