package com.example.leafstonescissorstcom.game_logic.matching_users

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
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

class StartActivity : AppCompatActivity() {

    private val NUMBER_OF_PLAYERS_INSIDE_COMP_GROUP = "4"
    private var playerFirstOrSecond = ""
    private lateinit var buttonStatsTournament: AppCompatButton

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
    private lateinit var roomsRefScoreState: DocumentReference
    private lateinit var collectionReference: CollectionReference
    var playerEmail = ""
    var playerTokens = ""
    var playerName = ""
    var currentFromMain = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

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
                buttonCompGame(playerName, buttonStartGameGroupComp, roomsRefFromMain.collection("games"), numberOfPlayers, false) //making room in same time, need delay
            }
        }

        //Button start 1v1 game:
        buttonStartGame.setOnClickListener {
            matchmake(playerName, db.collection("rooms"))
            buttonStartGame.isEnabled = false
            buttonStartGame.setBackgroundColor(Color.argb(255, 169, 169, 169))
            textViewWaiting.isVisible = true
        }

        //Button start group game:
        buttonStartGameGroupComp.setOnClickListener {
            buttonCompGame(playerName, buttonStartGameGroupComp, db.collection("groupRooms"), NUMBER_OF_PLAYERS_INSIDE_COMP_GROUP, true)
        }

        //score table
        collectionReference = db.collection("groupRooms")
        buttonStatsTournament.setOnClickListener {
            val intent = Intent(this, TourScoreTable::class.java).apply {
                putExtra("room_ref", roomsRefScoreState.path)
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
                register2.remove()
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
                    intent.putExtra("score_ref", roomsRefScoreState.path)
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
            register2.remove()
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
            putExtra("score_ref", roomsRefScoreState.path)
            Log.i("TAG4", "vALUE OF PLAYER 1 NAME $playerName")
            Log.i("TAG4", "vALUE OF PLAYER 2 NAME $player2NameFromFB")
        }
        startActivity(intent)
        finish()
    }

    /* LOGIC FOR SECOND BUTTON "START GROUP COMP" ******************************************************************************************************************************/
    var roomIdCompGroup: String = ""
    var roomIdOfScoreTable: String = ""
    var changeOnlyOnce = true
    var currentSend: String = ""
    private fun buttonCompGame(playerName: String, buttonStartGameGroupComp: AppCompatButton, roomGroupComp: CollectionReference, numberOfPlayers: String, onlyOnce: Boolean){
        buttonStatsTournament.visibility = View.VISIBLE
        textViewConnecting.isVisible = true
        buttonStartGameGroupComp.isEnabled = false
        buttonStartGameGroupComp.setBackgroundColor(Color.argb(255, 169, 169, 169))
        val query = roomGroupComp.whereEqualTo("status", "open").get()
            .addOnSuccessListener {documentListener ->
                if(documentListener.isEmpty){

                    var player1 = ""
                    roomGroupComp.add(roomPlayerData)
                        .addOnCompleteListener { documentReference ->  //first round
                            roomIdCompGroup = documentReference.result.id
                            if(changeOnlyOnce){
                                roomIdOfScoreTable = roomIdCompGroup
                                roomsRefScoreState = collectionReference.document(roomIdOfScoreTable)
                                changeOnlyOnce = false
                            }
                            if(onlyOnce){
                                playerFirstOrSecond = "first"
                                currentSend = "1"
                                player1 = "player1"
                            }else{
                                player1 = "player" + currentFromMain
                                Log.i("TAG", "Player1111: $player1, $currentFromMain")
                                currentSend = currentFromMain
                                if( (currentFromMain.toInt() % 2) == 0)
                                    playerFirstOrSecond = "second"
                                else
                                    playerFirstOrSecond = "first"
                            }

                            Log.d("TAG", "Room created with ID: ${documentReference.result.id}")
                            roomGroupComp.document(roomIdCompGroup).update(player1, playerName, "current", "2")
                            listeningToStatusCompGame(roomsRef = roomGroupComp.document(roomIdCompGroup), roomGroupComp) //listening to value status to know when to start a game.
                        }
                        .addOnFailureListener { exception ->
                            Log.e("TAG", "Error creating room: ", exception)
                        }
                }else{
                    val room = documentListener.first()
                    val roomId = room.id
                    roomIdCompGroup = roomId
                    if(changeOnlyOnce){
                        roomIdOfScoreTable = roomIdCompGroup
                        roomsRefScoreState = collectionReference.document(roomIdOfScoreTable)
                        changeOnlyOnce = false
                    }

                    listeningToStatusCompGame(roomsRef = roomGroupComp.document(roomId), roomGroupComp) //listening to value status to know when to start a game.

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
                                    if(current == numberOfPlayers){ // last round

                                        var newPlayerString = ""
                                        val game: String = current as String
                                        val newValueGame = (game.toInt() + 1).toString()
                                        if(onlyOnce){
                                            currentSend = numberOfPlayers
                                            if(game.toInt() % 2 == 0){ //setting player place for game 1v1
                                                playerFirstOrSecond = "second"
                                            }else{
                                                playerFirstOrSecond = "first"
                                            }
                                            newPlayerString = "player$game"
                                        }else{
                                            currentSend = currentFromMain
                                            if(currentFromMain.toInt() % 2 == 0){
                                                playerFirstOrSecond = "second"
                                            }else{
                                                playerFirstOrSecond = "first"
                                            }
                                            newPlayerString = "player$currentFromMain"
                                        }

                                        roomGroupComp.document(roomId).update("current", newValueGame, newPlayerString, playerName, "status", "close") //important
                                    }else{

                                        var newPlayerString = ""
                                        val game: String = current as String
                                        val newValueGame = (game.toInt() + 1).toString()
                                        if(onlyOnce){
                                            if(game.toInt() % 2 == 0){
                                                playerFirstOrSecond = "second"
                                            }else{
                                                playerFirstOrSecond = "first"
                                            }
                                            newPlayerString = "player${ (game.toInt())}"
                                            currentSend = game
                                        }else{
                                            currentSend = currentFromMain
                                            if(currentFromMain.toInt() % 2 == 0){
                                                playerFirstOrSecond = "second"
                                            }else{
                                                playerFirstOrSecond = "first"
                                            }
                                            newPlayerString = "player${currentFromMain}"
                                        }

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

    private lateinit var register2: ListenerRegistration

    private fun listeningToStatusCompGame(roomsRef: DocumentReference, collection: CollectionReference){
        register2 = roomsRef.addSnapshotListener { value, _ -> //live follow data change in firebase
            val statusOfRoom = value?.getString("status")!!
            Log.i("TAG", "Listening triggered2")
            if (statusOfRoom == "close") {
                //start activity (but first find how to pair players in games 1v1)
                if(playerFirstOrSecond == "first"){
                    matchmakeForCompGroup(playerName, roomsRef = collection.document(roomIdCompGroup).collection("games"), playerCheck = 1)
                    //createRoom(playerName = playerName, roomsRef = collection.document(roomIdCompGroup).collection("games")) //game not starting.
                    textViewConnecting.text = "Connecting ..."
                    textViewConnecting.isVisible = true
                    //newAcitvity(playerName, player2NameFromFB, player2EmailFromFB, playerEmail, roomId, player, playerTokens, player2TokensFromFB) need to call this somehow.
                }else if(playerFirstOrSecond == "second"){
                    textViewConnecting.text = "Connecting ..."
                    textViewConnecting.isVisible = true
                    startDelay(collection)
                }
            }
        }
    }
    private val delayMillis: Long = 3000 // 3 seconds delay, we are waiting for firebase to make a rooms and it can start with a game.
    private fun startDelay(collection: CollectionReference) {
        Handler(Looper.getMainLooper()).postDelayed({
            textViewConnecting.isVisible = false
            Log.i("TAG", "Id dokumentra: $roomIdCompGroup")
            Log.i("TAG", "Id dokumentra: $roomIdCompGroup")
            matchmakeForCompGroup(playerName = playerName, roomsRef = collection.document(roomIdCompGroup).collection("games"), playerCheck = 2)
            // This code will run after the specified delay
            // Perform your task here
        }, delayMillis)
    }

    private fun matchmakeForCompGroup(playerName: String, roomsRef: CollectionReference, playerCheck: Int = 1) { // promjeniti da se prima refereca!
        val query = roomsRef.whereEqualTo("status", "open").limit(1)
        query.get()
            .addOnSuccessListener { documents ->
                if (playerCheck == 1){
                    // No open rooms found, create a new room
                    createRoom(playerName = playerName, roomsRef = roomsRef, "group")
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                } else if(playerCheck == 2) {
                    // Found an open room, join the room
                    val room = documents.first()
                    val roomId = room.id

                    Log.i("TAG", "Id dokumentra pre joinRoom: $roomIdCompGroup")
                    Log.i("TAG", "Id dokumentra pre joinRoom: $roomId")
                    joinRoom(roomId, 2, playerName, roomsRef.document(roomId), "group") //not good ID cuz second player never entered creatingRoom, need to put it in firebase and read
                }
            }.addOnFailureListener { exception ->
                Log.e("TAG", "Error getting open rooms: ", exception)
            }
    }
    /*A***********************************************************************************************************************************************************************************/
}