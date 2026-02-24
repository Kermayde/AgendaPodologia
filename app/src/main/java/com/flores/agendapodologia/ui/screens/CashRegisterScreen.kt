package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.ui.components.WeekCalendar
import com.flores.agendapodologia.viewmodel.DailySummary
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashRegisterScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()

    // Formatear fecha para el título
    val dateFormat = SimpleDateFormat("EEEE d 'de' MMMM", Locale.getDefault())
    val dateTitle = dateFormat.format(Date(selectedDate))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                TopAppBar(
                    title = { Text("Corte de Caja") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                // Tira semanal reutilizada — comparte selectedDate con la agenda
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WeekCalendar(
                        selectedDate = selectedDate,
                        onDateSelected = { newDate -> viewModel.changeDate(newDate) },
                        onWeekChanged = { year, month ->
                            viewModel.updateDisplayedMonthFromWeek(year, month)
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Subtítulo con la fecha legible
            Text(
                text = dateTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (dailySummary.totalAppointments == 0) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin citas este día",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                // TARJETA PRINCIPAL: Total del día
                CashTotalCard(summary = dailySummary)

                Spacer(modifier = Modifier.height(12.dp))

                // DESGLOSE: Efectivo vs Banco
                PaymentBreakdownRow(summary = dailySummary)

                Spacer(modifier = Modifier.height(16.dp))

                // RESUMEN DE CITAS
                Text(
                    text = "Resumen de Citas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                AppointmentStatsCard(summary = dailySummary)

                // Espacio para la nav bar flotante
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

// ==================== COMPONENTES INTERNOS ====================

@Composable
private fun CashTotalCard(summary: DailySummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ingresos del Día",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = summary.totalFormatted,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
            if (summary.paidAppointments > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${summary.paidAppointments} cobro${if (summary.paidAppointments > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF388E3C)
                )
            }
        }
    }
}

@Composable
private fun PaymentBreakdownRow(summary: DailySummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // EFECTIVO
        PaymentMethodCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ShoppingCart,
            label = "Efectivo",
            amount = summary.cashFormatted,
            iconTint = Color(0xFF2E7D32),
            containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f)
        )

        // BANCO (Tarjeta + Transferencia)
        PaymentMethodCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CreditCard,
            label = "Tarjeta / Transf.",
            amount = summary.bankFormatted,
            iconTint = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun PaymentMethodCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    amount: String,
    iconTint: Color,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = iconTint
            )
        }
    }
}

@Composable
private fun AppointmentStatsCard(summary: DailySummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatRow(
                label = "Total de citas",
                value = "${summary.totalAppointments}",
                color = MaterialTheme.colorScheme.onSurface,
                isBold = true
            )

            if (summary.finishedAppointments > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            if (summary.paidAppointments > 0) {
                StatRowWithIcon(
                    icon = Icons.Default.AttachMoney,
                    label = "Cobradas",
                    value = "${summary.paidAppointments}",
                    color = Color(0xFF2E7D32)
                )
            }

            if (summary.warrantyAppointments > 0) {
                StatRowWithIcon(
                    icon = Icons.Default.ShoppingCart,
                    label = "Por garantía (gratis)",
                    value = "${summary.warrantyAppointments}",
                    color = Color(0xFF1565C0)
                )
            }

            if (summary.pendingAppointments > 0) {
                StatRowWithIcon(
                    icon = Icons.Default.Schedule,
                    label = "Pendientes",
                    value = "${summary.pendingAppointments}",
                    color = Color(0xFFF57C00)
                )
            }

            if (summary.cancelledAppointments > 0) {
                StatRowWithIcon(
                    icon = Icons.Default.EventBusy,
                    label = "Canceladas",
                    value = "${summary.cancelledAppointments}",
                    color = Color(0xFFE53935)
                )
            }

            if (summary.noShowAppointments > 0) {
                StatRowWithIcon(
                    icon = Icons.Default.PersonOff,
                    label = "No asistió",
                    value = "${summary.noShowAppointments}",
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StatRowWithIcon(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
