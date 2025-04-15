package com.example.myapplication

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class BookingsDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 5
        private const val DATABASE_NAME = "restaurant.db"

        private const val TABLE_BOOKINGS = "bookings"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CUSTOMER_NAME = "customer_name"
        private const val COLUMN_PHONE_NUMBER = "phone_number"
        private const val COLUMN_GUEST_COUNT = "guest_count"
        private const val COLUMN_TABLE_NUMBER = "table_number"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_NOTES = "notes"

        private const val CREATE_TABLE_BOOKINGS = ("CREATE TABLE $TABLE_BOOKINGS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CUSTOMER_NAME TEXT, " +
                "$COLUMN_PHONE_NUMBER TEXT, " +
                "$COLUMN_GUEST_COUNT INTEGER, " +
                "$COLUMN_TABLE_NUMBER TEXT, " +
                "$COLUMN_DATE TEXT, " +
                "$COLUMN_TIME TEXT, " +
                "$COLUMN_NOTES TEXT)")
    }

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            db?.execSQL(CREATE_TABLE_BOOKINGS)
            Log.d("BookingsDbHelper", "Table $TABLE_BOOKINGS created successfully.")
        } catch (e: Exception) {
            Log.e("BookingsDbHelper", "Error creating table $TABLE_BOOKINGS", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            try {
                db?.execSQL("ALTER TABLE $TABLE_BOOKINGS ADD COLUMN $COLUMN_NOTES TEXT")
                Log.d("BookingsDbHelper", "Column $COLUMN_NOTES added successfully.")
            } catch (e: Exception) {
                Log.e("BookingsDbHelper", "Error adding column $COLUMN_NOTES", e)
            }
        }
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("BookingsDbHelper", "Downgrading database from version $oldVersion to $newVersion.")

        if (oldVersion > newVersion) {
            // Handle downgrade logic
            // Example: Drop the table and recreate it without the new columns
            try {
                db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
                onCreate(db) // Recreate the table with the current version's schema
                Log.d("BookingsDbHelper", "Database downgraded successfully.")
            } catch (e: Exception) {
                Log.e("BookingsDbHelper", "Error downgrading database", e)
            }
        }
    }


    fun insertData(
        customerName: String,
        phoneNumber: String,
        guestCount: Int,
        tableNumber: String,
        date: String,
        time: String,
        notes: String? = null
    ) {
        val db = this.writableDatabase
        try {
            val insertStatement = "INSERT INTO $TABLE_BOOKINGS ($COLUMN_CUSTOMER_NAME, $COLUMN_PHONE_NUMBER, $COLUMN_GUEST_COUNT, $COLUMN_TABLE_NUMBER, $COLUMN_DATE, $COLUMN_TIME, $COLUMN_NOTES) VALUES (?, ?, ?, ?, ?, ?, ?)"
            db.execSQL(insertStatement, arrayOf(customerName, phoneNumber, guestCount, tableNumber, date, time, notes))
        } catch (e: Exception) {
            Log.e("BookingsDbHelper", "Error inserting data into $TABLE_BOOKINGS", e)
        } finally {
            db.close()
        }
    }

    fun getAllBookings() {
        TODO("Not yet implemented")
    }


}
