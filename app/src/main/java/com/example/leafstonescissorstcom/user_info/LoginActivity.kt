package com.example.leafstonescissorstcom.user_info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.leafstonescissorstcom.R
import com.example.leafstonescissorstcom.game_logic.matching_users.StartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var docRef: DocumentReference
    private var playerName: String? = ""
    private var playerTokens: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        // Initialize Firebase Auth
        auth = Firebase.auth

        val registerText: TextView = findViewById(R.id.textViewRegisterNow)

        registerText.setOnClickListener {
            val intent = Intent (this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val loginButton: AppCompatButton = findViewById(R.id.buttonLogin)
        loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin(){
        val email: EditText = findViewById(R.id.editTextEmailLogin)
        val password: EditText = findViewById(R.id.editTextPasswordLogin)


        //null checks:
        if(email.text.isEmpty() || password.text.isEmpty()){
            Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val emailInput = email.text.toString()
        val passwordImput = password.text.toString()
        docRef = db.collection("nameOfUsers").document(emailInput)
        docRef.get()
            .addOnSuccessListener {
                playerName = it.getString("name")
                Log.i("getnick", "Getting nick name successed $playerName")
            }
            .addOnFailureListener {
                Log.i("getnick", "Getting nick name failed")
            }

        docRef.get().addOnSuccessListener {
            playerTokens = it.getString("tokens")
            Log.i("getnick", "Getting nick name successed $playerTokens")
        }
            .addOnFailureListener {
                Log.i("getnick", "Getting nick name failed")
            }

        /*Sign in existing users*/
        auth.signInWithEmailAndPassword(emailInput, passwordImput)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to the Main Activity.
                    val intent = Intent(this, StartActivity::class.java)
                    intent.putExtra("name", playerName)
                    intent.putExtra("email", emailInput)
                    intent.putExtra("tokens", playerTokens)
                    startActivity(intent)
                    Log.i("TAG", "Authentication successed.")
                    //val user = auth.currentUser
                }else{
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Authentication failed. ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}