package com.flores.agendapodologia.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flores.agendapodologia.data.repository.AgendaRepository
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Cita que estamos viendo actualmente en detalle
    private val _currentDetailAppointment = MutableStateFlow<Appointment?>(null)
    val currentDetailAppointment = _currentDetailAppointment.asStateFlow()

    // La cita histórica (anterior) para mostrar referencia
    private val _previousAppointment = MutableStateFlow<Appointment?>(null)
    val previousAppointment = _previousAppointment.asStateFlow()

    fun selectAppointment(appointment: Appointment) {
        _currentDetailAppointment.value = appointment
        // Al seleccionar, cargamos automáticamente su historial
        viewModelScope.launch {
            val prev = repository.getPreviousAppointment(appointment.patientId, appointment.date)
            _previousAppointment.value = prev
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
            // Preparamos el objeto Paciente (sea nuevo o el que venía seleccionado)
            // Si selectedPatient es null, creamos uno nuevo con ID vacío
            val patientToSave = selectedPatient?.copy(
                name = patientName,
                phone = patientPhone
            ) ?: Patient(
                id = "", // ID vacío indica al repo que es nuevo
                name = patientName,
                phone = patientPhone
            )

            // Preparamos el objeto Cita
            val appointment = Appointment(
                podiatristName = podiatrist,
                serviceType = service,
                date = Date(date), // Convertimos Long a Date aquí
                status = "PENDIENTE"
            )

            repository.scheduleAppointment(appointment, patientToSave)
                .onSuccess {
                    onSuccess() // ¡Éxito! Cerramos la pantalla
                }
                .onFailure {
                    // Aquí podrías poner un estado de error para mostrar un SnackBar
                    Log.e("ViewModel", "Error al agendar", it)
                }
        }
    }
}