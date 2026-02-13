package com.flores.agendapodologia.data.repository

import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.Patient
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

    suspend fun getPreviousAppointment(patientId: String, currentAppointmentDate: Date): Appointment?

    suspend fun updateAppointmentNotes(appointmentId: String, notes: String): Result<Boolean>
}