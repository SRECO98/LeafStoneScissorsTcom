package com.example.leafstonescissorstcom.game_logic.tour_score

import android.util.Log
import com.google.firebase.firestore.DocumentReference

class GetDataFromFirebase {

    fun getDataFromFB(roomsRef: DocumentReference){
        roomsRef.get()
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    val documentSnapshot = task.result
                    if(documentSnapshot != null && documentSnapshot.exists()){
                        //playerOneChoose = documentSnapshot.getString("...")!!
                        //playerTwoChoose = documentSnapshot.getString("...")!!
                    }
                }else {
                    Log.i("TAG", "Getting choose data failed")
                }
            }.addOnFailureListener {
                Log.i("TAG", "Getting choose data failed, ${it}")
            }
    }
}