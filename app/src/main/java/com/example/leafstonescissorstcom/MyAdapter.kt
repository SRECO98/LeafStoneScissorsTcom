package com.example.leafstonescissorstcom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class MyAdapter(private val newsList: ArrayList<News>):
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = newsList[position]
        holder.titleImagePlayerOne.setImageResource(currentItem.titleImagePlayer1)
        holder.titleImagePlayerTwo.setImageResource(currentItem.titleImagePlayer2)
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val titleImagePlayerOne: ShapeableImageView = itemView.findViewById(R.id.imageViewPlayer1)
        val titleImagePlayerTwo: ShapeableImageView = itemView.findViewById(R.id.imageViewPlayer2)
    }
}