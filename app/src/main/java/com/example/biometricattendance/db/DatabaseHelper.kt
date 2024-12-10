package com.example.biometricattendance.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "biometric_attendance.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_USERS = "users"
        private const val TABLE_BIOMETRIC = "biometric_data"
        private const val TABLE_ATTENDANCE = "attendance"

        // Columns for Users table
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"

        // Columns for Biometric table
        private const val COLUMN_USER_ID = "user_id"
        private const val COLUMN_BIOMETRIC_HASH = "biometric_hash"

        // Columns for Attendance table
        private const val COLUMN_DATE = "date"
        private const val COLUMN_CHECK_TYPE = "check_type"
        private const val COLUMN_LOCATION = "location"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            )
        """
        val createBiometricTable = """
            CREATE TABLE $TABLE_BIOMETRIC (
                $COLUMN_USER_ID INTEGER PRIMARY KEY,
                $COLUMN_BIOMETRIC_HASH TEXT,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """
        val createAttendanceTable = """
            CREATE TABLE $TABLE_ATTENDANCE (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_ID INTEGER,
                $COLUMN_DATE TEXT,
                $COLUMN_CHECK_TYPE TEXT,
                $COLUMN_LOCATION TEXT,
                FOREIGN KEY($COLUMN_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """
        db?.execSQL(createUsersTable)
        db?.execSQL(createBiometricTable)
        db?.execSQL(createAttendanceTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BIOMETRIC")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
        onCreate(db)
    }

    // Check if an email already exists in the Users table
    fun isEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Insert a new user into the Users table
    fun insertUser(name: String, email: String, password: String): Long {
        if (isEmailExists(email)) {
            return -1 // Duplicate email
        }
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result
    }

    // Validate user credentials
    fun validateUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    // Retrieve user ID by email
    fun getUserIdByEmail(email: String): Int {
        val db = readableDatabase
        val query = "SELECT $COLUMN_ID FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val userId = if (cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)) else -1
        cursor.close()
        db.close()
        return userId
    }

    // Check if attendance is already marked for a specific date and type
    fun isAttendanceMarked(date: String, checkType: String, userId: Int): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_ATTENDANCE WHERE $COLUMN_DATE = ? AND $COLUMN_CHECK_TYPE = ? AND $COLUMN_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(date, checkType, userId.toString()))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Mark attendance for a specific date and type
    fun markAttendance(userId: Int, date: String, checkType: String, location: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_DATE, date)
            put(COLUMN_CHECK_TYPE, checkType)
            put(COLUMN_LOCATION, location)
        }
        val result = db.insert(TABLE_ATTENDANCE, null, values)
        db.close()
        return result
    }

    // Retrieve all attendance records for a specific user
    fun getAttendanceRecords(userId: Int): List<AttendanceRecord> {
        val db = readableDatabase
        val query = "SELECT $COLUMN_DATE, $COLUMN_CHECK_TYPE, $COLUMN_LOCATION FROM $TABLE_ATTENDANCE WHERE $COLUMN_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        val records = mutableListOf<AttendanceRecord>()
        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                val checkType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHECK_TYPE))
                val location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION))
                records.add(AttendanceRecord(date, checkType, location))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return records
    }

    // Insert or update biometric data
    fun insertOrUpdateBiometricData(userId: Int, biometricHash: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
            put(COLUMN_BIOMETRIC_HASH, biometricHash)
        }
        val result = db.replace(TABLE_BIOMETRIC, null, values)
        db.close()
        return result
    }

    // Retrieve biometric data for a user
    fun getBiometricData(userId: Int): String? {
        val db = readableDatabase
        val query = "SELECT $COLUMN_BIOMETRIC_HASH FROM $TABLE_BIOMETRIC WHERE $COLUMN_USER_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        val biometricHash = if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIOMETRIC_HASH)) else null
        cursor.close()
        db.close()
        return biometricHash
    }
}

// Data class for attendance records
data class AttendanceRecord(val date: String, val checkType: String, val location: String)
