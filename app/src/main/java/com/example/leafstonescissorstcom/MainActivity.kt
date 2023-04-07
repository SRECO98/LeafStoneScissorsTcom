package com.example.leafstonescissorstcom

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var timer: CountDownTimer
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var roomsScoreRef: CollectionReference
    lateinit var roomsChooseRef: CollectionReference
    var playerChoose = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonStone: AppCompatButton = findViewById(R.id.imageStone)
        val buttonLeaf: AppCompatButton = findViewById(R.id.imageLeaf)
        val buttonSccissors: AppCompatButton = findViewById(R.id.imageSccissors)
        val buttonGo: AppCompatButton = findViewById(R.id.buttonChoose)
        val textViewPlayerOneName: TextView = findViewById(R.id.playerOneName)
        val textViewPlayerTwoName: TextView = findViewById(R.id.playerTwoName)
        val textViewTimer: TextView = findViewById(R.id.textViewTimer)
        val player1Name = intent.getStringExtra("player1Name")
        val player2Name = intent.getStringExtra("player2Name")
        val roomId: String = intent.getStringExtra("room_id")!!
        val player:Int = intent.getIntExtra("player", 0)
        roomsScoreRef = db.collection("rooms").document(roomId).collection("scores") // ???????????????????????
        roomsChooseRef = db.collection("rooms").document(roomId).collection("choose") // ???????????????????????
        createRoomForPicturesChoice()
        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: ${player1Name}")
        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: ${player2Name}")

        textViewPlayerOneName.text = player1Name
        textViewPlayerTwoName.text = player2Name

        var playerOneCurrentScore = 0
        var playerTwoCurrentScore = 0

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
            saveChoose(player)
        }

        timer = object : CountDownTimer(8000, 1000){
            override fun onTick(remaining: Long) {
                if(remaining < 4000){
                    textViewTimer.setTextColor(Color.argb(255,255,0,0))
                }
                if(remaining < 1000){
                    return
                }
                textViewTimer.text = ((remaining/1000).toString())
            }

            override fun onFinish() {
                textViewTimer.text = "8"
                textViewTimer.setTextColor(Color.argb(255,251,239,2))
                loadChoose(player)
            }
        }
    }



    private fun saveScores(){

    }
    private fun loadScores(){

    }


    private var chooseRoom = hashMapOf(
        "choosePlayer1" to "unknown",
        "choosePlayer2" to "unknown"
    )
    private fun saveChoose(player: Int){
        val roomsChooseRef2 = roomsChooseRef.document(id)
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

        val query = roomsChooseRef.whereEqualTo("choosePlayer1", "unknown2") //check did we created room, if value is changed we did.
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
    private fun loadChoose(player: Int){
        var playerOneChoose = "0"
        var playertwoChoose = "0"
        roomsChooseRef.document(id).get()
            .addOnSuccessListener {
                Log.i("TAG", "Getting choose data successed")
                playerOneChoose = it.getString("choosePlayer1")!!
                playertwoChoose = it.getString("choosePlayer2")!!
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting choose data failed")
            }
        calculateWinner(playerOneChoose.toInt(), playertwoChoose.toInt(), player)
    }

    private fun calculateWinner(playerOneChoose: Int, playerTwoChoose: Int, player: Int){
        var scorePlayerOne = ""
        if(player == 1){
            if(playerOneChoose == 1){
                //dodati else, ili 4tu petlju ako ne izabere slicicu drugi igrac...
                if(playerTwoChoose == 1){
                    scorePlayerOne = "draw"
                }else if(playerTwoChoose == 2){
                    scorePlayerOne = "defeat"
                }else{
                    scorePlayerOne = "victory"
                }

            }else if(playerOneChoose == 2){

                if(playerTwoChoose == 1){
                    scorePlayerOne = "victory"
                }else if(playerTwoChoose == 2){
                    scorePlayerOne = "draw"
                }else{
                    scorePlayerOne = "defeat"
                }
            }else if(playerOneChoose == 3){

                if(playerTwoChoose == 1){
                    scorePlayerOne = "defeat"
                }else if(playerTwoChoose == 2){
                    scorePlayerOne = "victory"
                }else{
                    scorePlayerOne = "draw"
                }
            }else{
                if(playerTwoChoose != 0){
                    scorePlayerOne = "defeat"
                }else{
                    scorePlayerOne = "draw"
                }
            }

        }else if(player == 2){

            if(playerTwoChoose == 1){

                if(playerOneChoose == 1){

                }else if(playerOneChoose == 2){

                }else if(playerOneChoose == 3){

                }else{

                }

            }else if(playerTwoChoose == 2){

                if(playerOneChoose == 1){

                }else if(playerOneChoose == 2){

                }else if(playerOneChoose == 3){

                }else{

                }

            }else if(playerTwoChoose == 3){

                if(playerOneChoose == 1){

                }else if(playerOneChoose == 2){

                }else if(playerOneChoose == 3){

                }else{

                }

            }else{

            }

        }else{
            Toast.makeText(this, "External error! Soon will be fixed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateScore(){

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