package com.example.leafstonescissorstcom.game_logic.analysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leafstonescissorstcom.R
import com.google.android.material.imageview.ShapeableImageView

class MyAdapter(private val roundList: ArrayList<Round>):
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return roundList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = roundList[position]
        holder.titleImagePlayerOne.setImageResource(currentItem.titleImagePlayer1)
        holder.titleImagePlayerTwo.setImageResource(currentItem.titleImagePlayer2)
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleImagePlayerOne: ShapeableImageView = itemView.findViewById(R.id.imageViewPlayer1)
        val titleImagePlayerTwo: ShapeableImageView = itemView.findViewById(R.id.imageViewPlayer2)
    }
}