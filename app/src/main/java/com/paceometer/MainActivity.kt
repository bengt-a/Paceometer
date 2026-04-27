package com.paceometer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var speedometerView: SpeedometerView
    private lateinit var etaContainer: View
    private lateinit var etaText: TextView
    private lateinit var prefs: Prefs
    private lateinit var locationManager: LocationManager

    private var currentSpeedKmh = 0f
    private var lastLocationTimeMs = -1L

    private val locationListener = LocationListener { location ->
        onLocationUpdate(location)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            startLocationUpdates()
        } else {
            speedometerView.gpsStatus = "Platsåtkomst nekad"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        prefs = Prefs(this)
        speedometerView = findViewById(R.id.speedometerView)
        etaContainer    = findViewById(R.id.etaContainer)
        etaText         = findViewById(R.id.etaText)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val version = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.versionText).text =
            "v$version  •  Copyright \u00A9 2026 Bengt Alverborg"

        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Tap ETA card to change target distance (and reset driven counter)
        etaContainer.setOnClickListener {
            showDistanceDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        applySettings()
        checkPermissionAndStart()
    }

    override fun onPause() {
        super.onPause()
        try { locationManager.removeUpdates(locationListener) } catch (_: Exception) {}
    }

    private fun applySettings() {
        speedometerView.showKmhScale   = prefs.showKmh
        speedometerView.showKmhCenter  = prefs.showKmh
        speedometerView.showPaceScale  = prefs.showPace
        speedometerView.showPaceCenter = prefs.showPace
        speedometerView.speedColorArc  = prefs.speedColorArc
        etaContainer.visibility = if (prefs.showEta) View.VISIBLE else View.GONE
        speedometerView.remainingDistanceKm = remainingKm()
        updateEtaDisplay()
    }

    private fun showDistanceDialog() {
        val editText = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(prefs.etaDistanceKm.toString())
            selectAll()
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("Välj målsträcka (km)")
            .setMessage("Räknaren nollställs vid ny sträcka.")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val km = editText.text.toString().toIntOrNull()
                if (km != null && km > 0) {
                    prefs.etaDistanceKm = km
                    prefs.drivenKm = 0f
                    lastLocationTimeMs = -1L
                    speedometerView.remainingDistanceKm = remainingKm()
                    updateEtaDisplay()
                }
            }
            .setNegativeButton("Avbryt", null)
            .show()
        editText.postDelayed({
            editText.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun remainingKm(): Float = max(0f, prefs.etaDistanceKm.toFloat() - prefs.drivenKm)

    private fun checkPermissionAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> startLocationUpdates()

            else -> requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        speedometerView.gpsStatus = "Söker GPS…"
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500L,   // min interval ms
                0f,     // min distance m
                locationListener
            )
        } catch (e: Exception) {
            speedometerView.gpsStatus = "GPS ej tillgänglig"
        }
    }

    private fun onLocationUpdate(location: Location) {
        currentSpeedKmh = if (location.hasSpeed() && location.speed >= 0f) {
            location.speed * 3.6f
        } else {
            0f
        }

        // Track driven distance by integrating speed × time
        if (lastLocationTimeMs >= 0) {
            val dtSeconds = (location.time - lastLocationTimeMs) / 1000f
            if (dtSeconds > 0f && dtSeconds < 30f && location.speed > 0f) {
                val drivenM = location.speed * dtSeconds
                prefs.drivenKm = prefs.drivenKm + drivenM / 1000f
            }
        }
        lastLocationTimeMs = location.time

        speedometerView.gpsStatus = null
        speedometerView.setSpeed(currentSpeedKmh)
        speedometerView.remainingDistanceKm = remainingKm()
        updateEtaDisplay()
    }

    private fun updateEtaDisplay() {
        if (!prefs.showEta) return
        val remaining = remainingKm()
        etaText.text = if (currentSpeedKmh < 1f) {
            "${remaining.toInt()} km kvar  →  ∞ min"
        } else {
            val totalMin = (remaining / currentSpeedKmh * 60).toInt()
            val h = totalMin / 60
            val m = totalMin % 60
            val time = if (h > 0) "${h}h ${m.toString().padStart(2, '0')} min" else "${m} min"
            "${remaining.toInt()} km kvar  →  $time"
        }
    }
}
