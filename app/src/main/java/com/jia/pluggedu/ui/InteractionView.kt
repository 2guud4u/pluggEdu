package com.jia.pluggedu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InteractionScreen(
    viewModel: PluggedViewModel,
    ipAddress: String,
    logMessages: SnapshotStateList<PluggedViewModel.LogMessage>,
    onDisconnect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Align InteractTopBar to the top-center
        InteractTopBar(classCode = viewModel.connectedIp.value, viewModel.serverSize.intValue)
        // Center content in the Box

        when (viewModel.connectionMode.value) {
            "client" -> StudentContent(viewModel)
            "server" -> TeacherContent(viewModel)
        }
        Spacer(modifier = Modifier.weight(1f))
        DisconnectButton(
            mode = viewModel.connectionMode.value,
            onDisconnect = {
                if (viewModel.connectionMode.value == "server") {
                    viewModel.stopServer()
                } else {
                    viewModel.disconnectFromServer()
                }
                onDisconnect()

            }
        )
    }

}


@Composable
fun TeacherContent(viewModel: PluggedViewModel) {

    Column(
    ) {

        Button(onClick = {

        }) {
            Text("Comprehension Check")
        }

        HorizontalDivider(
            thickness = 2.dp, // Thicker line
            color = Color.Gray, // Custom color,
            modifier = Modifier
                .width(370.dp)
                .padding(vertical = 16.dp)
        )


        Card(

        ) {
            LazyColumn(
//            state = listState,
            ) {
                itemsIndexed(viewModel.questionsList) { index, message ->
                    MessageItem(message, { viewModel.removeQuestion(index) })
                }
            }
        }
    }


}

@Composable
fun StudentContent(viewModel: PluggedViewModel) {
    val messageToSend by remember { viewModel.messageToSend }
    Column() {
        Row() {
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text("Slow Down Please")
            }
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(),
            ) {
                Text("I Am Confused")
            }
        }
        HorizontalDivider(
            thickness = 2.dp, // Thicker line
            color = Color.Gray, // Custom color,
            modifier = Modifier
                .width(370.dp)
                .padding(vertical = 16.dp)
        )
        Column() {
            MessageInputRow(
                messageToSend = messageToSend,
                onMessageChange = { viewModel.messageToSend.value = it },
                onSendMessage = { viewModel.sendMessage(messageToSend) }
            )
        }
    }
}

@Composable
fun InteractTopBar(
    classCode: String,
    count: Int = 0,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column() {
                Text(
                    text = "Class IP",
                    style = MaterialTheme.typography.titleSmall, // Making it small
                )
                Text(
                    text = classCode,
                    style = MaterialTheme.typography.titleLarge,
                )

            }
            PeopleCount(count)
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp), // Adds padding on the sides
            thickness = 2.dp, // Thicker line
            color = Color.Gray // Custom color
        )

    }

}

@Composable
fun MessageItem(message: PluggedViewModel.LogMessage, onDeleteClick: () -> Unit) {

    //only log question
    if (message.type == PluggedViewModel.LogMessage.TYPE_QUESTION) {
        NotifCard(
            title = "Question",
            subtitle = "Anon",
            description = message.text,
            imageUrl = "",
            onDeleteClick = onDeleteClick,
            isElevated = false,
        )
    }


}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputRow(
    messageToSend: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageToSend,
            onValueChange = onMessageChange,
            label = { Text("Question") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            maxLines = 3
        )

        IconButton(
            onClick = onSendMessage,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun DisconnectButton(
    mode: String,
    onDisconnect: () -> Unit
) {
    Button(
        onClick = onDisconnect,
        modifier = Modifier
    ) {
        Text(if (mode == "server") "Stop Server" else "Disconnect")
    }
}