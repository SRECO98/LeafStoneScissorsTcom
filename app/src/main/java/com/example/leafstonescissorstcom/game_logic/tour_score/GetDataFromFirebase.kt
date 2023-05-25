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
                        val player1 = documentSnapshot.getString("player1")!!
                        val player2 = documentSnapshot.getString("player2")!!
                        val player3 = documentSnapshot.getString("player3")!!
                        val player4 = documentSnapshot.getString("player4")!!
                        val player5 = documentSnapshot.getString("player5")!!
                        val player6 = documentSnapshot.getString("player6")!!
                        val player7 = documentSnapshot.getString("player7")!!
                        val player8 = documentSnapshot.getString("player8")!!
                        val player21 = documentSnapshot.getString("player21")!!
                        val player22 = documentSnapshot.getString("player22")!!
                        val player23 = documentSnapshot.getString("player23")!!
                        val player24 = documentSnapshot.getString("player24")!!
                        val player31 = documentSnapshot.getString("player31")!!
                        val player32 = documentSnapshot.getString("player32")!!

                        val score1 = documentSnapshot.getString("score1")!!
                        val score2 = documentSnapshot.getString("score2")!!
                        val score3 = documentSnapshot.getString("score3")!!
                        val score4 = documentSnapshot.getString("score4")!!
                        val score5 = documentSnapshot.getString("score5")!!
                        val score6 = documentSnapshot.getString("score6")!!
                        val score7 = documentSnapshot.getString("score7")!!
                        val score8 = documentSnapshot.getString("score8")!!
                        val score21 = documentSnapshot.getString("score21")!!
                        val score22 = documentSnapshot.getString("score22")!!
                        val score23 = documentSnapshot.getString("score23")!!
                        val score24 = documentSnapshot.getString("score24")!!
                        val score31 = documentSnapshot.getString("score31")!!
                        val score32 = documentSnapshot.getString("score32")!!


                        updateScoreTable?.updateScoreTable(
                            player1, player2, player3, player4, player5, player6, player7, player8,
                            player21, player22, player23, player24,
                            player31, player32,
                            score1, score2, score3, score4, score5, score6, score7, score8,
                            score21, score22, score23, score24,
                            score31, score32
                        )
                    }
                }else {
                    Log.i("TAG", "Getting choose data failed")
                }
            }.addOnFailureListener {
                Log.i("TAG", "Getting choose data failed, ${it}")
            }
    }
    private var updateScoreTable: UpdateScoreTable? = null

    interface UpdateScoreTable{
        fun updateScoreTable(
            player1: String, player2: String, player3: String, player4: String,
            player5: String, player6: String, player7: String, player8: String,
            player21: String, player22: String, player23: String, player24: String,
            player31: String, player32: String,
            score1: String, score2: String, score3: String, score4: String,
            score5: String, score6: String, score7: String, score8: String,
            score21: String, score22: String, score23: String, score24: String,
            score31: String, score32: String,
        )
    }

    fun setUpdateScoreTableListener(listener: UpdateScoreTable){
        updateScoreTable = listener
    }

}