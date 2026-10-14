package com.example.model

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Represents an arrow on the puzzle board.
 * @param id Unique identifier for the arrow.
 * @param vertices Ordered list of vertices defining the arrow path from tail to head.
 *                 Adjacent vertices must share either the same x or same y (manhattan segments).
 * @param colorIndex Palette index for arrow coloring.
 */
data class Arrow(
    val id: String,
    val vertices: List<Point>,
    val colorIndex: Int = 0
) {
    init {
        require(vertices.size >= 2) { "Arrow must have at least 2 vertices (tail and head)" }
    }

    val tail: Point get() = vertices.first()
    val head: Point get() = vertices.last()

    /**
     * Direction the arrow head is pointing, derived from the last segment.
     */
    val direction: Direction by lazy {
        val last = vertices.last()
        val prev = vertices[vertices.size - 2]
        Direction.fromDelta(last.x - prev.x, last.y - prev.y)
    }

    /**
     * Set of all integer grid points occupied by this arrow's body and head.
     */
    val occupiedPoints: Set<Point> by lazy {
        val set = mutableSetOf<Point>()
        for (i in 0 until vertices.size - 1) {
            val p1 = vertices[i]
            val p2 = vertices[i + 1]
            if (p1.x == p2.x) {
                val startY = min(p1.y, p2.y)
                val endY = max(p1.y, p2.y)
                for (y in startY..endY) {
                    set.add(Point(p1.x, y))
                }
            } else if (p1.y == p2.y) {
                val startX = min(p1.x, p2.x)
                val endX = max(p1.x, p2.x)
                for (x in startX..endX) {
                    set.add(Point(x, p1.y))
                }
            }
        }
        set
    }

    /**
     * Calculates distance in grid units from a continuous (float) point to the nearest line segment of this arrow.
     */
    fun distanceTo(px: Float, py: Float): Float {
        var minDistance = Float.MAX_VALUE
        for (i in 0 until vertices.size - 1) {
            val p1 = vertices[i]
            val p2 = vertices[i + 1]
            val dist = pointToSegmentDistance(px, py, p1.x.toFloat(), p1.y.toFloat(), p2.x.toFloat(), p2.y.toFloat())
            if (dist < minDistance) {
                minDistance = dist
            }
        }
        return minDistance
    }

    private fun pointToSegmentDistance(
        px: Float, py: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        if (dx == 0f && dy == 0f) {
            return hypot(px - x1, py - y1)
        }
        val t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)
        val clampedT = t.coerceIn(0f, 1f)
        val nearestX = x1 + clampedT * dx
        val nearestY = y1 + clampedT * dy
        return hypot(px - nearestX, py - nearestY)
    }
}
