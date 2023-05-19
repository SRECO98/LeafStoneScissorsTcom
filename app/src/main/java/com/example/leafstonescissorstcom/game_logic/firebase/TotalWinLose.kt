package com.example.leafstonescissorstcom.game_logic.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentReference

class TotalWinLose {

    fun updatingTotalWinsTotalLosesTokensInFB(totalWins: String, totalLoses: String, tokens: String, docRef: DocumentReference) {
        try{
            docRef.update("totalWins", totalWins, "totalLoses", totalLoses, "tokens", tokens)
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



    fun gettingTotalWinsAndTotalLosesFromFirebase(docRef: DocumentReference, onSuccess: (totalWins: String, totalLoses: String) -> Unit ){
        docRef.get()
            .addOnSuccessListener {
                val totalWins = it.getString("totalWins")!!
                val totalLoses = it.getString("totalLoses")!!
                Log.i("TAG", "Getting total wins successed $totalWins")
                Log.i("TAG", "Getting total loses successed $totalLoses")
                onSuccess(totalWins, totalLoses)
            }
            .addOnFailureListener {
                Log.i("TAG", "Getting total wins or loses failed")
            }
    }

}