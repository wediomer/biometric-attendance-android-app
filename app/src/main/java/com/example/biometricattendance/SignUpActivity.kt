package com.example.biometricattendance

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.biometricattendance.db.DatabaseHelper

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val nameField: EditText = findViewById(R.id.nameField)
        val emailField: EditText = findViewById(R.id.emailField)
        val passwordField: EditText = findViewById(R.id.passwordField)
        val confirmPasswordField: EditText = findViewById(R.id.confirmPasswordField)
        val signUpButton: Button = findViewById(R.id.signUpButton)

        val db = DatabaseHelper(this)

        signUpButton.setOnClickListener {
            val name = nameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate password confirmation
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if email already exists
            if (db.isEmailExists(email)) {
                Toast.makeText(
                    this,
                    "Email already exists. Please use a different email.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Insert user into the database
            val userId = db.insertUser(name, email, password)
            if (userId > 0) {
                Toast.makeText(
                    this,
                    "Signup successful! Your User ID is: $userId",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
