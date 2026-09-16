package com.sortiva.core.domain

class GameEngine {

    companion object {
        
        /**
         * Checks if a pour from one tube to another is valid.
         */
        fun canPour(source: Tube, destination: Tube): Boolean {
            if (source.id == destination.id) return false
            if (source.isEmpty()) return false
            if (destination.isFull()) return false
            
            return destination.isEmpty() || source.topColor() == destination.topColor()
        }

        /**
         * Calculates the move details (amount and color) when pouring from source to destination.
         * Assumes canPour is true.
         */
        fun calculatePour(source: Tube, destination: Tube): Move? {
            if (!canPour(source, destination)) return null

            val color = source.topColor() ?: return null
            val availableSpace = destination.capacity - destination.liquids.size
            
            // Count how many consecutive blocks of the same color are at the top of the source
            var consecutiveCount = 0
            for (i in source.liquids.indices.reversed()) {
                if (source.liquids[i] == color) {
                    consecutiveCount++
                } else {
                    break
                }
            }

            val amountToPour = minOf(consecutiveCount, availableSpace)
            
            return Move(
                fromTubeIndex = source.id,
                toTubeIndex = destination.id,
                color = color,
                amount = amountToPour
            )
        }

        /**
         * Applies the move to the list of tubes and returns the new list.
         */
        fun applyMove(tubes: List<Tube>, move: Move): List<Tube> {
            val newTubes = tubes.toMutableList()
            
            val source = newTubes[move.fromTubeIndex]
            val dest = newTubes[move.toTubeIndex]

            val newSourceLiquids = source.liquids.dropLast(move.amount)
            val newDestLiquids = dest.liquids + List(move.amount) { move.color }

            newTubes[move.fromTubeIndex] = source.copy(liquids = newSourceLiquids)
            newTubes[move.toTubeIndex] = dest.copy(liquids = newDestLiquids)

            return newTubes
        }
        
        /**
         * Reverses a move.
         */
        fun reverseMove(tubes: List<Tube>, move: Move): List<Tube> {
            val newTubes = tubes.toMutableList()
            
            val source = newTubes[move.fromTubeIndex] // Originally source, now we pour back into it
            val dest = newTubes[move.toTubeIndex] // Originally dest, now we pour from it
            
            val newDestLiquids = dest.liquids.dropLast(move.amount)
            val newSourceLiquids = source.liquids + List(move.amount) { move.color }
            
            newTubes[move.fromTubeIndex] = source.copy(liquids = newSourceLiquids)
            newTubes[move.toTubeIndex] = dest.copy(liquids = newDestLiquids)
            
            return newTubes
        }

        /**
         * Checks if the game is won.
         */
        fun checkWin(tubes: List<Tube>): Boolean {
            return tubes.all { it.isComplete() }
        }
    }
}
