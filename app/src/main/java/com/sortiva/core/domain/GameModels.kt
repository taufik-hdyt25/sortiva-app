package com.sortiva.core.domain

import androidx.compose.ui.graphics.Color
import com.sortiva.ui.theme.*

enum class LiquidColor(val color: Color) {
    RED_CORAL(LiquidRedCoral),
    ORANGE(LiquidOrange),
    YELLOW(LiquidYellow),
    LIME(LiquidLime),
    GREEN(LiquidGreen),
    CYAN(LiquidCyan),
    BLUE(LiquidBlue),
    INDIGO(LiquidIndigo),
    PURPLE(LiquidPurple),
    PINK(LiquidPink),
    BROWN(LiquidBrown),
    GRAY(LiquidGray)
}

data class Tube(
    val id: Int,
    val liquids: List<LiquidColor> = emptyList(),
    val capacity: Int = 4
) {
    fun isFull(): Boolean = liquids.size >= capacity
    fun isEmpty(): Boolean = liquids.isEmpty()
    fun topColor(): LiquidColor? = liquids.lastOrNull()
    
    // A tube is complete if it's full and all colors are the same, OR if it's completely empty
    fun isComplete(): Boolean {
        if (isEmpty()) return true
        if (!isFull()) return false
        val firstColor = liquids.first()
        return liquids.all { it == firstColor }
    }
}

data class Level(
    val levelNumber: Int,
    val initialTubes: List<Tube>
)

data class Move(
    val fromTubeIndex: Int,
    val toTubeIndex: Int,
    val color: LiquidColor,
    val amount: Int
)
