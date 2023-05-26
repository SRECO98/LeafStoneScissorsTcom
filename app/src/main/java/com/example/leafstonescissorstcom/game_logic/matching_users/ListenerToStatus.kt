package com.example.leafstonescissorstcom.game_logic.matching_users

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration

private lateinit var register2: ListenerRegistration
class ListenerToStatus {

    fun listeningToStatusFromFB(firstRoomRefDoc: DocumentReference, fieldStatus: String, playerFirstOrSecond: String){ //"status" ili "status2"

        register2 = firstRoomRefDoc.addSnapshotListener { value, _ ->  //find a way to remove listenr if this down remove desont work!
            val statusOfRoom = value?.getString(fieldStatus)!!
            Log.i("TAG", "Listening triggered")
            if(statusOfRoom == "close"){
                register2.remove()
                //start activity (but first find how to pair players in games 1v1)
                if(playerFirstOrSecond == "first"){
                    /*matchmakeForCompGroup(playerName, roomsRef = collection.document(roomIdCompGroup).collection("games"), playerCheck = 1)
                    textViewConnecting.text = "Connecting ..."
                    textViewConnecting.isVisible = true*/
                }else if(playerFirstOrSecond == "second"){
                    /*textViewConnecting.text = "Connecting ..."
                    textViewConnecting.isVisible = true
                    startDelay(collection)*/
                }
            }
        }
    }
}