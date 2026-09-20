package com.brizzbi.android_screen_time_lens.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brizzbi.android_screen_time_lens.data.AppUsageInfo
import com.brizzbi.android_screen_time_lens.ui.theme.chartColors
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val LABEL_MIN_SWEEP_DEGREES = 16f
private const val TOOLTIP_VISIBLE_MS = 2600L

@Composable
fun DonutChart(
    apps: List<AppUsageInfo>,
    totalLabel: String,
    totalTime: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 28.dp,
    gapDegrees: Float = 3f
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(apps) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val totalMs = apps.sumOf { it.totalTimeMs }.coerceAtLeast(1)
    val fractions = remember(apps) { apps.map { it.totalTimeMs.toFloat() / totalMs } }

    var selectedIndex by remember(apps) { mutableIntStateOf(-1) }
    var tapPosition by remember(apps) { mutableStateOf(Offset.Zero) }
    var tooltipSize by remember { mutableStateOf(IntSize.Zero) }
    var chartSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            delay(TOOLTIP_VISIBLE_MS)
            selectedIndex = -1
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .onSizeChanged { chartSize = it }
    ) {

        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(fractions, strokeWidth, gapDegrees) {
                    detectTapGestures { tap ->
                        selectedIndex = sliceIndexAt(
                            tap = tap,
                            canvasSize = Size(this.size.width.toFloat(), this.size.height.toFloat()),
                            strokePx = strokeWidth.toPx(),
                            fractions = fractions,
                            gapDegrees = gapDegrees
                        )
                        tapPosition = tap
                    }
                }
        ) {
            val canvasSize = this.size
            val strokePx = strokeWidth.toPx()

            val arcOffset = strokePx / 2
            val arcSize = Size(
                width = canvasSize.width - strokePx,
                height = canvasSize.height - strokePx
            )
            val topLeft = Offset(arcOffset, arcOffset)

            val availableDegrees = 360f - gapDegrees * apps.size
            val ringRadius = (min(canvasSize.width, canvasSize.height) - strokePx) / 2f
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)

            var startAngle = -90f
            apps.forEachIndexed { index, app ->
                val fullSweep = fractions[index] * availableDegrees
                val sweepAngle = fullSweep * animationProgress.value
                val isSelected = index == selectedIndex

                drawArc(
                    color = chartColors.getOrElse(index) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = if (isSelected) strokePx * 1.18f else strokePx,
                        cap = StrokeCap.Round
                    )
                )

                if (animationProgress.value > 0.95f && fullSweep >= LABEL_MIN_SWEEP_DEGREES) {
                    val percent = (fractions[index] * 100f).roundToInt()
                    val layout = textMeasurer.measure(percent.toString() + "%", labelStyle)
                    val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                    val labelCenter = Offset(
                        x = center.x + (ringRadius * cos(midAngleRad)).toFloat(),
                        y = center.y + (ringRadius * sin(midAngleRad)).toFloat()
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            x = labelCenter.x - layout.size.width / 2f,
                            y = labelCenter.y - layout.size.height / 2f
                        )
                    )
                }

                startAngle += sweepAngle + gapDegrees
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = totalLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = totalTime,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Center
            )
        }

        val selectedApp = apps.getOrNull(selectedIndex)
        val tooltipPosition = tooltipOffset(tapPosition, tooltipSize, chartSize)

        AnimatedVisibility(
            visible = selectedApp != null,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(160)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { tooltipPosition }
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.onSizeChanged { tooltipSize = it }
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = selectedApp?.appName.orEmpty(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = tooltipSubtitle(selectedApp, totalMs),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.75f)
                        )
                    )
                }
            }
        }
    }
}

private fun tooltipSubtitle(app: AppUsageInfo?, totalMs: Long): String {
    if (app == null) return ""
    val percent = (app.totalTimeMs.toFloat() / totalMs * 100f).roundToInt()
    return app.totalTimeMs.toFormattedDuration() + " · " + percent + "%"
}

private fun tooltipOffset(tap: Offset, tooltip: IntSize, container: IntSize): IntOffset {
    if (tooltip.width == 0 || container.width == 0) {
        return IntOffset(tap.x.roundToInt(), tap.y.roundToInt())
    }

    val maxX = (container.width - tooltip.width).toFloat()
    val x = if (maxX <= 0f) maxX / 2f else (tap.x - tooltip.width / 2f).coerceIn(0f, maxX)

    val above = tap.y - tooltip.height - 16f
    val y = if (above >= 0f) above else tap.y + 16f

    return IntOffset(x.roundToInt(), y.roundToInt())
}

private fun sliceIndexAt(
    tap: Offset,
    canvasSize: Size,
    strokePx: Float,
    fractions: List<Float>,
    gapDegrees: Float
): Int {
    if (fractions.isEmpty()) return -1

    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val distance = hypot(tap.x - center.x, tap.y - center.y)
    val ringRadius = (min(canvasSize.width, canvasSize.height) - strokePx) / 2f
    if (abs(distance - ringRadius) > strokePx) return -1

    var angle = Math.toDegrees(
        atan2((tap.y - center.y).toDouble(), (tap.x - center.x).toDouble())
    ).toFloat()
    if (angle < 0f) angle += 360f

    val availableDegrees = 360f - gapDegrees * fractions.size
    var start = 270f

    fractions.forEachIndexed { index, fraction ->
        val sweep = fraction * availableDegrees
        val relative = (angle - start + 360f) % 360f
        if (relative <= sweep + gapDegrees) return index
        start = (start + sweep + gapDegrees) % 360f
    }

    return -1
}
