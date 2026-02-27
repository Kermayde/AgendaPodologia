package com.flores.agendapodologia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishAppointmentBottomSheet(
    serviceType: String,
    isWarrantyActive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, PaymentMethod, Double, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ESTADOS
    var isPaid by remember { mutableStateOf(!isWarrantyActive) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.EFECTIVO) }

    val isWarrantyApplicable = isWarrantyActive && serviceType == ServiceConstants.WARRANTY_APPLICABLE_SERVICE
    val isInvalidCharge = isWarrantyApplicable && isPaid

    var showOverrideDialog by remember { mutableStateOf(false) }
    var overrideReason by remember { mutableStateOf("") }

    val initialPrice = if (isWarrantyApplicable && !isPaid) {
        "0.0"
    } else {
        ServiceConstants.getSuggestedPrice(serviceType).toString()
    }
    var amountText by remember { mutableStateOf(initialPrice) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Título
            Text(
                text = "Finalizar Cita",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Banner de garantía
            if (isWarrantyApplicable) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Garantía vigente", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Text(
                                "Esta garantía cubre ${ServiceConstants.WARRANTY_APPLICABLE_SERVICE}. Puedes marcar la cita como sin costo o cobrar si es necesario.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Switch de cobro
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPaid) "Se realizó cobro" else "Sin costo (Garantía/Otro)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    if (isWarrantyApplicable && isPaid) {
                        Text(
                            "⚠️ Atención: el paciente tiene garantía vigente.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
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

            // Monto y método de pago
            if (isPaid) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
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

            Spacer(modifier = Modifier.height(24.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (isInvalidCharge) {
                            showOverrideDialog = true
                        } else {
                            val finalMethod = if (isPaid) selectedMethod else PaymentMethod.NONE
                            val finalAmount = if (isPaid) amountText.toDoubleOrNull() ?: 0.0 else 0.0
                            onConfirm(isPaid, finalMethod, finalAmount, "")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terminar")
                }
            }
        }
    }

    // Diálogo de confirmación para forzar cobro cuando hay garantía
    if (showOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("Confirmar cobro con garantía") },
            text = {
                Column {
                    Text("El paciente tiene una garantía vigente que cubre ${ServiceConstants.WARRANTY_APPLICABLE_SERVICE}.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Si cobras, deberías anotar la razón. Esto se registrará en la nota de la cita.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = overrideReason,
                        onValueChange = { overrideReason = it },
                        label = { Text("Motivo (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val finalMethod = if (isPaid) selectedMethod else PaymentMethod.NONE
                    val finalAmount = if (isPaid) amountText.toDoubleOrNull() ?: 0.0 else 0.0
                    onConfirm(isPaid, finalMethod, finalAmount, overrideReason)
                    showOverrideDialog = false
                }) { Text("Confirmar cobro") }
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

