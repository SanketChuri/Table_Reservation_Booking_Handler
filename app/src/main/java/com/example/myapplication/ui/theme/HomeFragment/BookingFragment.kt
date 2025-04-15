package com.example.myapplication

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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingFragment : Fragment(), OnBookingInteractionListener {

    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var recyclerView: RecyclerView
    private val bookings = mutableListOf<Booking>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_booking, container, false)

        // Initialize RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.recyclerViewBookings)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        bookingAdapter = BookingAdapter(bookings, this)
        recyclerView.adapter = bookingAdapter

        // Set up FAB for adding new bookings
        val fab: FloatingActionButton = view.findViewById(R.id.floatingActionButtonBookings)
        fab.setOnClickListener {
            showInputDialog()
        }

        // Load existing bookings
        loadBookings()

        return view
    }

    // Load all bookings from the database and update the UI
    @SuppressLint("NotifyDataSetChanged")
    private fun loadBookings() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val dbHelper = RestaurantDbHelper(requireContext())
                val allBookings = withContext(Dispatchers.IO) {
                    dbHelper.getAllBookings()
                }
                bookings.clear()
                bookings.addAll(allBookings)

                bookingAdapter = BookingAdapter(bookings, this@BookingFragment)
                recyclerView.adapter = bookingAdapter

                // Show empty view if there are no bookings
                val emptyView = view?.findViewById<TextView>(R.id.emptyView)
                if (bookings.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView?.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView?.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Delete a booking from the database
    private fun deleteBooking(bookingId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbHelper = RestaurantDbHelper(requireContext())
                dbHelper.deleteBooking(bookingId.toInt())
                withContext(Dispatchers.Main) {
                    loadBookings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Show dialog for adding or editing a booking
    @SuppressLint("MissingInflatedId")
    private fun showInputDialog(booking: Booking? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.booking_setup_dailoguebox, null)
        val editTextCustomerName = dialogView.findViewById<EditText>(R.id.editTextCustomerName)
        val editTextPhoneNumber = dialogView.findViewById<EditText>(R.id.editTextPhoneNumber)
        val editTextGuestCount = dialogView.findViewById<EditText>(R.id.editTextGuestCount)
        val spinnerTableNumber = dialogView.findViewById<Spinner>(R.id.spinnerTableNumber)
        val textViewDate = dialogView.findViewById<TextView>(R.id.textViewDate)
        val textViewTime = dialogView.findViewById<TextView>(R.id.textViewTime)
        val editTextNotes = dialogView.findViewById<EditText>(R.id.notes)

        var selectedDateForStorage: String? = null
        var selectedTimeForStorage: String? = null

        // Load table numbers for the spinner
        CoroutineScope(Dispatchers.Main).launch {
            val tableNumbers = getTableNumbers()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tableNumbers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTableNumber.adapter = adapter
        }

        val calendar = Calendar.getInstance()

        // Initialize the dialog with the existing booking data if editing
        booking?.let {
            editTextCustomerName.setText(it.customerName)
            editTextPhoneNumber.setText(it.phoneNumber)
            editTextGuestCount.setText(it.guestCount.toString())
            editTextNotes.setText(it.notes)

            // Convert and display date in readable format
            val storageDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayDateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
            selectedDateForStorage = it.date
            textViewDate.text = storageDateFormat.parse(it.date)?.let { date ->
                displayDateFormat.format(date)
            } ?: it.date

            // Convert and display time in readable format
            val storageTimeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            selectedTimeForStorage = it.time
            textViewTime.text = storageTimeFormat.parse(it.time)?.let { time ->
                displayTimeFormat.format(time)
            } ?: it.time

            spinnerTableNumber.setSelection(getTablePosition(it.tableNumber))
        }
        // Function to update available tables based on selected date and time
        fun updateAvailableTables(date: String, time: String) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val availableTables = withContext(Dispatchers.IO) {
                        val dbHelper = RestaurantDbHelper(requireContext())
                        dbHelper.getAvailableTables(date, time)
                    }
                    val tableNumbers = availableTables.map { it.first.toString() }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tableNumbers)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerTableNumber.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error loading available tables", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up date picker dialog
        textViewDate.setOnClickListener {
            val currentCalendar = Calendar.getInstance()
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
            currentCalendar.set(Calendar.MINUTE, 0)
            currentCalendar.set(Calendar.SECOND, 0)
            currentCalendar.set(Calendar.MILLISECOND, 0)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val dateStorageFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDateForStorage = dateStorageFormat.format(calendar.time)
                    val dateDisplayFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
                    textViewDate.text = dateDisplayFormat.format(calendar.time)

                    // Update available tables whenever date is selected
                    if (selectedTimeForStorage != null) {
                        updateAvailableTables(selectedDateForStorage!!, selectedTimeForStorage!!)
                    }
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // Set up time picker dialog
        textViewTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeStorageFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                selectedTimeForStorage = timeStorageFormat.format(calendar.time)
                val timeDisplayFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                textViewTime.text = timeDisplayFormat.format(calendar.time)

                // Update available tables whenever time is selected
                if (selectedDateForStorage != null) {
                    updateAvailableTables(selectedDateForStorage!!, selectedTimeForStorage!!)
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        // Build and show the dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle(if (booking != null) "Update Booking" else "New Booking")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val customerName = editTextCustomerName.text.toString().trim()
                val phoneNumber = editTextPhoneNumber.text.toString().trim()
                val guestCount = editTextGuestCount.text.toString().toIntOrNull() ?: 0
                val tableNumber = spinnerTableNumber.selectedItem?.toString() ?: ""
                val dateForStorage = selectedDateForStorage ?: ""
                val timeForStorage = selectedTimeForStorage ?: ""

                if (customerName.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter the customer's name.", Toast.LENGTH_SHORT).show()
                    showInputDialog(booking)
                    return@setPositiveButton
                }

                if (dateForStorage.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select a date.", Toast.LENGTH_SHORT).show()
                    showInputDialog(booking)
                    return@setPositiveButton
                }

                if (timeForStorage.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select a time.", Toast.LENGTH_SHORT).show()
                    showInputDialog(booking)
                    return@setPositiveButton
                }

                if (guestCount <= 0) {
                    Toast.makeText(requireContext(), "Please enter a valid guest count.", Toast.LENGTH_SHORT).show()
                    showInputDialog(booking)
                    return@setPositiveButton
                }

                if (tableNumber.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select a table number.", Toast.LENGTH_SHORT).show()
                    showInputDialog(booking)
                    return@setPositiveButton
                }

                CoroutineScope(Dispatchers.Main).launch {
                    if (booking != null) {
                        updateBooking(booking.customerId, customerName, phoneNumber, guestCount, tableNumber, dateForStorage, timeForStorage, editTextNotes.text.toString())
                    } else {
                        saveBooking(customerName, phoneNumber, guestCount, tableNumber, dateForStorage, timeForStorage, editTextNotes.text.toString())
                    }
                }
            }
            .setNegativeButton("Cancel", null)

        dialogBuilder.create().show()
    }


    // Update an existing booking in the database
    private fun updateBooking(bookingId: String, customerName: String, phoneNumber: String, guestCount: Int, tableNumber: String, date: String, time: String, notes: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbHelper = RestaurantDbHelper(requireContext())
                dbHelper.updateBooking(bookingId.toInt(), customerName, phoneNumber, guestCount, tableNumber, date, time, notes)
                withContext(Dispatchers.Main) {
                    loadBookings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Save a new booking to the database
    private fun saveBooking(customerName: String, phoneNumber: String, guestCount: Int, tableNumber: String, date: String, time: String, notes: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dbHelper = RestaurantDbHelper(requireContext())
                dbHelper.insertBooking(customerName, phoneNumber, guestCount, tableNumber, date, time, notes)
                withContext(Dispatchers.Main) {
                    loadBookings()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Get all table numbers from the database
    private suspend fun getTableNumbers(): List<String> {
        return withContext(Dispatchers.IO) {
            val dbHelper = RestaurantDbHelper(requireContext())
            val tables = dbHelper.getAllTables()
            tables.map { it.first.toString() }
        }
    }

    // Get the position of a table number in the spinner
    private fun getTablePosition(tableNumber: Int): Int {
        val tableNumbers = runBlocking { getTableNumbers() }
        return tableNumbers.indexOf(tableNumber.toString())
    }

    // Callback for updating a booking
    override fun onUpdateBooking(booking: Booking) {
        showInputDialog(booking)
    }

    // Callback for deleting a booking
    override fun onDeleteBooking(booking: Booking) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Booking")
            .setMessage("Are you sure you want to delete this booking?")
            .setPositiveButton("Yes") { _, _ ->
                deleteBooking(booking.customerId)
            }
            .setNegativeButton("No", null)
            .show()
    }
}
