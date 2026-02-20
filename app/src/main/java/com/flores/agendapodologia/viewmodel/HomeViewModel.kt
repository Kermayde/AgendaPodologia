package com.flores.agendapodologia.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flores.agendapodologia.data.repository.AgendaRepository
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.AppointmentStatus
import com.flores.agendapodologia.model.ClinicSettings
import com.flores.agendapodologia.model.Patient
import com.flores.agendapodologia.model.PatientStatus
import com.flores.agendapodologia.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
            //_isLoading.value = true // Opcional, si tienes estado de carga

            // 1. Buscamos la cita fresca por ID
            val appointment = repository.getAppointmentById(appointmentId)
            _currentDetailAppointment.value = appointment

            if (appointment != null) {
                // 2. Cargamos su historial
                val history = repository.getLastAppointments(appointment.patientId, appointment.date)
                _lastAppointments.value = history

                // 3. ¡IMPORTANTE! Verificamos la garantía (Esto estaba en la función muerta)
                checkWarranty(appointment.patientId)
            }
            //_isLoading.value = false
        }
    }

    // Cita que estamos viendo actualmente en detalle
    private val _currentDetailAppointment = MutableStateFlow<Appointment?>(null)
    val currentDetailAppointment = _currentDetailAppointment.asStateFlow()

    // La cita histórica (anterior) para mostrar referencia
    private val _lastAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val lastAppointments = _lastAppointments.asStateFlow()

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
                status = AppointmentStatus.PENDIENTE
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

    // --- ESTADOS PARA PRE-LLENADO DE NUEVA CITA ---
    private val _preselectedDate = MutableStateFlow<Long?>(null)
    val preselectedDate = _preselectedDate.asStateFlow()

    private val _preselectedHour = MutableStateFlow<Int?>(null)
    val preselectedHour = _preselectedHour.asStateFlow()

    fun setPreselectedTime(dateMillis: Long, hour: Int) {
        _preselectedDate.value = dateMillis
        _preselectedHour.value = hour
    }

    fun clearPreselectedTime() {
        _preselectedDate.value = null
        _preselectedHour.value = null
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

    // Estado de la garantía del paciente actual
    private val _warrantyStatus = MutableStateFlow(WarrantyState())
    val warrantyStatus = _warrantyStatus.asStateFlow()

    // Función para calcular si tiene garantía vigente
    fun checkWarranty(patientId: String) {
        viewModelScope.launch {
            // 1. Buscamos la última cita PAGADA que genera garantía
            val lastPaid = repository.getLastPaidWarrantyAppointment(patientId)

            if (lastPaid != null) {
                val today = Date()
                val diffInMillies = abs(today.time - lastPaid.date.time)
                val diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)

                // La regla de los 15 días
                if (diffInDays <= 15) {
                    val expiration = Calendar.getInstance().apply {
                        time = lastPaid.date
                        add(Calendar.DAY_OF_YEAR, 15)
                    }.time

                    _warrantyStatus.value = WarrantyState(
                        isActive = true,
                        daysRemaining = 15 - diffInDays,
                        expirationDate = expiration,
                        sourceAppointmentService = lastPaid.serviceType
                    )
                } else {
                    // Ya pasaron más de 15 días
                    _warrantyStatus.value = WarrantyState(isActive = false)
                }
            } else {
                // Nunca ha pagado o es nuevo
                _warrantyStatus.value = WarrantyState(isActive = false)
            }
        }
    }

    // Estado para la configuración de la clínica
    private val _clinicSettings = MutableStateFlow(ClinicSettings())
    val clinicSettings = _clinicSettings.asStateFlow()

    init {
        // En tu bloque init existente, agrega esta llamada:
        subscribeToSettings()
        // subscribeToPatients() (esto ya lo tenías)
        // loadAppointmentsForDate(...) (esto ya lo tenías)
    }

    private fun subscribeToSettings() {
        viewModelScope.launch {
            repository.getClinicSettings().collect { settings ->
                _clinicSettings.value = settings
            }
        }
    }

    // Función para cuando hagamos la pantalla de guardar cambios
    fun updateSettings(newSettings: ClinicSettings, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveClinicSettings(newSettings)
                .onSuccess { onSuccess() }
        }
    }

    fun finishAppointment(
        isPaid: Boolean,
        paymentMethod: PaymentMethod,
        amountCharged: Double, // <--- NUEVO PARÁMETRO
        usedWarranty: Boolean = false,
        onSuccess: () -> Unit
    ) {
        val currentAppt = _currentDetailAppointment.value ?: return

        viewModelScope.launch {
            repository.finishAppointment(currentAppt.id, isPaid, paymentMethod, amountCharged, usedWarranty)
                .onSuccess {
                    // Actualizamos el estado local
                    _currentDetailAppointment.value = currentAppt.copy(
                        status = AppointmentStatus.FINALIZADA,
                        isPaid = isPaid,
                        paymentMethod = paymentMethod,
                        amountCharged = amountCharged, // <--- ACTUALIZAMOS LOCALMENTE
                        completedAt = java.util.Date()
                        ,
                        usedWarranty = usedWarranty
                    )
                    onSuccess()
                }
        }
    }

    fun updateAppointment(appointment: Appointment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateAppointment(appointment)
                .onSuccess {
                    _currentDetailAppointment.value = appointment // Actualizamos la vista local
                    onSuccess()
                }
        }
    }

    fun deleteAppointment(appointmentId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAppointment(appointmentId)
                .onSuccess { onSuccess() }
        }
    }

    fun scheduleBlock(
        date: Long,
        service: String,
        podiatrist: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val appointment = Appointment(
                patientId = "BLOQUEO_ID", // Un ID falso para que no busque un paciente real
                patientName = "BLOQUEO / NO DISPONIBLE",
                patientPhone = "",
                podiatristName = podiatrist,
                serviceType = service,
                date = Date(date),
                status = AppointmentStatus.FINALIZADA, // Lo marcamos finalizado para que no pida cobrarlo
                isPaid = false,
                amountCharged = 0.0,
                isBlockout = true  // Marca explícitamente como bloqueo personal
            )

            repository.addAppointmentOnly(appointment)
                .onSuccess { onSuccess() }
        }
    }

    // 2. DENTRO DE LA CLASE HomeViewModel:
    // Creamos un Flow que "observa" a las citas y calcula el resumen automáticamente
    val dailySummary: StateFlow<DailySummary> = _appointments.map { appts ->
        var total = 0.0
        var cash = 0.0
        var bank = 0.0

        // Filtramos SOLO las citas terminadas y pagadas
        val paidAppointments = appts.filter {
            it.status == AppointmentStatus.FINALIZADA && it.isPaid
        }

        paidAppointments.forEach { appt ->
            total += appt.amountCharged
            when (appt.paymentMethod) {
                PaymentMethod.EFECTIVO -> cash += appt.amountCharged
                PaymentMethod.TARJETA, PaymentMethod.TRANSFERENCIA -> bank += appt.amountCharged
                else -> {}
            }
        }

        DailySummary(total, cash, bank)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary())
}

data class WarrantyState(
    val isActive: Boolean = false,
    val daysRemaining: Long = 0,
    val expirationDate: Date? = null,
    val sourceAppointmentService: String = "" // ¿Qué cita activó la garantía? (Quiropodia/Corrección)
)

// 1. DATA CLASS PARA EL RESUMEN (Ponlo al final del archivo)
data class DailySummary(
    val total: Double = 0.0,
    val cash: Double = 0.0,
    val cardAndTransfer: Double = 0.0 // Agrupamos tarjeta y transferencia para simplificar
) {
    // Helpers para formatear a moneda ($1,500.00)
    val totalFormatted: String get() = NumberFormat.getCurrencyInstance().format(total)
    val cashFormatted: String get() = NumberFormat.getCurrencyInstance().format(cash)
    val bankFormatted: String get() = NumberFormat.getCurrencyInstance().format(cardAndTransfer)
}
