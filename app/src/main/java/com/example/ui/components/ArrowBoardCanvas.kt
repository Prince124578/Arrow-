package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.Arrow
import com.example.model.Direction
import com.example.model.Point
import com.example.model.PuzzleBoard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.ArrowAmber
import com.example.ui.theme.ArrowCoral
import com.example.ui.theme.ArrowEmerald
import com.example.ui.theme.ArrowIvory
import com.example.ui.theme.ArrowTeal
import com.example.ui.theme.ArrowViolet
import com.example.ui.theme.BoardBackground
import com.example.ui.theme.NavyBorder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val ARROW_PALETTE = listOf(
    ArrowTeal,
    ArrowCoral,
    ArrowAmber,
    ArrowEmerald,
    ArrowViolet,
    ArrowIvory
)

@Composable
fun ArrowBoardCanvas(
    board: PuzzleBoard,
    activeArrows: List<Arrow>,
    animatingArrow: Arrow?,
    exitProgress: Float,
    blockedArrowId: String?,
    shakeProgress: Float,
    hintArrowId: String?,
    modifier: Modifier = Modifier,
    onArrowTapped: (Arrow) -> Unit
) {
    // Pulse animation for hint
    val hintGlow = remember { Animatable(0.4f) }
    LaunchedEffect(hintArrowId) {
        if (hintArrowId != null) {
            hintGlow.animateTo(
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            hintGlow.snapTo(0.4f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(board.width.toFloat() / board.height.toFloat())
            .padding(12.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = AccentTeal.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(24.dp))
            .background(BoardBackground),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(board.levelNumber, activeArrows.size) {
                    detectTapGestures { tapOffset ->
                        val cellW = size.width / board.width
                        val cellH = size.height / board.height
                        val gridX = (tapOffset.x - cellW * 0.5f) / cellW
                        val gridY = (tapOffset.y - cellH * 0.5f) / cellH

                        // Find closest active arrow within tap threshold
                        var closestArrow: Arrow? = null
                        var minDistance = Float.MAX_VALUE
                        for (arrow in activeArrows) {
                            val dist = arrow.distanceTo(gridX, gridY)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestArrow = arrow
                            }
                        }

                        // Tolerance of ~0.7 grid cell
                        if (closestArrow != null && minDistance <= 0.75f) {
                            onArrowTapped(closestArrow)
                        }
                    }
                }
        ) {
            val cellW = size.width / board.width
            val cellH = size.height / board.height
            val strokeWidth = (cellW.coerceAtMost(cellH) * 0.32f).coerceIn(10f, 26f)
            val headSize = strokeWidth * 2.1f

            // Draw subtle background grid dots/guide lines
            drawBoardGrid(board.width, board.height, cellW, cellH)

            // Render all active arrows
            for (arrow in activeArrows) {
                val isHint = (arrow.id == hintArrowId)
                val isBlocked = (arrow.id == blockedArrowId)
                val baseColor = ARROW_PALETTE[arrow.colorIndex % ARROW_PALETTE.size]

                var offsetX = 0f
                var offsetY = 0f

                // Blocked shake effect
                if (isBlocked && shakeProgress > 0f) {
                    val dampening = 1f - shakeProgress
                    val frequency = 4.0 * PI
                    val displacement = sin(shakeProgress * frequency).toFloat() * 18f * dampening
                    if (arrow.direction == Direction.UP || arrow.direction == Direction.DOWN) {
                        offsetX = displacement
                    } else {
                        offsetY = displacement
                    }
                }

                // Draw Hint Glow if active
                if (isHint) {
                    drawArrowGlow(
                        arrow = arrow,
                        cellW = cellW,
                        cellH = cellH,
                        strokeWidth = strokeWidth * 2.2f,
                        glowColor = AccentTeal.copy(alpha = 0.6f * hintGlow.value)
                    )
                }

                // Draw Arrow Body and Head
                val arrowColor = if (isBlocked) AccentCoral else baseColor
                drawArrow(
                    arrow = arrow,
                    cellW = cellW,
                    cellH = cellH,
                    strokeWidth = strokeWidth,
                    headSize = headSize,
                    color = arrowColor,
                    offsetX = offsetX,
                    offsetY = offsetY
                )
            }

            // Render currently escaping arrow
            if (animatingArrow != null) {
                val baseColor = ARROW_PALETTE[animatingArrow.colorIndex % ARROW_PALETTE.size]
                val exitDistance = (board.width + board.height) * cellW * exitProgress * 1.5f
                val offX = animatingArrow.direction.dx * exitDistance
                val offY = animatingArrow.direction.dy * exitDistance
                val alpha = (1f - exitProgress).coerceIn(0f, 1f)

                drawArrow(
                    arrow = animatingArrow,
                    cellW = cellW,
                    cellH = cellH,
                    strokeWidth = strokeWidth,
                    headSize = headSize,
                    color = baseColor.copy(alpha = alpha),
                    offsetX = offX,
                    offsetY = offY
                )
            }
        }
    }
}

private fun DrawScope.drawBoardGrid(
    width: Int,
    height: Int,
    cellW: Float,
    cellH: Float
) {
    val dotColor = Color(0x18CCD6F6)
    for (x in 0 until width) {
        for (y in 0 until height) {
            val cx = (x + 0.5f) * cellW
            val cy = (y + 0.5f) * cellH
            drawCircle(dotColor, radius = 2.5f, center = Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawArrowGlow(
    arrow: Arrow,
    cellW: Float,
    cellH: Float,
    strokeWidth: Float,
    glowColor: Color
) {
    val path = Path()
    arrow.vertices.forEachIndexed { index, pt ->
        val px = (pt.x + 0.5f) * cellW
        val py = (pt.y + 0.5f) * cellH
        if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }

    drawPath(
        path = path,
        color = glowColor,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawArrow(
    arrow: Arrow,
    cellW: Float,
    cellH: Float,
    strokeWidth: Float,
    headSize: Float,
    color: Color,
    offsetX: Float,
    offsetY: Float
) {
    // 1. Draw outer dark border for strong visual separation
    val darkOutline = Color(0xFF03070E)
    val path = Path()
    arrow.vertices.forEachIndexed { index, pt ->
        val px = (pt.x + 0.5f) * cellW + offsetX
        val py = (pt.y + 0.5f) * cellH + offsetY
        if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }

    // Outer line stroke
    drawPath(
        path = path,
        color = darkOutline,
        style = Stroke(width = strokeWidth + 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner vibrant line stroke
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Draw Arrow Head
    val headPt = arrow.head
    val hx = (headPt.x + 0.5f) * cellW + offsetX
    val hy = (headPt.y + 0.5f) * cellH + offsetY

    drawArrowHead(
        hx = hx,
        hy = hy,
        direction = arrow.direction,
        headSize = headSize,
        fillColor = color,
        outlineColor = darkOutline
    )
}

private fun DrawScope.drawArrowHead(
    hx: Float,
    hy: Float,
    direction: Direction,
    headSize: Float,
    fillColor: Color,
    outlineColor: Color
) {
    val path = Path()
    val halfBase = headSize * 0.65f

    when (direction) {
        Direction.UP -> {
            path.moveTo(hx, hy - headSize * 0.45f) // Tip
            path.lineTo(hx + halfBase, hy + headSize * 0.55f)
            path.lineTo(hx - halfBase, hy + headSize * 0.55f)
            path.close()
        }
        Direction.DOWN -> {
            path.moveTo(hx, hy + headSize * 0.45f) // Tip
            path.lineTo(hx + halfBase, hy - headSize * 0.55f)
            path.lineTo(hx - halfBase, hy - headSize * 0.55f)
            path.close()
        }
        Direction.RIGHT -> {
            path.moveTo(hx + headSize * 0.45f, hy) // Tip
            path.lineTo(hx - headSize * 0.55f, hy - halfBase)
            path.lineTo(hx - headSize * 0.55f, hy + halfBase)
            path.close()
        }
        Direction.LEFT -> {
            path.moveTo(hx - headSize * 0.45f, hy) // Tip
            path.lineTo(hx + headSize * 0.55f, hy - halfBase)
            path.lineTo(hx + headSize * 0.55f, hy + halfBase)
            path.close()
        }
    }

    // Outer border of arrow head
    drawPath(
        path = path,
        color = outlineColor,
        style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Fill of arrow head
    drawPath(path = path, color = fillColor)
}
