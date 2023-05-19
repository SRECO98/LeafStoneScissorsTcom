package com.example.leafstonescissorstcom.firebase

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.leafstonescissorstcom.MainActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirebaseMethods {

    private val choosesPlayer1 = arrayListOf<Int>()
    private val choosesPlayer2 = arrayListOf<Int>()

    fun saveChoose(player: Int, playerChoose: String, roomsChooseRef: DocumentReference){
        //updating picture of player one in room
        if(player == 1){
            choosesPlayer1.add(playerChoose.toInt())
            try{
                roomsChooseRef.update("choosePlayer1", playerChoose, "choosesArrayPlayer1", choosesPlayer1)
                    .addOnSuccessListener {
                        Log.i("Choose", "Successfully added picture choice for player 1")
                    }
                    .addOnFailureListener {
                        Log.i("Choose", "Failed while adding picture choice for player 1: $it")
                    }
            }catch (e: Exception){
                Log.i("TAG", "EXCEPTION IS: $e")
            }
        }else if(player == 2){ //updating picture of player two in room
            choosesPlayer2.add(playerChoose.toInt())
            try{
                roomsChooseRef.update("choosePlayer2", playerChoose, "choosesArrayPlayer2", choosesPlayer2)
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
        Log.i("saveChoose", "Function is over.")
    }

    fun loadChoose(player: Int, roomsChooseRef: DocumentReference){

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
                startDelay(player = player, roomsChooseRef = roomsChooseRef)
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting choose data failed")
            }
    }

    private val delayMillis: Long = 2000 // 5 seconds
    private fun startDelay(player: Int, roomsChooseRef: DocumentReference) { //delegating for 2 seconds because there is a little delay in getting data from firebase.
        Handler(Looper.getMainLooper()).postDelayed({
            val mainActivity = MainActivity()
            //reset on zero in case someone didnt lock anything so it doesnt take last results.
            mainActivity.playerChoose = "0"

            if(player == 1){
                resetChooseToZero(player = 1, roomsChooseRef = roomsChooseRef)
            }else if(player == 2){
                resetChooseToZero(player = 2, roomsChooseRef = roomsChooseRef)
            }
            // This code will run after the specified delay
            // Perform your task here
        }, delayMillis)
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
                        firebaseListener?.buttonChangeColor(stoneR = 255, stoneG = 0, stoneB = 255)
                    }
                    2 -> {
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
                        firebaseListener?.buttonChangeColor(stoneR = 0, stoneG = 0, stoneB = 255, leafR = 255, leafG = 0, leafB = 0)
                    }
                    3 -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        firebaseListener?.buttonChangeColor(stoneR = 0, stoneG = 0, stoneB = 255, scissorsR = 255, scissorsG = 0, scissorsB = 0)
                    }
                    else -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        firebaseListener?.buttonChangeColor(stoneR = 0, stoneG = 0, stoneB = 255)
                    }
                }

            }else if(playerOneChoose == 2){

                when (playerTwoChoose) {
                    1 -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        firebaseListener?.buttonChangeColor(stoneR = 255, stoneG = 0, stoneB = 0, leafR = 0, leafG = 0, leafB = 255)
                    }
                    2 -> {
                        scorePlayerOne = "draw"
                        scorePlayerTwo = "draw"
                        firebaseListener?.buttonChangeColor(leafR = 255, leafG = 0, leafB = 255)
                    }
                    3 -> {
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
                        firebaseListener?.buttonChangeColor(leafR = 0, leafG = 0, leafB = 255, scissorsR = 255, scissorsG = 0, scissorsB = 0)
                    }
                    else -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        firebaseListener?.buttonChangeColor(leafR = 0, leafG = 0, leafB = 255)
                    }
                }
            }else if(playerOneChoose == 3){

                when (playerTwoChoose) {
                    1 -> {
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
                        firebaseListener?.buttonChangeColor(scissorsR = 0, scissorsG = 0, scissorsB = 255, stoneR = 255, stoneG = 0, stoneB = 0)
                    }
                    2 -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        firebaseListener?.buttonChangeColor(scissorsR = 0, scissorsG = 0, scissorsB = 255, leafR = 255, leafG = 0, leafB = 0)
                    }
                    3 -> {
                        scorePlayerOne = "draw"
                        scorePlayerTwo = "draw"
                        firebaseListener?.buttonChangeColor(scissorsR = 255, scissorsG = 0, scissorsB = 255)
                    }
                    else -> {
                        scorePlayerOne = "victory"
                        scorePlayerTwo = "defeat"
                        firebaseListener?.buttonChangeColor(scissorsR = 0, scissorsG = 0, scissorsB = 255)
                    }
                }
            }else{
                if(playerTwoChoose != 0){
                    scorePlayerOne = "defeat"
                    scorePlayerTwo = "victory"
                    if(playerTwoChoose == 1)
                        firebaseListener?.buttonChangeColor(stoneR = 255, stoneG = 0, stoneB = 0)
                    else if(playerTwoChoose == 2)
                        firebaseListener?.buttonChangeColor(leafR = 255, leafG = 0, leafB = 0)
                    else
                        firebaseListener?.buttonChangeColor(scissorsR = 255, scissorsG = 0, scissorsB = 0)
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
                        firebaseListener?.buttonChangeColor(stoneR = 255, stoneG = 0, stoneB = 255)
                    }
                    2 -> {
                        scorePlayerTwo = "defeat"
                        scorePlayerOne = "victory"
                        firebaseListener?.buttonChangeColor(stoneR = 0, stoneG = 0, stoneB = 255, leafR = 255, leafG = 0, leafB = 0)
                    }
                    3 -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        firebaseListener?.buttonChangeColor(stoneR = 0, stoneG = 0, stoneB = 255, scissorsR = 255, scissorsG = 0, scissorsB = 0)
                    }
                    else -> {
                        scorePlayerOne = "defeat"
                        scorePlayerTwo = "victory"
                        firebaseListener?.buttonChangeColor(stoneR = 0, stoneG = 0, stoneB = 255)
                    }
                }

            }else if(playerTwoChoose == 2){

                when (playerOneChoose) {
                    1 -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        firebaseListener?.buttonChangeColor(stoneR = 255, stoneG = 0, stoneB = 0, leafR = 0, leafG = 0, leafB = 255)
                    }
                    2 -> {
                        scorePlayerTwo = "draw"
                        scorePlayerOne = "draw"
                        firebaseListener?.buttonChangeColor(leafR = 255, leafG = 0, leafB = 255)
                    }
                    3 -> {
                        scorePlayerTwo = "defeat"
                        scorePlayerOne = "victory"
                        firebaseListener?.buttonChangeColor(leafR = 0, leafG = 0, leafB = 255, scissorsR = 255, scissorsG = 0, scissorsB = 0)
                    }
                    else -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        firebaseListener?.buttonChangeColor(leafR = 0, leafG = 0, leafB = 255)
                    }
                }

            }else if(playerTwoChoose == 3){

                when (playerOneChoose) {
                    1 -> {
                        scorePlayerTwo = "defeat"
                        scorePlayerOne = "victory"
                        firebaseListener?.buttonChangeColor(stoneR = 255, stoneG = 0, stoneB = 0, scissorsR = 0, scissorsG = 0, scissorsB = 255)
                    }
                    2 -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        firebaseListener?.buttonChangeColor(leafR = 255, leafG = 0, leafB = 0, scissorsR = 0, scissorsG = 0, scissorsB = 255)
                    }
                    3 -> {
                        scorePlayerTwo = "draw"
                        scorePlayerOne = "draw"
                        firebaseListener?.buttonChangeColor(scissorsR = 255, scissorsG = 0, scissorsB = 255)
                    }
                    else -> {
                        scorePlayerTwo = "victory"
                        scorePlayerOne = "defeat"
                        firebaseListener?.buttonChangeColor(scissorsR = 0, scissorsG = 0, scissorsB = 255)
                    }
                }

            }else{
                if(playerOneChoose != 0){
                    scorePlayerTwo = "defeat"
                    scorePlayerOne = "victory"
                    if(playerOneChoose == 1)
                        firebaseListener?.buttonChangeColor(stoneR = 255, stoneG = 0, stoneB = 0)
                    else if(playerOneChoose == 2)
                        firebaseListener?.buttonChangeColor(leafR = 255, leafG = 0, leafB = 0)
                    else
                        firebaseListener?.buttonChangeColor(scissorsR = 255, scissorsG = 0, scissorsB = 0)
                }else{
                    scorePlayerTwo = "draw"
                    scorePlayerOne = "draw"
                }
            }
        }

        Log.i("TAG", "Scores: $scorePlayerOne and $scorePlayerTwo")
        Log.i("TAG", "Scores: $scorePlayerOne and $scorePlayerTwo")
        firebaseListener?.updateScore(scorePlayerOne, scorePlayerTwo)
    }

    private fun resetChooseToZero(player: Int, roomsChooseRef: DocumentReference){
        val playerChooseZero = "0"
        if(player == 1){
            try{
                roomsChooseRef.update("choosePlayer1", playerChooseZero)
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
                roomsChooseRef.update("choosePlayer2", playerChooseZero)
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

    private var chooseRoom = hashMapOf(
        "choosePlayer1" to "unknown",
        "choosePlayer2" to "unknown",
        "choosesArrayPlayer1" to "empty",
        "choosesArrayPlayer2" to "empty",
    )

    fun createNewHasMapStore(roomsChooseRef: DocumentReference) { //setting document in collection for score of players
        roomsChooseRef.set(chooseRoom as Map<String, Any>, SetOptions.merge())
            .addOnSuccessListener {
                Log.i("Choose", "Successfully added picture choice")
            }
            .addOnFailureListener {
                Log.i("Choose", "Failed while adding picture choice $it")
            }
    }

    private var firebaseListener: ChangeColorListener? = null

    interface ChangeColorListener {
        fun buttonChangeColor(stoneR: Int = 198, stoneG: Int = 182, stoneB: Int = 54,
                              leafR: Int = 198, leafG: Int = 182, leafB: Int = 54,
                              scissorsR: Int = 198, scissorsG: Int = 182, scissorsB: Int = 54
        )

        fun updateScore(scorePlayerOne: String, scorePlayerTwo: String)
    }

    fun setChangeColorListener(listener: ChangeColorListener) {
        firebaseListener = listener
    }


}