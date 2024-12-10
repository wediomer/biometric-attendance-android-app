package com.example.biometricattendance

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val checkInButton: Button = findViewById(R.id.checkInButton)
        val checkOutButton: Button = findViewById(R.id.checkOutButton)
        val viewAttendanceButton: Button = findViewById(R.id.viewAttendanceButton)

        checkInButton.setOnClickListener {
            Toast.makeText(this, "Check-In functionality goes here!", Toast.LENGTH_SHORT).show()
            // Add Check-In logic
        }

        checkOutButton.setOnClickListener {
            Toast.makeText(this, "Check-Out functionality goes here!", Toast.LENGTH_SHORT).show()
            // Add Check-Out logic
        }

        viewAttendanceButton.setOnClickListener {
            Toast.makeText(this, "View Attendance functionality goes here!", Toast.LENGTH_SHORT).show()
            // Add View Attendance logic
        }
    }
}
