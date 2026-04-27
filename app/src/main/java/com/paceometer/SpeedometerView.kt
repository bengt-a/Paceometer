package com.paceometer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.*

class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val MAX_SPEED = 200f
        private const val START_ANGLE = 135f
        private const val TOTAL_SWEEP = 270f
    }

    enum class InnerRingMode { KMH, TIME_TO_DISTANCE, PACE_AND_TIME }

    // ─── Public state ─────────────────────────────────────────────────────────

    var showKmhScale = true
        set(v) { field = v; invalidate() }
    var showPaceScale = true
        set(v) { field = v; invalidate() }
    var showKmhCenter = true
        set(v) { field = v; invalidate() }
    var showPaceCenter = true
        set(v) { field = v; invalidate() }
    var innerRingMode = InnerRingMode.KMH
        set(v) { field = v; invalidate() }
    var remainingDistanceKm = 500f
        set(v) { field = v; invalidate() }
    var gpsStatus: String? = "Söker GPS…"
        set(v) { field = v; invalidate() }
    var speedColorArc = false
        set(v) { field = v; invalidate() }

    // ─── Animation ────────────────────────────────────────────────────────────

    private var displayedSpeed = 0f

    private val speedAnimator = ValueAnimator().apply {
        duration = 400
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            displayedSpeed = it.animatedValue as Float
            invalidate()
        }
    }

    fun setSpeed(kmh: Float) {
        val target = kmh.coerceIn(0f, MAX_SPEED)
        speedAnimator.cancel()
        speedAnimator.setFloatValues(displayedSpeed, target)
        speedAnimator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        speedAnimator.cancel()
    }

    // Tap anywhere on the dial to toggle inner ring mode between km/h and time-to-distance
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val dx = event.x - cx
            val dy = event.y - cy
            if (sqrt(dx * dx + dy * dy) <= radius) {
                innerRingMode = when (innerRingMode) {
                    InnerRingMode.KMH -> InnerRingMode.TIME_TO_DISTANCE
                    InnerRingMode.TIME_TO_DISTANCE -> InnerRingMode.PACE_AND_TIME
                    InnerRingMode.PACE_AND_TIME -> InnerRingMode.KMH
                }
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // ─── Geometry ─────────────────────────────────────────────────────────────

    private var cx = 0f
    private var cy = 0f
    private var radius = 0f
    private val arcRect = RectF()

    // ─── Paints ───────────────────────────────────────────────────────────────

    private val bgPaint             = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bezelPaint          = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bezelEdgePaint      = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackPaint          = Paint(Paint.ANTI_ALIAS_FLAG)
    private val activePaint         = Paint(Paint.ANTI_ALIAS_FLAG)
    private val minorTickPaint      = Paint(Paint.ANTI_ALIAS_FLAG)
    private val majorTickPaint      = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scaleLabelPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerPaceLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bigNumPaint         = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paceOnlyPaint       = Paint(Paint.ANTI_ALIAS_FLAG)
    private val unitPaint           = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paceSmallPaint      = Paint(Paint.ANTI_ALIAS_FLAG)
    private val modePaint           = Paint(Paint.ANTI_ALIAS_FLAG)
    private val needleGlowPaint     = Paint(Paint.ANTI_ALIAS_FLAG)
    private val needlePaint         = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hubPaint            = Paint(Paint.ANTI_ALIAS_FLAG)
    private val statusPaint         = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)  // required for BlurMaskFilter
        isClickable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
        radius = minOf(cx, cy) * 0.90f
        val ar = radius * 0.80f
        arcRect.set(cx - ar, cy - ar, cx + ar, cy + ar)
        setupPaints()
    }

    private fun setupPaints() {
        val arcStroke = radius * 0.065f

        bgPaint.apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#0A0A14")
        }
        bezelPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.022f
            strokeCap = Paint.Cap.BUTT
        }
        bezelEdgePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.003f
            color = Color.parseColor("#0A0A18")
        }
        trackPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = arcStroke
            color = Color.parseColor("#14142A")
            strokeCap = Paint.Cap.ROUND
        }
        activePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = arcStroke
            strokeCap = Paint.Cap.ROUND
        }
        minorTickPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.007f
            color = Color.parseColor("#2A2A44")
            strokeCap = Paint.Cap.ROUND
        }
        majorTickPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.013f
            color = Color.parseColor("#606078")
            strokeCap = Paint.Cap.ROUND
        }
        scaleLabelPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.092f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        outerPaceLabelPaint.apply {
            color = Color.parseColor("#4DD0E1")
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.100f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        bigNumPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        paceOnlyPaint.apply {
            color = Color.parseColor("#00BCD4")
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.30f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        unitPaint.apply {
            color = Color.parseColor("#55556A")
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.10f
        }
        paceSmallPaint.apply {
            color = Color.parseColor("#00BCD4")
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.130f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        modePaint.apply {
            color = Color.parseColor("#7878A8")
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.07f
        }
        needleGlowPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.055f
            color = Color.parseColor("#44F44336")   // red glow
            strokeCap = Paint.Cap.ROUND
            maskFilter = BlurMaskFilter(radius * 0.04f, BlurMaskFilter.Blur.NORMAL)
        }
        needlePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.017f
            color = Color.parseColor("#F44336")      // red needle
            strokeCap = Paint.Cap.ROUND
        }
        hubPaint.apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        statusPaint.apply {
            color = Color.parseColor("#44445A")
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.085f
        }
    }

    // ─── Drawing ──────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (radius == 0f) return

        drawBackground(canvas)
        drawTrack(canvas)
        drawActiveArc(canvas)
        drawTicks(canvas)
        drawScaleLabels(canvas)
        drawNeedle(canvas)
        drawCenterValues(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawCircle(cx, cy, radius, bgPaint)
        drawBezel(canvas)
    }

    private fun drawBezel(canvas: Canvas) {
        val bezelR = radius * 0.975f
        val halfW  = radius * 0.011f
        bezelPaint.shader = SweepGradient(
            cx, cy,
            intArrayOf(
                Color.parseColor("#2A2A42"),
                Color.parseColor("#1A1A28"),
                Color.parseColor("#303050"),
                Color.parseColor("#545470"),
                Color.parseColor("#D0D0EC"),
                Color.parseColor("#E8E8FF"),
                Color.parseColor("#2A2A42")
            ),
            floatArrayOf(0.0f, 0.15f, 0.25f, 0.50f, 0.72f, 0.86f, 1.0f)
        )
        canvas.drawCircle(cx, cy, bezelR, bezelPaint)
        canvas.drawCircle(cx, cy, bezelR + halfW, bezelEdgePaint)
        canvas.drawCircle(cx, cy, bezelR - halfW, bezelEdgePaint)
    }

    private fun drawTrack(canvas: Canvas) {
        canvas.drawArc(arcRect, START_ANGLE, TOTAL_SWEEP, false, trackPaint)
    }

    private fun drawActiveArc(canvas: Canvas) {
        val fraction = displayedSpeed / MAX_SPEED
        val sweepDeg = fraction * TOTAL_SWEEP
        if (sweepDeg < 0.5f) return

        // Gradient positions are fractions of 360°; active arc spans 270° = 0.75 of 360°
        activePaint.shader = if (speedColorArc) {
            SweepGradient(
                cx, cy,
                intArrayOf(
                    Color.parseColor("#1B5E20"),  // 0 km/h  — mörkt grön
                    Color.parseColor("#00C853"),  // ~80 km/h — ljusgrönt
                    Color.parseColor("#FFD600"),  // ~105 km/h — gult
                    Color.parseColor("#FF6D00"),  // ~130 km/h — orange
                    Color.parseColor("#FF1744"),  // ~160 km/h — röd
                    Color.parseColor("#7F0000"),  // 200 km/h  — mörkröd
                    Color.parseColor("#1B5E20")   // wrap-around
                ),
                floatArrayOf(0f, 0.30f, 0.394f, 0.4875f, 0.60f, 0.75f, 1.0f)
            )
        } else {
            SweepGradient(
                cx, cy,
                intArrayOf(
                    Color.parseColor("#1565C0"),
                    Color.parseColor("#1E88E5"),
                    Color.parseColor("#00BCD4"),
                    Color.parseColor("#1565C0")
                ),
                floatArrayOf(0f, 0.375f, 0.75f, 1f)
            )
        }
        canvas.save()
        canvas.rotate(START_ANGLE, cx, cy)
        canvas.drawArc(arcRect, 0f, sweepDeg, false, activePaint)
        canvas.restore()
    }

    private fun drawTicks(canvas: Canvas) {
        val outerR  = radius * 0.72f
        val majorIR = radius * 0.66f
        val minorIR = radius * 0.70f
        val numMinor = 40

        for (i in 0..numMinor) {
            val frac = i.toFloat() / numMinor
            val angleRad = toRad(START_ANGLE + frac * TOTAL_SWEEP)
            val cos = cos(angleRad).toFloat()
            val sin = sin(angleRad).toFloat()
            val isMajor = i % 4 == 0
            val inR   = if (isMajor) majorIR else minorIR
            val paint = if (isMajor) majorTickPaint else minorTickPaint
            canvas.drawLine(cx + cos * inR, cy + sin * inR,
                            cx + cos * outerR, cy + sin * outerR, paint)
        }
    }

    private fun drawScaleLabels(canvas: Canvas) {
        val kmhValues = listOf(0, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200)
        val innerR = radius * 0.57f
        val outerR = radius * 0.88f

        for (kmh in kmhValues) {
            val frac = kmh.toFloat() / MAX_SPEED
            val angleRad = toRad(START_ANGLE + frac * TOTAL_SWEEP)
            val cos = cos(angleRad).toFloat()
            val sin = sin(angleRad).toFloat()

            // Inner ring: km/h labels OR time-to-remaining-distance
            if (showKmhScale) {
                val lx = cx + cos * innerR
                val ly = cy + sin * innerR + scaleLabelPaint.textSize * 0.38f
                val label = when (innerRingMode) {
                    InnerRingMode.KMH -> kmh.toString()
                    InnerRingMode.TIME_TO_DISTANCE, InnerRingMode.PACE_AND_TIME ->
                        if (kmh == 0) "∞" else formatTimeToDistance(kmh, remainingDistanceKm)
                }
                canvas.drawText(label, lx, ly, scaleLabelPaint)
            }

            // Outer ring: pace (KMH mode) or km/h (TIME_TO_DISTANCE mode)
            val lxOuter = cx + cos * outerR
            val lyOuter = cy + sin * outerR
            when (innerRingMode) {
                InnerRingMode.KMH, InnerRingMode.PACE_AND_TIME -> {
                    if (showPaceScale && kmh > 0) {
                        val pace = 600f / kmh
                        val paceStr = if (pace == pace.toInt().toFloat()) pace.toInt().toString()
                                      else String.format("%.1f", pace)
                        canvas.drawText(paceStr, lxOuter,
                            lyOuter + outerPaceLabelPaint.textSize * 0.38f, outerPaceLabelPaint)
                    }
                }
                InnerRingMode.TIME_TO_DISTANCE -> {
                    canvas.drawText(kmh.toString(), lxOuter,
                        lyOuter + outerPaceLabelPaint.textSize * 0.38f, outerPaceLabelPaint)
                }
            }
        }
    }

    private fun drawNeedle(canvas: Canvas) {
        val frac = displayedSpeed / MAX_SPEED
        val angleRad = toRad(START_ANGLE + frac * TOTAL_SWEEP)
        val cos = cos(angleRad).toFloat()
        val sin = sin(angleRad).toFloat()
        val tipR  = radius * 0.68f
        val tailR = radius * 0.12f

        canvas.drawLine(cx - cos * tailR, cy - sin * tailR,
                        cx + cos * tipR,  cy + sin * tipR, needleGlowPaint)
        canvas.drawLine(cx - cos * tailR, cy - sin * tailR,
                        cx + cos * tipR,  cy + sin * tipR, needlePaint)
        canvas.drawCircle(cx, cy, radius * 0.040f, hubPaint)
    }

    // ─── Center display ───────────────────────────────────────────────────────

    private fun drawCenterValues(canvas: Canvas) {
        val hasKmh  = showKmhCenter
        val hasPace = showPaceCenter

        if (!hasKmh && !hasPace) {
            gpsStatus?.let { canvas.drawText(it, cx, cy + statusPaint.textSize * 0.5f, statusPaint) }
            return
        }

        when {
            hasKmh && hasPace -> drawBothCenter(canvas)
            hasKmh            -> drawKmhCenter(canvas)
            hasPace           -> drawPaceCenter(canvas)
        }

        val gps = gpsStatus
        if (gps != null && displayedSpeed < 0.5f) {
            canvas.drawText(gps, cx, cy + radius * 0.34f, statusPaint)
        }

        // Mode indicator: shows current inner ring mode
        val modeLabel = when (innerRingMode) {
            InnerRingMode.KMH -> "↕ km/h"
            InnerRingMode.TIME_TO_DISTANCE -> context.getString(R.string.mode_time_left)
            InnerRingMode.PACE_AND_TIME -> context.getString(R.string.mode_pace_time)
        }
        canvas.drawText(modeLabel, cx, cy + radius * 0.47f, modePaint)
    }

    private fun drawBothCenter(canvas: Canvas) {
        val bigH   = bigNumPaint.textSize * 0.72f
        val unitH  = unitPaint.textSize * 1.15f
        val gapH   = radius * 0.14f
        val pace1H = paceSmallPaint.textSize * 1.10f
        val pace2H = paceSmallPaint.textSize * 0.90f
        val totalH = bigH + unitH + gapH + pace1H + pace2H
        var y = cy - radius * 0.12f - totalH / 2f

        y += bigH
        canvas.drawText(String.format("%.0f", displayedSpeed), cx, y, bigNumPaint)
        y += unitH
        canvas.drawText("km/h", cx, y, unitPaint)
        y += gapH + pace1H
        canvas.drawText(paceLine1(displayedSpeed), cx, y, paceSmallPaint)
        y += pace2H
        canvas.drawText("/ 10 km", cx, y, paceSmallPaint)
    }

    private fun drawKmhCenter(canvas: Canvas) {
        val bigH  = bigNumPaint.textSize * 0.72f
        val unitH = unitPaint.textSize * 1.20f
        val totalH = bigH + unitH
        var y = cy - radius * 0.05f - totalH / 2f

        y += bigH
        canvas.drawText(String.format("%.0f", displayedSpeed), cx, y, bigNumPaint)
        y += unitH
        canvas.drawText("km/h", cx, y, unitPaint)
    }

    private fun drawPaceCenter(canvas: Canvas) {
        val bigH  = paceOnlyPaint.textSize * 0.72f
        val unitH = unitPaint.textSize * 1.20f
        val totalH = bigH + unitH
        var y = cy - radius * 0.05f - totalH / 2f

        y += bigH
        canvas.drawText(paceCenterValue(displayedSpeed), cx, y, paceOnlyPaint)
        y += unitH
        canvas.drawText("min/10km", cx, y, unitPaint)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun paceLine1(kmh: Float): String {
        if (kmh < 0.5f) return "∞ min"
        return String.format("%.1f min", 600f / kmh)
    }

    private fun paceCenterValue(kmh: Float): String {
        if (kmh < 0.5f) return "∞"
        val pace = 600f / kmh
        return if (pace >= 100f) "∞" else String.format("%.1f", pace)
    }

    private fun formatTimeToDistance(kmh: Int, remainingKm: Float): String {
        if (remainingKm <= 0f) return "—"
        val totalMin = (remainingKm / kmh * 60f).roundToInt()
        return when {
            totalMin <= 0 -> "—"
            totalMin < 60 -> "${totalMin}m"
            else -> {
                val h = totalMin / 60
                val m = totalMin % 60
                if (m == 0) "${h}h" else "${h}h${m.toString().padStart(2, '0')}"
            }
        }
    }

    private fun toRad(deg: Float) = Math.toRadians(deg.toDouble())
}
