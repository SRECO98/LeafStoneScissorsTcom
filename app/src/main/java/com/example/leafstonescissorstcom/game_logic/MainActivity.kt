package com.example.leafstonescissorstcom.game_logic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.leafstonescissorstcom.R
import com.example.leafstonescissorstcom.game_logic.analysis.AnalyzedGameActivity
import com.example.leafstonescissorstcom.game_logic.firebase.FirebaseMethods
import com.example.leafstonescissorstcom.game_logic.firebase.TotalWinLose
import com.example.leafstonescissorstcom.game_logic.firebase.UpdateScore
import com.example.leafstonescissorstcom.game_logic.matching_users.StartActivity
import com.example.leafstonescissorstcom.game_logic.rematch.RematchMethods
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), RematchMethods.RematchListener, FirebaseMethods.ChangeColorListener {
    //crystaldisk for checking quality of disk on server.
    private val NUMBER_OF_ROUNDS: Int = 5
    private val TOKEN_NUMBER_GET_OR_LOSE = 50

    //2x se poziva saveChooice ako stisnemo pick
    //find a way to make a tour table (players added in first row, now add after finding who is winner and add scores after game is finished).
    private lateinit var timer: CountDownTimer
    val updateScore = UpdateScore()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var roomsChooseRef: DocumentReference
    private lateinit var secondRoomRefDoc: DocumentReference
    private lateinit var firstRoomRefDoc: DocumentReference

    var playerChoose = "0"
    private lateinit var textViewPlayerOneScore: TextView
    private lateinit var textViewPlayerTwoScore: TextView
    private lateinit var buttonStone: AppCompatButton
    private lateinit var buttonLeaf: AppCompatButton
    private lateinit var buttonSccissors: AppCompatButton
    private lateinit var buttonGo: AppCompatButton
    private lateinit var roomId: String

    private var numberOfPlayers = ""
    private var kindOfGame: String? = ""
    private var player1Name: String? = ""
    private var player2Name: String? = ""
    private var player1Email: String? = ""
    private var player2Email: String? = ""
    private var player1Tokens: String? = ""
    private var player2Tokens: String? = ""
    private var documentSecondRoomRefDoc: String? = ""
    private var documentFirstRoomRefDoc: String? = ""
    private var currentPlayerField: String = ""
    private var player1TokensAfterGame: Int = 0
    private var player2TokensAfterGame: Int = 0

    //Elements of dialog:
    private lateinit var textViewWinOrLose: TextView
    private lateinit var textViewPlayerOneName: TextView
    private lateinit var textViewPlayerTwoName: TextView
    private lateinit var textViewPlayerVSPlayer1: TextView
    private lateinit var textViewPlayerVSPlayer2: TextView
    private lateinit var textViewTotalWinsScore: TextView
    private lateinit var textViewTotalLosesScore: TextView
    private lateinit var textViewTokens: TextView
    private lateinit var buttonRematch: AppCompatButton
    private lateinit var buttonNewGame: AppCompatButton
    private lateinit var buttonAnalyze: AppCompatButton
    private var counterRounds: Int = 0
    var buttonChoose2 = "0"
    var roundOfGame = ""
    private lateinit var firebaseMethods: FirebaseMethods
    private lateinit var firebaseTotalWinLose: TotalWinLose
    private var totalWins: String = "0"
    private var totalLoses: String = "0"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseTotalWinLose = TotalWinLose()
        firebaseMethods = FirebaseMethods()
        firebaseMethods.setChangeColorListener(this)

        buttonStone = findViewById(R.id.imageStone)
        buttonLeaf = findViewById(R.id.imageLeaf)
        buttonSccissors = findViewById(R.id.imageSccissors)
        buttonGo = findViewById(R.id.buttonChoose)
        textViewPlayerOneScore = findViewById(R.id.playerOneScore)
        textViewPlayerTwoScore = findViewById(R.id.playerTwoScore)
        val textViewPlayerOneName: TextView = findViewById(R.id.playerOneName)
        val textViewPlayerTwoName: TextView = findViewById(R.id.playerTwoName)
        val textViewTimer: TextView = findViewById(R.id.textViewTimer)

        //getting reference to score of player and name of players in firebase
        documentFirstRoomRefDoc = intent.getStringExtra("score_ref")!!  //getting document from StartActivity
        firstRoomRefDoc = FirebaseFirestore.getInstance().document(documentFirstRoomRefDoc!!) //finding real ref by documentPath

        //reference of game logic in firebase
        documentSecondRoomRefDoc = intent.getStringExtra("roomsRef")!!
        secondRoomRefDoc = FirebaseFirestore.getInstance().document(documentSecondRoomRefDoc!!)
        kindOfGame = intent.getStringExtra("kindOfGame")
        player1Name = intent.getStringExtra("player1Name")
        player2Name = intent.getStringExtra("player2Name")
        player1Email = intent.getStringExtra("player1Email")
        player2Email = intent.getStringExtra("player2Email")
        player1Tokens = intent.getStringExtra("player1Tokens")
        player2Tokens = intent.getStringExtra("player2Tokens")
        currentPlayerField = intent.getStringExtra("current")!!
        roundOfGame = intent.getStringExtra("roundOfGame")!!
        numberOfPlayers = intent.getStringExtra("numberOfPlayers")!!
        roomId = intent.getStringExtra("room_id")!!
        val player:Int = intent.getIntExtra("player", 0)
        roomsChooseRef = db.collection("rooms").document(roomId)
        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: $player1Name")
        Log.i("TAG4", "VALUE ON NEXT ACTIVITY IS: $player2Name")
        if(kindOfGame == "solo"){
            firebaseMethods.createNewHasMapStore(roomsChooseRef)
        }else if(kindOfGame == "group"){
            updateScore.updateStatus2AndCurrent(firstRoomRefDoc)
            firebaseMethods.createNewHasMapStore(secondRoomRefDoc)
        }
        if(player == 1)
            firebaseTotalWinLose.gettingTotalWinsAndTotalLosesFromFirebase(db.collection("nameOfUsers").document(player1Email!!),
                onSuccess = {totalWins, totalloses ->  //sendng lambda fun like this cuz val cant be changed after sending as argument
                    this.totalWins = totalWins
                    this.totalLoses = totalloses
                })
        else
            firebaseTotalWinLose.gettingTotalWinsAndTotalLosesFromFirebase(db.collection("nameOfUsers").document(player2Email!!),
                onSuccess = {totalWins, totalloses ->
                    this.totalWins = totalWins
                    this.totalLoses = totalloses
                })

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
            if(buttonChoose2 == "0"){ //if one of players didnt chose a picture.
                Toast.makeText(this, "Please, choose one of the pictures!", Toast.LENGTH_SHORT).show()
            }else{
                GlobalScope.launch () {
                    try {
                        playerChoose = buttonChoose2
                        if(kindOfGame == "solo")
                            firebaseMethods.saveChoose(player, playerChoose, roomsChooseRef)
                        else
                            firebaseMethods.saveChoose(player, playerChoose, secondRoomRefDoc)
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
        timerFun(textViewTimer, player, this)
        timer.start()

        builder = AlertDialog.Builder(this)
        customView = LayoutInflater.from(this).inflate(R.layout.custom_layout_dialog, null)
        builder.setView(customView)
        dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //brise bijele uglove iz dialoga !!! (FINALLY)

        rematchMethods(roomId, player)
    }

    var stopFirstRound: Boolean = false
    private fun timerFun (textViewTimer: TextView, player: Int, context: Context){
        timer = object : CountDownTimer(13000, 1000) {
            override fun onTick(remaining: Long) {

                if(stopFirstRound){
                    if(remaining in 9001..11100){
                        if(doOnlyOnce2 == true){
                            doOnlyOnce2 = false //we will put it on true after finishing onTick
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
                }
                if (remaining in 3001..6999) {
                    textViewTimer.setTextColor(Color.argb(255, 255, 0, 0))
                }
                if(remaining > 3001){
                    textViewTimer.text = (((remaining - 3000) / 1000).toString()) //putting counter on screen
                }else{
                    if(doOnlyOnce == true){
                        doOnlyOnce = false //we will put it on true after finishing onTick
                        textViewTimer.text = ""
                        if(kindOfGame == "solo"){  //saving picks in firebase
                            firebaseMethods.saveChoose(player, playerChoose, roomsChooseRef)
                        }else{
                            firebaseMethods.saveChoose(player, playerChoose, secondRoomRefDoc)
                        }
                        startDelay(player, roomsChooseRef) //getting data 1 sec after
                        buttonGo.isEnabled = false //turn off buttons because user checked his choice
                        buttonStone.isEnabled = false
                        buttonLeaf.isEnabled = false
                        buttonSccissors.isEnabled = false
                        buttonGo.setTextColor(Color.WHITE)
                        buttonGo.setBackgroundColor(Color.argb(255, 169, 169, 169))
                    }
                }
            }

            override fun onFinish() {
                doOnlyOnce = true //next round it will open if at the end of round once.
                doOnlyOnce2 = true //next round it will open if at the end of round once.
                buttonChoose2 = "0"
                counterRounds += 1
                textViewTimer.text = "9"

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

                    if(kindOfGame == "solo"){
                        dialogGameOver(player)
                    }

                    else if(kindOfGame == "group"){

                        Log.i("TAG", "Round of game is: $roundOfGame")
                        val current = ( currentPlayerField.toInt() + 1 ) / 2
                        if(player == 1){

                            if(currentValuePlayerOne == NUMBER_OF_ROUNDS){
                                beginStartActivity(context, player1Name!!, player1Email!!, player1Tokens!!, current)
                                //add 1 to score field and name for next table
                                if(roundOfGame == "1"){
                                    updateScore.updateScoreFromTableInFB("1", "score"+currentPlayerField, firstRoomRefDoc) //jos prepraviti kada se upisuje za player21 itd..
                                    updateScore.updateNamesFromTableInFB(player1Name!!, "player2"+current.toString(), firstRoomRefDoc)
                                }else{
                                    updateScore.updateScoreFromTableInFB("1", "score"+roundOfGame+currentPlayerField, firstRoomRefDoc) //jos prepraviti kada se upisuje za player21 itd..
                                    updateScore.updateNamesFromTableInFB(player1Name!!, "player"+(roundOfGame.toInt()+1).toString()+current.toString(), firstRoomRefDoc)
                                }
                            }else{
                                if(roundOfGame == "1"){
                                    //add 0 to score field
                                    updateScore.updateScoreFromTableInFB("0", "score"+currentPlayerField, firstRoomRefDoc)
                                    //call dialog lost tournament
                                }else{
                                    updateScore.updateScoreFromTableInFB("0", "score"+roundOfGame+currentPlayerField, firstRoomRefDoc)
                                }
                            }

                        }else if(player == 2){

                            if (currentValuePlayerTwo == NUMBER_OF_ROUNDS){
                                beginStartActivity(context, player1Name!!, player2Email!!, player2Tokens!!, current)
                                //add 1 to score field and name for next table
                                if(roundOfGame == "1"){
                                    updateScore.updateScoreFromTableInFB("1", "score"+currentPlayerField, firstRoomRefDoc)
                                    updateScore.updateNamesFromTableInFB(player2Name!!, "player2"+current.toString(), firstRoomRefDoc)
                                }else{
                                    updateScore.updateScoreFromTableInFB("1", "score"+roundOfGame+currentPlayerField, firstRoomRefDoc)
                                    updateScore.updateNamesFromTableInFB(player2Name!!, "player"+(roundOfGame.toInt()+1).toString()+current.toString(), firstRoomRefDoc)
                                }

                            }else{
                                if(roundOfGame == "1"){
                                    //add 0 to score field
                                    updateScore.updateScoreFromTableInFB("0", "score"+currentPlayerField, firstRoomRefDoc)
                                    //call dialog lost tournament
                                }else{
                                    updateScore.updateScoreFromTableInFB("0", "score"+roundOfGame+currentPlayerField, firstRoomRefDoc)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun beginStartActivity(context: Context ,playerName: String, playerEmail: String, playerToken: String, current: Int){
        Log.i("TAG", "If openned once!")

        val intent = Intent(context, StartActivity::class.java).apply {
            putExtra("skip_once", "false")
            putExtra("current", current.toString())
            putExtra("roomsRef", documentFirstRoomRefDoc)
            putExtra("name", playerName)
            putExtra("email", playerEmail)
            putExtra("tokens", playerToken) //???
            putExtra("roundOfGame", (roundOfGame.toInt() + 1).toString())
            putExtra("numberOfPlayers", (numberOfPlayers.toInt() / 2).toString() )
        }
        startActivity(intent)
    }

    private fun startDelay(player: Int, roomsChooseRef: DocumentReference) { //delegating for 2 seconds because there is a little delay in getting data from firebase.
        val delayMillis = 1250L
        Handler(Looper.getMainLooper()).postDelayed({
            if(kindOfGame == "solo"){
                firebaseMethods.loadChoose(player = player, roomsChooseRef = roomsChooseRef)
            }else{
                firebaseMethods.loadChoose(player = player, roomsChooseRef = secondRoomRefDoc)
            }
        }, delayMillis)
    }

    var doOnlyOnce: Boolean = true
    var doOnlyOnce2: Boolean = true

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
        textViewTokens = customView.findViewById(R.id.textViewTokensDialog)
        buttonRematch = customView.findViewById(R.id.buttonRematch)
        buttonNewGame = customView.findViewById(R.id.buttonNewGame)
        buttonAnalyze = customView.findViewById(R.id.buttonAnalyze)

        //calculating values for jumping dialog when game is over between two players
        if(player == 1){
            if(currentValuePlayerOne == NUMBER_OF_ROUNDS){
                textViewWinOrLose.text = "Victory"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer1.text.toString())
                textViewPlayerVSPlayer1.text = (scores + 1).toString()
                val totalWins2: Int = Integer.parseInt(totalWins) + 1  //getting value from base before game plus 1
                textViewTotalWinsScore.text = totalWins2.toString() //putting taht value on screen
                textViewTotalLosesScore.text = totalLoses // same value we get from base passing to screen because it didnt change
                player1TokensAfterGame = player1Tokens!!.toInt() + TOKEN_NUMBER_GET_OR_LOSE
                textViewTokens.text = player1TokensAfterGame.toString()
                firebaseTotalWinLose.updatingTotalWinsTotalLosesTokensInFB(totalWins2.toString(), totalLoses, player1TokensAfterGame.toString(), db.collection("nameOfUsers").document(player1Email!!)) //adding new values to base
            }else{
                textViewWinOrLose.text = "Defeat"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer2.text.toString())
                textViewPlayerVSPlayer2.text = (scores + 1).toString()
                val totalLoses2: Int = Integer.parseInt(totalLoses) + 1
                textViewTotalWinsScore.text = totalWins
                textViewTotalLosesScore.text = totalLoses2.toString()
                player1TokensAfterGame = player1Tokens!!.toInt() - TOKEN_NUMBER_GET_OR_LOSE
                textViewTokens.text = player1TokensAfterGame.toString()
                firebaseTotalWinLose.updatingTotalWinsTotalLosesTokensInFB(totalWins, totalLoses2.toString(), player1TokensAfterGame.toString(), db.collection("nameOfUsers").document(player1Email!!))
            }
        }else{ //player == 2
            if(currentValuePlayerTwo == NUMBER_OF_ROUNDS){
                textViewWinOrLose.text = "Victory"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer2.text.toString())
                textViewPlayerVSPlayer2.text = (scores + 1).toString()
                val totalWins2: Int = Integer.parseInt(totalWins) + 1
                textViewTotalWinsScore.text = totalWins2.toString()
                textViewTotalLosesScore.text = totalLoses
                player2TokensAfterGame = player2Tokens!!.toInt() + TOKEN_NUMBER_GET_OR_LOSE
                textViewTokens.text = player2TokensAfterGame.toString()
                firebaseTotalWinLose.updatingTotalWinsTotalLosesTokensInFB(totalWins2.toString(), totalLoses, player2TokensAfterGame.toString(), db.collection("nameOfUsers").document(player2Email!!))
            }else{
                textViewWinOrLose.text = "Defeat"
                val scores: Int = Integer.parseInt(textViewPlayerVSPlayer1.text.toString())
                textViewPlayerVSPlayer1.text = (scores + 1).toString()
                val totalLoses2: Int = Integer.parseInt(textViewTotalLosesScore.text.toString()) + 1
                textViewTotalWinsScore.text = totalWins
                textViewTotalLosesScore.text = totalLoses2.toString()
                player2TokensAfterGame = player2Tokens!!.toInt() - TOKEN_NUMBER_GET_OR_LOSE
                textViewTokens.text = player2TokensAfterGame.toString()
                firebaseTotalWinLose.updatingTotalWinsTotalLosesTokensInFB(totalWins, totalLoses2.toString(), player2TokensAfterGame.toString(), db.collection("nameOfUsers").document(player2Email!!))
            }
        }

        textViewPlayerOneName.text = player1Name
        textViewPlayerTwoName.text = player2Name

        dialog.show()

        buttonNewGame.setOnClickListener {
            val intent: Intent = Intent(this, StartActivity::class.java).apply {
                if(player == 1){
                    putExtra("name", player1Name)
                    putExtra("email", player1Email)
                    putExtra("tokens", player1TokensAfterGame.toString()) //sending new token score
                }else{
                    putExtra("name", player2Name)
                    putExtra("email", player2Email)
                    putExtra("tokens", player2TokensAfterGame.toString()) //sending new token score
                }
            }
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        buttonRematch.setOnClickListener {
            rematchMethods.sendRematch(player = player, roomId = roomId)
            buttonRematch.isEnabled = false
        }

        buttonAnalyze.setOnClickListener {
            loadAnalyzedActivity()
        }
    }

    private fun loadAnalyzedActivity() {
        val intent = Intent(this, AnalyzedGameActivity::class.java).apply {
            putExtra("player1Name", player1Name)
            putExtra("player2Name", player2Name)
            putExtra("id", roomId)
        }
        startActivity(intent)
    }

    /*REMATCH LOGIC*******************************************************************************************/
    val rematchMethods: RematchMethods = RematchMethods()
    private fun rematchMethods(roomId: String, player: Int) {

        rematchMethods.setRematchListener(this)
        rematchMethods.createRematchFieldsInFirebase(roomId = roomId)
        rematchMethods.setListenerToFields(player = player, roomId = roomId, this)
    }

    override fun onRematch() {
        Log.i("TAG", "Rematch fun from main activity")
        dialog.dismiss() //closing dialog with scores
        textViewPlayerOneScore.text = "0"
        textViewPlayerTwoScore.text = "0"
        buttonStone.setBackgroundColor(Color.argb(23, 198, 182, 54))
        buttonLeaf.setBackgroundColor(Color.argb(23, 198, 182, 54))
        buttonSccissors.setBackgroundColor(Color.argb(23, 198, 182, 54))
        buttonGo.setTextColor(Color.BLACK)
        buttonGo.setBackgroundColor(Color.argb(255, 69, 194, 153))
        buttonGo.isEnabled = true
        playerChoose = "0"
        timer.start()
    }
    /*A***********************************************************************************************************/

    /*FIREBASE LOGIC AND CALCULATION OF POINTS*-*************************************************************************/
    //Fun for changing color of fields after every round.
    override fun buttonChangeColor(stoneR: Int, stoneG: Int, stoneB: Int, leafR: Int, leafG: Int, leafB: Int, scissorsR: Int, scissorsG: Int, scissorsB: Int) {
        buttonStone.setBackgroundColor(Color.argb(58, stoneR, stoneG, stoneB))
        buttonLeaf.setBackgroundColor(Color.argb(58, leafR, leafG, leafB))
        buttonSccissors.setBackgroundColor(Color.argb(58, scissorsR, scissorsG, scissorsB))
    }

    private var currentValuePlayerOne: Int = 0
    private var currentValuePlayerTwo: Int = 0
    override fun updateScore(scorePlayerOne: String, scorePlayerTwo: String) {

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
    /***********************************************************************************************************/
}