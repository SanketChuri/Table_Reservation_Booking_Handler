package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TablesDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 6
        private const val DATABASE_NAME = "restaurant.db"

        private const val TABLE_TABLES = "tables"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TABLE_NUMBER = "table_number"
        private const val COLUMN_TABLE_CAPACITY = "table_capacity"

        // Modify table creation to remove AUTOINCREMENT
        private const val CREATE_TABLE_TABLES = ("CREATE TABLE $TABLE_TABLES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY, " +  // No AUTOINCREMENT
                "$COLUMN_TABLE_NUMBER INTEGER, " +    // Should match the ID
                "$COLUMN_TABLE_CAPACITY TEXT)")
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_TABLES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TABLES")
        onCreate(db)
    }

    fun insertData(tableId: Int, tableCapacity: String) {
        val db = this.writableDatabase

        // Insert with explicit ID and matching table_number
        val insertStatement = "INSERT INTO $TABLE_TABLES ($COLUMN_ID, $COLUMN_TABLE_NUMBER, $COLUMN_TABLE_CAPACITY) " +
                "VALUES ($tableId, $tableId, '$tableCapacity')"
        db.execSQL(insertStatement)
        db.close()
    }

    fun deleteData(tableId: String) {
        val db = this.writableDatabase
        db.delete(TABLE_TABLES, "$COLUMN_ID = ?", arrayOf(tableId))
        db.close()
    }

    @SuppressLint("Range")
    fun getAllTables(): List<Pair<Int, String>> {
        val tables = mutableListOf<Pair<Int, String>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TABLES", null)
        while (cursor.moveToNext()) {
            val tableNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_TABLE_NUMBER))
            val tableCapacity = cursor.getString(cursor.getColumnIndex(COLUMN_TABLE_CAPACITY))
            tables.add(Pair(tableNumber, tableCapacity))
        }
        cursor.close()
        db.close()
        return tables
    }

    // Method to check if a table with a specific number exists
    fun tableExists(tableNumber: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_TABLES WHERE $COLUMN_TABLE_NUMBER = ?", arrayOf(tableNumber.toString()))
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }
}
