package com.example.myguidance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FabSubItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ExpandableFloatingActionButton(
    items: List<FabSubItem>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "FabRotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { subItem ->
                    FabSubItemRow(
                        subItem = subItem,
                        onItemClick = {
                            onExpandedChange(false)
                            subItem.onClick()
                        }
                    )
                }
            }
        }

        // Main FAB Button
        FloatingActionButton(
            onClick = { onExpandedChange(!isExpanded) },
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Menu Options",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}

@Composable
private fun FabSubItemRow(
    subItem: FabSubItem,
    onItemClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val isHighlighted = isHovered || isPressed

    // Smoothly scale up button and text label on hover/press
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.08f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "SubItemScale"
    )

    // Dynamic background color changes when hovered
    val buttonBgColor by animateColorAsState(
        targetValue = if (isHighlighted) Color(0xFFE0E0E0) else Color.White,
        label = "SubItemBgColor"
    )

    val labelBgColor by animateColorAsState(
        targetValue = if (isHighlighted) Color(0xFF222222) else Color.Black,
        label = "LabelBgColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.scale(scale)
    ) {
        // Label Badge
        Surface(
            shape = CircleShape,
            color = labelBgColor,
            contentColor = Color.White,
            shadowElevation = if (isHighlighted) 6.dp else 4.dp
        ) {
            Text(
                text = subItem.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Sub Action Button
        SmallFloatingActionButton(
            onClick = onItemClick,
            interactionSource = interactionSource,
            containerColor = buttonBgColor,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(
                imageVector = subItem.icon,
                contentDescription = subItem.label
            )
        }
    }
}