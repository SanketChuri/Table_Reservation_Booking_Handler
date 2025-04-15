package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.ui.theme.SettingsFragment.TableSetupFragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Find the button
        val button = view.findViewById<Button>(R.id.button3)

        // Set up click listener
        button.setOnClickListener {
            // Navigate to NewFragment
            val newFragment = TableSetupFragment()
            fragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, newFragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        return view
    }
}
