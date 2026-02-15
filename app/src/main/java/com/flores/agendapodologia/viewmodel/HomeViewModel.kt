package com.flores.agendapodologia.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flores.agendapodologia.data.repository.AgendaRepository
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.Patient
import com.flores.agendapodologia.model.PatientStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class HomeViewModel(
    private val repository: AgendaRepository
) : ViewModel() {

    // Estado de la lista de pacientes (Tu UI observará esto)
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    init {
        // Al iniciar, empezamos a escuchar cambios en tiempo real
        subscribeToPatients()
    }

    private fun subscribeToPatients() {
        viewModelScope.launch {
            repository.getPatients().collect { list ->
                _patients.value = list
            }
        }
    }

    fun saveNewPatient(name: String, phone: String) {
        viewModelScope.launch {
            val newPatient = Patient(name = name, phone = phone)
            repository.addPatient(newPatient)
                .onSuccess {
                    // Podrías mostrar un Toast aquí o limpiar el formulario
                }
                .onFailure {
                    // Manejar error
                }
        }
    }
    // Estado para el autocompletado
    private val _filteredPatients = MutableStateFlow<List<Patient>>(emptyList())
    val filteredPatients = _filteredPatients.asStateFlow()

    fun searchPatient(query: String) {
        if (query.isEmpty()) {
            _filteredPatients.value = emptyList()
        } else {
            // Filtramos la lista LOCAL que ya tenemos descargada (es más rápido que preguntar a Firebase cada letra)
            _filteredPatients.value = _patients.value.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    // Estado para la fecha seleccionada en el calendario (Por defecto: Hoy)
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate = _selectedDate.asStateFlow()

    // Estado para las citas de ESE día
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments = _appointments.asStateFlow()

    init {
        // Al iniciar, cargamos las citas de hoy
        loadAppointmentsForDate(System.currentTimeMillis())
    }

    fun changeDate(newDate: Long) {
        _selectedDate.value = newDate
        loadAppointmentsForDate(newDate)
    }

    private fun loadAppointmentsForDate(date: Long) {
        viewModelScope.launch {
            // Cancelamos la suscripción anterior automáticamente al llamar a collect de nuevo
            repository.getAppointmentsForDate(date).collect { list ->
                _appointments.value = list
            }
        }
    }

    // ...
    fun loadAppointmentDetails(appointmentId: String) {
        viewModelScope.launch {
            // 1. Buscamos la cita por ID
            val appointment = repository.getAppointmentById(appointmentId)
            _currentDetailAppointment.value = appointment

            // 2. Si la encontramos, cargamos su historial (igual que antes)
            if (appointment != null) {
                val history = repository.getLastAppointments(appointment.patientId, appointment.date)
                _lastAppointments.value = history
            }
        }
    }

    // Cita que estamos viendo actualmente en detalle
    private val _currentDetailAppointment = MutableStateFlow<Appointment?>(null)
    val currentDetailAppointment = _currentDetailAppointment.asStateFlow()

    // La cita histórica (anterior) para mostrar referencia
    private val _lastAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val lastAppointments = _lastAppointments.asStateFlow()

    fun selectAppointment(appointment: Appointment) {
        _currentDetailAppointment.value = appointment
        viewModelScope.launch {
            // Llamamos a la nueva función
            val history = repository.getLastAppointments(appointment.patientId, appointment.date)
            _lastAppointments.value = history
        }
    }

    fun saveNotes(notes: String, onSuccess: () -> Unit) {
        val current = _currentDetailAppointment.value ?: return
        viewModelScope.launch {
            repository.updateAppointmentNotes(current.id, notes)
                .onSuccess { onSuccess() }
        }
    }

    // Función para guardar Cita (y paciente si es nuevo)
    fun scheduleAppointment(
        patientName: String,
        patientPhone: String,
        selectedPatient: Patient?,
        date: Long, // Recibimos Long del DatePicker
        service: String,
        podiatrist: String,
        onSuccess: () -> Unit // Callback para avisar a la UI que terminó
    ) {
        viewModelScope.launch {
            val finalName = patientName.trim()
            val finalPhone = patientPhone.trim()

            // Variable para el paciente que se enviará a la transacción de la cita
            var patientToSave: Patient

            if (selectedPatient == null) {
                // CASO 1: PACIENTE NUEVO
                // Creamos objeto con ID vacío para que el Repo sepa que debe crearlo
                patientToSave = Patient(
                    id = "",
                    name = finalName,
                    phone = finalPhone
                )
            } else {
                // CASO 2: PACIENTE EXISTENTE
                // Verificamos si cambió algún dato
                if (selectedPatient.name != finalName || selectedPatient.phone != finalPhone) {

                    // Preparamos el objeto actualizado
                    val updatedPatient = selectedPatient.copy(
                        name = finalName,
                        phone = finalPhone
                    )

                    // ¡AQUÍ ESTÁ EL AJUSTE!
                    // Primero ejecutamos la actualización en cascada para corregir el historial antiguo
                    repository.updatePatientAndHistory(updatedPatient)

                    // Usamos el paciente actualizado para la nueva cita
                    patientToSave = updatedPatient
                } else {
                    // No hubo cambios, usamos el original
                    patientToSave = selectedPatient
                }
            }

            // CASO 3: CREAR LA NUEVA CITA
            // (Esto usa la transacción que ya teníamos para asegurar que se guarde Cita + Paciente)
            val appointment = Appointment(
                podiatristName = podiatrist,
                serviceType = service,
                date = Date(date),
                status = "PENDIENTE"
            )

            repository.scheduleAppointment(appointment, patientToSave)
                .onSuccess {
                    onSuccess() // Cierra la pantalla
                }
                .onFailure {
                    // Aquí podrías manejar el error
                    it.printStackTrace()
                }
        }
    }

    // --- ESTADOS PARA DIRECTORIO ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Lista filtrada derivada de la lista original (_patients) y el query
    // Usamos combine para reaccionar a cambios en cualquiera de los dos
    val directoryList = _patients.combine(_searchQuery) { patients, query ->
        if (query.isBlank()) {
            patients
        } else {
            patients.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Paciente seleccionado para ver detalle
    private val _currentPatient = MutableStateFlow<Patient?>(null)
    val currentPatient = _currentPatient.asStateFlow()

    // Citas históricas de ESE paciente (reutilizamos la lógica del historial)
    // Usamos la misma variable _lastAppointments o creamos una _patientHistory específica si quieres cargar TODAS

    // --- FUNCIONES ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun loadPatientDetail(patientId: String) {
        viewModelScope.launch {
            val patient = repository.getPatientById(patientId)
            _currentPatient.value = patient
            // Cargar sus últimas 20 citas, por ejemplo
            if (patient != null) {
                // Aquí podrías crear un método en repo que traiga TODAS las citas, no solo 3
                val history = repository.getLastAppointments(patientId, Date()) // Reutilizando la de 3 por ahora
                _lastAppointments.value = history
            }
        }
    }

    fun togglePatientStatus(patient: Patient) {
        val newStatus = if (patient.status == PatientStatus.BLOCKED) PatientStatus.ACTIVE else PatientStatus.BLOCKED
        viewModelScope.launch {
            repository.updatePatientStatus(patient.id, newStatus)
                .onSuccess {
                    // Actualizamos el estado local para que la UI cambie rápido
                    _currentPatient.value = patient.copy(status = newStatus)
                }
        }
    }

    fun deleteCurrentPatient(onSuccess: () -> Unit) {
        val patient = _currentPatient.value ?: return
        viewModelScope.launch {
            repository.deletePatientAndAppointments(patient.id)
                .onSuccess { onSuccess() }
        }
    }

    fun updatePatient(patient: Patient, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updatePatientAndHistory(patient)
                .onSuccess {
                    // Actualizamos el estado local para reflejar cambios inmediatos
                    _currentPatient.value = patient
                    onSuccess()
                }
                .onFailure {
                    // Manejar error (Toast o Log)
                }
        }
    }
}