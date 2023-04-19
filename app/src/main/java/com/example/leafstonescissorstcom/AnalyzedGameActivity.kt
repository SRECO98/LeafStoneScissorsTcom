package com.example.leafstonescissorstcom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AnalyzedGameActivity : AppCompatActivity() {

    val arrayPlayerOneId: ArrayList<Int> = ArrayList()
    val arrayPlayerTwoId: ArrayList<Int> = ArrayList()
    lateinit var newRecycleView : RecyclerView
    lateinit var newArrayList: ArrayList<News>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyzed_game)

        val textViewPlayerOne: TextView = findViewById(R.id.textViewPLayerOne)
        val textViewPlayerTwo: TextView = findViewById(R.id.textViewPLayerTwo)
        val arrayChoosesPlayer1 = intent.getIntArrayExtra("arrayPlayer1")!!
        val arrayChoosesPlayer2 = intent.getIntArrayExtra("arrayPlayer2")!!
        val player1Name = intent.getStringExtra("player1Name")
        val player2Name = intent.getStringExtra("player2Name")
        textViewPlayerOne.text = player1Name
        textViewPlayerTwo.text = player2Name

        imageIds(arrayChoosesPlayer1, arrayChoosesPlayer2)
    }

    private fun imageIds(arrayChoosesPlayer1: IntArray, arrayChoosesPlayer2: IntArray){
        for (item in arrayChoosesPlayer1){
            when (item){
                1 -> arrayPlayerOneId.add(R.drawable.rock)
                2 -> arrayPlayerOneId.add(R.drawable.paper)
                3 -> arrayPlayerOneId.add(R.drawable.scissors)
                else -> {
                    arrayPlayerOneId.add(R.drawable.empty)
                }
            }
        }

        for (item in arrayChoosesPlayer2){
            when (item){
                1 -> arrayPlayerTwoId.add(R.drawable.rock)
                2 -> arrayPlayerTwoId.add(R.drawable.paper)
                3 -> arrayPlayerTwoId.add(R.drawable.scissors)
                else -> {
                    arrayPlayerTwoId.add(R.drawable.empty)
                }
            }
        }

        newRecycleView = findViewById(R.id.recycleView)
        newRecycleView.layoutManager = LinearLayoutManager(this)
        newRecycleView.setHasFixedSize(true)

        newArrayList = arrayListOf<News>()
        getUserData()
    }

    private fun getUserData(){
        for (i in arrayPlayerOneId.indices){
            val news = News(arrayPlayerOneId[i], arrayPlayerTwoId[i])
            newArrayList.add(news)
        }

        newRecycleView.adapter = MyAdapter(newArrayList)
    }
}