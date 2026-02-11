package com.flores.agendapodologia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val db = Firebase.firestore
            val testData = hashMapOf(
                "mensaje" to "Hola Firebase desde la clínica",
                "fecha" to FieldValue.serverTimestamp()
            )

            db.collection("pruebas").add(testData)
                .addOnSuccessListener {
                    Log.d("FIREBASE", "¡Conexión exitosa! ID: ${it.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("FIREBASE", "Error de conexión", e)
                }
        }
    }
}
