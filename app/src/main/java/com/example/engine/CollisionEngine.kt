package com.example.engine

import com.example.model.Arrow
import com.example.model.Point
import com.example.model.PuzzleBoard

object CollisionEngine {

    /**
     * Computes the exit corridor points extending from the arrow's head in its direction
     * until reaching outside the board boundaries.
     */
    fun getExitCorridor(
        arrow: Arrow,
        boardWidth: Int,
        boardHeight: Int
    ): List<Point> {
        val corridor = mutableListOf<Point>()
        var curr = arrow.head + arrow.direction
        while (curr.x in 0 until boardWidth && curr.y in 0 until boardHeight) {
            corridor.add(curr)
            curr += arrow.direction
        }
        return corridor
    }

    /**
     * Checks if the specified arrow has a clear escape corridor without colliding with
     * any other active arrows on the board.
     */
    fun canArrowEscape(
        arrow: Arrow,
        activeArrows: Collection<Arrow>,
        boardWidth: Int,
        boardHeight: Int
    ): Boolean {
        val otherArrows = activeArrows.filter { it.id != arrow.id }
        if (otherArrows.isEmpty()) return true

        // Build set of occupied points by other active arrows
        val occupiedByOthers = HashSet<Point>()
        for (other in otherArrows) {
            occupiedByOthers.addAll(other.occupiedPoints)
        }

        val corridor = getExitCorridor(arrow, boardWidth, boardHeight)
        for (point in corridor) {
            if (point in occupiedByOthers) {
                return false
            }
        }
        return true
    }

    /**
     * Returns the list of other active arrows that directly block this arrow's exit corridor.
     */
    fun getBlockingArrows(
        arrow: Arrow,
        activeArrows: Collection<Arrow>,
        boardWidth: Int,
        boardHeight: Int
    ): List<Arrow> {
        val otherArrows = activeArrows.filter { it.id != arrow.id }
        if (otherArrows.isEmpty()) return emptyList()

        val corridor = getExitCorridor(arrow, boardWidth, boardHeight).toSet()
        return otherArrows.filter { other ->
            other.occupiedPoints.any { it in corridor }
        }
    }

    /**
     * Returns all arrows among activeArrows that currently have a clear escape corridor.
     */
    fun findEscapableArrows(
        activeArrows: Collection<Arrow>,
        boardWidth: Int,
        boardHeight: Int
    ): List<Arrow> {
        val occupiedMap = HashMap<Point, Arrow>()
        for (a in activeArrows) {
            for (p in a.occupiedPoints) {
                occupiedMap[p] = a
            }
        }

        val escapable = mutableListOf<Arrow>()
        for (arrow in activeArrows) {
            val corridor = getExitCorridor(arrow, boardWidth, boardHeight)
            val isBlocked = corridor.any { pt ->
                val blocker = occupiedMap[pt]
                blocker != null && blocker.id != arrow.id
            }
            if (!isBlocked) {
                escapable.add(arrow)
            }
        }
        return escapable
    }

    /**
     * Simulates the puzzle board to verify if it can be solved by sequentially
     * removing escapable arrows until all are cleared.
     * Returns true if a complete valid solving sequence exists.
     */
    fun isSolvable(board: PuzzleBoard): Boolean {
        return solveSequence(board) != null
    }

    /**
     * Solves the puzzle by iterative reduction.
     * Returns the sequence of arrow IDs to solve, or null if a deadlock/unsolvable state is reached.
     */
    fun solveSequence(board: PuzzleBoard): List<String>? {
        val remaining = board.arrows.associateBy { it.id }.toMutableMap()
        val solution = mutableListOf<String>()

        while (remaining.isNotEmpty()) {
            val escapable = findEscapableArrows(remaining.values, board.width, board.height)
            if (escapable.isEmpty()) {
                // Deadlock reached!
                return null
            }

            // Pick the first escapable arrow (greedy choice is sound for 1D corridor elimination)
            // In arrow mazes, clearing an arrow only frees up more space and never blocks another.
            // Hence monotonicity holds: if A can escape now, it can still escape later.
            val toRemove = escapable.first()
            remaining.remove(toRemove.id)
            solution.add(toRemove.id)
        }

        return solution
    }
}
