package com.example.leafstonescissorstcom

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.database.MutableData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var timer: CountDownTimer
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var roomsScoreRef: CollectionReference
    private lateinit var roomsChooseRef: CollectionReference
    private var playerChoose = "0"
    private lateinit var textViewPlayerOneScore: TextView
    private lateinit var textViewPlayerTwoScore: TextView
    private lateinit var buttonStone: AppCompatButton
    private lateinit var buttonLeaf: AppCompatButton
    private lateinit var buttonSccissors: AppCompatButton
    private lateinit var buttonGo: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonStone = findViewById(R.id.imageStone)
        buttonLeaf = findViewById(R.id.imageLeaf)
        buttonSccissors = findViewById(R.id.imageSccissors)
        buttonGo = findViewById(R.id.buttonChoose)
        val textViewPlayerOneName: TextView = findViewById(R.id.playerOneName)
        val textViewPlayerTwoName: TextView = findViewById(R.id.playerTwoName)
        textViewPlayerOneScore = findViewById(R.id.playerOneScore)
        textViewPlayerTwoScore = findViewById(R.id.playerTwoScore)
        val textViewTimer: TextView = findViewById(R.id.textViewTimer)
        val player1Name = intent.getStringExtra("player1Name")
        val player2Name = intent.getStringExtra("player2Name")
        val roomId: String = intent.getStringExtra("room_id")!!
        val player:Int = intent.getIntExtra("player", 0)
        roomsChooseRef = db.collection("rooms").document(roomId).collection("choose") // ???????????????????????
        createRoomForPicturesChoice()

        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: $player1Name")
        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: $player2Name")

        textViewPlayerOneName.text = player1Name
        textViewPlayerTwoName.text = player2Name

        buttonStone.setOnClickListener {
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 180))
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 54))
            playerChoose = "1"
        }
        buttonLeaf.setOnClickListener {
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 180))
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 54))
            playerChoose = "2"
        }
        buttonSccissors.setOnClickListener {
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 180))
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 54))
            playerChoose = "3"
        }
        buttonGo.setOnClickListener {
            buttonGo.isEnabled = false //turn off buttons because user checked his choice
            buttonStone.isEnabled = false
            buttonLeaf.isEnabled = false
            buttonSccissors.isEnabled = false
            buttonGo.setTextColor(Color.WHITE)
            buttonGo.setBackgroundColor(Color.argb(255, 169, 169, 169))
            saveChoose(player)
        }

        timerFun(textViewTimer, player)
    }

    private fun timerFun (textViewTimer: TextView, player: Int){
        timer = object : CountDownTimer(9000, 1000) {
            override fun onTick(remaining: Long) {
                if (remaining < 4000) {
                    textViewTimer.setTextColor(Color.argb(255, 255, 0, 0))
                }
                if (remaining < 1000) {
                    return
                }
                textViewTimer.text = ((remaining / 1000).toString())
            }

            override fun onFinish() {
                textViewTimer.text = "9"
                textViewTimer.setTextColor(Color.argb(255, 251, 239, 2))
                loadChoose(player)
                if (Integer.parseInt(textViewPlayerOneScore.text.toString()) < 5 && Integer.parseInt(
                        textViewPlayerTwoScore.text.toString()) < 5){
                    buttonGo.isEnabled = true //tur on buttons again cuz new timer will begin
                    buttonStone.isEnabled = true
                    buttonLeaf.isEnabled = true
                    buttonSccissors.isEnabled = true
                    buttonStone.setBackgroundColor(Color.argb(23, 198, 182, 54))
                    buttonLeaf.setBackgroundColor(Color.argb(23, 198, 182, 54))
                    buttonSccissors.setBackgroundColor(Color.argb(23, 198, 182, 54))
                    buttonGo.setTextColor(Color.BLACK)
                    buttonGo.setBackgroundColor(Color.argb(255, 69, 194, 153))
                    timer.start()
                }
            }
        }
    }

    private var chooseRoom = hashMapOf(
        "choosePlayer1" to "unknown",
        "choosePlayer2" to "unknown"
    )
    private fun saveChoose(player: Int){
        val roomsChooseRef2 = roomsChooseRef.document(id)
        Log.i("TAG", "document id: $id")
        //updating picture of player one in room
        if(player == 1){
            chooseRoom = hashMapOf(
                "choosePlayer1" to playerChoose
            )

            roomsChooseRef2.update(chooseRoom as Map<String, Any>)
                .addOnSuccessListener {
                    Log.i("Choose", "Successfully added picture choice")
                }
                .addOnFailureListener {
                    Log.i("Choose", "Failed while adding picture choice ${it.toString()}")
                }
        }else if(player == 2){ //updating picture of player two in room

            chooseRoom = hashMapOf(
                "choosePlayer2" to playerChoose
            )

            roomsChooseRef2.update(chooseRoom as Map<String, Any>)
                .addOnSuccessListener {
                    Log.i("Choose", "Successfully added picture choice")
                }
                .addOnFailureListener {
                    Log.i("Choose", "Failed while adding picture choice")
                }
        }
        if(playerChoose == "0"){ //if one of players didnt chose a picture.
            Toast.makeText(this, "Please, choose one of the pictures!", Toast.LENGTH_SHORT).show()
        }

    }

    var id: String = "empty"
    private fun createRoomForPicturesChoice(){

        val query = roomsChooseRef.whereEqualTo("choosePlayer1", "unknown2").limit(1) //check did we created room, if value is changed we did.
        query.get()
            .addOnSuccessListener {documents ->
                if(documents.isEmpty){
                    chooseRoom = hashMapOf(
                        "choosePlayer1" to "unknown2",
                        "choosePlayer2" to "unknown2"
                    )

                    roomsChooseRef.add(chooseRoom)
                        .addOnSuccessListener {   //making roon
                            Log.d("TAG", "Room created with ID: ${it.id}")
                            id = it.id
                        }
                        .addOnFailureListener {
                            Log.e("TAG", "Error creating room: ", it)
                        }
                }else{
                    id = documents.first().id
                    Log.i("TAG", "Room is created already.")
                }
            }
    }

    /*private fun createRoomForPicturesChoice() {
        try {
            Firebase.firestore.runTransaction { transaction ->
                val query = roomsChooseRef.whereEqualTo("choosePlayer1", "unknown2")
                query.get().addOnSuccessListener { documents ->
                    Log.i("TAG7", "Document is empty or not: ${documents.isEmpty}")
                    if (documents.isEmpty) {
                        chooseRoom = hashMapOf(
                            "choosePlayer1" to "unknown2",
                            "choosePlayer2" to "unknown2"
                        )

                        transaction.update(roomsChooseRef.document(chooseRoom),)

                        roomsChooseRef.add(chooseRoom)
                            .addOnSuccessListener {   //making roon
                                Log.d("TAG", "Room created with ID: ${it.id}")
                                id = it.id
                            }
                            .addOnFailureListener {
                                Log.e("TAG", "Error creating room: ", it)
                            }
                    } else {
                        id = documents.first().id
                        Log.i("TAG", "Room is created already.")
                    }
                }
            }
        }catch (e: java.lang.Exception){
            Log.i("TAG", e.toString())
        }
    }*/

   /* private fun createRoomForPicturesChoice() {
        val query = roomsChooseRef.whereEqualTo("choosePlayer1", "unknown2")

        // Use a transaction to create or get the existing room
        roomsChooseRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                // Check if the room already exists
                val existingRoom = mutableData.children.firstOrNull { it.child("choosePlayer1").value == "unknown2" }
                if (existingRoom != null) {
                    id = existingRoom.key!!
                    Log.i("TAG", "Room is created already.")
                    return Transaction.success(mutableData)
                }

                // Create a new room
                chooseRoom = hashMapOf(
                    "choosePlayer1" to "unknown2",
                    "choosePlayer2" to "unknown2"
                )

                val newRoomRef = roomsChooseRef.push()
                newRoomRef.setValue(chooseRoom)
                id = newRoomRef.key!!
                Log.d("TAG", "Room created with ID: $id")

                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (databaseError != null) {
                    Log.e("TAG", "Transaction failed", databaseError.toException())
                }
            }
        })
    }*/
    
    private fun loadChoose(player: Int){
        var playerOneChoose: String
        var playertwoChoose: String
        roomsChooseRef.document(id).get()
            .addOnSuccessListener {
                Log.i("TAG", "Getting choose data successed")
                playerOneChoose = it.getString("choosePlayer1")!!
                playertwoChoose = it.getString("choosePlayer2")!!
                if(playerOneChoose == "unknown2" && playertwoChoose == "unknown2" ){
                    playerOneChoose = "0"
                    playertwoChoose = "0"
                }else if(playerOneChoose == "unknown2"){
                    playerOneChoose = "0"
                }else if(playertwoChoose == "unknown2"){
                    playertwoChoose = "0"
                }
                calculateWinner(playerOneChoose.toInt(), playertwoChoose.toInt(), player)
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting choose data failed")
            }
    }

    private fun calculateWinner(playerOneChoose: Int, playerTwoChoose: Int, player: Int){
        var scorePlayerOne = ""
        var scorePlayerTwo = ""
        Log.i("TAG", "Chooses: $playerOneChoose and $playerTwoChoose")
        if(player == 1){
            if(playerOneChoose == 1){

                when (playerTwoChoose) {
                    1 -> {
                        scorePlayerOne = "draw"
                        scorePlayerTwo = "draw"
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    }
                    2 -> {
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
                        buttonStone.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    3 -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        buttonStone.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    else -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        buttonStone.setBackgroundColor(Color.argb(58, 0, 0, 255))
                    }
                }

            }else if(playerOneChoose == 2){

                when (playerTwoChoose) {
                    1 -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    2 -> {
                        scorePlayerOne = "draw"
                        scorePlayerTwo = "draw"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    }
                    3 -> {
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    else -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 0, 0, 255))
                    }
                }
            }else if(playerOneChoose == 3){

                when (playerTwoChoose) {
                    1 -> {
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    2 -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    3 -> {
                        scorePlayerOne = "draw"
                        scorePlayerTwo = "draw"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    }
                    else -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 0, 0, 255))
                    }
                }
            }else{
                if(playerTwoChoose != 0){
                    scorePlayerOne = "defeat"
                    scorePlayerTwo = "victory"
                    buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                }else{
                    scorePlayerOne = "draw"
                    scorePlayerTwo = "draw"
                    buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 255))
                }
            }

        }else if(player == 2){

            if(playerTwoChoose == 1){

                when (playerOneChoose) {
                    1 -> {
                        scorePlayerTwo = "draw"
                        scorePlayerOne = "draw"
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    }
                    2 -> {
                        scorePlayerTwo = "defeat"
                        scorePlayerOne = "victory"
                        buttonStone.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    3 -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        buttonStone.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    else -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        buttonStone.setBackgroundColor(Color.argb(58, 0, 0, 255))
                    }
                }

            }else if(playerTwoChoose == 2){

                when (playerOneChoose) {
                    1 -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    2 -> {
                        scorePlayerTwo = "draw"
                        scorePlayerOne = "draw"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    }
                    3 -> {
                        scorePlayerTwo = "defeat"
                        scorePlayerOne = "victory"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    else -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        buttonLeaf.setBackgroundColor(Color.argb(58, 0, 0, 255))
                    }
                }

            }else if(playerTwoChoose == 3){

                when (playerOneChoose) {
                    1 -> {
                        scorePlayerTwo = "defeat"
                        scorePlayerOne = "victory"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    2 -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 0, 0, 255))
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    }
                    3 -> {
                        scorePlayerTwo = "draw"
                        scorePlayerOne = "draw"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    }
                    else -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        buttonSccissors.setBackgroundColor(Color.argb(58, 0, 0, 255))
                    }
                }

            }else{
                if(playerOneChoose != 0){
                    scorePlayerTwo = "defeat"
                    scorePlayerOne = "victory"
                    buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                }else{
                    scorePlayerTwo = "draw"
                    scorePlayerOne = "draw"
                    buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 255))
                    buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 255))
                }
            }

        }else{
            Toast.makeText(this, "External error! Soon will be fixed", Toast.LENGTH_SHORT).show()
        }

        Log.i("TAG", "Scores: $scorePlayerOne and $scorePlayerTwo")
        Log.i("TAG", "Scores: $scorePlayerOne and $scorePlayerTwo")
        updateScore(scorePlayerOne, scorePlayerTwo)
    }

    private fun updateScore(scorePlayerOne: String, scorePlayerTwo: String){
        Log.i("TAG UPDATE","Scores: $scorePlayerOne and $scorePlayerTwo")
        when (scorePlayerOne) {
            "victory" -> {
                var currentValuePlayerOne: Int = Integer.parseInt(textViewPlayerOneScore.text.toString())
                currentValuePlayerOne = currentValuePlayerOne + 1
                textViewPlayerOneScore.text = currentValuePlayerOne.toString()
            }
            "defeat" -> {
                var currentValuePlayerTwo: Int = Integer.parseInt(textViewPlayerTwoScore.text.toString())
                currentValuePlayerTwo = currentValuePlayerTwo + 1
                textViewPlayerTwoScore.text = currentValuePlayerTwo.toString()
            }
            else -> {
                //draw
            }
        }
    }

    override fun onStart() {
        super.onStart()
        timer.start()
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
    }
}