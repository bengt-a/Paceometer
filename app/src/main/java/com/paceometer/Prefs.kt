package com.paceometer

import android.content.Context

class Prefs(context: Context) {
    private val p = context.getSharedPreferences("paceometer", Context.MODE_PRIVATE)

    var showKmh: Boolean
        get() = p.getBoolean("show_kmh", true)
        set(v) = p.edit().putBoolean("show_kmh", v).apply()

    var showPace: Boolean
        get() = p.getBoolean("show_pace", true)
        set(v) = p.edit().putBoolean("show_pace", v).apply()

    var showEta: Boolean
        get() = p.getBoolean("show_eta", true)
        set(v) = p.edit().putBoolean("show_eta", v).apply()

    var etaDistanceKm: Int
        get() = p.getInt("eta_distance", 500)
        set(v) = p.edit().putInt("eta_distance", v).apply()

    var drivenKm: Float
        get() = p.getFloat("driven_km", 0f)
        set(v) = p.edit().putFloat("driven_km", v).apply()

    var speedColorArc: Boolean
        get() = p.getBoolean("speed_color_arc", false)
        set(v) = p.edit().putBoolean("speed_color_arc", v).apply()
}
