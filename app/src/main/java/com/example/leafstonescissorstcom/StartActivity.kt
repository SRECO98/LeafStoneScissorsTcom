package com.example.leafstonescissorstcom

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var playerOneName: String = ""
    var playerTwoName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val buttonStartGame = findViewById<AppCompatButton>(R.id.buttonStartGame)

        val textViewWelcome = findViewById<TextView>(R.id.textViewWelcomePlayer)
        val playerName = intent.extras?.getString("name") ?: "No message found"

        val newString = textViewWelcome.text.toString() + " " + playerName + "!"
        Log.i("getnick", "Change textview: $newString")
        textViewWelcome.text = newString

        buttonStartGame.setOnClickListener {
            matchmake(playerName)
        }
    }

    private fun matchmake(playerName: String) {
        val roomsRef = db.collection("rooms")
        val query = roomsRef.whereEqualTo("status", "open").limit(1)

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    //save player name to send as intent
                    playerOneName = playerName

                    // No open rooms found, create a new room
                    createRoom(playerName)
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                } else {
                    // Found an open room, join the room
                    val room = documents.first()
                    val roomId = room.id

                    //save player name to send as intent
                    playerTwoName = playerName

                    // Add the player to the room
                    joinRoom(roomId, playerName)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting open rooms: ", exception)
            }
    }

    private fun createRoom(playerName: String) {
        val roomsRef = db.collection("rooms")

        val roomData = hashMapOf(
            "player1" to playerName,
            "status" to "open"
        )

        roomsRef.add(roomData)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "Room created with ID: ${documentReference.id}")
                // Join the room as the first player
                joinRoom(documentReference.id, playerName)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error creating room: ", exception)
            }

        Log.i("TAG", roomData.toString())
        Log.i("TAG", roomData.toString())
        Log.i("TAG", roomData.toString())
        Log.i("TAG", roomData.toString())
        Log.i("TAG", roomData.toString())
        Log.i("TAG", roomData.toString())
        Log.i("TAG", roomData.toString())
        Log.i("TAG", roomData.toString())
    }

    private fun joinRoom(roomId: String, playerName: String) {
        val roomsRef = db.collection("rooms").document(roomId)

        // Update the room data to add the player as the second player
        val roomData = hashMapOf(
            "player2" to playerName,
            "status" to "full"
        )

        roomsRef.update(roomData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("TAG", "Joined room with ID: $roomId")
                // Navigate to the game activity
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("room_id", roomId)
                    putExtra("player1Name", playerOneName)
                    putExtra("player2Name", playerTwoName)
                }
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error joining room: ", exception)
            }

        Log.i("TAG2", roomData.toString())
        Log.i("TAG2", roomData.toString())
        Log.i("TAG2", roomData.toString())
        Log.i("TAG2", roomData.toString())
        Log.i("TAG2", roomData.toString())
        Log.i("TAG2", roomData.toString())
        Log.i("TAG2", roomData.toString())
        Log.i("TAG2", roomData.toString())
    }

}