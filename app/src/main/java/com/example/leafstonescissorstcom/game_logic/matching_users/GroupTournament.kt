package com.example.leafstonescissorstcom.game_logic.matching_users

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

class GroupTournament {

    lateinit var firstRoomRefId: String
    private var playerFirstOrSecond = ""
    var currentSend: String = ""

    fun buttonTourGameFirstOpen(
        playerName: String,
        roomGroupTour: CollectionReference,
        numberOfPlayers: String,
        roomPlayerData: HashMap<String, String>
    ) {
        val query = roomGroupTour.whereEqualTo("status", "open").get()
            .addOnSuccessListener { documentListener ->
                if (documentListener.isEmpty) {
                    roomGroupTour.add(roomPlayerData)
                        .addOnCompleteListener { documentReference ->
                            firstRoomRefId = documentReference.result.id

                            playerFirstOrSecond = "first"
                            currentSend = "1"
                            Log.d("TAG", "Room created with ID: ${firstRoomRefId}")
                            roomGroupTour.document(firstRoomRefId)
                                .update("player1", playerName, "current", "2")
                            //listeningToStatus()

                        }.addOnFailureListener {
                            Log.e("TAG", "Error creating room tournament first call.")
                        }
                }else{
                    firstRoomRefId = documentListener.first().id  //Getting id as joining user.
                    //listeningToStatus()

                    val firstRoomRefDoc = roomGroupTour.document(firstRoomRefId)
                    Log.i("TAG", "firstRoomRefDoc is: $firstRoomRefDoc")

                    firstRoomRefDoc.get() //izvlacenje vrijednosti current iz firebase da bismo znali koji je igrac po redu
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Log.i("TAG", "Task is succesful!")
                                val documentSnapshot = task.result
                                if(documentSnapshot != null && documentSnapshot.exists()){
                                    Log.i("TAG", "DocumentSnapshot exist and its not null")
                                    val current = documentSnapshot.get("current")
                                    Log.i("TAG", "Current is: $current")
                                    if(current != null){
                                        if(current == numberOfPlayers){ //last player in tournament

                                            val playerField = "player$current"
                                            playerFirstOrSecond = "second"
                                            currentSend = current.toString() //Ovu vrednost saljemo u MainActivity
                                            firstRoomRefDoc.update(playerField, playerName, "status", "close") //now it will start function listeningTosTATUS
                                        }else{

                                            val playerField = "player$current"
                                            val nextCurrent = (current as Int + 1).toString()
                                            currentSend = current.toString()
                                            if(current % 2 == 0){
                                                playerFirstOrSecond = "second"
                                            }else{
                                                playerFirstOrSecond = "first"
                                            }
                                            firstRoomRefDoc.update("current", nextCurrent, playerField, playerName)
                                        }
                                    }else {
                                        // Handle the case when the "current" parameter is not found
                                        println("The current player is null from Firebase")
                                    }
                                }else {
                                    // Handle the case when the document doesn't exist
                                    println("The document doesn't exist")
                                }
                            }
                        }.addOnFailureListener {
                            Log.i("TAG", "Getting document from firebase failed: $it")
                        }
                }
            }.addOnFailureListener{
                Log.i("TAG", "Getting query from firebase failed: $it")
            }
    }


    fun buttonTourGameSecondOpen(
        playerName: String,
        firstRoomRefDoc: DocumentReference,
        numberOfPlayers: String,
    ){

    }

}