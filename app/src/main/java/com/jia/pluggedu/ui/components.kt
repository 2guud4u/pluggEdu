package com.jia.pluggedu.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TextField
import androidx.compose.material3.Card
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
@Composable
fun GenericAvatar(size: Int = 48) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer // Background color
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
@Composable
fun PeopleCount(count: Int) {



    BadgedBox(
        badge = {
            if (count > 0) {
                Badge(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ) {
                    Text("$count")
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.size(35.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer // Background color
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }


    }
}

@Composable
fun RefreshButton(onRefresh: () -> Unit) {
    IconButton(onClick = onRefresh) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh"
        )
    }
}

@Composable
fun TextWithCaption(caption: String, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall, // Caption style
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp)) // Space between caption and text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ItemWithCaption(caption: String, item: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall, // Caption style
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp)) // Space between caption and text
        item()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericTextInput(
    text: String,
    onTextChange: (String) -> Unit,
    labelText: String = "Input label",
    buttonText: String = "Button",
    onButtonClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelSmall, // Caption style
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(4.dp)) // Space between caption and text
        TextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,

                )
        )
    }

    Button(
        onClick = onButtonClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),



        ) {
        Text(buttonText)
    }
}

@Composable
fun NotifCard(
    title: String,
    subtitle: String,
    description: String,
    imageUrl: String,
    onDeleteClick: () -> Unit = {},

    isElevated: Boolean = true,

) {
    val cardComposable: @Composable () -> Unit = {



        Row(
            modifier = Modifier
                .fillMaxWidth()


        ) {

            Spacer(modifier = Modifier.width(16.dp).padding(horizontal = 16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically).padding(vertical = 16.dp)

            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }



        }
    }

    if (isElevated) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            cardComposable()
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = BorderStroke(1.dp, Color.LightGray)

        ) {
            cardComposable()
        }
    }
}



