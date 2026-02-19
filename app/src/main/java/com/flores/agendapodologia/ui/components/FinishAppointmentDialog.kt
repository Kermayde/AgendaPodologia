package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.PaymentMethod
import com.flores.agendapodologia.util.ServiceConstants

@Composable
fun FinishAppointmentDialog(
    serviceType: String, // <--- NUEVO: Necesitamos saber qué servicio es
    isWarrantyActive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, PaymentMethod, Double) -> Unit // <--- NUEVO: Devuelve el Double
) {
    // ESTADOS
    var isPaid by remember { mutableStateOf(!isWarrantyActive) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.EFECTIVO) }

    // ESTADO DEL DINERO
    // Calculamos el precio inicial basado en el catálogo y la garantía
    val initialPrice = if (isWarrantyActive && serviceType == "Correcciones") {
        "0.0"
    } else {
        ServiceConstants.getSuggestedPrice(serviceType).toString()
    }
    var amountText by remember { mutableStateOf(initialPrice) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar Cita") },
        text = {
            Column {
                // 1. SWITCH DE COBRO
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isPaid) "Se realizó cobro" else "Sin costo (Garantía/Otro)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        if (isWarrantyActive && isPaid) {
                            Text("¡Ojo! Garantía disponible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Switch(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it },
                        thumbContent = {
                            Icon(
                                imageVector = if (isPaid) Icons.Default.ShoppingCart else Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                // 2. MONTO Y MÉTODO DE PAGO (Solo visibles si se cobra)
                if (isPaid) {
                    // --- CAMPO DE CANTIDAD ---
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            // Solo permitimos números y un punto decimal
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amountText = it
                            }
                        },
                        label = { Text("Cantidad Cobrada") },
                        leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- MÉTODOS DE PAGO ---
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
                                RadioButton(selected = (selectedMethod == method), onClick = null)
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
                    val finalMethod = if (isPaid) selectedMethod else PaymentMethod.NONE
                    // Convertimos el texto a Double de forma segura. Si está vacío o es inválido, pasa 0.0
                    val finalAmount = if (isPaid) amountText.toDoubleOrNull() ?: 0.0 else 0.0

                    onConfirm(isPaid, finalMethod, finalAmount)
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