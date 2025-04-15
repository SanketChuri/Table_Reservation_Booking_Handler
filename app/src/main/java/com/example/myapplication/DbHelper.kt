package com.example.myapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RestaurantDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 6
        private const val DATABASE_NAME = "restaurant.db"

        // Bookings Table
        private const val TABLE_BOOKINGS = "bookings"
        private const val COLUMN_BOOKING_ID = "id"
        private const val COLUMN_CUSTOMER_NAME = "customer_name"
        private const val COLUMN_PHONE_NUMBER = "phone_number"
        private const val COLUMN_GUEST_COUNT = "guest_count"
        private const val COLUMN_TABLE_NUMBER_BOOKING = "table_number"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_TIME = "time"
        private const val COLUMN_NOTES = "notes"

        private const val CREATE_TABLE_BOOKINGS = ("CREATE TABLE $TABLE_BOOKINGS (" +
                "$COLUMN_BOOKING_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CUSTOMER_NAME TEXT, " +
                "$COLUMN_PHONE_NUMBER TEXT, " +
                "$COLUMN_GUEST_COUNT INTEGER, " +
                "$COLUMN_TABLE_NUMBER_BOOKING INTEGER, " +
                "$COLUMN_DATE TEXT, " +
                "$COLUMN_TIME TEXT, " +
                "$COLUMN_NOTES TEXT)")

        // Tables Table
        private const val TABLE_TABLES = "tables"
        private const val COLUMN_TABLE_ID = "id"
        private const val COLUMN_TABLE_NUMBER_TABLES = "table_number"
        private const val COLUMN_TABLE_CAPACITY = "table_capacity"

        private const val CREATE_TABLE_TABLES = ("CREATE TABLE $TABLE_TABLES (" +
                "$COLUMN_TABLE_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_TABLE_NUMBER_TABLES INTEGER, " +
                "$COLUMN_TABLE_CAPACITY TEXT)")
    }

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            db?.execSQL(CREATE_TABLE_BOOKINGS)
            db?.execSQL(CREATE_TABLE_TABLES)
            Log.d("RestaurantDbHelper", "Tables created successfully.")
        } catch (e: Exception) {
            Log.e("RestaurantDbHelper", "Error creating tables", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            if (oldVersion < 5) {
                db?.execSQL("ALTER TABLE $TABLE_BOOKINGS ADD COLUMN $COLUMN_NOTES TEXT")
                Log.d("RestaurantDbHelper", "Column $COLUMN_NOTES added successfully to $TABLE_BOOKINGS.")
            }
            if (oldVersion < 6) {
                db?.execSQL("DROP TABLE IF EXISTS $TABLE_TABLES")
                db?.execSQL(CREATE_TABLE_TABLES)
                Log.d("RestaurantDbHelper", "$TABLE_TABLES recreated successfully.")
            }
        } catch (e: Exception) {
            Log.e("RestaurantDbHelper", "Error upgrading database", e)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("RestaurantDbHelper", "Downgrading database from version $oldVersion to $newVersion.")
        try {
            if (oldVersion > newVersion) {
                db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
                db?.execSQL("DROP TABLE IF EXISTS $TABLE_TABLES")
                onCreate(db)
                Log.d("RestaurantDbHelper", "Database downgraded successfully.")
            }
        } catch (e: Exception) {
            Log.e("RestaurantDbHelper", "Error downgrading database", e)
        }
    }

    // Bookings methods (like insertData, getAllBookings) go here
    fun insertBooking(customerName: String, phoneNumber: String, guestCount: Int, tableNumber: String?, date: String, time: String, notes: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("customer_name", customerName)
            put("phone_number", phoneNumber)
            put("guest_count", guestCount)
            put("table_number", tableNumber)
            put("date", date)
            put("time", time)
            put("notes", notes)  // Added line
        }
        db.insert("Bookings", null, contentValues)
    }


    // Tables methods (like insertData, getAllTables, tableExists) go here
    fun insertTable(tableId: Int, tableCapacity: String) {
        val db = this.writableDatabase
        try {
            val insertStatement = "INSERT INTO $TABLE_TABLES ($COLUMN_TABLE_ID, $COLUMN_TABLE_NUMBER_TABLES, $COLUMN_TABLE_CAPACITY) VALUES (?, ?, ?)"
            db.execSQL(insertStatement, arrayOf(tableId, tableId, tableCapacity))
        } catch (e: Exception) {
            Log.e("RestaurantDbHelper", "Error inserting data into $TABLE_TABLES", e)
        } finally {
            db.close()
        }
    }

    fun deleteTable(tableId: Int) {
        val db = this.writableDatabase
        try {
            db.delete(TABLE_TABLES, "$COLUMN_TABLE_ID = ?", arrayOf(tableId.toString()))
        } catch (e: Exception) {
            Log.e("RestaurantDbHelper", "Error deleting data from $TABLE_TABLES", e)
        } finally {
            db.close()
        }
    }

    @SuppressLint("Range")
    fun getAllTables(): List<Pair<Int, String>> {
        val tables = mutableListOf<Pair<Int, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TABLES", null)
        while (cursor.moveToNext()) {
            val tableNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_TABLE_NUMBER_TABLES))
            val tableCapacity = cursor.getString(cursor.getColumnIndex(COLUMN_TABLE_CAPACITY))
            tables.add(Pair(tableNumber, tableCapacity))
        }
        cursor.close()
        db.close()
        return tables
    }

    fun tableExists(tableNumber: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_TABLES WHERE $COLUMN_TABLE_NUMBER_TABLES = ?", arrayOf(tableNumber.toString()))
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun updateBooking(bookingId: Int, customerName: String, phoneNumber: String, guestCount: Int, tableNumber: String, date: String, time: String, notes: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_CUSTOMER_NAME, customerName)
            put(COLUMN_PHONE_NUMBER, phoneNumber)
            put(COLUMN_GUEST_COUNT, guestCount)
            put(COLUMN_TABLE_NUMBER_BOOKING, tableNumber)
            put(COLUMN_DATE, date)
            put(COLUMN_TIME, time)
            put(COLUMN_NOTES, notes)
        }
        db.update(TABLE_BOOKINGS, contentValues, "$COLUMN_BOOKING_ID = ?", arrayOf(bookingId.toString()))
        db.close()
    }

    fun deleteBooking(bookingId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_BOOKINGS, "$COLUMN_BOOKING_ID = ?", arrayOf(bookingId.toString()))
        db.close()
    }


    @SuppressLint("Range")
    fun getAllBookings(): List<Booking> {
        val bookings = mutableListOf<Booking>()
        val db = this.readableDatabase

        // SQL query to order bookings by date in ascending order
        val cursor = db.rawQuery("SELECT * FROM $TABLE_BOOKINGS ORDER BY $COLUMN_DATE ASC, $COLUMN_TIME ASC", null)

        while (cursor.moveToNext()) {
            val booking = Booking(
                customerId = cursor.getString(cursor.getColumnIndex(COLUMN_BOOKING_ID)),
                customerName = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME)),
                phoneNumber = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)),
                guestCount = cursor.getInt(cursor.getColumnIndex(COLUMN_GUEST_COUNT)),
                tableNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_TABLE_NUMBER_BOOKING)),
                date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE)),
                time = cursor.getString(cursor.getColumnIndex(COLUMN_TIME)),
                notes = cursor.getString(cursor.getColumnIndex(COLUMN_NOTES))
            )
            bookings.add(booking)
        }

        cursor.close()
        db.close()
        return bookings
    }

    @SuppressLint("Range")
    fun getAvailableTables(date: String, time: String): List<Pair<Int, String>> {
        val db = this.readableDatabase

        // Parse the date and time to handle the buffer calculation
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val bookingDateTime = dateTimeFormat.parse("$date $time")

        // Calculate the start time (-1 hour) and end time (+1 hour)
        val calendar = Calendar.getInstance()

        // Set start time with -1 hour
        if (bookingDateTime != null) {
            calendar.time = bookingDateTime
        }
        calendar.add(Calendar.HOUR_OF_DAY, -1)
        val startTime = dateTimeFormat.format(calendar.time)

        // Set end time with +1 hour
        if (bookingDateTime != null) {
            calendar.time = bookingDateTime
        }
        calendar.add(Calendar.HOUR_OF_DAY, 2) // Add 2 hours from the start (1 hour after the booking time)
        val endTime = dateTimeFormat.format(calendar.time)

        // Query to get all tables that are booked within the time buffer
        val reservedTablesQuery = """
        SELECT $COLUMN_TABLE_NUMBER_BOOKING 
        FROM $TABLE_BOOKINGS 
        WHERE $COLUMN_DATE = ? AND ($COLUMN_TIME BETWEEN ? AND ?)
    """

        val reservedTablesCursor = db.rawQuery(reservedTablesQuery, arrayOf(date, startTime.split(" ")[1], endTime.split(" ")[1]))
        val reservedTableNumbers = mutableListOf<Int>()

        while (reservedTablesCursor.moveToNext()) {
            val tableNumber = reservedTablesCursor.getInt(reservedTablesCursor.getColumnIndex(COLUMN_TABLE_NUMBER_BOOKING))
            reservedTableNumbers.add(tableNumber)
        }

        reservedTablesCursor.close()

        // Query to get all tables
        val allTablesQuery = "SELECT * FROM $TABLE_TABLES"
        val allTablesCursor = db.rawQuery(allTablesQuery, null)
        val availableTables = mutableListOf<Pair<Int, String>>()

        while (allTablesCursor.moveToNext()) {
            val tableNumber = allTablesCursor.getInt(allTablesCursor.getColumnIndex(COLUMN_TABLE_NUMBER_TABLES))
            val tableCapacity = allTablesCursor.getString(allTablesCursor.getColumnIndex(COLUMN_TABLE_CAPACITY))

            // Add table to available tables list if it's not in the reservedTableNumbers list
            if (!reservedTableNumbers.contains(tableNumber)) {
                availableTables.add(Pair(tableNumber, tableCapacity))
            }
        }

        allTablesCursor.close()
        db.close()

        return availableTables
    }

    @SuppressLint("Range")
    fun getTotalBookingsForToday(): Int {
        val db = this.readableDatabase

        // Get current date in "yyyy-MM-dd" format
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        val cursor = db.rawQuery(
            "SELECT COUNT(*) AS count FROM $TABLE_BOOKINGS WHERE $COLUMN_DATE = ?",
            arrayOf(today)
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex("count"))
        }
        cursor.close()
        db.close()
        return count
    }

    @SuppressLint("Range")
    fun getTotalBookingsFromTodayToEndOfWeek(): Int {
        val db = this.readableDatabase

        // Get today's date in "yyyy-MM-dd" format
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

        // Get the end of the current week (Sunday)
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val endOfWeek = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Query to count bookings from today to the end of the week
        val cursor = db.rawQuery(
            "SELECT COUNT(*) AS count FROM $TABLE_BOOKINGS WHERE $COLUMN_DATE BETWEEN ? AND ?",
            arrayOf(today, endOfWeek)
        )

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex("count"))
        }
        cursor.close()
        db.close()
        return count
    }






}
