package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.layout.Spacer
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
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = monthYearFormat.format(Date(selectedDate))
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Ícono indicando expansión
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        actions = {
            IconButton(onClick = onGoToToday) {
                Icon(
                    Icons.Default.Today,
                    "Ir a Hoy",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(onClick = onOpenDirectory) {
                Icon(
                    Icons.Default.Person,
                    "Directorio",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Default.Settings,
                    "Configuración",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}


