package com.example.biometricattendance

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.biometricattendance.db.DatabaseHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: DatabaseHelper
    private val officeLat = 8.954485 // Example office latitude
    private val officeLon = 38.745747 // Example office longitude
    private val gpsThreshold = 100 // Distance threshold in meters
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Retrieve userId passed from SignInActivity
        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Invalid user. Please log in again.", Toast.LENGTH_SHORT).show()
            finish()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = DatabaseHelper(this)

        requestLocationPermissions()

        val checkInButton: Button = findViewById(R.id.checkInButton)
        val checkOutButton: Button = findViewById(R.id.checkOutButton)
        val viewAttendanceButton: Button = findViewById(R.id.viewAttendanceButton)

        checkInButton.setOnClickListener {
            handleAttendance("Check-In")
        }

        checkOutButton.setOnClickListener {
            handleAttendance("Check-Out")
        }

        viewAttendanceButton.setOnClickListener {
            viewAttendance()
        }
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            fetchCurrentLocation()
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Toast.makeText(
                    this,
                    "Current Location: Lat: ${location.latitude}, Lon: ${location.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Request a new location if last location is null
                requestNewLocation()
            }
        }
    }

    private fun requestNewLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    Toast.makeText(
                        this@HomeActivity,
                        "New Location: Lat: ${location.latitude}, Lon: ${location.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAttendance(type: String) {
        // Same as before, ensure biometric and location checks before marking attendance.
        BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                if (ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null && isWithinOfficeLocation(location.latitude, location.longitude)) {
                            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            if (db.isAttendanceMarked(todayDate, type, userId)) {
                                Toast.makeText(
                                    this@HomeActivity,
                                    "Already $type for today!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                db.markAttendance(userId, todayDate, type, "Office Premises")
                                Toast.makeText(this@HomeActivity, "$type successful!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(
                                this@HomeActivity,
                                "You are not on office premises.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@HomeActivity, "Biometric authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate to $type")
                .setDeviceCredentialAllowed(true)
                .build()
        )
    }

    private fun isWithinOfficeLocation(lat: Double, lon: Double): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(lat, lon, officeLat, officeLon, results)
        return results[0] <= gpsThreshold
    }

    private fun viewAttendance() {
        val attendanceList = db.getAttendanceRecords(userId)
        if (attendanceList.isEmpty()) {
            Toast.makeText(this, "No attendance records found.", Toast.LENGTH_SHORT).show()
        } else {
            val attendanceRecords = attendanceList.joinToString(separator = "\n") { record ->
                "Date: ${record.date}, Type: ${record.checkType}, Location: ${record.location}"
            }
            Toast.makeText(this, attendanceRecords, Toast.LENGTH_LONG).show()
        }
    }
}
