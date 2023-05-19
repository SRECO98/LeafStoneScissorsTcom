package com.example.leafstonescissorstcom.game_logic.analysis

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leafstonescissorstcom.R
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class AnalyzedGameActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val arrayPlayerOneId: ArrayList<Int> = ArrayList()
    private val arrayPlayerTwoId: ArrayList<Int> = ArrayList()
    private lateinit var newRecycleView : RecyclerView
    private lateinit var newArrayList: ArrayList<Round>
    private lateinit var roomsChooseRef: DocumentReference
    private lateinit var buttonClose: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyzed_game)

        Log.d("TAG", "onCreate called") // add a logging statement here

        val textViewPlayerOne: TextView = findViewById(R.id.textViewPLayerOne)
        val textViewPlayerTwo: TextView = findViewById(R.id.textViewPLayerTwo)
        buttonClose = findViewById(R.id.buttonClose)

        val roomId = intent.getStringExtra("id")!!
        val player1Name = intent.getStringExtra("player1Name")
        val player2Name = intent.getStringExtra("player2Name")
        textViewPlayerOne.text = player1Name
        textViewPlayerTwo.text = player2Name

        loadAnalyzedGamesOfPlayers(roomId)

        buttonClose.setOnClickListener {
            finish()
        }
    }

    private fun loadAnalyzedGamesOfPlayers(roomId: String) {
        roomsChooseRef = db.collection("rooms").document(roomId)
        roomsChooseRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val choosesArrayPlayer1 = documentSnapshot.get("choosesArrayPlayer1") as? ArrayList<*>
                val choosesArrayPlayer2 = documentSnapshot.get("choosesArrayPlayer2") as? ArrayList<*>
                if (choosesArrayPlayer1 != null && choosesArrayPlayer2 != null) {
                    Log.d("TAG", "User one choices: $choosesArrayPlayer1 ")
                    Log.d("TAG", "User two choices: $choosesArrayPlayer2 ")
                    imageIds(choosesArrayPlayer1, choosesArrayPlayer2)
                } else {
                    Log.e("TAG", "Error: null array")
                }
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Error getting user document", e)
            }
    }

    private fun imageIds(arrayChoosesPlayer1: ArrayList<*>, arrayChoosesPlayer2: ArrayList<*>){
        for (item in arrayChoosesPlayer1) {
            when (item) {
                1L -> arrayPlayerOneId.add(R.drawable.rock)
                2L -> arrayPlayerOneId.add(R.drawable.paper)
                3L -> arrayPlayerOneId.add(R.drawable.scissors)
                else -> {
                    arrayPlayerOneId.add(R.drawable.empty)
                }
            }
        }

        for (item in arrayChoosesPlayer2){
            when (item){
                1L -> arrayPlayerTwoId.add(R.drawable.rock)
                2L -> arrayPlayerTwoId.add(R.drawable.paper)
                3L -> arrayPlayerTwoId.add(R.drawable.scissors)
                else -> {
                    arrayPlayerTwoId.add(R.drawable.empty)
                }
            }
        }

        newRecycleView = findViewById(R.id.recycleView)
        newRecycleView.layoutManager = LinearLayoutManager(this)
        newRecycleView.setHasFixedSize(true)

        newArrayList = arrayListOf()
        getUserData()
    }

    private fun getUserData(){
        for (i in arrayPlayerOneId.indices){
            val round = Round(arrayPlayerOneId[i], arrayPlayerTwoId[i])
            newArrayList.add(round)
        }

        newRecycleView.adapter = MyAdapter(newArrayList)
    }
}