package com.example.model

/**
 * Represents a complete puzzle board.
 */
data class PuzzleBoard(
    val levelNumber: Int,
    val title: String,
    val width: Int,
    val height: Int,
    val arrows: List<Arrow>,
    val targetTimeSeconds: Int = 60
)
