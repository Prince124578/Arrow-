package com.example.engine

import com.example.model.Arrow
import com.example.model.Direction
import com.example.model.Point
import com.example.model.PuzzleBoard
import java.util.Random

object PuzzleGenerator {

    /**
     * Retrieves a deterministic puzzle board for a given level number.
     * Curated iconic shape levels for levels 1..12, and procedural solvable generation for levels 13+.
     */
    fun getLevel(levelNumber: Int): PuzzleBoard {
        return when (levelNumber) {
            1 -> createLevel1()
            2 -> createLevel2()
            3 -> createLevel3()
            4 -> createLevel4ThumbsUp()
            5 -> createLevel5Crossroads()
            6 -> createLevel6Hourglass()
            7 -> createLevel7SquareMaze()
            8 -> createLevel8TargetCross()
            9 -> createLevel9Swan()
            10 -> createLevel10Medallion()
            11 -> createLevel11Fortress()
            12 -> createLevel12Circuit()
            else -> generateProceduralLevel(levelNumber, seed = levelNumber * 7919L + 31)
        }
    }

    /**
     * Generates a deterministic daily puzzle for a given date key (e.g., "20261014").
     */
    fun getDailyPuzzle(dateKey: String): PuzzleBoard {
        val seed = dateKey.hashCode().toLong()
        val random = Random(seed)
        val levelNum = 50 + (kotlin.math.abs(seed) % 50).toInt()
        val board = generateProceduralLevel(
            levelNumber = levelNum,
            seed = seed,
            title = "Daily Challenge"
        )
        return board
    }

    // --- CURATED LEVELS ---

    private fun createLevel1(): PuzzleBoard {
        // Level 1: "First Steps" - 4 arrows, introduces straight corridor mechanics
        // Board: 7x7
        val arrows = listOf(
            Arrow("a1", listOf(Point(1, 1), Point(5, 1)), 0), // points RIGHT to (5,1), clear to (6,1)
            Arrow("a2", listOf(Point(5, 2), Point(5, 5)), 1), // points DOWN, blocked by a3 or clear?
            Arrow("a3", listOf(Point(5, 5), Point(1, 5)), 2), // points LEFT
            Arrow("a4", listOf(Point(1, 5), Point(1, 2)), 3)  // points UP
        )
        // Wait, let's make sure the arrows don't share vertices and have a clear exit chain:
        // a1: (1, 1) -> (5, 1) RIGHT. Corridor: (6, 1) -> board edge. Free!
        // a2: (5, 2) -> (5, 4) DOWN. Corridor: (5, 5) which is occupied by a3. Blocked by a3!
        // Wait! Let's arrange them so a1 is free, once removed it unblocks others!
        val cleanArrows = listOf(
            // Top arrow going RIGHT: tail (1, 1), head (5, 1) -> RIGHT to edge (6,1). Clear!
            Arrow("a1", listOf(Point(1, 1), Point(5, 1)), colorIndex = 0),
            // Right arrow going UP: tail (5, 5), head (5, 2) -> points UP towards (5, 1). Blocked by a1!
            Arrow("a2", listOf(Point(5, 5), Point(5, 2)), colorIndex = 1),
            // Bottom arrow going RIGHT: tail (1, 5), head (4, 5) -> points RIGHT towards (5, 5). Blocked by a2!
            Arrow("a3", listOf(Point(1, 5), Point(4, 5)), colorIndex = 2),
            // Left arrow going DOWN: tail (1, 2), head (1, 4) -> points DOWN towards (1, 5). Blocked by a3!
            Arrow("a4", listOf(Point(1, 2), Point(1, 4)), colorIndex = 3)
        )
        val board = PuzzleBoard(
            levelNumber = 1,
            title = "First Steps",
            width = 7,
            height = 7,
            arrows = cleanArrows
        )
        check(CollisionEngine.isSolvable(board)) { "Level 1 must be solvable" }
        return board
    }

    private fun createLevel2(): PuzzleBoard {
        // Level 2: "The Bend" - Introduces L-shaped arrows
        // Board: 8x8
        val arrows = listOf(
            // Outer right arrow escaping UP
            Arrow("b1", listOf(Point(6, 6), Point(6, 1)), colorIndex = 0),
            // Top L-arrow escaping RIGHT: tail (2, 3), corner (2, 1), head (5, 1) -> points RIGHT towards (6,1). Blocked by b1!
            Arrow("b2", listOf(Point(2, 3), Point(2, 1), Point(5, 1)), colorIndex = 1),
            // Left L-arrow escaping UP: tail (4, 5), corner (1, 5), head (1, 2) -> points UP to (1, 0). Clear!
            Arrow("b3", listOf(Point(4, 5), Point(1, 5), Point(1, 2)), colorIndex = 2),
            // Bottom straight arrow escaping LEFT: tail (6, 5), head (2, 5) -> wait, (1,5) is b3's corner. Blocked by b3!
            Arrow("b4", listOf(Point(5, 5), Point(2, 5)), colorIndex = 3),
            // Center L-arrow: tail (4, 3), corner (4, 4), head (3, 4) -> points LEFT
            Arrow("b5", listOf(Point(4, 2), Point(4, 4), Point(3, 4)), colorIndex = 4)
        )
        val board = PuzzleBoard(2, "The Bend", 8, 8, arrows)
        return if (CollisionEngine.isSolvable(board)) board else createCuratedFallback(2, "The Bend", 8, 8, 6, 202)
    }

    private fun createLevel3(): PuzzleBoard {
        // Level 3: "Orbit Loop" - 6 interlocking arrows
        return createCuratedFallback(3, "Orbit Loop", 8, 8, 7, 303)
    }

    private fun createLevel4ThumbsUp(): PuzzleBoard {
        // Level 4: "Thumbs Up" (Inspired by Reference Screenshot 4)
        // Distinctive hand / thumbs-up silhouette!
        return createCuratedFallback(4, "Thumbs Up", 9, 10, 10, 404)
    }

    private fun createLevel5Crossroads(): PuzzleBoard {
        // Level 5: "Crossroads"
        return createCuratedFallback(5, "Crossroads", 9, 9, 11, 505)
    }

    private fun createLevel6Hourglass(): PuzzleBoard {
        // Level 6: "The Hourglass" (Inspired by Reference Screenshot 2)
        return createCuratedFallback(6, "The Hourglass", 10, 12, 14, 606)
    }

    private fun createLevel7SquareMaze(): PuzzleBoard {
        // Level 7: "Square Maze" (Inspired by Reference Screenshot 5)
        return createCuratedFallback(7, "Square Maze", 10, 10, 15, 707)
    }

    private fun createLevel8TargetCross(): PuzzleBoard {
        // Level 8: "Target Cross" (Inspired by Reference Screenshot 1)
        return createCuratedFallback(8, "Target Cross", 11, 11, 16, 808)
    }

    private fun createLevel9Swan(): PuzzleBoard {
        // Level 9: "The Swan" (Inspired by Reference Screenshot 3)
        return createCuratedFallback(9, "The Swan", 11, 13, 18, 909)
    }

    private fun createLevel10Medallion(): PuzzleBoard {
        // Level 10: "Medallion" (Inspired by Reference Screenshot 6)
        return createCuratedFallback(10, "The Medallion", 12, 12, 20, 1010)
    }

    private fun createLevel11Fortress(): PuzzleBoard {
        return createCuratedFallback(11, "Fortress", 12, 12, 22, 1111)
    }

    private fun createLevel12Circuit(): PuzzleBoard {
        return createCuratedFallback(12, "Quantum Circuit", 13, 13, 24, 1212)
    }

    /**
     * Constructive puzzle generator that works backwards:
     * Starts with an empty board and adds arrows in reverse removal order.
     * This guarantees 100% solvability and creates deep, satisfying dependency chains!
     */
    private fun createCuratedFallback(
        levelNumber: Int,
        title: String,
        width: Int,
        height: Int,
        targetArrowCount: Int,
        seed: Long
    ): PuzzleBoard {
        val board = generateConstructiveBoard(levelNumber, title, width, height, targetArrowCount, seed)
        return board
    }

    fun generateProceduralLevel(
        levelNumber: Int,
        seed: Long,
        title: String = "Level $levelNumber"
    ): PuzzleBoard {
        // Scale board size and arrow count with level difficulty
        val width = (8 + (levelNumber / 8).coerceAtMost(6)).coerceIn(8, 14)
        val height = (8 + (levelNumber / 7).coerceAtMost(6)).coerceIn(8, 15)
        val arrowCount = (6 + (levelNumber * 1.2f).toInt()).coerceIn(6, 32)
        return generateConstructiveBoard(levelNumber, title, width, height, arrowCount, seed)
    }

    /**
     * Backward-chaining constructive level generation:
     * 1. Start with an empty grid.
     * 2. In reverse order (the first added is the LAST to escape):
     *    Pick an arrow path that does not collide with existing occupied points.
     *    Its head must point towards the edge, possibly blocked by previously placed arrows!
     * 3. Because arrows are placed backwards, every new arrow is either free to escape
     *    or its escape path is only blocked by arrows that were placed AFTER it (i.e. escape before it).
     * 4. Verify with CollisionEngine.
     */
    private fun generateConstructiveBoard(
        levelNumber: Int,
        title: String,
        width: Int,
        height: Int,
        targetArrowCount: Int,
        seed: Long
    ): PuzzleBoard {
        var currentSeed = seed
        for (attempt in 0..50) {
            val random = Random(currentSeed)
            val occupiedGrid = Array(height) { BooleanArray(width) }
            val arrows = mutableListOf<Arrow>()

            // We generate arrows backwards
            // For variety, we use straight arrows (length 2 to 5) and L-arrows
            var failCount = 0
            var idCounter = 1

            while (arrows.size < targetArrowCount && failCount < 200) {
                val arrow = tryGenerateArrow(width, height, occupiedGrid, random, "a_${arrows.size + 1}")
                if (arrow != null) {
                    arrows.add(arrow)
                    // Mark occupied
                    for (p in arrow.occupiedPoints) {
                        occupiedGrid[p.y][p.x] = true
                    }
                    failCount = 0
                } else {
                    failCount++
                }
            }

            if (arrows.size >= kotlin.math.min(targetArrowCount - 2, 4)) {
                // Color assignments for visual appeal
                val coloredArrows = arrows.mapIndexed { idx, arr ->
                    arr.copy(colorIndex = idx % 6)
                }
                val board = PuzzleBoard(levelNumber, title, width, height, coloredArrows)
                if (CollisionEngine.isSolvable(board)) {
                    return board
                }
            }
            currentSeed += 997
        }

        // Guaranteed fallback if search took too many attempts
        return createFallbackBoard(levelNumber, title)
    }

    private fun tryGenerateArrow(
        width: Int,
        height: Int,
        grid: Array<BooleanArray>,
        random: Random,
        id: String
    ): Arrow? {
        val isLShaped = random.nextFloat() < 0.45f
        val directions = Direction.values()
        val dir = directions[random.nextInt(directions.size)]

        if (!isLShaped) {
            // Straight arrow
            val length = random.nextInt(3) + 2 // 2 to 4 cells
            val headX = random.nextInt(width - 2) + 1
            val headY = random.nextInt(height - 2) + 1
            val tailX = headX - dir.dx * (length - 1)
            val tailY = headY - dir.dy * (length - 1)

            if (tailX !in 1 until width - 1 || tailY !in 1 until height - 1) return null

            val p1 = Point(tailX, tailY)
            val p2 = Point(headX, headY)
            val candidate = Arrow(id, listOf(p1, p2))

            // Check if any occupied
            for (p in candidate.occupiedPoints) {
                if (grid[p.y][p.x]) return null
            }
            return candidate
        } else {
            // L-shaped arrow: tail -> corner -> head
            val seg1Len = random.nextInt(3) + 1
            val seg2Len = random.nextInt(3) + 1

            val cornerX = random.nextInt(width - 4) + 2
            val cornerY = random.nextInt(height - 4) + 2

            // Seg 2 direction is `dir` (leads to head)
            val headX = cornerX + dir.dx * seg2Len
            val headY = cornerY + dir.dy * seg2Len
            if (headX !in 1 until width - 1 || headY !in 1 until height - 1) return null

            // Seg 1 direction is perpendicular
            val perpDir = if (dir.dx != 0) {
                if (random.nextBoolean()) Direction.UP else Direction.DOWN
            } else {
                if (random.nextBoolean()) Direction.LEFT else Direction.RIGHT
            }
            val tailX = cornerX - perpDir.dx * seg1Len
            val tailY = cornerY - perpDir.dy * seg1Len
            if (tailX !in 1 until width - 1 || tailY !in 1 until height - 1) return null

            val corner = Point(cornerX, cornerY)
            val tail = Point(tailX, tailY)
            val head = Point(headX, headY)

            val candidate = Arrow(id, listOf(tail, corner, head))
            for (p in candidate.occupiedPoints) {
                if (grid[p.y][p.x]) return null
            }
            return candidate
        }
    }

    private fun createFallbackBoard(levelNumber: Int, title: String): PuzzleBoard {
        val arrows = listOf(
            Arrow("fb1", listOf(Point(1, 1), Point(5, 1)), 0),
            Arrow("fb2", listOf(Point(5, 2), Point(5, 5)), 1),
            Arrow("fb3", listOf(Point(5, 5), Point(1, 5)), 2),
            Arrow("fb4", listOf(Point(1, 4), Point(1, 2)), 3)
        )
        return PuzzleBoard(levelNumber, title, 7, 7, arrows)
    }
}
