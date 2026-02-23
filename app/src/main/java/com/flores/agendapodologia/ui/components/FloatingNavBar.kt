package com.flores.agendapodologia.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FloatingNavBar(
    currentRoute: String?,
    onNavigateToHome: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddAppointment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnHome = currentRoute == "home"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Pacientes
                NavBarItem(
                    icon = Icons.Filled.People,
                    contentDescription = "Pacientes",
                    isSelected = currentRoute == "patient_directory",
                    onClick = onNavigateToPatients
                )

                Spacer(modifier = Modifier.width(4.dp))

                // 2. Centro: Home / Agregar Cita
                CenterNavButton(
                    isOnHome = isOnHome,
                    onHomeClick = onNavigateToHome,
                    onAddClick = onAddAppointment
                )

                Spacer(modifier = Modifier.width(4.dp))

                // 3. Settings
                NavBarItem(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Configuración",
                    isSelected = currentRoute == "settings",
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "containerColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onSecondaryContainer
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconColor"
    )

    Box(
        modifier = Modifier
            .scale(animatedScale)
            .size(45.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 24.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CenterNavButton(
    isOnHome: Boolean,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isOnHome)
            MaterialTheme.colorScheme.secondary
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "centerColor"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isOnHome)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "centerIconColor"
    )

    val icon = if (isOnHome) Icons.Filled.Add else Icons.Filled.CalendarMonth
    val description = if (isOnHome) "Agregar Cita" else "Agenda"

    Box(
        modifier = Modifier
            //.size(36.dp)
            .width(60.dp)
            .height(45.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 28.dp),
                onClick = if (isOnHome) onAddClick else onHomeClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}


