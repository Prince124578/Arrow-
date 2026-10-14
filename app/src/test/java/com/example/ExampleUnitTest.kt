package com.example

import com.example.engine.CollisionEngine
import com.example.engine.PuzzleGenerator
import com.example.model.Arrow
import com.example.model.Direction
import com.example.model.Point
import com.example.model.PuzzleBoard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testArrowEscapabilityAndCollision() {
        // Arrow A pointing RIGHT from (1, 2) to (3, 2). Corridor: (4, 2) to (5, 2) on 6x6 board.
        val arrowA = Arrow("A", listOf(Point(1, 2), Point(3, 2)), 0)
        // Arrow B blocking corridor at (4, 2)
        val arrowB = Arrow("B", listOf(Point(4, 1), Point(4, 3)), 1)

        val board = PuzzleBoard(1, "Test", 6, 6, listOf(arrowA, arrowB))

        // When B is present, A is blocked
        assertFalse(CollisionEngine.canArrowEscape(arrowA, board.arrows, board.width, board.height))
        val blockers = CollisionEngine.getBlockingArrows(arrowA, board.arrows, board.width, board.height)
        assertEquals(1, blockers.size)
        assertEquals("B", blockers.first().id)

        // Arrow B points DOWN from (4, 1) to (4, 3). Corridor: (4, 4), (4, 5). Clear to edge!
        assertTrue(CollisionEngine.canArrowEscape(arrowB, board.arrows, board.width, board.height))

        // When B is removed, A can escape!
        assertTrue(CollisionEngine.canArrowEscape(arrowA, listOf(arrowA), board.width, board.height))
    }

    @Test
    fun testCuratedLevelsAreSolvable() {
        for (lvl in 1..12) {
            val board = PuzzleGenerator.getLevel(lvl)
            assertTrue("Level $lvl must have arrows", board.arrows.isNotEmpty())
            val solution = CollisionEngine.solveSequence(board)
            assertNotNull("Level $lvl must be solvable", solution)
            assertEquals("Solution must clear all arrows", board.arrows.size, solution?.size)
        }
    }

    @Test
    fun testProceduralLevelsAreSolvable() {
        for (lvl in 13..20) {
            val board = PuzzleGenerator.getLevel(lvl)
            assertTrue(board.arrows.isNotEmpty())
            val solution = CollisionEngine.solveSequence(board)
            assertNotNull("Procedural Level $lvl must be solvable", solution)
        }
    }

    @Test
    fun testDailyPuzzleIsSolvable() {
        val dateKey = "20261014"
        val dailyBoard = PuzzleGenerator.getDailyPuzzle(dateKey)
        assertTrue(dailyBoard.arrows.isNotEmpty())
        val solution = CollisionEngine.solveSequence(dailyBoard)
        assertNotNull("Daily puzzle must be solvable", solution)
    }

    @Test
    fun testArrowDistanceToPoint() {
        // Horizontal segment from (1, 1) to (4, 1)
        val arrow = Arrow("test", listOf(Point(1, 1), Point(4, 1)))
        assertEquals(0f, arrow.distanceTo(2f, 1f), 0.001f)
        assertEquals(1f, arrow.distanceTo(2f, 2f), 0.001f)
    }
}
