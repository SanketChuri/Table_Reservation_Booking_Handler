package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Schedule backup work every 48 hours
        scheduleBackupWork()

        // Get references to UI elements
        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.login_button)

        // Set up login button click listener
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Simple login logic
            if (username == "admin" && password == "password") {
                // Show success message
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                // Open HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
                // Optional: Finish MainActivity so it cannot be returned to with the back button

            } else {
                // Show failure message
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Schedule backup work to run every 48 hours
    private fun scheduleBackupWork() {
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(10, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder()
                .setRequiresCharging(true) // Optional: Only perform backup while charging
                .setRequiredNetworkType(NetworkType.CONNECTED) // Optional: Requires network
                .build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DatabaseBackup",
            ExistingPeriodicWorkPolicy.REPLACE,
            backupRequest
        )
    }
}
