package com.example.leafstonescissorstcom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.leafstonescissorstcom.rematch.RematchMethods
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    //crystaldisk for checking quality of disk on server.
    private val NUMBER_OF_ROUNDS: Int = 5
    //2x se poziva saveChooice ako stisnemo pick
    private lateinit var timer: CountDownTimer
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var roomsChooseRef: DocumentReference
    private var playerChoose = "0"
    private lateinit var textViewPlayerOneScore: TextView
    private lateinit var textViewPlayerTwoScore: TextView
    private lateinit var buttonStone: AppCompatButton
    private lateinit var buttonLeaf: AppCompatButton
    private lateinit var buttonSccissors: AppCompatButton
    private lateinit var buttonGo: AppCompatButton
    private lateinit var roomId: String
    private var player1Name: String? = ""
    private var player2Name: String? = ""
    private var player1Email: String? = ""
    private var player2Email: String? = ""

    //Elements of dialog:
    private lateinit var textViewWinOrLose: TextView
    private lateinit var textViewPlayerOneName: TextView
    private lateinit var textViewPlayerTwoName: TextView
    private lateinit var textViewPlayerVSPlayer1: TextView
    private lateinit var textViewPlayerVSPlayer2: TextView
    private lateinit var textViewTotalWinsScore: TextView
    private lateinit var textViewTotalLosesScore: TextView
    private lateinit var buttonRematch: AppCompatButton
    private lateinit var buttonNewGame: AppCompatButton
    private lateinit var buttonAnalyze: AppCompatButton
    private var counterRounds: Int = 0
    var buttonChoose2 = "0"

    //to fast covering choice of another player (blue and red) make it a little longer, when same choice make it half red/half blue somehow

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
        player1Name = intent.getStringExtra("player1Name")
        player2Name = intent.getStringExtra("player2Name")
        player1Email = intent.getStringExtra("player1Email")
        player2Email = intent.getStringExtra("player2Email")
        Log.i("TAG", "emailovi!! $player1Email $player2Email")
        Log.i("TAG", "emailovi!! $player1Email $player2Email")
        roomId = intent.getStringExtra("room_id")!!
        val player:Int = intent.getIntExtra("player", 0)
        roomsChooseRef = db.collection("rooms").document(roomId)
        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: $player1Name")
        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: $player2Name")
        createNewHasMapStore()
        if(player == 1)
            gettingTotalWinsAndTotalLosesFromFirebase(player1Email!!)
        else
            gettingTotalWinsAndTotalLosesFromFirebase(player2Email!!)


        textViewPlayerOneName.text = player1Name
        textViewPlayerTwoName.text = player2Name

        buttonStone.setOnClickListener {
            buttonStone.setBackgroundColor(Color.argb(120, 255, 255, 0))
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonChoose2 = "1"
        }
        buttonLeaf.setOnClickListener {
            buttonLeaf.setBackgroundColor(Color.argb(120, 255, 255, 0))
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonChoose2 = "2"
        }
        buttonSccissors.setOnClickListener {
            buttonSccissors.setBackgroundColor(Color.argb(120, 255, 255, 0))
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonChoose2 = "3"
        }

        buttonGo.setOnClickListener {
            userPressedButtonLock = true
            if(buttonChoose2 == "0"){ //if one of players didnt chose a picture.
                Toast.makeText(this, "Please, choose one of the pictures!", Toast.LENGTH_SHORT).show()
            }else{
                GlobalScope.launch {
                    try {
                        playerChoose = buttonChoose2
                        saveChoose(player)
                    }catch (e: Exception){
                        Log.i("TAG", "exception global scope coroutine: $e")
                    }
                }

                buttonGo.isEnabled = false //turn off buttons because user checked his choice
                buttonStone.isEnabled = false
                buttonLeaf.isEnabled = false
                buttonSccissors.isEnabled = false
                buttonGo.setTextColor(Color.WHITE)
                buttonGo.setBackgroundColor(Color.argb(255, 169, 169, 169))
            }
        }
        timerFun(textViewTimer, player)
        timer.start()
        rematchMethods(roomId, player)

        builder = AlertDialog.Builder(this)
        customView = LayoutInflater.from(this).inflate(R.layout.custom_layout_dialog, null)
        builder.setView(customView)
        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //brise bijele uglove iz dialoga !!! (FINALLY)
    }

    var stopFirstRound: Boolean = false
    var userPressedButtonLock = false // if user didnt press button lock we must enter auto zero to firebase
    private fun timerFun (textViewTimer: TextView, player: Int){
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(remaining: Long) {

                if(stopFirstRound){
                    if(remaining in 7001..8100){
                        textViewTimer.text = ""
                        buttonGo.isEnabled = true //tur on buttons again cuz new timer will begin
                        buttonStone.isEnabled = true
                        buttonLeaf.isEnabled = true
                        buttonSccissors.isEnabled = true
                        buttonGo.setTextColor(Color.BLACK)
                        buttonGo.setBackgroundColor(Color.argb(255, 69, 194, 153))
                        playerChoose = "0"

                        buttonStone.setBackgroundColor(Color.argb(23, 198, 182, 54))
                        buttonLeaf.setBackgroundColor(Color.argb(23, 198, 182, 54))
                        buttonSccissors.setBackgroundColor(Color.argb(23, 198, 182, 54))
                    }
                }
                if (remaining < 4000) {
                    textViewTimer.setTextColor(Color.argb(255, 255, 0, 0))
                }
                textViewTimer.text = ((remaining / 1000).toString())
            }

            override fun onFinish() {
                buttonChoose2 = "0"
                counterRounds += 1
                textViewTimer.text = "9"
                Log.i("tag", "User pressed button: $userPressedButtonLock")
                if(!userPressedButtonLock){
                        GlobalScope.launch {
                            try {
                                saveChoose(player)
                            }catch (e: Exception){
                                Log.i("TAG", "exception global scope coroutine: $e")
                            }
                        }
                        buttonGo.isEnabled = false //turn off buttons because user checked his choice
                        buttonStone.isEnabled = false
                        buttonLeaf.isEnabled = false
                        buttonSccissors.isEnabled = false
                        buttonGo.setTextColor(Color.WHITE)
                        buttonGo.setBackgroundColor(Color.argb(255, 169, 169, 169))
                }else{
                    userPressedButtonLock = false
                }
                textViewTimer.setTextColor(Color.argb(255, 251, 239, 2))
                if (Integer.parseInt(textViewPlayerOneScore.text.toString()) < NUMBER_OF_ROUNDS && Integer.parseInt( //this doest get alst value on time because it needs 2-3 sec to update score on screen.
                        textViewPlayerTwoScore.text.toString()) < NUMBER_OF_ROUNDS){

                    val gr = Integer.parseInt(textViewPlayerOneScore.text.toString())
                    val gr2 = Integer.parseInt(textViewPlayerTwoScore.text.toString())
                    Log.i("TAG", "if in method onFinish: p1: $gr ")
                    Log.i("TAG", "if in method onFinish: p2: $gr2 ")
                    stopFirstRound = true
                    timer.start()
                }else{
                    dialogGameOver(player)
                }
                loadChoose(player)
            }
        }
    }

    private var chooseRoom = hashMapOf(
        "choosePlayer1" to "unknown",
        "choosePlayer2" to "unknown",
        "choosesArrayPlayer1" to "empty",
        "choosesArrayPlayer2" to "empty",
    )

    private lateinit var builder: AlertDialog.Builder
    private lateinit var customView: View
    private lateinit var dialog: AlertDialog

    @SuppressLint("SetTextI18n")
    private fun dialogGameOver(player: Int){

        //Dialog View elements
        textViewWinOrLose = customView.findViewById(R.id.textViewWinOrLose)
        textViewPlayerOneName = customView.findViewById(R.id.textViewPlayerOneName)
        textViewPlayerTwoName = customView.findViewById(R.id.textViewPlayerTwoName)
        textViewPlayerVSPlayer1 = customView.findViewById(R.id.textViewPlayerVSPlayer1)
        textViewPlayerVSPlayer2 = customView.findViewById(R.id.textViewPlayerVSPlayer2)
        textViewTotalWinsScore = customView.findViewById(R.id.textViewTotalWinsScore)
        textViewTotalLosesScore = customView.findViewById(R.id.textViewTotalLosesScore)
        buttonRematch = customView.findViewById(R.id.buttonRematch)
        buttonNewGame = customView.findViewById(R.id.buttonNewGame)
        buttonAnalyze = customView.findViewById(R.id.buttonAnalyze)

        //calculating values for jumping dialog when game is over between two players
        if(player == 1){
            if(currentValuePlayerOne == 5){
                textViewWinOrLose.text = "Victory"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer1.text.toString())
                textViewPlayerVSPlayer1.text = (scores + 1).toString()
                val totalWins2: Int = Integer.parseInt(totalWins) + 1  //getting value from base before game plus 1
                textViewTotalWinsScore.text = totalWins2.toString() //putting taht value on screen
                textViewTotalLosesScore.text = totalLoses // same value we get from base passing to screen because it didnt change
                updatingTotalWinsAndTotalLosesInFirebase(player1Email!!, totalWins2.toString(), totalLoses) //adding new values to base
            }else{
                textViewWinOrLose.text = "Defeat"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer2.text.toString())
                textViewPlayerVSPlayer2.text = (scores + 1).toString()
                val totalLoses2: Int = Integer.parseInt(totalLoses) + 1
                textViewTotalWinsScore.text = totalWins
                textViewTotalLosesScore.text = totalLoses2.toString()
                updatingTotalWinsAndTotalLosesInFirebase(player1Email!!, totalWins, totalLoses2.toString())
            }
        }else{ //player == 2
            if(currentValuePlayerTwo == 5){
                textViewWinOrLose.text = "Victory"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer2.text.toString())
                textViewPlayerVSPlayer2.text = (scores + 1).toString()
                val totalWins2: Int = Integer.parseInt(totalWins) + 1
                textViewTotalWinsScore.text = totalWins2.toString()
                textViewTotalLosesScore.text = totalLoses
                updatingTotalWinsAndTotalLosesInFirebase(player2Email!!, totalWins2.toString(), totalLoses)
            }else{
                textViewWinOrLose.text = "Defeat"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer1.text.toString())
                textViewPlayerVSPlayer1.text = (scores + 1).toString()
                val totalLoses2: Int = Integer.parseInt(textViewTotalLosesScore.text.toString()) + 1
                textViewTotalWinsScore.text = totalWins
                textViewTotalLosesScore.text = totalLoses2.toString()
                updatingTotalWinsAndTotalLosesInFirebase(player2Email!!, totalWins, totalLoses2.toString())
            }
        }

        textViewPlayerOneName.text = player1Name
        textViewPlayerTwoName.text = player2Name

        dialog.show()

        buttonNewGame.setOnClickListener {
            val intent: Intent = Intent(this, StartActivity::class.java).apply {
                if(player == 1){
                    putExtra("name", player1Name)
                }else{
                    putExtra("name", player2Name)
                }
            }
            startActivity(intent)
            finish()
        }

        buttonRematch.setOnClickListener {
            rematchMethods.sendRematch(player = player, roomId = roomId)
            buttonRematch.isEnabled = false
            /*textViewPlayerOneScore.text = "0"
            textViewPlayerTwoScore.text = "0"
            buttonStone.setBackgroundColor(Color.argb(23, 198, 182, 54))
            buttonLeaf.setBackgroundColor(Color.argb(23, 198, 182, 54))
            buttonSccissors.setBackgroundColor(Color.argb(23, 198, 182, 54))
            buttonGo.setTextColor(Color.BLACK)
            buttonGo.setBackgroundColor(Color.argb(255, 69, 194, 153))
            buttonGo.isEnabled = true
            playerChoose = "0"
            timer.start()
            dialog.cancel()*/
        }

        buttonAnalyze.setOnClickListener {
            loadAnalyzedActivity()
        }
    }

    fun rematchMatchmaking(){
        this.dialog.dismiss() //closing dialog with scores
    }

    private fun loadAnalyzedActivity() {
        val intent = Intent(this, AnalyzedGameActivity::class.java).apply {
            putExtra("player1Name", player1Name)
            putExtra("player2Name", player2Name)
            putExtra("id", roomId)
        }
        startActivity(intent)
    }

    private fun createNewHasMapStore() { //setting document in collection for score of players
        val roomsChooseRef2 = roomsChooseRef
        roomsChooseRef2.set(chooseRoom as Map<String, Any>, SetOptions.merge())
            .addOnSuccessListener {
                Log.i("Choose", "Successfully added picture choice")
            }
            .addOnFailureListener {
                Log.i("Choose", "Failed while adding picture choice $it")
            }
    }

    private val choosesPlayer1 = arrayListOf<Int>()
    private val choosesPlayer2 = arrayListOf<Int>()
    private fun saveChoose(player: Int){
        var i = 0
        i += 1
        Log.i("Choose", "saveChoose function called $i")
        //updating picture of player one in room
        if(player == 1){
            choosesPlayer1.add(playerChoose.toInt())
            Log.i("TAG12", "choosesPlayer1.toString()")
            try{
                roomsChooseRef.update("choosePlayer1", playerChoose)
                    .addOnSuccessListener {
                        Log.i("Choose", "Successfully added picture choice for player 2")
                    }
                    .addOnFailureListener {
                        Log.i("Choose", "Failed while adding picture choice for player 2: $it")
                    }
                roomsChooseRef.update("choosesArrayPlayer1", choosesPlayer1)
                    .addOnSuccessListener {
                        Log.i("Choose", "Successfully added picture choice for player 2")
                    }
                    .addOnFailureListener {
                        Log.i("Choose", "Failed while adding picture choice for player 2: $it")
                    }
            }catch (e: Exception){
                Log.i("TAG", "EXCEPTION IS: $e")
            }
            Log.i("TAG", "Checking update in saveChoose")
        }else if(player == 2){ //updating picture of player two in room
            choosesPlayer2.add(playerChoose.toInt())
            try{
                roomsChooseRef.update("choosePlayer2", playerChoose)
                    .addOnSuccessListener {
                        Log.i("Choose", "Successfully added picture choice for player 2")
                    }
                    .addOnFailureListener {
                        Log.i("Choose", "Failed while adding picture choice for player 2: $it")
                    }
                roomsChooseRef.update("choosesArrayPlayer2", choosesPlayer2)
                    .addOnSuccessListener {
                        Log.i("Choose", "Successfully added picture choice for player 2")
                    }
                    .addOnFailureListener {
                        Log.i("Choose", "Failed while adding picture choice for player 2: $it")
                    }
            }catch (e: Exception){
                Log.i("TAG", "EXCEPTION IS: $e")
            }
        }
    }

    private fun resetChooseToZero(player: Int){
        Log.i("Choose", "saveChoose function called")
        //updating picture of player one in room
        if(player == 1){
            Log.i("TAG12", "choosesPlayer1.toString()")
            try{
                roomsChooseRef.update("choosePlayer1", playerChoose)
                    .addOnSuccessListener {
                        Log.i("Choose", "Successfully added picture choice for player 2")
                    }
                    .addOnFailureListener {
                        Log.i("Choose", "Failed while adding picture choice for player 2: $it")
                    }
            }catch (e: Exception){
                Log.i("TAG", "EXCEPTION IS: $e")
            }
            Log.i("TAG", "Checking update in saveChoose")
        }else if(player == 2){ //updating picture of player two in room
            try{
                roomsChooseRef.update("choosePlayer2", playerChoose)
                    .addOnSuccessListener {
                        Log.i("Choose", "Successfully added picture choice for player 2")
                    }
                    .addOnFailureListener {
                        Log.i("Choose", "Failed while adding picture choice for player 2: $it")
                    }
            }catch (e: Exception){
                Log.i("TAG", "EXCEPTION IS: $e")
            }
        }
    }

    private fun loadChoose(player: Int){
        var playerOneChoose: String
        var playerTwoChoose: String
        roomsChooseRef.get()
            .addOnSuccessListener {
                Log.i("TAG", "Getting choose data successed")
                playerOneChoose = it.getString("choosePlayer1")!!
                playerTwoChoose = it.getString("choosePlayer2")!!
                if(playerOneChoose == "unknown" && playerTwoChoose == "unknown" ){
                    playerOneChoose = "0"
                    playerTwoChoose = "0"
                }else if(playerOneChoose == "unknown"){
                    playerOneChoose = "0"
                }else if(playerTwoChoose == "unknown"){
                    playerTwoChoose = "0"
                }
                Log.i("TAG", "Players choose before calculating: p1: $playerOneChoose ")
                Log.i("TAG", "Players choose before calculating: p2: $playerTwoChoose ")
                calculateWinner(playerOneChoose.toInt(), playerTwoChoose.toInt(), player)
                //reset on zero in case someone didnt lock anything so it doesnt take last results.
                playerChoose = "0"
                resetChooseToZero(player = 1)
                resetChooseToZero(player = 2)
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting choose data failed")
            }
    }

    private fun calculateWinner(playerOneChoose: Int, playerTwoChoose: Int, player: Int){

        Log.i("TAG", "Player Choose in calculation function: p1: $playerOneChoose")
        Log.i("TAG", "Player Choose in calculation function: p2: $playerTwoChoose")

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
                    if(playerTwoChoose == 1)
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    else if(playerTwoChoose == 2)
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    else
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 0))
                }else{
                    scorePlayerOne = "draw"
                    scorePlayerTwo = "draw"
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
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
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
                    if(playerOneChoose == 1)
                        buttonStone.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    else if(playerOneChoose == 2)
                        buttonLeaf.setBackgroundColor(Color.argb(58, 255, 0, 0))
                    else
                        buttonSccissors.setBackgroundColor(Color.argb(58, 255, 0, 0))
                }else{
                    scorePlayerTwo = "draw"
                    scorePlayerOne = "draw"
                }
            }

        }else{
            Toast.makeText(this, "External error! Soon will be fixed", Toast.LENGTH_SHORT).show()
        }

        Log.i("TAG", "Scores: $scorePlayerOne and $scorePlayerTwo")
        Log.i("TAG", "Scores: $scorePlayerOne and $scorePlayerTwo")
        updateScore(scorePlayerOne, scorePlayerTwo)
    }

    private var currentValuePlayerOne: Int = 0
    private var currentValuePlayerTwo: Int = 0
    private fun updateScore(scorePlayerOne: String, scorePlayerTwo: String){
        Log.i("TAG UPDATE","Scores: $scorePlayerOne and $scorePlayerTwo")

        when (scorePlayerOne) {
            "victory" -> {
                currentValuePlayerOne = Integer.parseInt(textViewPlayerOneScore.text.toString())
                currentValuePlayerOne = currentValuePlayerOne + 1
                textViewPlayerOneScore.text = currentValuePlayerOne.toString()
            }
            "defeat" -> {
                currentValuePlayerTwo = Integer.parseInt(textViewPlayerTwoScore.text.toString())
                currentValuePlayerTwo = currentValuePlayerTwo + 1
                textViewPlayerTwoScore.text = currentValuePlayerTwo.toString()
            }
            else -> {
                //draw
            }
        }
    }

    private fun updatingTotalWinsAndTotalLosesInFirebase(email: String, totalWins: String, totalLoses: String) {
        val docRef = db.collection("nameOfUsers").document(email)
        try{
            docRef.update("totalWins", totalWins, "totalLoses", totalLoses)
                .addOnSuccessListener {
                    Log.i("Choose", "Successfully added plus one to total wins in FB")
                }
                .addOnFailureListener {
                    Log.i("Choose", "Failed while adding plus one to FB for totalWins")
                }
        }catch (e: Exception){
                Log.i("TAG", "EXCEPTION IS: $e")
        }
    }

    private var totalWins: String = "0"
    private var totalLoses: String = "0"
    private fun gettingTotalWinsAndTotalLosesFromFirebase(email: String) {
        val docRef = db.collection("nameOfUsers").document(email)
        docRef.get()
            .addOnSuccessListener {
                totalWins = it.getString("totalWins")!!
                totalLoses = it.getString("totalLoses")!!
                Log.i("TAG", "Getting total wins successed $totalWins")
                Log.i("TAG", "Getting total loses successed $totalLoses")
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting total wins or loses failed")
            }
    }

    /*REMATCH LOGIC*******************************************************************************************/
    val rematchMethods: RematchMethods = RematchMethods()
    private fun rematchMethods(roomId: String, player: Int) {

        rematchMethods.createRematchFieldsInFirebase(roomId = roomId)
        rematchMethods.setListenerToFields(player = player, roomId = roomId, this)
    }
    /*********************************************************************************************************/
}