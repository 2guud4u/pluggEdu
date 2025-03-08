package com.jia.pluggedu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Send
import kotlinx.coroutines.launch

sealed class Routes {
    @Serializable
    data object Start

    @Serializable
    data object Connect

    @Serializable
    data object Interact
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluggedScreen(viewModel: PluggedViewModel, ipAddress: String) {
    // Collect state from ViewModel
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val mode by remember { viewModel.connectionMode }
    val serverIp by remember { viewModel.serverIp }
    val port by remember { viewModel.port }
    val isConnected by remember { viewModel.isConnected }
    val logMessages = viewModel.questionsList
    val navController = rememberNavController()
    val connectionStatus by remember {viewModel.connectionStatus}
    fun postSnackBar(msg: String) {
        scope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }
    LaunchedEffect(connectionStatus) {
        when (connectionStatus) {
            Status.CLIENT_CONNECT_SUCC -> {
                postSnackBar("Connection Success!")
                navController.navigate(
                    Routes.Interact
                )
            }
            Status.CLIENT_CONNECT_FAIL -> {
                postSnackBar("Connection Failure")

            }
            Status.SERVER_START_FAIL -> {
                postSnackBar("Server Start Failed")
            }
            Status.SERVER_START_SUCC -> {
                postSnackBar("Server Started!")
                navController.navigate(
                    Routes.Interact
                )
            }
            null -> {}
        }
        viewModel.connectionStatus.value = null
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {

            Scaffold(snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }, topBar = {
                PluggedTopBar()
            }, bottomBar = {
                PluggedBottomBar()

            }, floatingActionButton = {
                FloatingActionButton(onClick = {
                    postSnackBar("floaty")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }) { innerPadding ->
                // Main content area with flexible height
                Box(
                    modifier = Modifier

                        .fillMaxWidth()
                        .padding(innerPadding), contentAlignment = Alignment.Center
                ) {
                    NavHost(
                        navController = navController, startDestination = Routes.Start
                    ) {

                        composable<Routes.Start> {

                            ModeSelectionScreen(viewModel, onSelect = { mode ->
                                navController.navigate(
                                    Routes.Connect
                                )
                                viewModel.setMode(mode);

                            }

                            )


                        }

                        composable<Routes.Connect> {
                            if (mode.isNotEmpty() && !isConnected) {
                                ConnectionSettingsScreen(mode = mode,
                                    ipAddress = ipAddress,
                                    serverIp = serverIp,
                                    port = port,
                                    onServerIpChange = { viewModel.serverIp.value = it },
                                    onPortChange = { viewModel.port.value = it },
                                    onStartServer = {
                                        viewModel.startServer(port,
                                            ipAddress,
                                            postSnackBar = { msg -> postSnackBar(msg) })

                                    },
                                    onConnectToServer = {
                                        viewModel.connectToServer(serverIp,
                                            port,
                                            postSnackBar = { msg -> postSnackBar(msg) })

                                    },
                                    onBack = {
                                        navController.navigate(
                                            Routes.Start
                                        )
                                    }


                                )
                            }

                        }
                        composable<Routes.Interact> { backstackEntry ->

                                InteractionScreen(
                                    viewModel,
                                    ipAddress = ipAddress,
                                    logMessages,
                                    onDisconnect = {
                                        navController.navigate(
                                            Routes.Start
                                        )
                                        viewModel.connectionMode.value = ("")
                                    })



                        }
                    }

                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluggedTopBar(
    canNavigateBack: Boolean = false,
    onUpClick: () -> Unit = { },
) {
    CenterAlignedTopAppBar(title = { Text("Plugged", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {

            if (canNavigateBack) {
                IconButton(onClick = onUpClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            } else {
                IconButton(onClick = { /* Handle menu */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        },
        actions = {
            IconButton(onClick = { /* Handle settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
//            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        )
    )
}

@Preview
@Composable
fun PluggedBottomBar() {
    BottomAppBar(
    ) {
        Row(
            horizontalArrangement = Arrangement.Center, // Center content horizontally
            modifier = Modifier.fillMaxWidth(), // Ensure the Row takes up the full width
            verticalAlignment = Alignment.CenterVertically // Ensure vertical alignment
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Person Icon",
                modifier = Modifier.size(40.dp), // Size of the icon
                tint = Color.Black // You can change the color here
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Person Icon",
                modifier = Modifier.size(40.dp), // Size of the icon
                tint = Color.Black // You can change the color here
            )
        }

    }
}