package com.flores.agendapodologia.data.repository

import android.util.Log
import com.flores.agendapodologia.model.Appointment
import com.flores.agendapodologia.model.AppointmentStatus
import com.flores.agendapodologia.model.ClinicSettings
import com.flores.agendapodologia.model.Patient
import com.flores.agendapodologia.model.PatientStatus
import com.flores.agendapodologia.model.PaymentMethod
import com.flores.agendapodologia.util.ServiceConstants
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

    override suspend fun getLastAppointments(patientId: String, currentAppointmentDate: Date): List<Appointment> {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .whereLessThan("date", currentAppointmentDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(3) // <--- AQUI CAMBIAMOS A 3
                .get()
                .await()

            if (!snapshot.isEmpty) {
                snapshot.toObjects(Appointment::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getUpcomingAppointments(patientId: String): List<Appointment> {
        return try {
            val now = Date()
            val snapshot = db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .whereGreaterThanOrEqualTo("date", now)
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(10)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                // Filtramos en memoria para evitar necesitar un índice compuesto extra
                snapshot.toObjects(Appointment::class.java)
                    .filter { it.status == AppointmentStatus.PENDIENTE }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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

    override suspend fun getAppointmentById(id: String): Appointment? {
        return try {
            val doc = db.collection("appointments").document(id).get().await()
            doc.toObject(Appointment::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Metodos para eliminar pacientes y citas:
    override suspend fun getPatientById(id: String): Patient? {
        return try {
            db.collection("patients").document(id).get().await().toObject(Patient::class.java)
        } catch (e: Exception) { null }
    }

    override suspend fun updatePatientStatus(patientId: String, status: PatientStatus): Result<Boolean> {
        return try {
            db.collection("patients").document(patientId).update("status", status).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updatePatientStatusWithReason(patientId: String, status: PatientStatus, blockReason: String): Result<Boolean> {
        return try {
            db.collection("patients").document(patientId).update(
                mapOf(
                    "status" to status,
                    "blockReason" to blockReason
                )
            ).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun deletePatientAndAppointments(patientId: String): Result<Boolean> {
        return try {
            val batch = db.batch()

            // 1. Buscar todas las citas del paciente
            val appointmentsSnapshot = db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .get()
                .await()

            // 2. Agregar eliminación de cada cita al lote (Batch)
            for (document in appointmentsSnapshot.documents) {
                batch.delete(document.reference)
            }

            // 3. Agregar eliminación del paciente al lote
            val patientRef = db.collection("patients").document(patientId)
            batch.delete(patientRef)

            // 4. Ejecutar todo junto
            batch.commit().await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePatientAndHistory(patient: Patient): Result<Boolean> {
        return try {
            db.runTransaction { transaction ->
                // 1. Referencia al Paciente
                val patientRef = db.collection("patients").document(patient.id)

                // 2. Leemos el dato actual para ver si cambió el nombre
                // (Importante para decidir si gastamos recursos actualizando citas)
                val snapshot = transaction.get(patientRef)
                val oldName = snapshot.getString("name") ?: ""

                // 3. Actualizamos al Paciente
                transaction.set(patientRef, patient)

                // 4. Si el nombre cambió, actualizamos TODAS sus citas (Lógica pesada pero necesaria)
                // Nota: En una transacción de Firestore, las lecturas deben ir antes que las escrituras.
                // PERO, Firestore no permite Querys dentro de transacciones fácilmente si no son por ID.
                // ESTRATEGIA: Para no complicar la transacción con queries masivos,
                // haremos esto en dos pasos: Primero el paciente, y luego un Batch para las citas.
                // Para fines de esta app (tráfico bajo), es seguro hacerlo fuera de la transacción principal
                // o usar un BatchWrite aparte.

            }.await()

            // PASO 2: Actualización en Cascada (Fuera de la transacción del documento único)
            // Esto corre después de que se aseguró la actualización del paciente.
            val batch = db.batch()

            val appointmentsSnapshot = db.collection("appointments")
                .whereEqualTo("patientId", patient.id)
                .get()
                .await()

            var needsBatchCommit = false

            for (doc in appointmentsSnapshot.documents) {
                // Solo actualizamos si el nombre o teléfono en la cita son diferentes a los nuevos
                val docName = doc.getString("patientName")
                val docPhone = doc.getString("patientPhone")

                if (docName != patient.name || docPhone != patient.phone) {
                    batch.update(doc.reference, mapOf(
                        "patientName" to patient.name,
                        "patientPhone" to patient.phone
                    ))
                    needsBatchCommit = true
                }
            }

            if (needsBatchCommit) {
                batch.commit().await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Busca la última cita PAGADA que sea válida para garantía (Quiropodia o Corrección)
//    override suspend fun getLastPaidWarrantyAppointment(patientId: String): Appointment? {
//        return try {
//            val snapshot = db.collection("appointments")
//                .whereEqualTo("patientId", patientId)
//                .whereEqualTo("isPaid", true) // Solo nos interesan las que pagó
//                .whereIn("serviceType", ServiceConstants.WARRANTY_TRIGGER_SERVICES) // Quiropodia o Correcciones
//                .orderBy("date", Query.Direction.DESCENDING) // La más reciente
//                .limit(1)
//                .get()
//                .await()
//
//            if (!snapshot.isEmpty) {
//                snapshot.documents[0].toObject(Appointment::class.java)
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//    override suspend fun getLastPaidWarrantyAppointment(patientId: String): Appointment? {
//        return try {
//            // 1. Traemos TODAS las citas pagadas de este paciente, ordenadas por fecha
//            val snapshot = db.collection("appointments")
//                .whereEqualTo("patientId", patientId)
//                .whereEqualTo("isPaid", true)
//                .orderBy("date", Query.Direction.DESCENDING)
//                .get()
//                .await()
//
//            if (!snapshot.isEmpty) {
//                // 2. Filtramos en MEMORIA (Kotlin)
//                // Buscamos la primera que sea Quiropodia o Correcciones
//                val appointments = snapshot.toObjects(Appointment::class.java)
//
//                appointments.firstOrNull { appointment ->
//                    appointment.serviceType in ServiceConstants.WARRANTY_TRIGGER_SERVICES
//                }
//            } else {
//                null
//            }
//        } catch (e: Exception) {
//            Log.e("REPO", "Error buscando garantía: ${e.message}") // Agrega Log para ver errores
//            e.printStackTrace()
//            null
//        }
//    }

    override suspend fun getLastPaidWarrantyAppointment(patientId: String): Appointment? {
        return try {
            val snapshot = db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .whereEqualTo("paid", true) // <--- CAMBIO: "paid" en lugar de "isPaid"
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val appointments = snapshot.toObjects(Appointment::class.java)

                // Filtramos en memoria (Quiropodia o Correcciones)
                appointments.firstOrNull { appointment ->
                    appointment.serviceType in ServiceConstants.WARRANTY_TRIGGER_SERVICES
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // En la Implementación (AgendaRepositoryImpl.kt):
    override suspend fun finishAppointment(
        appointmentId: String,
        isPaid: Boolean,
        paymentMethod: PaymentMethod,
        amountCharged: Double,
        usedWarranty: Boolean
    ): Result<Boolean> {
        return try {
            val updates = mapOf(
                "status" to AppointmentStatus.FINALIZADA,
                "paid" to isPaid,
                "paymentMethod" to paymentMethod,
                "amountCharged" to amountCharged,
                "completedAt" to java.util.Date(),
                // Guardamos si se usó la garantía en esta cita
                "usedWarranty" to usedWarranty
            )

            db.collection("appointments").document(appointmentId)
                .update(updates)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar cualquier dato de la cita
    override suspend fun updateAppointment(appointment: Appointment): Result<Boolean> {
        return try {
            db.collection("appointments").document(appointment.id)
                .set(appointment) // .set sobrescribe el documento con los nuevos datos
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar cita
    override suspend fun deleteAppointment(appointmentId: String): Result<Boolean> {
        return try {
            db.collection("appointments").document(appointmentId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // En la Implementación (AgendaRepositoryImpl.kt):
    override suspend fun addAppointmentOnly(appointment: Appointment): Result<Boolean> {
        return try {
            val docRef = db.collection("appointments").document()
            // Le asignamos el ID generado por Firebase
            val apptToSave = appointment.copy(id = docRef.id)
            docRef.set(apptToSave).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getClinicSettings(): Flow<ClinicSettings> = callbackFlow {
        val listener = db.collection("settings").document("working_hours")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("REPO_SETTINGS", "Error obteniendo configuraciones: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Convertir el documento a ClinicSettings usando mapper personalizado
                    try {
                        val rawData = snapshot.data ?: emptyMap()
                        Log.d("REPO_SETTINGS", "Datos raw de Firebase: $rawData")
                        val settings = mapDocumentToClinicSettings(rawData)
                        Log.d("REPO_SETTINGS", "Settings mapeadas: sunday.isOpen=${settings.sunday.isOpen}, monday.isOpen=${settings.monday.isOpen}")
                        trySend(settings)
                    } catch (e: Exception) {
                        Log.e("REPO_SETTINGS", "Error mapeando configuraciones", e)
                        trySend(ClinicSettings())
                    }
                } else {
                    // Si no existe (primera vez que abres la app), mandamos los valores por defecto
                    Log.d("REPO_SETTINGS", "Documento de settings no existe, usando valores por defecto")
                    trySend(ClinicSettings())
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun saveClinicSettings(settings: ClinicSettings): Result<Boolean> {
        return try {
            Log.d("REPO_SETTINGS", "Guardando settings: sunday.isOpen=${settings.sunday.isOpen}, monday.isOpen=${settings.monday.isOpen}")
            val data = mapClinicSettingsToDocument(settings)
            Log.d("REPO_SETTINGS", "Datos para guardar en Firebase: $data")
            db.collection("settings").document("working_hours")
                .set(data)
                .await()
            Log.d("REPO_SETTINGS", "Settings guardadas exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("REPO_SETTINGS", "Error guardando settings", e)
            Result.failure(e)
        }
    }

    override fun getAppointmentsForTomorrow(): Flow<List<Appointment>> = callbackFlow {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)

        // Inicio de mañana (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfTomorrow = calendar.time

        // Fin de mañana (23:59:59.999)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfTomorrow = calendar.time

        val subscription = db.collection("appointments")
            .whereGreaterThanOrEqualTo("date", startOfTomorrow)
            .whereLessThanOrEqualTo("date", endOfTomorrow)
            .orderBy("date", Query.Direction.ASCENDING)
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

    override suspend fun markReminderSent(appointmentId: String): Result<Boolean> {
        return try {
            db.collection("appointments").document(appointmentId)
                .update("isReminderSent", true)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mappers personalizados para ClinicSettings
    private fun mapClinicSettingsToDocument(settings: ClinicSettings): Map<String, Any> {
        return mapOf(
            "monday" to mapDayScheduleToMap(settings.monday),
            "tuesday" to mapDayScheduleToMap(settings.tuesday),
            "wednesday" to mapDayScheduleToMap(settings.wednesday),
            "thursday" to mapDayScheduleToMap(settings.thursday),
            "friday" to mapDayScheduleToMap(settings.friday),
            "saturday" to mapDayScheduleToMap(settings.saturday),
            "sunday" to mapDayScheduleToMap(settings.sunday)
        )
    }

    private fun mapDayScheduleToMap(day: com.flores.agendapodologia.model.DaySchedule): Map<String, Any> {
        return mapOf(
            "isOpen" to day.isOpen,
            "shift1Start" to day.shift1Start,
            "shift1End" to day.shift1End,
            "hasShift2" to day.hasShift2,
            "shift2Start" to day.shift2Start,
            "shift2End" to day.shift2End
        )
    }

    private fun mapDocumentToClinicSettings(data: Map<String, Any>): ClinicSettings {
        return ClinicSettings(
            monday = mapToDaySchedule(data["monday"]),
            tuesday = mapToDaySchedule(data["tuesday"]),
            wednesday = mapToDaySchedule(data["wednesday"]),
            thursday = mapToDaySchedule(data["thursday"]),
            friday = mapToDaySchedule(data["friday"]),
            saturday = mapToDaySchedule(data["saturday"]),
            sunday = mapToDaySchedule(data["sunday"])
        )
    }

    private fun mapToDaySchedule(value: Any?): com.flores.agendapodologia.model.DaySchedule {
        if (value !is Map<*, *>) {
            return com.flores.agendapodologia.model.DaySchedule()
        }

        @Suppress("UNCHECKED_CAST")
        val map = value as Map<String, Any>

        return com.flores.agendapodologia.model.DaySchedule(
            isOpen = (map["isOpen"] as? Boolean) ?: true,
            shift1Start = (map["shift1Start"] as? Number)?.toInt() ?: 10,
            shift1End = (map["shift1End"] as? Number)?.toInt() ?: 14,
            hasShift2 = (map["hasShift2"] as? Boolean) ?: true,
            shift2Start = (map["shift2Start"] as? Number)?.toInt() ?: 16,
            shift2End = (map["shift2End"] as? Number)?.toInt() ?: 20
        )
    }
}