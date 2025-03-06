package com.jia.pluggedu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment


@Composable
fun ModeSelectionScreen(viewModel: PluggedViewModel, onSelect: (String) -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("You are a")
        Button(
            onClick = { onSelect("server") },
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Teacher!")
        }
        Button(
            onClick = { onSelect("client") },
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Student!")
        }
    }
}