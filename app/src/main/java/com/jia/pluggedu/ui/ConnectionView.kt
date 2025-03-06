package com.jia.pluggedu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionSettingsScreen(
    mode: String,
    ipAddress: String,
    serverIp: String,
    port: String,
    onServerIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onStartServer: () -> Unit,
    onConnectToServer: () -> Unit,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = if (mode == "server") "Session Settings" else "Enter Class Info!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (mode == "server") {
            Text(
                text = "Session Ip: $ipAddress",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                label = { Text("Port") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBack
                ) {
                    Text("Back")
                }

                Button(
                    onClick = onStartServer
                ) {
                    Text("Start")
                }
            }


        } else { // Client mode
            OutlinedTextField(
                value = serverIp,
                onValueChange = onServerIpChange,
                label = { Text("Class IP") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                label = { Text("Port") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBack
                ) {
                    Text("Back")
                }

                Button(
                    onClick = onConnectToServer
                ) {
                    Text("Start")
                }
            }

        }
    }

}