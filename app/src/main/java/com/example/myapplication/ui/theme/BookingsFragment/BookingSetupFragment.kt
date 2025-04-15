package com.example.myapplication.ui.theme.SettingsFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.RestaurantDbHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingSetupFragment : Fragment() {

    //Consider using a data class to hold these related properties
    private var selectedDateForStorage: String? = null
    private var selectedTimeForStorage: String? = null
    private var selectedDateForDisplay: String? = null
    private var selectedTimeForDisplay: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tablesetup, container, false)

        // Set up the FloatingActionButton to show the input dialog
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            showInputDialog()
        }

        return view
    }

    @SuppressLint("MissingInflatedId")
    private fun showInputDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.booking_setup_dailoguebox, null)
        // Find all the views in the dialog
        val editTextCustomerName = dialogView.findViewById<EditText>(R.id.editTextCustomerName)
        val editTextPhoneNumber = dialogView.findViewById<EditText>(R.id.editTextPhoneNumber)
        val editTextGuestCount = dialogView.findViewById<EditText>(R.id.editTextGuestCount)
        val spinnerTableNumber = dialogView.findViewById<Spinner>(R.id.spinnerTableNumber)
        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewDate)
        val textViewTime = dialogView.findViewById<TextView>(R.id.textViewTime)
        val editTextNotes = dialogView.findViewById<EditText>(R.id.notes)

        // Fetch table numbers from the database
        CoroutineScope(Dispatchers.Main).launch {
            val tableNumbers = getTableNumbers()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tableNumbers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTableNumber.adapter = adapter
        }

        val calendar = Calendar.getInstance()

//        textViewDate.setOnClickListener {
//            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
//                calendar.set(Calendar.YEAR, year)
//                calendar.set(Calendar.MONTH, month)
//                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//                val dateStorageFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                selectedDateForStorage = dateStorageFormat.format(calendar.time)
//                val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
//                selectedDateForDisplay = dateDisplayFormat.format(calendar.time)
//                textViewDate.text = selectedDateForDisplay
//            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
//        }

        // Set up date picker
        textViewDate.setOnClickListener {
            // Get the current date with Calendar instance
            val currentCalendar = Calendar.getInstance()

            // Ensure the minimum date is set to the start of today
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
            currentCalendar.set(Calendar.MINUTE, 0)
            currentCalendar.set(Calendar.SECOND, 0)
            currentCalendar.set(Calendar.MILLISECOND, 0)

            // Create a DatePickerDialog with the current date
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Set the selected date in the Calendar
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // Format the selected date for storage and display
                    val dateStorageFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDateForStorage = dateStorageFormat.format(calendar.time)
                    val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    selectedDateForDisplay = dateDisplayFormat.format(calendar.time)

                    // Update the TextView to display the selected date
                    textViewDate.text = selectedDateForDisplay
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
            )

            // Set the minimum selectable date to today
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()


            // Show the DatePickerDialog
            datePickerDialog.show()
        }

        // Set up time picker
        textViewTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeStorageFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                selectedTimeForStorage = timeStorageFormat.format(calendar.time)
                val timeDisplayFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                selectedTimeForDisplay = timeDisplayFormat.format(calendar.time)
                textViewTime.text = selectedTimeForDisplay
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        // Build and show the dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Input Dialog")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val customerName = editTextCustomerName.text.toString()
                val phoneNumber = editTextPhoneNumber.text.toString()
                val guestCount = editTextGuestCount.text.toString().toIntOrNull() ?: 0
                val tableNumber = spinnerTableNumber.selectedItem.toString()

                // Save the booking to the database
                CoroutineScope(Dispatchers.Main).launch {
                    saveToDatabase(customerName, phoneNumber, guestCount, tableNumber, selectedDateForStorage, selectedTimeForStorage, editTextNotes.text.toString())
                }
            }
            .setNegativeButton("Cancel", null)

        dialogBuilder.create().show()
    }

    // Fetch table numbers from the database
    private suspend fun getTableNumbers(): List<String> {
        return withContext(Dispatchers.IO) {
            val dbHelper = RestaurantDbHelper(requireContext())
            val tables = dbHelper.getAllTables()
            tables.map { it.first.toString() }
        }
    }

    // Save the booking to the database
    private suspend fun saveToDatabase(customerName: String?, phoneNumber: String?, guestCount: Int, tableNumber: String?, date: String?, time: String?, editTextNotes: String?) {
        withContext(Dispatchers.IO) {
            val dbHelper = RestaurantDbHelper(requireContext())
            if (customerName != null && phoneNumber != null && date != null && time != null) {
                dbHelper.insertBooking(customerName, phoneNumber, guestCount, tableNumber, date, time, editTextNotes)
            }
        }
    }

}
