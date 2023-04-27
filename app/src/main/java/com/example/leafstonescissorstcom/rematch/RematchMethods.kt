package com.example.leafstonescissorstcom.rematch

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.leafstonescissorstcom.R
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class RematchMethods {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var docRef: DocumentReference
    private lateinit var registerListener: ListenerRegistration
    private var roomData = hashMapOf("player1Rematch" to "false", "player2Rematch" to "false")

    fun createRematchFieldsInFirebase(roomId: String){
        docRef = db.collection("rooms").document(roomId)

        val fields: HashMap<String, Any> = HashMap()
        fields.put("player1Rematch", "false")
        fields.put("player2Rematch", "false")

        docRef.set(fields, SetOptions.merge())
            .addOnSuccessListener {
                Log.i("TAG", "Seting user1 request field succeded")
            }
            .addOnFailureListener {
                Log.i("TAG", "Seting user1 request field failed")
            }
    }

    lateinit var statusRequestPLayer1: String
    lateinit var statusRequestPLayer2: String

    fun setListenerToFields(player: Int, roomId: String, context: Context){
        docRef = db.collection("rooms").document(roomId)
        registerListener = docRef.addSnapshotListener { value, _ ->
            statusRequestPLayer1 = value?.getString("player1Rematch")!!
            statusRequestPLayer2 = value.getString("player2Rematch")!!
            if(player == 1 && statusRequestPLayer2.toBoolean()){
                showDialog(context)
            }else if(player == 2 && statusRequestPLayer1.toBoolean()){
                showDialog(context)
            }
        }
    }

    fun sendRematch(player: Int, roomId: String){
        docRef = db.collection("rooms").document(roomId)

        if(player == 1){
            roomData = hashMapOf(
                "player1Rematch" to "true"
            )
        }else if(player == 2){
            roomData = hashMapOf(
                "player2Rematch" to "true"
            )
        }

        docRef.update(roomData as Map<String, Any>)
            .addOnSuccessListener {
                Log.i("TAG", "Request from player 1 sent sucessessfully")
            }
            .addOnFailureListener {
                Log.i("TAG", "Request from player 1 sent unsucessessfully")
            }
    }

    private fun showDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val customView = LayoutInflater.from(context).inflate(R.layout.custom_layout_rematch, null)
        builder.setView(customView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //brise bijele uglove iz dialoga !!! (FINALLY)
        dialog.show()
    }

    private fun stopListeningToFirebase(){
        registerListener.remove()
    }
}