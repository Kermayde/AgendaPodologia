package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.PaymentMethod

@Composable
fun FinishAppointmentDialog(
    isWarrantyActive: Boolean, // Para sugerir defaults
    onDismiss: () -> Unit,
    onConfirm: (Boolean, PaymentMethod) -> Unit
) {
    // ESTADOS
    // Si hay garantía, por defecto NO se cobra. Si no, SÍ se cobra.
    var isPaid by remember { mutableStateOf(!isWarrantyActive) }

    // Por defecto Efectivo si se cobra
    var selectedMethod by remember { mutableStateOf(PaymentMethod.EFECTIVO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Cita") },
        text = {
            Column {
                // 1. SWITCH DE COBRO
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isPaid) "Se realizó cobro" else "Sin costo (Garantía)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        if (isWarrantyActive && isPaid) {
                            Text("(Garantía disponible)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Switch(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it },
                        thumbContent = {
                            Icon(
                                imageVector = if (isPaid) Icons.Default.ThumbUp else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                // 2. MÉTODOS DE PAGO (Solo visibles si se cobra)
                if (isPaid) {
                    Text("Método de Pago:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(Modifier.selectableGroup()) {
                        val methods = listOf(
                            PaymentMethod.EFECTIVO to "Efectivo",
                            PaymentMethod.TARJETA to "Tarjeta",
                            PaymentMethod.TRANSFERENCIA to "Transferencia"
                        )

                        methods.forEach { (method, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .selectable(
                                        selected = (selectedMethod == method),
                                        onClick = { selectedMethod = method },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (selectedMethod == method),
                                    onClick = null // null para que el click lo maneje el Row
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Si no se pagó, el método es NONE
                    val finalMethod = if (isPaid) selectedMethod else PaymentMethod.NONE
                    onConfirm(isPaid, finalMethod)
                }
            ) {
                Text("Terminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}