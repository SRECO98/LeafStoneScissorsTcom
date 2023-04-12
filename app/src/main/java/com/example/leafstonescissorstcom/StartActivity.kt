package com.example.leafstonescissorstcom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class StartActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var roomData = hashMapOf("player1" to "value", "player2" to "Value2", "status" to "default")
    private lateinit var textViewWaiting: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val buttonStartGame = findViewById<AppCompatButton>(R.id.buttonStartGame)
        textViewWaiting = findViewById(R.id.textViewWaitPlayer)
        val textViewWelcome = findViewById<TextView>(R.id.textViewWelcomePlayer)
        val playerName = intent.extras?.getString("name") ?: "No message found"

        val newString = textViewWelcome.text.toString() + " " + playerName + "!"
        Log.i("getnick", "Change textview: $newString")
        textViewWelcome.text = newString

        buttonStartGame.setOnClickListener {
            matchmake(playerName)
            buttonStartGame.isEnabled = false
            buttonStartGame.setBackgroundColor(Color.argb(255, 169, 169, 169))
            textViewWaiting.isVisible = true
        }
    }

    private fun matchmake(playerName: String) {
        val roomsRef = db.collection("rooms")
        val query = roomsRef.whereEqualTo("status", "open").limit(1)
        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {


                    // No open rooms found, create a new room
                    createRoom(playerName)
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                } else {
                    // Found an open room, join the room
                    val room = documents.first()
                    val roomId = room.id


                    // Add the player to the room
                    joinRoom(roomId, 2, playerName)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting open rooms: ", exception)
            }
    }

    private fun createRoom(playerName: String) {
        val roomsRef = db.collection("rooms")

        roomData = hashMapOf(
            "player1" to playerName,
            "player2" to "unknown",
            "status" to "open",
        )
        Log.i("TAG", "roomData: $roomData")

        roomsRef.add(roomData)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "Room created with ID: ${documentReference.id}")
                // Join the room as the first player
                joinRoom(documentReference.id, player = 1, playerName)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error creating room: ", exception)
            }
    }

    private fun joinRoom(roomId: String, player: Int, playerName: String) {
        val roomsRef = db.collection("rooms").document(roomId)
        var player1NameFromFirebase: String? = "unknown"
        roomsRef.get()
            .addOnSuccessListener {
                Log.i("TAG", "Getting data successed")
                player1NameFromFirebase = it.getString("player1")
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting data failed")
            }

        if (player == 2) {
            // Update the room data to add the player as the second player
            roomData = hashMapOf(
                "player2" to playerName,
                "status" to "full"
            )
            Log.i("TAG", "roomData: $roomData")

            roomsRef.update(roomData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("TAG", "Joined room with ID: $roomId")
                    // Navigate to the game activity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("player1Name", player1NameFromFirebase)
                    intent.putExtra("player2Name", playerName)
                    intent.putExtra("room_id", roomId)
                    intent.putExtra("player", player)
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->
                    Log.e("TAG", "Error joining room: ", exception)
                }
        } else {
            val roomsRef2 = db.collection("rooms").document(roomId)
            var statusOfRoom: String
            var player2NameFromFB: String

            register = roomsRef2.addSnapshotListener { value, _ -> //live follow when second user came so it can open new activity together
                statusOfRoom = value?.getString("status")!!
                player2NameFromFB = value.getString("player2")!!
                if (statusOfRoom == "full") {
                    newAcitvity(playerName, player2NameFromFB, roomId, player)
                }
            }
        }
    }
    private lateinit var register: ListenerRegistration
    private fun newAcitvity(playerName: String, player2NameFromFB: String, roomId: String, player: Int){
        register.remove()
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("player1Name", playerName)
            putExtra("player2Name", player2NameFromFB)
            putExtra("room_id", roomId)
            putExtra("player", player)
            Log.i("TAG4", "vALUE OF PLAYER 1 NAME $playerName")
            Log.i("TAG4", "vALUE OF PLAYER 2 NAME $player2NameFromFB")
        }
        startActivity(intent)
    }
}