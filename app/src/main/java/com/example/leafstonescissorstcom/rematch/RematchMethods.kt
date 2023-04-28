package com.example.leafstonescissorstcom.rematch

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.example.leafstonescissorstcom.MainActivity
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
    var onlyOnce: Boolean = false
    fun setListenerToFields(player: Int, roomId: String, context: Context){
        docRef = db.collection("rooms").document(roomId)
        registerListener = docRef.addSnapshotListener { value, _ ->
            statusRequestPLayer1 = value?.getString("player1Rematch")!!
            statusRequestPLayer2 = value.getString("player2Rematch")!!
            if( (statusRequestPLayer1.toBoolean() || statusRequestPLayer2.toBoolean()) && !onlyOnce){
                onlyOnce = true //We must not make object dialog twice because we cant cancel it later in code, we lose reference to it.
                builder = AlertDialog.Builder(context)
                customView = LayoutInflater.from(context).inflate(R.layout.custom_layout_rematch, null)
                builder.setView(customView)
                dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //brise bijele uglove iz dialoga !!! (FINALLY)
            }
            if(statusRequestPLayer1.toBoolean() && statusRequestPLayer2.toBoolean()){ //if user accepted rematch
                Log.i("TAG", "Both true in base")
                dialog.dismiss()
                registerListener.remove()
                val mainActivity = MainActivity()
                mainActivity.rematchMatchmaking() //starting new game between two same players
            }else if(player == 1 && statusRequestPLayer2.toBoolean()){ //seinding rematch request to player2
                showDialog(context, "player1Rematch")
            }else if(player == 2 && statusRequestPLayer1.toBoolean()){ //sending rematch request to player1
                showDialog(context, "player2Rematch")
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

    private lateinit var builder: AlertDialog.Builder
    private lateinit var customView: View
    private lateinit var dialog: AlertDialog

    private fun showDialog(context: Context, firebaseField: String) {

        dialog.show()

        val buttonYes: AppCompatButton = customView.findViewById(R.id.buttonYes)
        val buttonNo: AppCompatButton = customView.findViewById(R.id.buttonNo)

        buttonYes.setOnClickListener {
            docRef.update(firebaseField, "true")
                .addOnSuccessListener {
                    Log.i("TAG", "Successfully added true to second field for rematch request")
                }
                .addOnFailureListener {
                    Log.i("TAG", "Unsuccessfully added true to second field for rematch request")
                }
        }
        buttonNo.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun stopListeningToFirebase(){
        registerListener.remove()
    }
}