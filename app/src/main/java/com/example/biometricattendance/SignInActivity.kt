package com.example.biometricattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.biometricattendance.db.DatabaseHelper

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val emailField: EditText = findViewById(R.id.emailField)
        val passwordField: EditText = findViewById(R.id.passwordField)
        val signInButton: Button = findViewById(R.id.signInButton)

        val db = DatabaseHelper(this)

        signInButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (db.validateUser(email, password)) {
                val userId = db.getUserIdByEmail(email) // Retrieve userId based on email
                if (userId != -1) { // Ensure valid userId is retrieved
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java).apply {
                        putExtra("USER_ID", userId) // Pass userId to HomeActivity
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "User ID not found. Please contact support.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
