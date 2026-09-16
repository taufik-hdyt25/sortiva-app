package com.sortiva.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sortiva.core.data.DataStoreManager
import com.sortiva.core.domain.LevelRepository
import com.sortiva.feature.game.GameScreen
import com.sortiva.feature.game.GameViewModel
import com.sortiva.feature.home.HomeScreen

import com.sortiva.feature.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Simple repository and data store instantiation for MVP
    val levelRepository = remember { LevelRepository() }
    val dataStoreManager = remember { DataStoreManager(context) }
    
    val unlockedLevel by dataStoreManager.unlockedLevelFlow.collectAsState(initial = 1)
    val soundEnabled by dataStoreManager.soundEnabledFlow.collectAsState(initial = true)
    val vibrationEnabled by dataStoreManager.vibrationEnabledFlow.collectAsState(initial = true)
    
    val scope = rememberCoroutineScope()

    NavHost(navController = navController, startDestination = "home") {
        
        composable("home") {
            HomeScreen(
                onPlayClicked = {
                    navController.navigate("game/$unlockedLevel")
                },
                onSettingsClicked = {
                    navController.navigate("settings")
                }
            )
        }
        

        composable("settings") {
            SettingsScreen(
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                onSoundToggled = { enabled ->
                    scope.launch { dataStoreManager.setSoundEnabled(enabled) }
                },
                onVibrationToggled = { enabled ->
                    scope.launch { dataStoreManager.setVibrationEnabled(enabled) }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("game/{levelId}") { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId")?.toIntOrNull() ?: 1
            
            val viewModel: GameViewModel = viewModel()
            
            LaunchedEffect(levelId) {
                viewModel.loadLevel(levelRepository.getLevel(levelId))
            }
            
            GameScreen(
                viewModel = viewModel,
                soundEnabled = soundEnabled,
                vibrationEnabled = vibrationEnabled,
                onBack = {
                    navController.popBackStack()
                },
                onNextLevel = {
                    // Unlock next level
                    val nextLevel = levelId + 1
                    scope.launch {
                        dataStoreManager.saveUnlockedLevel(nextLevel)
                    }
                    
                    navController.navigate("game/$nextLevel") {
                        popUpTo("home")
                    }
                }
            )
        }
    }
}
