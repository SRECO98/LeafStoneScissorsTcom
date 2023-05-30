package com.example.leafstonescissorstcom.game_logic.tour_score

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.leafstonescissorstcom.R
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class TourScoreTable : AppCompatActivity(), GetDataFromFirebase.UpdateScoreTable {

    private lateinit var getDataFromFirebase: GetDataFromFirebase
    private lateinit var firstRoomRefDoc: DocumentReference

    private lateinit var textViewPlayer1: TextView
    private lateinit var textViewPlayer2: TextView
    private lateinit var textViewPlayer3: TextView
    private lateinit var textViewPlayer4: TextView
    private lateinit var textViewPlayer5: TextView
    private lateinit var textViewPlayer6: TextView
    private lateinit var textViewPlayer7: TextView
    private lateinit var textViewPlayer8: TextView
    private lateinit var textViewPlayer21: TextView
    private lateinit var textViewPlayer22: TextView
    private lateinit var textViewPlayer23: TextView
    private lateinit var textViewPlayer24: TextView
    private lateinit var textViewPlayer31: TextView
    private lateinit var textViewPlayer32: TextView

    private lateinit var textViewScore1: TextView
    private lateinit var textViewScore2: TextView
    private lateinit var textViewScore3: TextView
    private lateinit var textViewScore4: TextView
    private lateinit var textViewScore5: TextView
    private lateinit var textViewScore6: TextView
    private lateinit var textViewScore7: TextView
    private lateinit var textViewScore8: TextView
    private lateinit var textViewScore21: TextView
    private lateinit var textViewScore22: TextView
    private lateinit var textViewScore23: TextView
    private lateinit var textViewScore24: TextView
    private lateinit var textViewScore31: TextView
    private lateinit var textViewScore32: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour_score_table)

        val documentPath = intent.getStringExtra("room_ref")!!  //getting document from StartActivity
        firstRoomRefDoc = FirebaseFirestore.getInstance().document(documentPath) //finding real ref by documentPath

        getDataFromFirebase = GetDataFromFirebase()
        getDataFromFirebase.setUpdateScoreTableListener(this)
        getDataFromFirebase.getDataFromFB(firstRoomRefDoc)

        textViewPlayer1 = findViewById(R.id.textViewPlayer1)
        textViewPlayer2 = findViewById(R.id.textViewPlayer2)
        textViewPlayer3 = findViewById(R.id.textViewPlayer3)
        textViewPlayer4 = findViewById(R.id.textViewPlayer4)
        textViewPlayer5 = findViewById(R.id.textViewPlayer5)
        textViewPlayer6 = findViewById(R.id.textViewPlayer6)
        textViewPlayer7 = findViewById(R.id.textViewPlayer7)
        textViewPlayer8 = findViewById(R.id.textViewPlayer8)

        textViewPlayer21 = findViewById(R.id.textViewPlayer21)
        textViewPlayer22 = findViewById(R.id.textViewPlayer22)
        textViewPlayer23 = findViewById(R.id.textViewPlayer23)
        textViewPlayer24 = findViewById(R.id.textViewPlayer24)

        textViewPlayer31 = findViewById(R.id.textViewPlayer31)
        textViewPlayer32 = findViewById(R.id.textViewPlayer32)


        textViewScore1 = findViewById(R.id.textViewPlayer1Score)
        textViewScore2 = findViewById(R.id.textViewPlayer2Score)
        textViewScore3 = findViewById(R.id.textViewPlayer3Score)
        textViewScore4 = findViewById(R.id.textViewPlayer4Score)
        textViewScore5 = findViewById(R.id.textViewPlayer5Score)
        textViewScore6 = findViewById(R.id.textViewPlayer6Score)
        textViewScore7 = findViewById(R.id.textViewPlayer7Score)
        textViewScore8 = findViewById(R.id.textViewPlayer8Score)

        textViewScore21 = findViewById(R.id.textViewPlayer21Score)
        textViewScore22 = findViewById(R.id.textViewPlayer22Score)
        textViewScore23 = findViewById(R.id.textViewPlayer23Score)
        textViewScore24 = findViewById(R.id.textViewPlayer24Score)

        textViewScore31 = findViewById(R.id.textViewPlayer31Score)
        textViewScore32 = findViewById(R.id.textViewPlayer32Score)
    }

    override fun updateScoreTable(
        player1: String, player2: String, player3: String, player4: String,
        player5: String, player6: String, player7: String, player8: String,
        player21: String, player22: String, player23: String, player24: String,
        player31: String, player32: String,
        score1: String, score2: String, score3: String, score4: String,
        score5: String, score6: String, score7: String, score8: String,
        score21: String, score22: String, score23: String, score24: String,
        score31: String, score32: String
    ) {
        textViewPlayer1.text = player1
        textViewPlayer2.text = player2
        textViewPlayer3.text = player3
        textViewPlayer4.text = player4
        textViewPlayer5.text = player5
        textViewPlayer6.text = player6
        textViewPlayer7.text = player7
        textViewPlayer8.text = player8

        textViewPlayer21.text = player21
        textViewPlayer22.text = player22
        textViewPlayer23.text = player23
        textViewPlayer24.text = player24

        textViewPlayer31.text = player31
        textViewPlayer32.text = player32



        textViewScore1.text = score1
        textViewScore2.text = score2
        textViewScore3.text = score3
        textViewScore4.text = score4
        textViewScore5.text = score5
        textViewScore6.text = score6
        textViewScore7.text = score7
        textViewScore8.text = score8

        textViewScore21.text = score21
        textViewScore22.text = score22
        textViewScore23.text = score23
        textViewScore24.text = score24

        textViewScore31.text = score31
        textViewScore32.text = score32
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

}