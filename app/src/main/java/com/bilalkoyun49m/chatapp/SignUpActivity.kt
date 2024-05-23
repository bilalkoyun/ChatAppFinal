package com.bilalkoyun49m.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailText = findViewById<EditText>(R.id.emailText)
        val passwordText = findViewById<EditText>(R.id.passwordText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val loginRedirectButton = findViewById<Button>(R.id.loginRedirectButton)

        signupButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign up success, update UI with the signed-in user's information
                            Log.d("SignUpActivity", "createUserWithEmail:success")
                            val user = auth.currentUser
                            user?.let {
                                val email = user.email
                                val uid = user.uid
                                // Kullanıcının email adresi ve UID'sini kullanabilirsiniz
                            }
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            // If sign up fails, display a message to the user.
                            Log.w("SignUpActivity", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Sign up failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }

        loginRedirectButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
