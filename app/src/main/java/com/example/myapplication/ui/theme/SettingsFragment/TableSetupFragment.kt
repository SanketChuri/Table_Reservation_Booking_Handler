package com.example.myapplication.ui.theme.SettingsFragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.RestaurantDbHelper
import com.example.myapplication.ui.theme.TableAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TableSetupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TableAdapter
    private lateinit var dbHelper: RestaurantDbHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tablesetup, container, false)

        // Initialize the unified RestaurantDbHelper
        dbHelper = RestaurantDbHelper(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewTables)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3) // Set 3 columns

        // Set up FAB for adding new tables
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            showInputDialog()
        }
        // Load existing table data
        loadTableData()

        return view
    }

    private fun showInputDialog() {
        // Inflate the custom layout for the dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.table_setup_dailoguebox, null)
        val editTableNumber = dialogView.findViewById<EditText>(R.id.editTableNumber)
        val editTableCapacity = dialogView.findViewById<EditText>(R.id.editTableCapacity)

        // Build the AlertDialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Table Setup")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val tableNumberStr = editTableNumber.text.toString().trim()
                val tableCapacity = editTableCapacity.text.toString().trim()

                // Input validation
                when {
                    tableNumberStr.isEmpty() -> {
                        Toast.makeText(requireContext(), "Please enter table number", Toast.LENGTH_SHORT).show()
                        showInputDialog() // Re-open the dialog to correct input
                    }
                    tableCapacity.isEmpty() -> {
                        Toast.makeText(requireContext(), "Please enter table capacity", Toast.LENGTH_SHORT).show()
                        showInputDialog() // Re-open the dialog to correct input
                    }
                    else -> {
                        val tableNumber = tableNumberStr.toIntOrNull()
                        if (tableNumber == null) {
                            Toast.makeText(requireContext(), "Invalid table number format", Toast.LENGTH_SHORT).show()
                            showInputDialog() // Re-open the dialog to correct input
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                if (dbHelper.tableExists(tableNumber)) {
                                    Toast.makeText(requireContext(), "Table already exists", Toast.LENGTH_SHORT).show()
                                } else {
                                    saveToDatabase(tableNumber, tableCapacity)
                                    loadTableData() // Refresh data after saving
                                }
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)

        // Create and show the dialog
        dialogBuilder.create().show()
    }

    // Save table data to the database
    private suspend fun saveToDatabase(tableNumber: Int, tableCapacity: String) {
        withContext(Dispatchers.IO) {
            dbHelper.insertTable(tableNumber, tableCapacity) // Adjusted method name to insert table data
        }
    }
    // Load all table data from the database and update the UI
    private fun loadTableData() {
        CoroutineScope(Dispatchers.Main).launch {
            val tables = withContext(Dispatchers.IO) {
                dbHelper.getAllTables()
            }
            // Sort the list of tables by tableNumber in ascending order
            val sortedTables = tables.sortedBy { it.first }

            adapter = TableAdapter(requireContext(), sortedTables, dbHelper)
            recyclerView.adapter = adapter
        }
    }
}
