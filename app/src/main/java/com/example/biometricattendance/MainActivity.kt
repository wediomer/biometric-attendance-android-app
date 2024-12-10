package com.example.biometricattendance

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start with the SignUpActivity
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }
}