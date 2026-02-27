package com.flores.agendapodologia.data.repository

import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.ClinicSettings
import com.flores.agendapodologia.model.Patient
import com.flores.agendapodologia.model.PatientStatus
import com.flores.agendapodologia.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface AgendaRepository {
    // Usamos 'suspend' para operaciones de una sola vez (como guardar)
    suspend fun addPatient(patient: Patient): Result<Boolean>
    // Usamos 'Flow' para recibir datos en tiempo real (si tu madre agrega uno, te aparece a ti)
    fun getPatients(): Flow<List<Patient>>
    suspend fun updatePatient(patient: Patient): Result<Boolean>
    suspend fun scheduleAppointment(appointment: Appointment, patient: Patient): Result<Boolean>
    fun getAppointmentsForDate(date: Long): Flow<List<Appointment>>
    suspend fun getLastAppointments(patientId: String, currentAppointmentDate: Date): List<Appointment>
    suspend fun getUpcomingAppointments(patientId: String): List<Appointment>
    suspend fun updateAppointmentNotes(appointmentId: String, notes: String): Result<Boolean>
    suspend fun getAppointmentById(id: String): Appointment?
    suspend fun getPatientById(id: String): Patient?
    suspend fun deletePatientAndAppointments(patientId: String): Result<Boolean>
    suspend fun updatePatientStatus(patientId: String, status: PatientStatus): Result<Boolean>
    suspend fun updatePatientStatusWithReason(patientId: String, status: PatientStatus, blockReason: String): Result<Boolean>
    suspend fun updatePatientAndHistory(patient: Patient): Result<Boolean>
    suspend fun getLastPaidWarrantyAppointment(patientId: String): Appointment?
    suspend fun finishAppointment(appointmentId: String, isPaid: Boolean, paymentMethod: PaymentMethod, amountCharged: Double, usedWarranty: Boolean = false): Result<Boolean>
    suspend fun updateAppointment(appointment: Appointment): Result<Boolean>
    suspend fun deleteAppointment(appointmentId: String): Result<Boolean>
    suspend fun addAppointmentOnly(appointment: Appointment): Result<Boolean>
    fun getClinicSettings(): Flow<ClinicSettings>
    suspend fun saveClinicSettings(settings: ClinicSettings): Result<Boolean>
    fun getAppointmentsForTomorrow(): Flow<List<Appointment>>
    suspend fun markReminderSent(appointmentId: String): Result<Boolean>
}