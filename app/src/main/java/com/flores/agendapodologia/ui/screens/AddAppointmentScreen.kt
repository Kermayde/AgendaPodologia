package com.flores.agendapodologia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.flores.agendapodologia.model.Patient
import com.flores.agendapodologia.ui.components.DatePickerModal
import com.flores.agendapodologia.ui.components.PatientAutocomplete
import com.flores.agendapodologia.ui.components.TimePickerModal
import com.flores.agendapodologia.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    // ESTADOS DEL FORMULARIO
    val filteredPatients by viewModel.filteredPatients.collectAsState()

    // NUEVO ESTADO: ¿Estamos en modo edición de un paciente existente?
    var isEditingExisting by remember { mutableStateOf(false) }

    // Datos del Paciente
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var patientNameQuery by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Fecha y Hora
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableIntStateOf(10) } // 10 AM por defecto
    var selectedMinute by remember { mutableIntStateOf(0) }

    // Datos del Servicio
    var serviceType by remember { mutableStateOf("Corte General") }
    var podiatrist by remember { mutableStateOf("Carlos") } // Tú por defecto ;)

    // Lógica: Si seleccionamos un paciente, rellenamos datos
    LaunchedEffect(selectedPatient) {
        selectedPatient?.let {
            phone = it.phone
            patientNameQuery = it.name
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Cita") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Scroll por si el teclado tapa algo
        ) {
            // --- LÓGICA DE UI DEL PACIENTE ---

            Text("Datos del Paciente", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (selectedPatient == null) {
                // MODO 1: BUSCADOR / NUEVO REGISTRO

                // 1. El Buscador (Nombre)
                PatientAutocomplete(
                    patientsFound = filteredPatients,
                    onQueryChanged = { query ->
                        patientNameQuery = query
                        viewModel.searchPatient(query)
                        // Si borra el nombre, limpiamos el teléfono también para evitar datos huérfanos
                        if (query.isEmpty()) phone = ""
                    },
                    onPatientSelected = { patient ->
                        selectedPatient = patient
                        patientNameQuery = patient.name
                        phone = patient.phone
                        isEditingExisting = false
                        viewModel.searchPatient("")
                    },
                    onClearSelection = {
                        selectedPatient = null
                        phone = ""
                    }
                )

                // 2. Campo de Teléfono (SOLO aparece si escribió un nombre y NO seleccionó a nadie)
                // Usamos AnimatedVisibility para que se vea elegante al aparecer
                androidx.compose.animation.AnimatedVisibility(
                    visible = patientNameQuery.isNotEmpty()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono (Nuevo Paciente)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            // Teclado numérico
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                // Un color sutil para indicar que es un registro nuevo
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                focusedLabelColor = MaterialTheme.colorScheme.secondary
                            )
                        )

                        Text(
                            text = "Se registrará como paciente nuevo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }
            } else {
                // MODO 2: PACIENTE SELECCIONADO (VISTA BLOQUEADA / EDICIÓN)

                // Tarjeta que muestra que ya está vinculado
                OutlinedCard(
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isEditingExisting) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEditingExisting) "Editando Ficha Maestra" else "Paciente Vinculado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row {
                                // Botón EDITAR (Lápiz)
                                IconButton(onClick = { isEditingExisting = !isEditingExisting }) {
                                    Icon(
                                        imageVector = if (isEditingExisting) Icons.Default.Check else Icons.Default.Edit,
                                        contentDescription = "Editar datos del paciente"
                                    )
                                }
                                // Botón CERRAR (X) - Desvincular
                                IconButton(onClick = {
                                    selectedPatient = null
                                    patientNameQuery = ""
                                    phone = ""
                                    isEditingExisting = false
                                }) {
                                    Icon(Icons.Default.Clear, "Desvincular")
                                }
                            }
                        }

                        // Campo NOMBRE
                        OutlinedTextField(
                            value = patientNameQuery,
                            onValueChange = { patientNameQuery = it },
                            label = { Text("Nombre") },
                            enabled = isEditingExisting, // <--- AQUÍ ESTÁ LA MAGIA
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = Color.Transparent, // Que parezca solo texto cuando está bloqueado
                                disabledLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Campo TELÉFONO
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono") },
                            enabled = isEditingExisting, // <--- AQUÍ TAMBIÉN
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                if (isEditingExisting) {
                    Text(
                        text = "⚠️ Los cambios se guardarán en la ficha del paciente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. FECHA Y HORA
            Text("Fecha y Hora", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Selector de FECHA
                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDateMillis)),
                    onValueChange = {},
                    label = { Text("Fecha") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true },
                    enabled = false, // Deshabilitamos entrada de texto, pero el click funciona por el modifier
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Selector de HORA
                OutlinedTextField(
                    value = String.format("%02d:%02d", selectedHour, selectedMinute),
                    onValueChange = {},
                    label = { Text("Hora") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.Add, null) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTimePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. PODÓLOGO (Radio Buttons)
            Text("Asignar a", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = podiatrist == "Carlos", onClick = { podiatrist = "Carlos" })
                Text("Carlos", modifier = Modifier.padding(end = 16.dp))

                RadioButton(selected = podiatrist == "Karla", onClick = { podiatrist = "Karla" })
                Text("Dra. Karla")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÓN GUARDAR
            Button(
                onClick = {
                    // Combinar fecha y hora
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                    }

                    viewModel.scheduleAppointment(
                        patientName = patientNameQuery,
                        patientPhone = phone,
                        selectedPatient = selectedPatient,
                        date = calendar.timeInMillis,
                        service = serviceType,
                        podiatrist = podiatrist,
                        onSuccess = {
                            // Solo volvemos atrás si se guardó correctamente
                            onBack()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = patientNameQuery.isNotEmpty() && phone.isNotEmpty()
            ) {
                Text("Agendar Cita")
            }
        }
    }

    // DIÁLOGOS EMERGENTES
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { date ->
                if(date != null) selectedDateMillis = date
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerModal(
            onTimeSelected = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
            },
            onDismiss = { showTimePicker = false }
        )
    }
}