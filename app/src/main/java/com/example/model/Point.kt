package com.example.model

data class Point(val x: Int, val y: Int) {
    operator fun plus(dir: Direction): Point = Point(x + dir.dx, y + dir.dy)
    operator fun plus(other: Point): Point = Point(x + other.x, y + other.y)
    operator fun minus(other: Point): Point = Point(x - other.x, y - other.y)

    fun manhattanDistance(other: Point): Int =
        kotlin.math.abs(x - other.x) + kotlin.math.abs(y - other.y)
}
