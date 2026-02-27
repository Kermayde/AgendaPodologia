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
import com.flores.agendapodologia.model.ReminderPreference
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

// ═════════════════════════════════════════════════════════════════
//  Eventos de UI — la UI los observa una sola vez (one-shot)
// ═════════════════════════════════════════════════════════════════

sealed interface UiEvent {
    data class ShowSuccess(val message: String) : UiEvent
    data class ShowError(val message: String) : UiEvent
}

// ═════════════════════════════════════════════════════════════════
//  HomeViewModel
// ═════════════════════════════════════════════════════════════════

class HomeViewModel(
    private val repository: AgendaRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val WARRANTY_DAYS = 15
        private const val REMINDER_MIN_DAYS_AHEAD = 4L
    }

    // ── Eventos UI (Snackbar / Toast) ──────────────────────────
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // ── Indicador de carga global ──────────────────────────────
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Pacientes
    // ═══════════════════════════════════════════════════════════

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _filteredPatients = MutableStateFlow<List<Patient>>(emptyList())
    val filteredPatients: StateFlow<List<Patient>> = _filteredPatients.asStateFlow()

    private val _currentPatient = MutableStateFlow<Patient?>(null)
    val currentPatient: StateFlow<Patient?> = _currentPatient.asStateFlow()

    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val upcomingAppointments: StateFlow<List<Appointment>> = _upcomingAppointments.asStateFlow()

    fun searchPatient(query: String) {
        _filteredPatients.value = if (query.isEmpty()) {
            emptyList()
        } else {
            _patients.value.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    fun saveNewPatient(
        name: String,
        phone: String,
        reminderPreference: ReminderPreference = ReminderPreference.WHATSAPP
    ) {
        launchWithLoading {
            val newPatient = Patient(name = name, phone = phone, reminderPreference = reminderPreference)
            repository.addPatient(newPatient)
                .onSuccess { _uiEvent.emit(UiEvent.ShowSuccess("Paciente guardado")) }
                .onFailure { emitError("Error al guardar paciente", it) }
        }
    }

    fun loadPatientDetail(patientId: String) {
        launchWithLoading {
            val patient = repository.getPatientById(patientId)
            _currentPatient.value = patient

            if (patient != null) {
                val history = repository.getLastAppointments(patientId, Date())
                _lastAppointments.value = history

                val upcoming = repository.getUpcomingAppointments(patientId)
                _upcomingAppointments.value = upcoming
            }
        }
    }

    fun togglePatientStatus(patient: Patient, blockReason: String = "") {
        val newStatus = if (patient.status == PatientStatus.BLOCKED) PatientStatus.ACTIVE else PatientStatus.BLOCKED
        val reason = if (newStatus == PatientStatus.BLOCKED) blockReason else ""

        launchWithLoading {
            repository.updatePatientStatusWithReason(patient.id, newStatus, reason)
                .onSuccess {
                    _currentPatient.value = patient.copy(status = newStatus, blockReason = reason)
                    val msg = if (newStatus == PatientStatus.BLOCKED) "Paciente bloqueado" else "Paciente desbloqueado"
                    _uiEvent.emit(UiEvent.ShowSuccess(msg))
                }
                .onFailure { emitError("Error al cambiar estado del paciente", it) }
        }
    }

    fun deleteCurrentPatient(onSuccess: () -> Unit) {
        val patient = _currentPatient.value ?: return
        launchWithLoading {
            repository.deletePatientAndAppointments(patient.id)
                .onSuccess {
                    _uiEvent.emit(UiEvent.ShowSuccess("Paciente eliminado"))
                    onSuccess()
                }
                .onFailure { emitError("Error al eliminar paciente", it) }
        }
    }

    fun updatePatient(patient: Patient, onSuccess: () -> Unit) {
        launchWithLoading {
            repository.updatePatientAndHistory(patient)
                .onSuccess {
                    _currentPatient.value = patient
                    _uiEvent.emit(UiEvent.ShowSuccess("Paciente actualizado"))
                    onSuccess()
                }
                .onFailure { emitError("Error al actualizar paciente", it) }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Calendario y fecha
    // ═══════════════════════════════════════════════════════════

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _displayedMonth = MutableStateFlow(
        Calendar.getInstance().let { Pair(it.get(Calendar.YEAR), it.get(Calendar.MONTH)) }
    )
    val displayedMonth: StateFlow<Pair<Int, Int>> = _displayedMonth.asStateFlow()

    private val _isCalendarExpanded = MutableStateFlow(false)
    val isCalendarExpanded: StateFlow<Boolean> = _isCalendarExpanded.asStateFlow()

    fun changeDate(newDate: Long) {
        _selectedDate.value = newDate

        val cal = Calendar.getInstance().apply { timeInMillis = newDate }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)

        val (currentYear, currentMonth) = _displayedMonth.value
        if (currentYear != year || currentMonth != month) {
            _displayedMonth.value = Pair(year, month)
        }

        loadAppointmentsForDate(newDate)
    }

    fun changeDisplayedMonth(year: Int, month: Int) {
        _displayedMonth.value = Pair(year, month)

        val firstDayMillis = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        changeDate(firstDayMillis)
    }

    fun updateDisplayedMonthFromWeek(year: Int, month: Int) {
        _displayedMonth.value = Pair(year, month)
    }

    fun moveMonth(forward: Boolean) {
        val (year, month) = _displayedMonth.value
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            add(Calendar.MONTH, if (forward) 1 else -1)
        }
        _displayedMonth.value = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun toggleCalendarExpanded() {
        _isCalendarExpanded.update { !it }
    }

    fun setCalendarExpanded(expanded: Boolean) {
        _isCalendarExpanded.value = expanded
    }

    fun goToToday() {
        val today = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        changeDate(today)
        _displayedMonth.value = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Citas del día
    // ═══════════════════════════════════════════════════════════

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private var appointmentsJob: Job? = null

    private fun loadAppointmentsForDate(date: Long) {
        appointmentsJob?.cancel()
        appointmentsJob = viewModelScope.launch {
            repository.getAppointmentsForDate(date).collect { list ->
                _appointments.value = list
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Detalle de cita
    // ═══════════════════════════════════════════════════════════

    private val _currentDetailAppointment = MutableStateFlow<Appointment?>(null)
    val currentDetailAppointment: StateFlow<Appointment?> = _currentDetailAppointment.asStateFlow()

    private val _lastAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val lastAppointments: StateFlow<List<Appointment>> = _lastAppointments.asStateFlow()

    fun loadAppointmentDetails(appointmentId: String) {
        launchWithLoading {
            _currentDetailAppointment.value = null
            _lastAppointments.value = emptyList()

            val appointment = repository.getAppointmentById(appointmentId)
            _currentDetailAppointment.value = appointment

            if (appointment != null) {
                val history = repository.getLastAppointments(appointment.patientId, appointment.date)
                _lastAppointments.value = history
                checkWarranty(appointment.patientId)
            }
        }
    }

    fun saveNotes(notes: String, onSuccess: () -> Unit) {
        val current = _currentDetailAppointment.value ?: return
        launchWithLoading {
            repository.updateAppointmentNotes(current.id, notes)
                .onSuccess {
                    _uiEvent.emit(UiEvent.ShowSuccess("Notas guardadas"))
                    onSuccess()
                }
                .onFailure { emitError("Error al guardar notas", it) }
        }
    }

    fun finishAppointment(
        isPaid: Boolean,
        paymentMethod: PaymentMethod,
        amountCharged: Double,
        usedWarranty: Boolean = false,
        onSuccess: () -> Unit
    ) {
        val currentAppt = _currentDetailAppointment.value ?: return

        launchWithLoading {
            repository.finishAppointment(currentAppt.id, isPaid, paymentMethod, amountCharged, usedWarranty)
                .onSuccess {
                    _currentDetailAppointment.value = currentAppt.copy(
                        status = AppointmentStatus.FINALIZADA,
                        isPaid = isPaid,
                        paymentMethod = paymentMethod,
                        amountCharged = amountCharged,
                        completedAt = Date(),
                        usedWarranty = usedWarranty
                    )
                    _uiEvent.emit(UiEvent.ShowSuccess("Cita finalizada"))
                    onSuccess()
                }
                .onFailure { emitError("Error al finalizar cita", it) }
        }
    }

    fun updateAppointment(appointment: Appointment, onSuccess: () -> Unit) {
        launchWithLoading {
            repository.updateAppointment(appointment)
                .onSuccess {
                    _currentDetailAppointment.value = appointment
                    _uiEvent.emit(UiEvent.ShowSuccess("Cita actualizada"))
                    onSuccess()
                }
                .onFailure { emitError("Error al actualizar cita", it) }
        }
    }

    fun deleteAppointment(appointmentId: String, onSuccess: () -> Unit) {
        launchWithLoading {
            repository.deleteAppointment(appointmentId)
                .onSuccess {
                    _uiEvent.emit(UiEvent.ShowSuccess("Cita eliminada"))
                    onSuccess()
                }
                .onFailure { emitError("Error al eliminar cita", it) }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Agendar citas y bloqueos
    // ═══════════════════════════════════════════════════════════

    private val _preselectedDate = MutableStateFlow<Long?>(null)
    val preselectedDate: StateFlow<Long?> = _preselectedDate.asStateFlow()

    private val _preselectedHour = MutableStateFlow<Int?>(null)
    val preselectedHour: StateFlow<Int?> = _preselectedHour.asStateFlow()

    fun setPreselectedTime(dateMillis: Long, hour: Int) {
        _preselectedDate.value = dateMillis
        _preselectedHour.value = hour
    }

    fun clearPreselectedTime() {
        _preselectedDate.value = null
        _preselectedHour.value = null
    }

    fun scheduleAppointment(
        patientName: String,
        patientPhone: String,
        selectedPatient: Patient?,
        date: Long,
        service: String,
        podiatrist: String,
        reminderPreference: ReminderPreference = ReminderPreference.WHATSAPP,
        onSuccess: () -> Unit
    ) {
        launchWithLoading {
            val finalName = patientName.trim()
            val finalPhone = patientPhone.trim()

            val patientToSave = resolvePatient(selectedPatient, finalName, finalPhone, reminderPreference)

            val appointment = Appointment(
                podiatristName = podiatrist,
                serviceType = service,
                date = Date(date),
                status = AppointmentStatus.PENDIENTE
            )

            repository.scheduleAppointment(appointment, patientToSave)
                .onSuccess {
                    _uiEvent.emit(UiEvent.ShowSuccess("Cita agendada correctamente"))
                    onSuccess()
                }
                .onFailure { emitError("Error al agendar cita", it) }
        }
    }

    fun scheduleBlock(
        date: Long,
        service: String,
        podiatrist: String,
        onSuccess: () -> Unit
    ) {
        launchWithLoading {
            val appointment = Appointment(
                patientId = "BLOQUEO_ID",
                patientName = "BLOQUEO / NO DISPONIBLE",
                patientPhone = "",
                podiatristName = podiatrist,
                serviceType = service,
                date = Date(date),
                status = AppointmentStatus.FINALIZADA,
                isPaid = false,
                amountCharged = 0.0,
                isBlockout = true
            )

            repository.addAppointmentOnly(appointment)
                .onSuccess {
                    _uiEvent.emit(UiEvent.ShowSuccess("Horario bloqueado"))
                    onSuccess()
                }
                .onFailure { emitError("Error al bloquear horario", it) }
        }
    }

    /** Resuelve el paciente final: crea uno nuevo o actualiza el existente si cambió. */
    private suspend fun resolvePatient(
        selectedPatient: Patient?,
        name: String,
        phone: String,
        reminderPreference: ReminderPreference
    ): Patient {
        if (selectedPatient == null) {
            return Patient(id = "", name = name, phone = phone, reminderPreference = reminderPreference)
        }

        val hasChanges = selectedPatient.name != name ||
                selectedPatient.phone != phone ||
                selectedPatient.reminderPreference != reminderPreference

        if (!hasChanges) return selectedPatient

        val updatedPatient = selectedPatient.copy(
            name = name,
            phone = phone,
            reminderPreference = reminderPreference
        )
        repository.updatePatientAndHistory(updatedPatient)
        return updatedPatient
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Directorio de pacientes
    // ═══════════════════════════════════════════════════════════

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val directoryList: StateFlow<List<Patient>> = _patients
        .combine(_searchQuery) { patients, query ->
            if (query.isBlank()) patients
            else patients.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Recordatorios
    // ═══════════════════════════════════════════════════════════

    private val _tomorrowAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    private val _markedReminderIds = MutableStateFlow<Set<String>>(emptySet())

    val pendingReminders: StateFlow<List<PendingReminder>> =
        combine(_tomorrowAppointments, _patients, _markedReminderIds) { appointments, patients, markedIds ->
            val patientMap = patients.associateBy { it.id }

            appointments
                .filter { appt -> shouldShowReminder(appt, markedIds, patientMap) }
                .map { appt ->
                    PendingReminder(
                        appointment = appt,
                        patientName = appt.patientName,
                        patientPhone = appt.patientPhone,
                        reminderPreference = patientMap[appt.patientId]?.reminderPreference
                            ?: ReminderPreference.WHATSAPP
                    )
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRemindersCount: StateFlow<Int> = pendingReminders
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun markReminderSent(appointmentId: String) {
        _markedReminderIds.update { it + appointmentId }
        viewModelScope.launch {
            repository.markReminderSent(appointmentId)
                .onFailure {
                    _markedReminderIds.update { ids -> ids - appointmentId }
                    emitError("Error al marcar recordatorio", it)
                }
        }
    }

    private fun shouldShowReminder(
        appt: Appointment,
        markedIds: Set<String>,
        patientMap: Map<String, Patient>
    ): Boolean {
        if (appt.isBlockout) return false
        if (appt.isReminderSent || appt.id in markedIds) return false
        if ((appt.date.time - appt.createdAt) < TimeUnit.DAYS.toMillis(REMINDER_MIN_DAYS_AHEAD)) return false

        val preference = patientMap[appt.patientId]?.reminderPreference ?: ReminderPreference.WHATSAPP
        return preference != ReminderPreference.NINGUNO
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Garantía
    // ═══════════════════════════════════════════════════════════

    private val _warrantyStatus = MutableStateFlow(WarrantyState())
    val warrantyStatus: StateFlow<WarrantyState> = _warrantyStatus.asStateFlow()

    fun checkWarranty(patientId: String) {
        viewModelScope.launch {
            val lastPaid = repository.getLastPaidWarrantyAppointment(patientId)

            _warrantyStatus.value = if (lastPaid != null) {
                calculateWarrantyState(lastPaid)
            } else {
                WarrantyState(isActive = false)
            }
        }
    }

    private fun calculateWarrantyState(lastPaid: Appointment): WarrantyState {
        val today = Date()
        val diffMillis = abs(today.time - lastPaid.date.time)
        val diffDays = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS)

        if (diffDays > WARRANTY_DAYS) return WarrantyState(isActive = false)

        val expiration = Calendar.getInstance().apply {
            time = lastPaid.date
            add(Calendar.DAY_OF_YEAR, WARRANTY_DAYS)
        }.time

        return WarrantyState(
            isActive = true,
            daysRemaining = WARRANTY_DAYS - diffDays,
            expirationDate = expiration,
            sourceAppointmentService = lastPaid.serviceType
        )
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Configuración de clínica
    // ═══════════════════════════════════════════════════════════

    private val _clinicSettings = MutableStateFlow(ClinicSettings())
    val clinicSettings: StateFlow<ClinicSettings> = _clinicSettings.asStateFlow()

    fun updateSettings(newSettings: ClinicSettings, onSuccess: () -> Unit) {
        launchWithLoading {
            repository.saveClinicSettings(newSettings)
                .onSuccess {
                    _uiEvent.emit(UiEvent.ShowSuccess("Configuración guardada"))
                    onSuccess()
                }
                .onFailure { emitError("Error al guardar configuración", it) }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Resumen diario (Caja)
    // ═══════════════════════════════════════════════════════════

    val dailySummary: StateFlow<DailySummary> = _appointments
        .map { appts -> buildDailySummary(appts) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary())

    private fun buildDailySummary(appts: List<Appointment>): DailySummary {
        val realAppointments = appts.filter { !it.isBlockout }

        val paidAppointments = realAppointments.filter {
            it.status == AppointmentStatus.FINALIZADA && it.isPaid
        }

        var total = 0.0
        var cash = 0.0
        var bank = 0.0

        paidAppointments.forEach { appt ->
            total += appt.amountCharged
            when (appt.paymentMethod) {
                PaymentMethod.EFECTIVO -> cash += appt.amountCharged
                PaymentMethod.TARJETA, PaymentMethod.TRANSFERENCIA -> bank += appt.amountCharged
                else -> { /* sin acción */ }
            }
        }

        return DailySummary(
            total = total,
            cash = cash,
            cardAndTransfer = bank,
            totalAppointments = realAppointments.size,
            finishedAppointments = realAppointments.count { it.status == AppointmentStatus.FINALIZADA },
            paidAppointments = paidAppointments.size,
            warrantyAppointments = realAppointments.count { it.status == AppointmentStatus.FINALIZADA && it.usedWarranty },
            cancelledAppointments = realAppointments.count { it.status == AppointmentStatus.CANCELADA },
            noShowAppointments = realAppointments.count { it.status == AppointmentStatus.NO_ASISTIO },
            pendingAppointments = realAppointments.count { it.status == AppointmentStatus.PENDIENTE }
        )
    }

    // ═══════════════════════════════════════════════════════════
    //  REGIÓN: Suscripciones y init
    // ═══════════════════════════════════════════════════════════

    init {
        subscribeToPatients()
        subscribeToTomorrowAppointments()
        subscribeToSettings()
        loadAppointmentsForDate(System.currentTimeMillis())
    }

    private fun subscribeToPatients() {
        viewModelScope.launch {
            repository.getPatients().collect { _patients.value = it }
        }
    }

    private fun subscribeToTomorrowAppointments() {
        viewModelScope.launch {
            repository.getAppointmentsForTomorrow().collect { list ->
                _tomorrowAppointments.value = list
                val confirmedIds = list.filter { it.isReminderSent }.map { it.id }.toSet()
                _markedReminderIds.update { ids -> ids - confirmedIds }
            }
        }
    }

    private fun subscribeToSettings() {
        viewModelScope.launch {
            repository.getClinicSettings().collect { _clinicSettings.value = it }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  Utilidades privadas
    // ═══════════════════════════════════════════════════════════

    /** Lanza una coroutine con indicador de carga automático. */
    private fun launchWithLoading(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            _isLoading.value = true
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado", e)
                _uiEvent.emit(UiEvent.ShowError("Ocurrió un error inesperado"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Registra y emite un error a la UI. */
    private suspend fun emitError(userMessage: String, throwable: Throwable) {
        Log.e(TAG, userMessage, throwable)
        _uiEvent.emit(UiEvent.ShowError(userMessage))
    }
}

// ═════════════════════════════════════════════════════════════════
//  Data classes de soporte
// ═════════════════════════════════════════════════════════════════

data class WarrantyState(
    val isActive: Boolean = false,
    val daysRemaining: Long = 0,
    val expirationDate: Date? = null,
    val sourceAppointmentService: String = ""
)

data class DailySummary(
    val total: Double = 0.0,
    val cash: Double = 0.0,
    val cardAndTransfer: Double = 0.0,
    val totalAppointments: Int = 0,
    val finishedAppointments: Int = 0,
    val paidAppointments: Int = 0,
    val warrantyAppointments: Int = 0,
    val cancelledAppointments: Int = 0,
    val noShowAppointments: Int = 0,
    val pendingAppointments: Int = 0
) {
    val totalFormatted: String get() = currencyFormatter.format(total)
    val cashFormatted: String get() = currencyFormatter.format(cash)
    val bankFormatted: String get() = currencyFormatter.format(cardAndTransfer)

    private companion object {
        val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance()
    }
}

data class PendingReminder(
    val appointment: Appointment,
    val patientName: String,
    val patientPhone: String,
    val reminderPreference: ReminderPreference
)

