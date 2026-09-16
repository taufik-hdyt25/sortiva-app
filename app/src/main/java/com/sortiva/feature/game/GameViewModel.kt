package com.sortiva.feature.game

import androidx.lifecycle.ViewModel
import com.sortiva.core.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameState(
    val levelNumber: Int = 1,
    val tubes: List<Tube> = emptyList(),
    val selectedTubeIndex: Int? = null,
    val isWon: Boolean = false,
    val undoStack: List<Move> = emptyList()
)

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    fun loadLevel(level: Level) {
        _uiState.value = GameState(
            levelNumber = level.levelNumber,
            tubes = level.initialTubes
        )
    }

    fun onTubeClicked(tubeIndex: Int) {
        val state = _uiState.value
        if (state.isWon) return

        val selected = state.selectedTubeIndex

        if (selected == null) {
            // Select the tube if it's not empty
            if (state.tubes[tubeIndex].isNotEmpty()) {
                _uiState.update { it.copy(selectedTubeIndex = tubeIndex) }
            }
        } else {
            // If clicking the same tube, deselect
            if (selected == tubeIndex) {
                _uiState.update { it.copy(selectedTubeIndex = null) }
                return
            }

            // Try to pour from selected to tubeIndex
            val source = state.tubes[selected]
            val dest = state.tubes[tubeIndex]

            val move = GameEngine.calculatePour(source, dest)
            if (move != null) {
                val newTubes = GameEngine.applyMove(state.tubes, move)
                val isWon = GameEngine.checkWin(newTubes)
                
                _uiState.update {
                    it.copy(
                        tubes = newTubes,
                        selectedTubeIndex = null,
                        undoStack = it.undoStack + move,
                        isWon = isWon
                    )
                }
            } else {
                // If invalid pour, change selection to the new tube if it's not empty
                if (state.tubes[tubeIndex].isNotEmpty()) {
                    _uiState.update { it.copy(selectedTubeIndex = tubeIndex) }
                } else {
                    _uiState.update { it.copy(selectedTubeIndex = null) }
                }
            }
        }
    }

    fun undo() {
        val state = _uiState.value
        if (state.isWon || state.undoStack.isEmpty()) return
        
        val lastMove = state.undoStack.last()
        val newTubes = GameEngine.reverseMove(state.tubes, lastMove)
        
        _uiState.update {
            it.copy(
                tubes = newTubes,
                selectedTubeIndex = null, // clear selection on undo
                undoStack = it.undoStack.dropLast(1)
            )
        }
    }

    fun restart() {
        // In a real app, we would reload the initial state of the current level.
        // For MVP, we can reverse all moves in the undo stack, or just loadLevel again
        // if we keep a reference to the initial level.
        // Assuming we will pass the initial level to loadLevel:
        // We'll need the LevelRepository to fetch it again, or we can just reconstruct it by undoing all.
        val state = _uiState.value
        if (state.undoStack.isEmpty()) return
        
        var currentTubes = state.tubes
        for (move in state.undoStack.reversed()) {
            currentTubes = GameEngine.reverseMove(currentTubes, move)
        }
        
        _uiState.update {
            it.copy(
                tubes = currentTubes,
                selectedTubeIndex = null,
                isWon = false,
                undoStack = emptyList()
            )
        }
    }

    // Helper extension
    private fun Tube.isNotEmpty() = !this.isEmpty()
}
