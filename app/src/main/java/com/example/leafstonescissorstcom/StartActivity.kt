package com.example.leafstonescissorstcom

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class StartActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var roomData = hashMapOf("player1" to "value", "player2" to "Value2", "status" to "default")
    private var roomPlayerData = hashMapOf("player1" to "value", "player2" to "value", "player3" to "value", "player4" to "value",
        "player5" to "value", "player6" to "value", "player7" to "value", "player8" to "value", "current" to "1", "status" to "open")
    private lateinit var textViewWaiting: TextView
    private lateinit var textViewTokens: TextView
    var playerEmail = ""
    var playerTokens = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val buttonStartGame = findViewById<AppCompatButton>(R.id.buttonStart1v1)
        val buttonStartGameGroupComp = findViewById<AppCompatButton>(R.id.buttonStartGroupComp)
        textViewWaiting = findViewById(R.id.textViewWaitPlayer)
        textViewTokens = findViewById(R.id.textViewTokens)
        val textViewWelcome = findViewById<TextView>(R.id.textViewWelcomePlayer)
        val playerName = intent.extras?.getString("name") ?: ""
        playerEmail = intent.extras?.getString("email") ?: ""
        playerTokens = intent.extras?.getString("tokens") ?: ""
        Log.i("TAG", " $playerTokens check tokens player one" )

        if(playerName == "" || playerEmail == "" || playerTokens == ""){
            finish()
        }
        val newString = textViewWelcome.text.toString() + " " + playerName + "!"
        Log.i("getnick", "Change textview: $newString")
        textViewWelcome.text = newString
        textViewTokens.text = playerTokens

        buttonStartGame.setOnClickListener {
            matchmake(playerName)
            buttonStartGame.isEnabled = false
            buttonStartGame.setBackgroundColor(Color.argb(255, 169, 169, 169))
            textViewWaiting.isVisible = true
        }

        buttonCompGame(playerName, buttonStartGameGroupComp)
    }


    private fun matchmake(playerName: String) { // promjeniti da se prima refereca!
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
            "player1Email" to playerEmail,
            "player2Email" to "unknown",
            "player1Tokens" to playerTokens,
            "player2Tokens" to "unknown",
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
        var player1EmailFromFirebase: String? = "unknown"
        var player1TokensFromFirebase: String? = "unknown"
        roomsRef.get()
            .addOnSuccessListener {
                Log.i("TAG", "Getting data successed")
                player1NameFromFirebase = it.getString("player1")
                player1EmailFromFirebase = it.getString("player1Email")
                player1TokensFromFirebase = it.getString("player1Tokens")
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting data failed")
            }

        if (player == 2) {
            // Update the room data to add the player as the second player
            roomData = hashMapOf(
                "player2" to playerName,
                "status" to "full",
                "player2Email" to playerEmail,
                "player2Tokens" to playerTokens,
            )
            Log.i("TAG", "roomData: $roomData")

            roomsRef.update(roomData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("TAG", "Joined room with ID: $roomId")
                    // Navigate to the game activity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("player1Name", player1NameFromFirebase)
                    intent.putExtra("player2Name", playerName)
                    intent.putExtra("player1Email", player1EmailFromFirebase)
                    intent.putExtra("player2Email", playerEmail)
                    intent.putExtra("player1Tokens", player1TokensFromFirebase)
                    intent.putExtra("player2Tokens", playerTokens)
                    intent.putExtra("room_id", roomId)
                    intent.putExtra("player", player)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e("TAG", "Error joining room: ", exception)
                }
        } else { //player == 1
            val roomsRef2 = db.collection("rooms").document(roomId)
            var statusOfRoom: String
            var player2NameFromFB: String
            var player2EmailFromFB: String
            var player2TokensFromFB: String

            register = roomsRef2.addSnapshotListener { value, _ -> //live follow when second user came so it can open new activity together
                statusOfRoom = value?.getString("status")!!
                player2NameFromFB = value.getString("player2")!!
                player2EmailFromFB = value.getString("player2Email")!!
                player2TokensFromFB = value.getString("player2Tokens")!!
                if (statusOfRoom == "full") {
                    newAcitvity(playerName, player2NameFromFB, player2EmailFromFB, playerEmail, roomId, player, playerTokens, player2TokensFromFB)
                }
            }
        }
    }
    private lateinit var register: ListenerRegistration
    private fun newAcitvity(playerName: String, player2NameFromFB: String, player2EmailFromFB: String,
                            player1Email: String, roomId: String, player: Int, tokensPlayer1: String, tokensPlayer2: String){
        register.remove()
        Log.i("TAG", "Player emails 1: $player1Email")
        Log.i("TAG" ,"Player emails 2: $player2EmailFromFB")
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("player1Name", playerName)
            putExtra("player2Name", player2NameFromFB)
            putExtra("player1Email", player1Email)
            putExtra("player2Email", player2EmailFromFB)
            putExtra("room_id", roomId)
            putExtra("player", player)
            putExtra("player1Tokens", tokensPlayer1)
            putExtra("player2Tokens", tokensPlayer2)
            Log.i("TAG4", "vALUE OF PLAYER 1 NAME $playerName")
            Log.i("TAG4", "vALUE OF PLAYER 2 NAME $player2NameFromFB")
        }
        startActivity(intent)
        finish()
    }

    private fun buttonCompGame(playerName: String, buttonStartGameGroupComp: AppCompatButton){
        buttonStartGameGroupComp.setOnClickListener {
            val roomGroupComp = db.collection("rooms")
            val query = roomGroupComp.whereEqualTo("status", "open").get()
                .addOnSuccessListener {documentListener ->
                    if(documentListener.isEmpty){
                        var roomId: String = ""
                        roomGroupComp.add(roomPlayerData)
                            .addOnSuccessListener { documentReference ->
                                roomId = documentReference.id
                                Log.d("TAG", "Room created with ID: ${documentReference.id}")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("TAG", "Error creating room: ", exception)
                            }
                        listeningToStatusCompGame(roomsRef = roomGroupComp.document(roomId)) //listening to value status to know when to start a game.
                    }else{
                        val room = documentListener.first()
                        val roomId = room.id

                        listeningToStatusCompGame(roomsRef = roomGroupComp.document(roomId)) //listening to value status to know when to start a game.

                        val roomRef = roomGroupComp.document(roomId)
                        Log.i("TAG", "Id is: $roomId")
                        roomRef.get().addOnCompleteListener{ task ->
                            if (task.isSuccessful) {
                                Log.i("TAG", "Task is succesful!")
                                val documentSnapshot = task.result
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    Log.i("TAG", "DocumentSnapshot exist and its not null")
                                    val current = documentSnapshot.get("current")
                                    Log.i("TAG", "Current is: $current")
                                    if (current != null) {
                                        // Use the current value
                                        if(current == "8"){
                                            val game: String = current as String
                                            val newValueGame = (game.toInt() + 1).toString()
                                            val newPlayerString = "player$game"
                                            roomGroupComp.document(roomId).update("current", newValueGame, newPlayerString, playerName, "status", "close")
                                            //START GAME (put some value to true in firebase and start activity on all phones)
                                        }else{
                                            val game: String = current as String
                                            val newValueGame = (game.toInt() + 1).toString()
                                            val newPlayerString = "player$game"
                                            roomGroupComp.document(roomId).update("current", newValueGame, newPlayerString, playerName)
                                        }
                                    } else {
                                        // Handle the case when the "current" parameter is not found
                                        println("The current player is not available")
                                    }
                                } else {
                                    // Handle the case when the document doesn't exist
                                    println("The document doesn't exist")
                                }
                            } else {
                                // Handle any errors that occurred during the operation
                                val exception = task.exception
                                println("Error getting document: ${exception?.message}")
                            }
                        }
                    }
                }
        }
    }

    private lateinit var register2: ListenerRegistration
    private fun listeningToStatusCompGame(roomsRef: DocumentReference, ){
        register2 = roomsRef.addSnapshotListener { value, _ -> //live follow data change in firebase
            val statusOfRoom = value?.getString("status")!!
            /*player2NameFromFB = value.getString("player2")!!
            player2EmailFromFB = value.getString("player2Email")!!
            player2TokensFromFB = value.getString("player2Tokens")!!*/
            if (statusOfRoom == "close") {
                //start activity (but first find how to pair players in games 1v1)
            }
        }
    }


}