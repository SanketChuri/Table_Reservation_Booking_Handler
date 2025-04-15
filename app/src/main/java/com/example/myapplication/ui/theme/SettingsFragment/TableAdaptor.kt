package com.example.myapplication.ui.theme

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.RestaurantDbHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Adapter class for managing a list of tables displayed in a RecyclerView.
class TableAdapter(
    private val context: Context,
    private var tables: List<Pair<Int, String>>,
    private val dbHelper: RestaurantDbHelper // Use the unified RestaurantDbHelper
) : RecyclerView.Adapter<TableAdapter.ViewHolder>() {

    //Inflates the table card layout to represent each table.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.tables_card, parent, false)
        return ViewHolder(view)
    }

    //Binds the table data (number and capacity) to the ViewHolder UI components.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the table number and capacity from the data set
        val (tableNumber, tableCapacity) = tables[position]

        // Set the text fields for table number and capacity
        holder.tableNumber.text = tableNumber.toString()
        holder.tableCapacity.text = tableCapacity

        holder.deleteButton.setOnClickListener {
            // Show an AlertDialog to confirm deletion
            AlertDialog.Builder(context)
                .setTitle("Delete Table")
                .setMessage("Are you sure you want to delete this table?")
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    // Launch a coroutine to handle table deletion in the background
                    CoroutineScope(Dispatchers.Main).launch {
                        deleteTable(tableNumber, position)
                    }
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
        }
    }

    //Returns the total number of tables in the data set.
    override fun getItemCount() = tables.size

    //Deletes a table from the database and updates the UI by removing it from the list.
    private suspend fun deleteTable(tableNumber: Int, position: Int) {
        withContext(Dispatchers.IO) {
            dbHelper.deleteTable(tableNumber) // Adjusted method to match RestaurantDbHelper
        }
        // Remove the table from the list
        tables = tables.toMutableList().apply { removeAt(position) }
        withContext(Dispatchers.Main) {
            notifyItemRemoved(position)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tableNumber: TextView = itemView.findViewById(R.id.tableNumber)
        val tableCapacity: TextView = itemView.findViewById(R.id.tableCapacity)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }
}
