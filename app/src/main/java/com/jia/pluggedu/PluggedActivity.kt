package com.jia.pluggedu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.jia.pluggedu.ui.PluggedScreen
import com.jia.pluggedu.ui.PluggedViewModel

class PluggedActivity : ComponentActivity() {
    private val viewModel: PluggedViewModel by viewModels()

    // For permission request
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
//            viewModel.addLogMessage("All permissions granted")
        } else {
//            viewModel.addLogMessage("Some permissions were denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        requestNeededPermissions()

        // Get the device's IP address
        val ipAddress = viewModel.getLocalIpAddress()

        setContent {
            PluggedScreen(viewModel, ipAddress)
        }
    }

    private fun requestNeededPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestPermissionsLauncher.launch(permissions)
        }
    }

    override fun onDestroy() {
        viewModel.cleanup()
        super.onDestroy()
    }
}