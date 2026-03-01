package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.data.UserPreferences
import com.flores.agendapodologia.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onNavigateToSchedule: () -> Unit = {}
) {
    // ── Padding inferior para no quedar tapado por la FloatingNavBar ──
    val bottomPadding = with(LocalDensity.current) {
        WindowInsets.navigationBars.getBottom(this).toDp()
    } + NAV_BAR_OFFSET

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ═══════════════════════════════════════════════════
            //  SECCIÓN: General
            // ═══════════════════════════════════════════════════
            SettingsSectionHeader(title = "General")

            // Configuración de Horarios (funcional)
            SettingsItem(
                icon = Icons.Default.CalendarMonth,
                title = "Horarios de atención",
                subtitle = "Configura días y turnos de atención",
                onClick = onNavigateToSchedule
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Formato de hora (funcional con switch)
            val use12Hour by userPreferences.use12HourFormat.collectAsState()

            SettingsToggleItem(
                icon = Icons.Default.AccessTime,
                title = "Formato de 12 horas",
                subtitle = if (use12Hour) "Ejemplo: 2:30 PM" else "Ejemplo: 14:30",
                checked = use12Hour,
                onCheckedChange = { userPreferences.setUse12HourFormat(it) }
            )

            // ═══════════════════════════════════════════════════
            //  SECCIÓN: Apariencia
            // ═══════════════════════════════════════════════════
            SettingsSectionHeader(title = "Apariencia")

            // Tema de la app (placeholder)
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Tema",
                subtitle = "Seguir ajustes del sistema",
                onClick = { /* TODO: Implementar selector de tema */ },
                enabled = false
            )

            // ═══════════════════════════════════════════════════
            //  SECCIÓN: Cuenta
            // ═══════════════════════════════════════════════════
            SettingsSectionHeader(title = "Cuenta")

            // Inicio de sesión (placeholder)
            SettingsItem(
                icon = Icons.Default.Login,
                title = "Iniciar sesión",
                subtitle = "No has iniciado sesión",
                onClick = { /* TODO: Implementar inicio de sesión */ },
                enabled = false
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Cerrar sesión (placeholder)
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Cerrar sesión",
                subtitle = null,
                onClick = { /* TODO: Implementar cierre de sesión */ },
                enabled = false,
                tint = MaterialTheme.colorScheme.error
            )

            // ═══════════════════════════════════════════════════
            //  SECCIÓN: Acerca de
            // ═══════════════════════════════════════════════════
            SettingsSectionHeader(title = "Acerca de")

            SettingsItem(
                icon = Icons.Default.Info,
                title = "Versión de la app",
                subtitle = "0.8",
                onClick = {},
                showChevron = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pie de página
            Text(
                text = "Agenda Podología © 2026",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  Componentes reutilizables para la pantalla de configuración
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    showChevron: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val alpha = if (enabled) 1f else 0.5f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint.copy(alpha = alpha),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tint.copy(alpha = alpha)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray.copy(alpha = alpha)
                    )
                }
            }

            if (showChevron && enabled) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Item de configuración con un Switch en lugar de chevron. */
@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tint
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

