package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

//DashboardFragment displays total bookings for today and the rest of the week.

class DashboardFragment : Fragment() {

    // TextView to display total bookings for the week
    private lateinit var totalBookingsWeekTextView: TextView
    // TextView to display total bookings for today
    private lateinit var totalBookingsTodayTextView: TextView
    // Database helper for fetching restaurant bookings data
    private lateinit var dbHelper: RestaurantDbHelper

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize TextViews
        totalBookingsWeekTextView = view.findViewById(R.id.total_bookings_week)
        totalBookingsTodayTextView = view.findViewById(R.id.total_bookings_today)

        // Initialize the database helper
        dbHelper = RestaurantDbHelper(requireContext())

        // Fetch the total bookings data from the database
        val totalBookingsForWeek = dbHelper.getTotalBookingsFromTodayToEndOfWeek()
        Log.d("DashboardFragment", "Total Bookings for Week: $totalBookingsForWeek")
        val totalBookingsForToday = dbHelper.getTotalBookingsForToday()

        // Update TextViews with the fetched data
        totalBookingsWeekTextView.text = "Total Bookings for Week: $totalBookingsForWeek"
        totalBookingsTodayTextView.text = "Total Bookings for Today: $totalBookingsForToday"

        return view
    }
}
