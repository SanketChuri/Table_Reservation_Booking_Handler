package com.example.myapplication

import android.annotation.SuppressLint
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

// Adapter class for displaying bookings in a RecyclerView
class BookingAdapter(
    private val bookingList: List<Booking>,
    private val listener: OnBookingInteractionListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Constants for view types
    private val VIEW_TYPE_DATE_HEADER = 0
    private val VIEW_TYPE_BOOKING = 1

    // Data class to represent items in the adapter
    private data class AdapterItem(val date: String?, val booking: Booking?)

    // List to hold all items
    private val items = mutableListOf<AdapterItem>()

    init {
        // Populate the items list with date headers and bookings
        var lastDate: String? = null
        for (booking in bookingList) {
            if (booking.date != lastDate) {
                items.add(AdapterItem(booking.date, null))
                lastDate = booking.date
            }
            items.add(AdapterItem(null, booking))
        }
    }

    // Determine the view type for a given position
    override fun getItemViewType(position: Int): Int {
        return if (items[position].date != null) VIEW_TYPE_DATE_HEADER else VIEW_TYPE_BOOKING
    }

    // Create appropriate ViewHolder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_DATE_HEADER) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.date_header, parent, false)
            DateHeaderViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.bookings_card, parent, false)
            BookingViewHolder(itemView)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = items[position]

        if (holder is DateHeaderViewHolder) {
            // Bind date header data
            val originalDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
            val formattedDate = currentItem.date?.let {
                originalDateFormat.parse(it)?.let { date ->
                    targetDateFormat.format(date)
                }
            } ?: currentItem.date
            holder.dateHeader.text = formattedDate

            // Format and set the day of the week
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val formattedDay = currentItem.date?.let {
                originalDateFormat.parse(it)?.let { date ->
                    dayFormat.format(date)
                }
            } ?: ""
            holder.dayHeader.text = formattedDay

        } else if (holder is BookingViewHolder) {
            // Bind booking data
            val currentBooking = currentItem.booking!!

            // Format time from 24-hour to 12-hour format
            val originalTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val targetTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = originalTimeFormat.parse(currentBooking.time)?.let {
                targetTimeFormat.format(it)
            } ?: currentBooking.time

            // Set booking details to the view
            holder.customerName.text = currentBooking.customerName
            holder.phoneNumber.text = currentBooking.phoneNumber
            holder.guestCount.text = currentBooking.guestCount.toString()
            holder.tableNumber.text = currentBooking.tableNumber.toString()
            holder.time.text = formattedTime
            holder.notes.text = Html.fromHtml("<b>Notes:</b> ${currentBooking.notes ?: "No notes"}")

            // Handle visibility and functionality of "See More" button for note
            if (currentBooking.notes.isNullOrEmpty()) {
                holder.seeMore.visibility = View.GONE
            } else {
                holder.seeMore.visibility = View.VISIBLE
                holder.seeMore.text = "See More"

                holder.seeMore.setOnClickListener {
                    if (holder.isExpanded) {
                        // Collapse the notes
                        holder.notes.maxLines = 2
                        holder.notes.ellipsize = TextUtils.TruncateAt.END
                        holder.seeMore.text = "See More"
                    } else {
                        // Expand the notes
                        holder.notes.maxLines = Integer.MAX_VALUE
                        holder.notes.ellipsize = null
                        holder.seeMore.text = "See Less"
                    }
                    holder.isExpanded = !holder.isExpanded
                }
            }

            // Set click listeners for update and delete buttons
            holder.buttonUpdate.setOnClickListener {
                listener.onUpdateBooking(currentBooking)
            }
            holder.buttonDelete.setOnClickListener {
                listener.onDeleteBooking(currentBooking)
            }
        }
    }

    override fun getItemCount() = items.size

    // ViewHolder for booking items
    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateHeader: TextView = itemView.findViewById(R.id.date_header_text)
        val dayHeader: TextView = itemView.findViewById(R.id.day_header_text)
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.card_customerName)
        val phoneNumber: TextView = itemView.findViewById(R.id.card_customerNumber)
        val guestCount: TextView = itemView.findViewById(R.id.card_guestCount)
        val tableNumber: TextView = itemView.findViewById(R.id.card_tableNumber)
        val time: TextView = itemView.findViewById(R.id.card_bookingTime)
        val notes: TextView = itemView.findViewById(R.id.card_Notes)
        val seeMore: TextView = itemView.findViewById(R.id.card_SeeMore)
        val buttonUpdate: ImageButton = itemView.findViewById(R.id.button_update)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.button_delete)
        var isExpanded: Boolean = false
    }
}

// Interface for handling booking interactions
interface OnBookingInteractionListener {
    fun onUpdateBooking(booking: Booking)
    fun onDeleteBooking(booking: Booking)
}

// Data class representing a booking
data class Booking(
    val customerId: String,
    val customerName: String,
    val phoneNumber: String,
    val guestCount: Int,
    val tableNumber: Int,
    val date: String,
    val time: String,
    val notes: String?
)