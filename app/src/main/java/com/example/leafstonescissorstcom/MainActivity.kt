package com.example.leafstonescissorstcom

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    //refewrenca da ne pisemo stalno sve, dovljno je samo docRef...
    private val docRef: DocumentReference = db.collection("nameOfColletion").document("Doc name")
    /*docRef.set("value") //postavjanje
    docRef.get("value") //uzimanje*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonStone: AppCompatButton = findViewById(R.id.imageStone)
        val buttonLeaf: AppCompatButton = findViewById(R.id.imageLeaf)
        val buttonSccissors: AppCompatButton = findViewById(R.id.imageSccissors)

        buttonStone.setOnClickListener {
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 180))
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 54))
        }
        buttonLeaf.setOnClickListener {
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 180))
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 54))
        }
        buttonSccissors.setOnClickListener {
            buttonSccissors.setBackgroundColor(Color.argb(58, 198, 182, 180))
            buttonStone.setBackgroundColor(Color.argb(58, 198, 182, 54))
            buttonLeaf.setBackgroundColor(Color.argb(58, 198, 182, 54))
        }
    }
}

