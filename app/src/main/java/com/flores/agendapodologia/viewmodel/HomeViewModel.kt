package com.flores.agendapodologia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flores.agendapodologia.data.repository.AgendaRepository
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    // Función para guardar Cita (y paciente si es nuevo)
    fun scheduleAppointment(
        patientName: String,
        patientPhone: String,
        selectedPatient: Patient?, // Pasamos el objeto entero, no solo el ID
        date: Long,
        service: String,
        podiatrist: String
    ) {
        viewModelScope.launch {
            var finalPatientId = selectedPatient?.id
            val finalPatientName = patientName.trim()

            if (finalPatientId == null) {
                // CASO 1: PACIENTE NUEVO
                val newPatient = Patient(name = finalPatientName, phone = patientPhone)
                // Nota: Aquí necesitaríamos refactorizar addPatient para que devuelva el ID generado
                // Por ahora asumimos que se crea.
                repository.addPatient(newPatient)
                // (En un sistema real, recuperarías el ID aquí)
            } else {
                // CASO 2: PACIENTE EXISTENTE
                // Verificamos si hubo cambios en sus datos
                if (selectedPatient.name != finalPatientName || selectedPatient.phone != patientPhone) {
                    // ¡El usuario editó los datos! Actualizamos la ficha maestra
                    val updatedPatient = selectedPatient.copy(
                        name = finalPatientName,
                        phone = patientPhone
                    )
                    repository.updatePatient(updatedPatient)
                }
            }

            // CASO 3: CREAR LA CITA
            val newAppointment = Appointment(
                patientId = finalPatientId ?: "temp_id", // Ojo con esto en prod
                patientName = finalPatientName, // Guardamos el nombre ACTUALIZADO en la cita
                podiatristName = podiatrist,
                serviceType = service,
                date = date,
                // ...
            )
            // repository.addAppointment(newAppointment)
        }
    }
}