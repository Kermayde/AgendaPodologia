package com.flores.agendapodologia.data.repository

import android.util.Log
import com.flores.agendapodologia.model.Patient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

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
}