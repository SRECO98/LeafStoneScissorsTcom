package com.example.leafstonescissorstcom.game_logic.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentReference

class UpdateScore {

    fun updateScoreFromTableInFB(value: String, field: String, firstRoomRefDoc: DocumentReference){
        firstRoomRefDoc.update(field, value)
            .addOnSuccessListener {
                Log.i("TAG", "Updating score value in firebase for table is successful")
            }
            .addOnFailureListener {
                Log.i("TAG", "Updating score value in firebase for table is not successful")
            }
    }

    fun updateNamesFromTableInFB(value: String, field: String, firstRoomRefDoc: DocumentReference){
        firstRoomRefDoc.update(field, value)
            .addOnSuccessListener {
                Log.i("TAG", "Updating names value in firebase for table is successful")
            }
            .addOnFailureListener {
                Log.i("TAG", "Updating names value in firebase for table is not successful")
            }
    }

    fun updateStatus2AndCurrent(firstRoomRefDoc: DocumentReference){
        firstRoomRefDoc.update("status2", "wait", "current", "1")
            .addOnSuccessListener {
                Log.i("TAG", "testtesttest. Updating names value in firebase for table is successful")
            }
            .addOnFailureListener {
                Log.i("TAG", "Updating names value in firebase for table is not successful")
            }
    }

}