package com.example.cafemode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cafemode.databinding.ActivityMainBinding
import com.example.cafemode.utils.AudioPermissionHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        binding.toggleButton.setOnClickListener {
            if (AudioPermissionHelper.hasAllPermissions(this)) {
                toggleCafeMode()
            } else {
                AudioPermissionHelper.requestPermissions(this)
            }
        }

        binding.intensitySlider.addOnChangeListener { _, value, _ ->
            updateServiceParameter(CafeModeService.ACTION_UPDATE_INTENSITY, "intensity", value)
        }

        binding.spatialSlider.addOnChangeListener { _, value, _ ->
            updateServiceParameter(CafeModeService.ACTION_UPDATE_SPATIAL, "spatial", value)
        }
    }

    private fun updateServiceParameter(action: String, paramName: String, value: Float) {
        if (isServiceRunning) {
            val intent = Intent(this, CafeModeService::class.java).apply {
                this.action = action
                putExtra(paramName, value)
            }
            startService(intent)
        }
    }

    private fun toggleCafeMode() {
        if (isServiceRunning) {
            stopCafeMode()
        } else {
            startCafeMode()
        }
    }

    private fun startCafeMode() {
        val intent = Intent(this, CafeModeService::class.java)
        startForegroundService(intent)
        isServiceRunning = true
        updateUI()
        showToast("🎵 Café Mode Activated - Your music now has that perfect ambient feel!")
    }

    private fun stopCafeMode() {
        val intent = Intent(this, CafeModeService::class.java)
        stopService(intent)
        isServiceRunning = false
        updateUI()
        showToast("Café Mode Deactivated")
    }

    private fun updateUI() {
        binding.toggleButton.text = if (isServiceRunning) "Turn Off Café Mode" else "Turn On Café Mode"
        binding.statusText.text = if (isServiceRunning) "Status: ☕ Active" else "Status: Inactive"
        binding.intensitySlider.isEnabled = isServiceRunning
        binding.spatialSlider.isEnabled = isServiceRunning
    }

    private fun checkPermissions() {
        if (!AudioPermissionHelper.hasAllPermissions(this)) {
            AudioPermissionHelper.requestPermissions(this)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AudioPermissionHelper.PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showToast("✅ Permissions granted - Ready to transform your audio!")
            } else {
                showToast("⚠️ Audio permissions required for café mode processing")
            }
        }
    }
}