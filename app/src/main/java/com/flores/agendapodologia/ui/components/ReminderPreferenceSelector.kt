package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.ReminderPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPreferenceSelector(
    selected: ReminderPreference,
    onSelected: (ReminderPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Preferencia de Aviso",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selected == ReminderPreference.WHATSAPP,
                onClick = { onSelected(ReminderPreference.WHATSAPP) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                icon = {}
            ) {
                Text("WhatsApp", style = MaterialTheme.typography.labelMedium)
            }
            SegmentedButton(
                selected = selected == ReminderPreference.LLAMADA,
                onClick = { onSelected(ReminderPreference.LLAMADA) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                icon = {}
            ) {
                Text("Llamada", style = MaterialTheme.typography.labelMedium)
            }
            SegmentedButton(
                selected = selected == ReminderPreference.NINGUNO,
                onClick = { onSelected(ReminderPreference.NINGUNO) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                icon = {}
            ) {
                Text("Ninguno", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}


