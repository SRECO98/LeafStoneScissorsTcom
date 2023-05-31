package com.example.leafstonescissorstcom.game_logic.matching_users

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration

private lateinit var register2: ListenerRegistration
class ListenerToStatus {

    fun listeningToStatusFromFB(firstRoomRefDoc: DocumentReference, fieldStatus: String, playerFirstOrSecond: String, playerName: String, currentSend: String){ //"status" ili "status2"

        Log.i("tag", "Listening triggered, Id: $firstRoomRefDoc")
        matchmakeTwoPlayers?.getFirstRoomRefDoc(firstRoomRefDoc, currentSend)
        register2 = firstRoomRefDoc.addSnapshotListener { value, _ ->  //find a way to remove listenr if this down remove desont work!
            val statusOfRoom = value?.getString(fieldStatus)!!
            Log.i("TAG", "Listening triggered")
            if(statusOfRoom == "close"){
                register2.remove()
                //start activity (but first find how to pair players in games 1v1)
                if(playerFirstOrSecond == "first"){
                    matchmakeForCompGroup(playerName, firstRoomRefDoc.collection("games"),1, currentSend)
                    matchmakeTwoPlayers?.updateTextViewConnecting(boolean = true, text = "Connecting ...")
                }else if(playerFirstOrSecond == "second"){
                    matchmakeTwoPlayers?.updateTextViewConnecting(boolean = true, text = "Connecting ...")
                    startDelay(playerName, firstRoomRefDoc.collection("games"),2, (currentSend.toInt() - 1).toString())
                }
            }
        }
    }

    private fun matchmakeForCompGroup(playerName: String, secondRoomGroupTour: CollectionReference, playerCheck: Int, statusValue: String){
        Log.i("TAG", "Function matchmakeForCompGroup started.")
        val query = secondRoomGroupTour.whereEqualTo("status", "openpl$statusValue").limit(1) //status value will have "openpl1" or "openpl3", "pl5" and "pl7"
        query.get()
            .addOnSuccessListener { documents ->
                if (playerCheck == 1){
                    // No open rooms found, create a new room
                    matchmakeTwoPlayers?.callCreateRoom(playerName, secondRoomGroupTour, "group", "openpl$statusValue")
                    Log.i("TAG", "Document is:" + documents.isEmpty.toString())
                } else if(playerCheck == 2) {
                    // Found an open room, join the room
                    val room = documents.first()
                    val secondRoomRedId = room.id

                    matchmakeTwoPlayers?.callJoinRoom(secondRoomRedId, playerName, secondRoomGroupTour.document(secondRoomRedId), "group") //not good ID cuz second player never entered creatingRoom, need to put it in firebase and read
                }
            }.addOnFailureListener { exception ->
                Log.e("TAG", "Error getting open rooms: ", exception)
            }
    }

    private val delayMillis: Long = 3000
    private fun startDelay(playerName: String, secondRoomGroupTour: CollectionReference, playerCheck: Int, statusValue: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            matchmakeTwoPlayers?.updateTextViewConnecting(boolean = false)
            matchmakeForCompGroup(playerName, secondRoomGroupTour, playerCheck, statusValue)
            // This code will run after the specified delay
            // Perform your task here
        }, delayMillis)
    }


    private var matchmakeTwoPlayers: MatchmakeTwoPlayers? = null
    interface MatchmakeTwoPlayers{
        fun callCreateRoom(playerName: String, secondRoomGroupTour: CollectionReference, typeOfGame: String, statusValue: String)
        fun callJoinRoom(secondRoomRedId: String, playerName: String, secondRoomRefDoc: DocumentReference, typeOfGame: String)
        fun getFirstRoomRefDoc(firstRoomRefDoc: DocumentReference, currentSend: String)
        fun updateTextViewConnecting(boolean: Boolean, text: String = "")
    }

    fun setMatchmakeTwoPlayersListener(listener: MatchmakeTwoPlayers){
        matchmakeTwoPlayers = listener
    }

}