package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.viewmodel.WarrantyState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WarrantyBanner(warrantyState: WarrantyState) {
    if (warrantyState.isActive) {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val dateString = warrantyState.expirationDate?.let { dateFormat.format(it) } ?: "?"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp)), // Borde verde
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9) // Verde muy clarito
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32) // Verde oscuro
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Garantía Vigente (${warrantyState.daysRemaining} días restantes)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Text(
                        text = "Cubre correcciones hasta el $dateString.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Activada por: ${warrantyState.sourceAppointmentService}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}