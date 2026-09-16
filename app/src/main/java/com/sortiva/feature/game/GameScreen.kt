package com.sortiva.feature.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    soundEnabled: Boolean = true,
    vibrationEnabled: Boolean = true,
    onBack: () -> Unit,
    onNextLevel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val toneGenerator = remember { android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 70) }

    LaunchedEffect(uiState.isWon) {
        if (uiState.isWon) {
            if (vibrationEnabled) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            }
            if (soundEnabled) {
                toneGenerator.startTone(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
            }
        }
    }

    if (uiState.isWon) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss */ },
            title = { Text("Level Completed!") },
            text = { Text("Great job sorting the colors.") },
            confirmButton = {
                Button(onClick = onNextLevel) {
                    Text("Next Level")
                }
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Home")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Area
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }
            Text(
                text = "Level ${uiState.levelNumber}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Placeholder for pause or settings
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Tubes Grid
        // Dynamically choose columns based on tube count (e.g., 4 tubes -> 4 cols, 8 tubes -> 4 cols 2 rows)
        val tubeCount = uiState.tubes.size
        val columns = if (tubeCount > 6) 4 else if (tubeCount > 0) minOf(tubeCount, 5) else 1

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(uiState.tubes) { index, tube ->
                TubeComposable(
                    tube = tube,
                    isSelected = uiState.selectedTubeIndex == index,
                    onTubeClick = { 
                        if (vibrationEnabled) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        }
                        if (soundEnabled) {
                            toneGenerator.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 35)
                        }
                        viewModel.onTubeClicked(index) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.undo() },
                enabled = uiState.undoStack.isNotEmpty() && !uiState.isWon
            ) {
                Text("Undo")
            }
            Button(
                onClick = { viewModel.restart() },
                enabled = uiState.undoStack.isNotEmpty() && !uiState.isWon
            ) {
                Text("Restart")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
