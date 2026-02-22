package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCalendarHeader(
    selectedDate: Long,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onGoToToday: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // Formateador dinámico: si es año actual, solo mes; si no, mes abreviado + año
    val monthYearFormat = run {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val selectedYear = selectedCal.get(Calendar.YEAR)

        if (currentYear == selectedYear) {
            // Solo mes completo: "Febrero"
            SimpleDateFormat("MMMM", Locale.getDefault())
        } else {
            // Mes abreviado + año: "Feb 2027"
            SimpleDateFormat("MMM yyyy", Locale.getDefault())
        }
    }

    TopAppBar(
        title = {
            // Título clickeable para expandir/colapsar
            TextButton(
                onClick = onToggleExpanded,
            ) {
                Text(
                    text = monthYearFormat.format(Date(selectedDate))
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Ícono indicando expansión
                Box(
                    modifier = Modifier
                        .clip( shape = MaterialTheme.shapes.medium )
                        .width( 32.dp )
                        .background( color = MaterialTheme.colorScheme.secondaryContainer ),
                    contentAlignment = androidx.compose.ui.Alignment.Center

                ) {
                    Icon(
                        imageVector = if (isExpanded)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        actions = {
            IconButton(onClick = onGoToToday) {
                Icon(
                    Icons.Default.Today,
                    "Ir a Hoy",

                )
            }
            IconButton(onClick = onOpenDirectory) {
                Icon(
                    Icons.Default.Person,
                    "Directorio",

                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Default.Settings,
                    "Configuración",

                )
            }
        }
    )
}


