package com.sortiva.feature.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.sortiva.core.domain.Tube

@Composable
fun TubeComposable(
    tube: Tube,
    isSelected: Boolean,
    onTubeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Lift the tube slightly if selected
    val offsetY by animateDpAsState(targetValue = if (isSelected) (-16).dp else 0.dp)

    // Remember the liquids for smooth drain animations
    var displayLiquids by remember { mutableStateOf(tube.liquids) }
    val animatedSize by animateFloatAsState(
        targetValue = tube.liquids.size.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "liquidLevel"
    )

    androidx.compose.runtime.LaunchedEffect(tube.liquids) {
        var changed = false
        for (i in tube.liquids.indices) {
            if (i >= displayLiquids.size || tube.liquids[i] != displayLiquids[i]) {
                changed = true
                break
            }
        }
        if (changed || tube.liquids.size > displayLiquids.size || tube.liquids.isEmpty()) {
            displayLiquids = tube.liquids
        }
    }

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .height(180.dp)
            .width(50.dp)
            .clickable { onTubeClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Draw the glass tube
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 3.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp)
                )
        )

        // Draw liquids
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(3.dp)
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
        ) {
            val totalCapacity = tube.capacity
            val sectionHeight = size.height / totalCapacity
            
            var remainingHeight = animatedSize * sectionHeight
            
            displayLiquids.forEachIndexed { index, liquidColor ->
                if (remainingHeight <= 0f) return@forEachIndexed
                
                val heightForThisBlock = minOf(sectionHeight, remainingHeight)
                
                // Draw from bottom to top
                val bottomY = size.height - (index * sectionHeight)
                val topY = bottomY - heightForThisBlock
                
                drawRoundRect(
                    color = liquidColor.color,
                    topLeft = Offset(0f, topY),
                    size = Size(size.width, heightForThisBlock),
                    cornerRadius = CornerRadius(0f, 0f) // The clipping box handles the bottom rounded corners
                )
                
                remainingHeight -= heightForThisBlock
            }
        }
    }
}
