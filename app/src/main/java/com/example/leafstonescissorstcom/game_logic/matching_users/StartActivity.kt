package com.example.leafstonescissorstcom.game_logic.matching_users

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.example.leafstonescissorstcom.R
import com.example.leafstonescissorstcom.game_logic.MainActivity
import com.example.leafstonescissorstcom.game_logic.tour_score.TourScoreTable
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class StartActivity : AppCompatActivity(), ListenerToStatus.MatchmakeTwoPlayers{

    private val NUMBER_OF_PLAYERS_INSIDE_COMP_GROUP = "4"
    private var playerFirstOrSecond = ""
    private lateinit var buttonStatsTournament: AppCompatButton
    private lateinit var listenerToStatus: ListenerToStatus
    val groupTournament: GroupTournament = GroupTournament()

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var roomData = hashMapOf("player1" to "value", "player2" to "Value2", "status" to "default")
    //Those are fields for firebase, in those fields we will remember who passed to the next stage of tournament
    private var roomPlayerData = hashMapOf(
        //osmina
        "player1" to "value", "player2" to "value", "player3" to "value", "player4" to "value",
        "player5" to "value", "player6" to "value", "player7" to "value", "player8" to "value",
        //cetvrt finale
        "player21" to "value", "player22" to "value", "player23" to "value", "player24" to "value",
        //finale                                            //Ovo je potrebno radi logike.
        "player31" to "value", "player32" to "value",      "current" to "1", "status" to "open",
        //osmina score
        "score1" to "-", "score2" to "-", "score3" to "-", "score4" to "-",
        "score5" to "-", "score6" to "-", "score7" to "-", "score8" to "-",
        //cetvrtina score
        "score21" to "-", "score22" to "-", "score23" to "-", "score24" to "-",
        //finale score
        "score31" to "-", "score32" to "-",
        )
    private lateinit var textViewWaiting: TextView
    private lateinit var textViewTokens: TextView
    private lateinit var textViewConnecting: TextView
    private lateinit var roomsRefFromMain: DocumentReference
    private lateinit var firstRoomRefDoc: DocumentReference
    private lateinit var collectionReference: CollectionReference
    var playerEmail = ""
    var playerTokens = ""
    var playerName = ""
    var currentFromMain = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        listenerToStatus = ListenerToStatus()
        listenerToStatus.setMatchmakeTwoPlayersListener(this)

        buttonStatsTournament = findViewById(R.id.buttonStatsTournament)
        val buttonStartGame = findViewById<AppCompatButton>(R.id.buttonStart1v1)
        val buttonStartGameGroupComp = findViewById<AppCompatButton>(R.id.buttonStartGroupComp)
        textViewWaiting = findViewById(R.id.textViewWaitPlayer)
        textViewTokens = findViewById(R.id.textViewTokens)
        textViewConnecting = findViewById(R.id.textViewConnecting)
        val textViewWelcome = findViewById<TextView>(R.id.textViewWelcomePlayer)
        playerName = intent.extras?.getString("name") ?: ""
        playerEmail = intent.extras?.getString("email") ?: ""
        playerTokens = intent.extras?.getString("tokens") ?: ""
        val skipOnce = intent.extras?.getString("skip_once") ?: ""

        if(playerName == "" || playerEmail == "" || playerTokens == "" || skipOnce == ""){
            finish()
        }

        val newString = textViewWelcome.text.toString() + " " + playerName + "!"
        Log.i("getnick", "Change textview: $newString")
        textViewWelcome.text = newString
        textViewTokens.text = playerTokens

        if(skipOnce == "false"){ //When player is openning this activity as second round in tournament.
            currentFromMain = intent.extras?.getString("current") ?: ""
            Log.i("TAG", "currentFromMain $currentFromMain") //Test showed good result
            val numberOfPlayers: String = ( ( NUMBER_OF_PLAYERS_INSIDE_COMP_GROUP.toInt() / 2)).toString()
            if(numberOfPlayers.toInt() == 1){
                // do dialog winner of tournament
            }else{
                val documentPath = intent.getStringExtra("roomsRef")!!
                roomsRefFromMain = FirebaseFirestore.getInstance().document(documentPath)
                Log.i("TAG", "Room id score: $roomIdOfScoreTable")
                textViewConnecting.text = "Waiting for other players to finish their game."
                textViewConnecting.isVisible = true
                buttonStatsTournament.isVisible = true
                buttonStartGame.isEnabled = false
                buttonStartGameGroupComp.isEnabled = false
                groupTournament.buttonTourGameSecondOpen(playerName, roomsRefFromMain, NUMBER_OF_PLAYERS_INSIDE_COMP_GROUP, currentFromMain, listenerToStatus)
            }
        }

        //Button start 1v1 game:
        buttonStartGame.setOnClickListener {
            matchmake(playerName, db.collection("rooms"))
            buttonStartGame.isEnabled = false
            buttonStartGameGroupComp.isEnabled = false
            buttonStartGame.setBackgroundColor(Color.argb(255, 169, 169, 169))
            textViewWaiting.isVisible = true
        }

        //Button start group game:
        buttonStartGameGroupComp.setOnClickListener {
            textViewConnecting.isVisible = true
            buttonStartGame.isEnabled = false
            buttonStartGameGroupComp.isEnabled = false
            groupTournament.buttonTourGameFirstOpen(playerName, db.collection("groupRooms"), NUMBER_OF_PLAYERS_INSIDE_COMP_GROUP, roomPlayerData, listenerToStatus)
        }

        //score table
        collectionReference = db.collection("groupRooms")
        buttonStatsTournament.setOnClickListener {
            val intent = Intent(this, TourScoreTable::class.java).apply {
                putExtra("room_ref", firstRoomRefDoc.path)
            }
            startActivity(intent)
        }
    }

    private fun matchmake(playerName: String, roomsRef: CollectionReference) { // promjeniti da se prima refereca!
        val query = roomsRef.whereEqualTo("status", "open").limit(1)
        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No open rooms found, create a new room
                    createRoom(playerName = playerName, roomsRef = roomsRef, "solo")
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                } else {
                    // Found an open room, join the room
                    val room = documents.first()
                    val roomId = room.id
                    // Add the player to the room
                    joinRoom(roomId, 2, playerName, roomsRef.document(roomId), "solo")
                }
            }.addOnFailureListener { exception ->
                Log.e("TAG", "Error getting open rooms: ", exception)
            }
    }


    private fun createRoom(playerName: String, roomsRef: CollectionReference, kindOfGame: String) {

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
                joinRoom(documentReference.id, player = 1, playerName, roomsRef = roomsRef.document(documentReference.id), kindOfGame = kindOfGame)
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error creating room: ", exception)
            }
    }

    private fun joinRoom(roomId: String, player: Int, playerName: String, roomsRef: DocumentReference, kindOfGame: String) {
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

            if(kindOfGame != "solo"){ //if game is solo 1v1 then skip removing register cause its not initialized.
                Log.i("TAG", "delete register")
                //register2.remove()
            }
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
                    intent.putExtra("kindOfGame", kindOfGame)
                    intent.putExtra("roomsRef", roomsRef.path)
                    intent.putExtra("current", currentSend)
                    intent.putExtra("score_ref", firstRoomRefDoc.path)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e("TAG", "Error joining room: ", exception)
                }
        } else { //player == 1
            var statusOfRoom: String
            var player2NameFromFB: String
            var player2EmailFromFB: String
            var player2TokensFromFB: String

            register = roomsRef.addSnapshotListener { value, _ -> //live follow when second user came so it can open new activity together
                Log.i("TAG", "Listening triggered1")
                statusOfRoom = value?.getString("status")!!
                player2NameFromFB = value.getString("player2")!!
                player2EmailFromFB = value.getString("player2Email")!!
                player2TokensFromFB = value.getString("player2Tokens")!!
                if (statusOfRoom == "full") {
                    newAcitvity(playerName, player2NameFromFB, player2EmailFromFB, playerEmail,
                        roomId, player, playerTokens, player2TokensFromFB, kindOfGame, roomsRef)
                }
            }
        }
    }
    private lateinit var register: ListenerRegistration
    private fun newAcitvity(playerName: String, player2NameFromFB: String, player2EmailFromFB: String,
                            player1Email: String, roomId: String, player: Int, tokensPlayer1: String,
                            tokensPlayer2: String, kindOfGame: String, roomsRef: DocumentReference){
        register.remove()
        Log.i("TAG", "delete register: ${kindOfGame}")
        if(kindOfGame != "solo"){ //if game is solo 1v1 then skip removing register cause its not initialized.
            Log.i("TAG", "delete register")
            //register2.remove()
        }
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
            putExtra("kindOfGame", kindOfGame)
            putExtra("roomsRef", roomsRef.path)
            putExtra("current", currentSend)
            putExtra("score_ref", firstRoomRefDoc.path)
            Log.i("TAG4", "vALUE OF PLAYER 1 NAME $playerName")
            Log.i("TAG4", "vALUE OF PLAYER 2 NAME $player2NameFromFB")
        }
        startActivity(intent)
        finish()
    }

    /* LOGIC FOR SECOND BUTTON "START GROUP COMP" ******************************************************************************************************************************/
    var roomIdOfScoreTable: String = ""
    var currentSend: String = ""

    override fun callCreateRoom(playerName: String, secondRoomGroupTour: CollectionReference, typeOfGame: String) {
        Log.i("TAG", "Function createRoom called.")
        createRoom(playerName = playerName, roomsRef = secondRoomGroupTour, kindOfGame = typeOfGame)
    }

    override fun callJoinRoom(secondRoomRedId: String, playerName: String, secondRoomRefDoc: DocumentReference, typeOfGame: String) {
        Log.i("TAG", "Function joinRoom called.")
        joinRoom(secondRoomRedId, 2, playerName, secondRoomRefDoc, typeOfGame)
    }

    override fun getFirstRoomRefDoc(firstRoomRefDoc: DocumentReference, currentSend: String) { //update vrednosti u StartActivity koje dobijamo u HelperKlasi
        this.firstRoomRefDoc = firstRoomRefDoc
        this.currentSend = currentSend
    }

    override fun updateTextViewConnecting(boolean: Boolean, text: String) {
        textViewConnecting.text = text
        textViewConnecting.isVisible = boolean
    }
    /*A*************************************************************************************************************************************************/
}
