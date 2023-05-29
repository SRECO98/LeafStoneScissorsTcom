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
        roomPlayerData: HashMap<String, String>,
        listenerToStatus: ListenerToStatus,
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
                            //calling listener
                            listenerToStatus.listeningToStatusFromFB(roomGroupTour.document(firstRoomRefId), "status", playerFirstOrSecond, playerName, currentSend)

                        }.addOnFailureListener {
                            Log.e("TAG", "Error creating room tournament first call.")
                        }
                }else{
                    firstRoomRefId = documentListener.first().id  //Getting id as joining user.

                    val firstRoomRefDoc = roomGroupTour.document(firstRoomRefId)
                    Log.i("TAG", "firstRoomRefDoc is: $firstRoomRefDoc")

                    firstRoomRefDoc.get() //izvlacenje vrijednosti current iz firebase da bismo znali koji je igrac po redu
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                Log.i("TAG", "Task is succesful!")
                                val documentSnapshot = task.result
                                if(documentSnapshot != null && documentSnapshot.exists()){
                                    Log.i("TAG", "DocumentSnapshot exist and its not null")
                                    val current: String = documentSnapshot.get("current").toString() //current = trenutan broj igraca u sobi, ceka se max broj da bi poceli game
                                    Log.i("TAG", "Current is: $current")
                                    if(current != null){

                                        val playerField = "player$current"
                                        currentSend = current//Ovu vrednost saljemo u MainActivity

                                        if(current == numberOfPlayers){ //last player in tournament

                                            playerFirstOrSecond = "second"
                                            firstRoomRefDoc.update(playerField, playerName, "status", "close") //now it will start function listeningTosTATUS
                                        }else{

                                            val nextCurrent = (current.toInt() + 1).toString()
                                            if(current.toInt() % 2 == 0){
                                                playerFirstOrSecond = "second"
                                            }else{
                                                playerFirstOrSecond = "first"
                                            }
                                            firstRoomRefDoc.update("current", nextCurrent, playerField, playerName)
                                        }
                                        //calling listener
                                        Log.i("TAG", "Function listening should be called now!")
                                        listenerToStatus.listeningToStatusFromFB(firstRoomRefDoc, "status", playerFirstOrSecond, playerName, currentSend)
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
        currentFromMain: String,
        listenerToStatus: ListenerToStatus,
    ){

        firstRoomRefDoc.get() //izvlacenje vrijednosti current iz firebase da bismo znali koji je igrac po redu
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.i("TAG", "Task is succesful!")
                    val documentSnapshot = task.result
                    if(documentSnapshot != null && documentSnapshot.exists()){
                        Log.i("TAG", "DocumentSnapshot exist and its not null")
                        val current: String = documentSnapshot.get("current").toString() //current = trenutan broj igraca u sobi, ceka se max broj da bi poceli game
                        Log.i("TAG", "Current is: $current")
                        if(current != null){

                            val playerField = "player2"+currentFromMain //tacna vrednost igraca iz maina mi treba ne koja je soba!! Popraviti u Main! i ovo je samo za drugu rundu, treca nece biti okey.
                            currentSend = currentFromMain

                            if(current == numberOfPlayers){ // poslednji player je usao u sobu

                                if(currentFromMain.toInt() % 2 == 0){
                                    playerFirstOrSecond = "second"
                                }else{
                                    playerFirstOrSecond = "first"
                                }
                                firstRoomRefDoc.update(playerField, playerName, "status2", "close") //game pocinje
                            }else{

                                val nextCurrent = (current.toInt() + 1).toString()
                                if(currentFromMain.toInt() % 2 == 0){
                                    playerFirstOrSecond = "second"
                                }else{
                                    playerFirstOrSecond = "first"
                                }
                                firstRoomRefDoc.update(playerField, playerName, "current", nextCurrent)

                            }
                            //calling listener
                            listenerToStatus.listeningToStatusFromFB(firstRoomRefDoc, "status2", playerFirstOrSecond, playerName, currentSend)
                        }else{
                            Log.i("TAG", "Current in StartActivity from Main is null.")
                        }
                    }else{
                        Log.i("TAG", "Document snapshot doesnt exist or it is null.")
                    }
                }
            }.addOnFailureListener {
                Log.i("TAG", "Failed getting docuemnt from firebase: $it")
            }
    }

}