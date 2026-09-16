package com.sortiva.core.domain

import kotlin.random.Random

class LevelRepository {

    private val cachedLevels = mutableMapOf<Int, Level>()

    fun getLevel(levelNumber: Int): Level {
        return cachedLevels.getOrPut(levelNumber) {
            generateLevel(levelNumber)
        }
    }

    private fun generateLevel(levelNumber: Int): Level {
        // Simple difficulty scaling
        // Level 1: 3 colors, 2 empty
        // Level 50: 10 colors, 2 empty
        val numColors = when {
            levelNumber <= 5 -> 3
            levelNumber <= 15 -> 4
            levelNumber <= 25 -> 5
            levelNumber <= 35 -> 7
            else -> 9
        }
        val numEmpty = 2
        val totalTubes = numColors + numEmpty

        val availableColors = LiquidColor.values().take(numColors)
        
        // 1. Create Solved State
        val tubes = MutableList(totalTubes) { index ->
            if (index < numColors) {
                MutableList(4) { availableColors[index] }
            } else {
                mutableListOf<LiquidColor>()
            }
        }

        // 2. Perform Reverse Pours to shuffle
        val random = Random(levelNumber) // Deterministic based on level number
        val shuffleMoves = 30 + (levelNumber * 2)

        for (i in 0 until shuffleMoves) {
            // Pick a tube to take liquid from (in reverse pour, this is the destination of the forward pour)
            val nonEmptyTubes = tubes.indices.filter { tubes[it].isNotEmpty() }
            if (nonEmptyTubes.isEmpty()) continue
            
            val srcIdx = nonEmptyTubes.random(random)
            val srcTube = tubes[srcIdx]
            val topColor = srcTube.last()

            // Count how many of this color are on top
            var colorCount = 0
            for (c in srcTube.reversed()) {
                if (c == topColor) colorCount++ else break
            }

            // Pick how much to move (1 to colorCount)
            val amountToMove = random.nextInt(1, colorCount + 1)

            // Pick a destination tube that has space
            val validDestinations = tubes.indices.filter { idx ->
                idx != srcIdx && tubes[idx].size + amountToMove <= 4
            }

            if (validDestinations.isNotEmpty()) {
                val dstIdx = validDestinations.random(random)
                
                // Perform reverse pour
                for (k in 0 until amountToMove) {
                    tubes[dstIdx].add(tubes[srcIdx].removeLast())
                }
            }
        }

        // Convert to immutable domain models
        val finalTubes = tubes.mapIndexed { index, list ->
            Tube(id = index, liquids = list.toList())
        }

        return Level(levelNumber = levelNumber, initialTubes = finalTubes)
    }
}
