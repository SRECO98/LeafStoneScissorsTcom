package com.example.leafstonescissorstcom.user_info

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.leafstonescissorstcom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var docRef: DocumentReference
    private lateinit var nickname: EditText
    private lateinit var email: EditText
    private val TOKENS_PER_REGISTRATION: String = "1000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nickname = findViewById(R.id.editTextNicknameRegister)
        email = findViewById(R.id.editTextEmailRegister)

        // Initialize Firebase Auth
        auth = Firebase.auth
        val loginText: TextView = findViewById(R.id.textViewLoginNow)
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val registerButton: AppCompatButton = findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener {
            performSignIn() //getting mail and password from user and registering him in firebase.
        }

    }

    private fun performSignIn() {
        val password = findViewById<EditText>(R.id.editTextPasswordRegister)
        val passwordRepeat = findViewById<EditText>(R.id.editTextPasswordRepeatRegister)
        docRef = db.collection("nameOfUsers").document(email.text.toString())

        if(email.text.isEmpty() || password.text.isEmpty()){
            Toast.makeText(this, "Fields can't be empty!", Toast.LENGTH_SHORT).show()
        }else if(password.text.toString().length < 4 || password.text.toString().length > 20){
            Toast.makeText(this, "Password must have between 4 and 20 letters.", Toast.LENGTH_SHORT).show()
        }else if(passwordRepeat.text.toString().length != password.text.toString().length){
            Toast.makeText(this, "Your password must be the same in both fields.", Toast.LENGTH_SHORT).show()
        }else if(nickname.text.toString().length < 3 || nickname.text.toString().length > 12){
            Toast.makeText(this, "Nickname must have between 3 and 12 letters.", Toast.LENGTH_SHORT).show()
        }else{
            val inputEmail = email.text.toString()
            val inputPassword = password.text.toString()

            auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        saveNickName()
                        // Sign in success, moving to next acitivity.
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(baseContext, "Authentication successed.", Toast.LENGTH_SHORT).show()

                        //val user = auth.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error occurred ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun saveNickName(){
        val nickName = nickname.text.toString()
        val name: HashMap<String, Any> = HashMap()
        name.put("name", nickName)
        name.put("totalWins", "0")
        name.put("totalLoses", "0")
        name.put("tokens", TOKENS_PER_REGISTRATION)

        docRef.set(name)
            .addOnSuccessListener {
                Log.i("username", "Registering user name success.")
            }
            .addOnFailureListener {
                Log.i("username", "Registering user name failed.")
            }
    }
}