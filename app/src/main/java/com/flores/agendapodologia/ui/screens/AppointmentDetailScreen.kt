package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.AppointmentStatus
import com.flores.agendapodologia.model.PaymentMethod
import com.flores.agendapodologia.ui.components.DatePickerModal
import com.flores.agendapodologia.ui.components.FinishAppointmentBottomSheet
import com.flores.agendapodologia.ui.components.ServiceSelector
import com.flores.agendapodologia.ui.components.StatusSelector
import com.flores.agendapodologia.ui.components.TimePickerModal
import com.flores.agendapodologia.ui.components.WarrantyBanner
import com.flores.agendapodologia.util.ServiceConstants
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.sequences.ifEmpty
import kotlin.text.format
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onNavigateToPatient: (String) -> Unit = {}
) {
    // Observamos la lista
    val historyList by viewModel.lastAppointments.collectAsState()

    // Estado para mostrar el diálogo
    var showFinishDialog by remember { mutableStateOf(false) }

    val appointment by viewModel.currentDetailAppointment.collectAsState()
    val warrantyState by viewModel.warrantyStatus.collectAsState()

    // ESTADO DE EDICIÓN
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ESTADOS TEMPORALES (Buffer de edición)
    var editDate by remember { mutableStateOf(Date()) }
    var editService by remember { mutableStateOf("") }
    var editPodiatrist by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf(AppointmentStatus.PENDIENTE) }

    // Selectores de fecha/hora
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Estado local para las notas que estás escribiendo
    var currentNotes by remember { mutableStateOf("") }

    // Sincronizar notas cuando la cita se carga/cambia (la carga es asíncrona)
    LaunchedEffect(appointment?.id) {
        currentNotes = appointment?.notes ?: ""
    }

    // Sincronizar datos al entrar en modo edición
    LaunchedEffect(isEditing) {
        if (isEditing && appointment != null) {
            editDate = appointment!!.date
            editService = appointment!!.serviceType
            editPodiatrist = appointment!!.podiatristName
            editStatus = appointment!!.status
        }
    }

    // Formateadores de fecha
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)
    val timeFormat = SimpleDateFormat("HH:mm", LocalLocale.current.platformLocale)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editando Cita" else "Detalle de Cita") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) isEditing = false else onBack()
                    }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (!isEditing) {
                        // MODO LECTURA: Editar y Borrar
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Editar Cita")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        // MODO EDICIÓN: Guardar
                        IconButton(onClick = {
                            if (appointment != null) {
                                // Lógica de Negocio:
                                // Si el estado NO es FINALIZADA (ej. Pendiente, Cancelada, No Asistió),
                                // debemos anular el pago y la fecha de cierre.
                                val isNotFinished = editStatus != AppointmentStatus.FINALIZADA

                                val updatedAppt = appointment!!.copy(
                                    date = editDate,
                                    serviceType = editService,
                                    podiatristName = editPodiatrist,
                                    status = editStatus,

                                    // --- AQUÍ ESTÁ EL CAMBIO ---
                                    // Si no está finalizada, forzamos a falso/null.
                                    // Si sí está finalizada, mantenemos lo que ya tenía.
                                    isPaid = if (isNotFinished) false else appointment!!.isPaid,
                                    paymentMethod = if (isNotFinished) PaymentMethod.NONE else appointment!!.paymentMethod,
                                    completedAt = if (isNotFinished) null else appointment!!.completedAt
                                )

                                viewModel.updateAppointment(updatedAppt) {
                                    isEditing = false
                                }
                            }
                        }) {
                            Icon(
                                Icons.Default.Check,
                                "Guardar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
//        floatingActionButton = {
//            if (!isEditing && appointment?.status == AppointmentStatus.PENDIENTE) {
//                ExtendedFloatingActionButton(
//                    onClick = { showFinishDialog = true },
//                    icon = { Icon(Icons.Default.Check, null) },
//                    text = { Text("Terminar Cita") },
//                    containerColor = MaterialTheme.colorScheme.primary
//                )
//            }
//        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!isEditing && appointment?.status == AppointmentStatus.PENDIENTE) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (appointment != null) {
                                    val updated = appointment!!.copy(
                                        status = AppointmentStatus.NO_ASISTIO,
                                        isPaid = false,
                                        paymentMethod = PaymentMethod.NONE,
                                        completedAt = null
                                    )
                                    viewModel.updateAppointment(updated) {}
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("No Asistió", maxLines = 1)
                        }

                        Button(
                            onClick = { showFinishDialog = true },
                        ) {
                            Text("Terminar Cita", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                if (appointment != null) {
                                    val updated = appointment!!.copy(
                                        status = AppointmentStatus.CANCELADA,
                                        isPaid = false,
                                        paymentMethod = PaymentMethod.NONE,
                                        completedAt = null
                                    )
                                    viewModel.updateAppointment(updated) {}
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Canceló", maxLines = 1)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (appointment == null) return@Scaffold

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // --- CONTENIDO VARIABLE SEGÚN MODO ---

            if (isEditing) {
                // ================= MODO EDICIÓN =================

                Text("Reprogramar Fecha y Hora", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth()) {
                    // Selector FECHA
                    OutlinedTextField(
                        value = dateFormat.format(editDate),
                        onValueChange = {},
                        label = { Text("Fecha") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.DateRange, null) },
                        modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                        enabled = false, // Click manejado por modifier
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Selector HORA
                    OutlinedTextField(
                        value = timeFormat.format(editDate),
                        onValueChange = {},
                        label = { Text("Hora") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.CheckCircle, null) },
                        modifier = Modifier.weight(1f).clickable { showTimePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector SERVICIO
                ServiceSelector(
                    selectedService = editService,
                    onServiceSelected = { editService = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector PODÓLOGO
                Text("Podólogo", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = editPodiatrist == "Carlos", onClick = { editPodiatrist = "Carlos" })
                    Text("Carlos", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = editPodiatrist == "Karla", onClick = { editPodiatrist = "Karla" })
                    Text("Dra. Karla")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector ESTATUS (Aquí puedes poner: Cancelada, No Asistió, o volver a Pendiente)
                StatusSelector(
                    currentStatus = editStatus,
                    onStatusSelected = { editStatus = it }
                )

            } else {
                // SECCIÓN 0: Info del Paciente (Clickable para ir al detalle)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            appointment?.patientId?.let { patientId ->
                                onNavigateToPatient(patientId)
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appointment?.patientName ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = appointment?.patientPhone ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Servicio: ${appointment?.serviceType ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Podólogo: ${appointment?.podiatristName ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Ver paciente",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Banner de Garantía (Lo ponemos al principio para que sea evidente)
                // Mostrar banner SOLO si la cita actual es del tipo que aplica garantía (por ejemplo: Correcciones)
                if (warrantyState.isActive && appointment?.serviceType == ServiceConstants.WARRANTY_APPLICABLE_SERVICE) {
                    WarrantyBanner(warrantyState = warrantyState)
                }

                // 2. Si la cita actual es de "Correcciones" y hay garantía, avisamos que es GRATIS
                if (appointment?.serviceType == ServiceConstants.WARRANTY_APPLICABLE_SERVICE && warrantyState.isActive) {
                    Text(
                        text = "✨ Esta cita no debería tener costo por garantía.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // SECCIÓN 1: Sesión Actual (Lo importante)
                Text("Notas de la Sesión Actual", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Escribe aquí el tratamiento, observaciones o recomendaciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentNotes,
                    onValueChange = { currentNotes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp), // Campo grande para escribir a gusto
                    label = { Text("Tratamiento / Observaciones") },
                    placeholder = { Text("Ej: Se realizó corte de uña encarnada en pie derecho. Paciente reportó dolor leve...") },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveNotes(currentNotes) { onBack() } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios")
                }

                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                // SECCIÓN 2: Historial Reciente
                Text("Historial Reciente (Últimas 3 visitas)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (historyList.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        historyList.forEach { pastAppointment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.List, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dateFormat.format(pastAppointment.date),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        // Indicador si esta cita usó garantía
                                        if (pastAppointment.usedWarranty) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "[GARANTÍA]",
                                                color = Color(0xFF2E7D32), // verde
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Tipo de servicio
                                    Text(
                                        text = "Servicio: ${pastAppointment.serviceType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Estado de la cita
                                    val statusText = when (pastAppointment.status) {
                                        AppointmentStatus.FINALIZADA -> "Finalizada"
                                        AppointmentStatus.CANCELADA -> "Cancelada"
                                        AppointmentStatus.NO_ASISTIO -> "No Asistió"
                                        AppointmentStatus.PENDIENTE -> "Pendiente"
                                    }
                                    val statusColor = when (pastAppointment.status) {
                                        AppointmentStatus.FINALIZADA -> Color(0xFF2E7D32)
                                        AppointmentStatus.CANCELADA -> MaterialTheme.colorScheme.error
                                        AppointmentStatus.NO_ASISTIO -> MaterialTheme.colorScheme.error
                                        AppointmentStatus.PENDIENTE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Text(
                                        text = "Estado: $statusText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = pastAppointment.notes.ifEmpty { "Sin notas." },
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 3
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Atendió: ${pastAppointment.podiatristName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.align(Alignment.End),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Caso vacío
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No hay visitas anteriores registradas.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET ---
    if (showFinishDialog) {
        FinishAppointmentBottomSheet(
            serviceType = appointment?.serviceType ?: "Otro",
            isWarrantyActive = warrantyState.isActive && appointment?.serviceType == ServiceConstants.WARRANTY_APPLICABLE_SERVICE,
            onDismiss = { showFinishDialog = false },
            onConfirm = { isPaid, method, amount, overrideReason ->
                val usedWarranty = (!isPaid) && (warrantyState.isActive && appointment?.serviceType == ServiceConstants.WARRANTY_APPLICABLE_SERVICE)

                viewModel.finishAppointment(isPaid, method, amount, usedWarranty) {
                    if (overrideReason.isNotBlank()) {
                        val current = viewModel.currentDetailAppointment.value
                        if (current != null) {
                            val newNotes = (current.notes.ifEmpty { "" } + "\n[Override reason]: " + overrideReason).trim()
                            viewModel.saveNotes(newNotes) { /* no-op */ }
                        }
                    }

                    showFinishDialog = false
                }
            }
        )
    }


    // 1. Borrar Cita
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar Cita?") },
            text = { Text("Esta acción eliminará la cita permanentemente.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAppointment(appointment!!.id) {
                            showDeleteDialog = false
                            onBack() // Regresar a la agenda
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }

    // 2. DatePicker (Reutilizamos lógica UTC fix)
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { utcMillis ->
                if (utcMillis != null) {
                    // Mantenemos la HORA actual, solo cambiamos la FECHA
                    val cal = Calendar.getInstance()
                    cal.time = editDate // Hora actual seleccionada

                    val newDateCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    newDateCal.timeInMillis = utcMillis

                    cal.set(Calendar.YEAR, newDateCal.get(Calendar.YEAR))
                    cal.set(Calendar.MONTH, newDateCal.get(Calendar.MONTH))
                    cal.set(Calendar.DAY_OF_MONTH, newDateCal.get(Calendar.DAY_OF_MONTH))

                    editDate = cal.time
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // 3. TimePicker
    if (showTimePicker) {
        TimePickerModal(
            onTimeSelected = { hour, minute ->
                val cal = Calendar.getInstance()
                cal.time = editDate
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                editDate = cal.time
            },
            onDismiss = { showTimePicker = false }
        )
    }
}