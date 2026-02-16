package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.util.ServiceConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceSelector(
    selectedService: String,
    onServiceSelected: (String) -> Unit
) {
    Column {
        Text("Tipo de Servicio", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Usamos LazyRow para que se pueda deslizar horizontalmente si son muchos
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(ServiceConstants.SERVICES_LIST) { service ->
                FilterChip(
                    selected = (service == selectedService),
                    onClick = { onServiceSelected(service) },
                    label = { Text(service) },
                    leadingIcon = if (service == selectedService) {
                        { Icon(Icons.Default.Check, null) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}