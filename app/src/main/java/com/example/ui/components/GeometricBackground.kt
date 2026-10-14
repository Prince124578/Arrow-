package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DeepNavy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GeometricBackground(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val animProgress by if (!reducedMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 24000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "bg_float"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Base deep navy fill
        drawRect(color = DeepNavy)

        // Floating geometric shapes
        val shapes = listOf(
            FloatingShape(0.12f, 0.18f, 24f, AccentTeal.copy(alpha = 0.08f), ShapeType.CHEVRON, 0.4f),
            FloatingShape(0.85f, 0.15f, 32f, AccentCoral.copy(alpha = 0.07f), ShapeType.TRIANGLE, -0.6f),
            FloatingShape(0.25f, 0.75f, 38f, AccentTeal.copy(alpha = 0.06f), ShapeType.HEXAGON, 0.3f),
            FloatingShape(0.82f, 0.82f, 28f, AccentCoral.copy(alpha = 0.08f), ShapeType.CHEVRON, -0.5f),
            FloatingShape(0.50f, 0.40f, 44f, Color.White.copy(alpha = 0.03f), ShapeType.RING, 0.2f),
            FloatingShape(0.15f, 0.50f, 20f, AccentTeal.copy(alpha = 0.05f), ShapeType.TRIANGLE, 0.7f),
            FloatingShape(0.78f, 0.48f, 26f, AccentCoral.copy(alpha = 0.06f), ShapeType.HEXAGON, -0.4f)
        )

        for (shape in shapes) {
            val floatOffset = if (!reducedMotion) {
                sin((animProgress * 2 * PI + shape.x * 10).toDouble()).toFloat() * 18f
            } else 0f

            val cx = size.width * shape.x
            val cy = size.height * shape.y + floatOffset
            val rot = if (!reducedMotion) animProgress * 360f * shape.rotSpeed else 0f

            drawFloatingShape(cx, cy, shape.size, shape.color, shape.type, rot)
        }
    }
}

private enum class ShapeType { CHEVRON, TRIANGLE, HEXAGON, RING }

private data class FloatingShape(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val type: ShapeType,
    val rotSpeed: Float
)

private fun DrawScope.drawFloatingShape(
    cx: Float,
    cy: Float,
    radius: Float,
    color: Color,
    type: ShapeType,
    rotDeg: Float
) {
    val rad = (rotDeg * PI / 180f).toFloat()
    when (type) {
        ShapeType.RING -> {
            drawCircle(color = color, radius = radius, center = Offset(cx, cy), style = Stroke(width = 2.5f))
        }
        ShapeType.TRIANGLE -> {
            val path = Path()
            for (i in 0 until 3) {
                val a = rad + i * (2 * PI / 3).toFloat()
                val px = cx + cos(a) * radius
                val py = cy + sin(a) * radius
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = color, style = Stroke(width = 2f))
        }
        ShapeType.HEXAGON -> {
            val path = Path()
            for (i in 0 until 6) {
                val a = rad + i * (2 * PI / 6).toFloat()
                val px = cx + cos(a) * radius
                val py = cy + sin(a) * radius
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path = path, color = color, style = Stroke(width = 2f))
        }
        ShapeType.CHEVRON -> {
            val path = Path()
            val cosA = cos(rad)
            val sinA = sin(rad)
            fun rotate(x: Float, y: Float): Offset {
                return Offset(cx + x * cosA - y * sinA, cy + x * sinA + y * cosA)
            }
            val p1 = rotate(-radius * 0.7f, -radius * 0.5f)
            val p2 = rotate(0f, 0f)
            val p3 = rotate(-radius * 0.7f, radius * 0.5f)
            path.moveTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
            path.lineTo(p3.x, p3.y)
            drawPath(path = path, color = color, style = Stroke(width = 3f))
        }
    }
}
