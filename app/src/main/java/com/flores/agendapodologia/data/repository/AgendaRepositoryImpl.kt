package com.flores.agendapodologia.data.repository

import android.util.Log
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.Calendar

class AgendaRepositoryImpl(
    private val db: FirebaseFirestore
) : AgendaRepository {

    override suspend fun addPatient(patient: Patient): Result<Boolean> {
        return try {
            // Creamos un documento nuevo con ID automático
            val document = db.collection("patients").document()
            // Asignamos ese ID a nuestro objeto para tener referencia
            val patientWithId = patient.copy(id = document.id)

            // Guardamos en la nube
            document.set(patientWithId).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("REPO", "Error guardando paciente", e)
            Result.failure(e)
        }
    }

    override fun getPatients(): Flow<List<Patient>> = callbackFlow {
        // Nos suscribimos a la colección 'patients' ordenada por nombre
        val subscription = db.collection("patients")
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Si falla, cerramos el flujo
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Mapeamos los documentos de Firebase a nuestra clase Patient
                    val patients = snapshot.toObjects(Patient::class.java)
                    trySend(patients) // Enviamos la lista nueva a la UI
                }
            }

        // Importante: Esto se ejecuta cuando la UI deja de escuchar (para no gastar batería/datos)
        awaitClose { subscription.remove() }
    }

    override suspend fun updatePatient(patient: Patient): Result<Boolean> {
        return try {
            if (patient.id.isEmpty()) throw Exception("No se puede editar paciente sin ID")

            // set con SetOptions.merge() actualiza solo los campos cambiados, o .set normal sobrescribe todo el documento
            db.collection("patients").document(patient.id)
                .set(patient)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }
    override suspend fun scheduleAppointment(appointment: Appointment, patient: Patient): Result<Boolean> {
        return try {
            db.runTransaction { transaction ->
                // 1. Referencia del Paciente
                val patientRef = if (patient.id.isNotEmpty()) {
                    // Si ya tiene ID, apuntamos a ese documento
                    db.collection("patients").document(patient.id)
                } else {
                    // Si es nuevo, creamos una referencia nueva (el ID se genera aquí)
                    db.collection("patients").document()
                }

                // 2. Operación con el Paciente (Crear o Actualizar)
                // transaction.set maneja tanto creación como sobrescritura (update)
                // Al usar el mismo ID de referencia, si existía lo actualiza.
                // IMPORTANTE: Asignamos el ID generado al objeto paciente para guardarlo bien
                val finalPatient = patient.copy(id = patientRef.id)
                transaction.set(patientRef, finalPatient)

                // 3. Referencia de la Cita
                val appointmentRef = db.collection("appointments").document()

                // 4. Preparamos la Cita final
                // Vinculamos la cita con el ID real del paciente (sea nuevo o viejo)
                val finalAppointment = appointment.copy(
                    id = appointmentRef.id,
                    patientId = finalPatient.id,
                    patientName = finalPatient.name, // Desnormalización
                    patientPhone = finalPatient.phone
                )

                // 5. Guardar la Cita
                transaction.set(appointmentRef, finalAppointment)

            }.await()

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getAppointmentsForDate(date: Long): Flow<List<Appointment>> = callbackFlow {
        // 1. Calcular el rango del día (Start & End)
        val calendar = Calendar.getInstance().apply { timeInMillis = date }

        // Inicio del día (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        // Fin del día (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time

        // 2. Consulta a Firestore
        val subscription = db.collection("appointments")
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThanOrEqualTo("date", endOfDay)
            .orderBy("date", Query.Direction.ASCENDING) // Ordenar por hora (8am, 9am...)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val appointments = snapshot.toObjects(Appointment::class.java)
                    trySend(appointments)
                }
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun getPreviousAppointment(patientId: String, currentAppointmentDate: Date): Appointment? {
        return try {
            // Buscamos citas de este paciente, ANTES de la fecha actual, ordenadas por fecha descendente (la más reciente primero)
            val snapshot = db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .whereLessThan("date", currentAppointmentDate) // Clave: Solo anteriores a hoy
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1) // Solo queremos la inmediata anterior
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.documents[0].toObject(Appointment::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun updateAppointmentNotes(appointmentId: String, notes: String): Result<Boolean> {
        return try {
            db.collection("appointments").document(appointmentId)
                .update("notes", notes) // Solo actualizamos el campo notas
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}