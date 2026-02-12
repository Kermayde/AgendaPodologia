package com.flores.agendapodologia.data.repository

import com.flores.agendapodologia.model.Patient
import kotlinx.coroutines.flow.Flow

interface AgendaRepository {
    // Usamos 'suspend' para operaciones de una sola vez (como guardar)
    suspend fun addPatient(patient: Patient): Result<Boolean>

    // Usamos 'Flow' para recibir datos en tiempo real (si tu madre agrega uno, te aparece a ti)
    fun getPatients(): Flow<List<Patient>>

    suspend fun updatePatient(patient: Patient): Result<Boolean>
}