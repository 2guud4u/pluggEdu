package com.jia.pluggedu.ui
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//enum class Status{
//    CLIENT_CONNECT_SUCC,
//    CLIENT_CONNECT_FAIL,
//    SERVER_START_FAIL,
//    SERVER_START_SUCC
//}
class PluggedViewModel : ViewModel() {
    private val TAG = "WebSocketApp"
    private val DEFAULT_PORT = 45678
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // State
    val questionsList = mutableStateListOf<LogMessage>()
    val poll = mutableStateOf<LogMessage?>(null)
    val connectionMode = mutableStateOf("")
    val serverIp = mutableStateOf("")
    val port = mutableStateOf(DEFAULT_PORT.toString())
    val messageToSend = mutableStateOf("")
    val isConnected = mutableStateOf(false)
    //    val connectionStatus = mutableStateOf<Status?>(null)
    val connectedIp = mutableStateOf("")
    val serverSize = mutableIntStateOf(0)


    // WebSocket components
    private var server: MyWebSocketServer? = null
    private var client: MyWebSocketClient? = null

    // Track connected clients for server mode
    private val connectedClients = mutableStateListOf<WebSocket>()

    fun setMode(mode: String) {
        connectionMode.value = mode
        addQuestions(LogMessage("Selected $mode mode", LogMessage.TYPE_SYSTEM))
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        val structured = WebSocketMessage.createChatMessage(message)

        when {
            server != null -> {
                server?.broadcast(structured)
                // Add our own message to the log
                processMessage(structured, "You (broadcast)")
            }
            client != null -> {
                client?.send(structured)
                // Add our own message to the log
                processMessage(structured, "You")
            }
        }
        messageToSend.value = ""
    }

    fun startServer(portNumber: String, ipAddress: String, postSnackBar: (String)->Unit) {
        try {
            val portNum = portNumber.toInt()
            startServerInternal(portNum, postSnackBar)
            isConnected.value = true
            connectedIp.value = ipAddress

        } catch (e: NumberFormatException) {
            addQuestions(LogMessage("Invalid port number", LogMessage.TYPE_ERROR))
        }
    }

    private fun startServerInternal(port: Int, postSnackBar: (String)->Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Attempt to check if the port is available
                if (!checkPortAvailability(port)) {
                    postSnackBar("Port $port is already in use. Try another port.")
                    return@launch
                }

                // Try binding to all interfaces
                server = MyWebSocketServer(InetSocketAddress("0.0.0.0", port))
                server?.start()
                Log.d(TAG, "Server started on port: $port")
                postSnackBar("Server Started!")
                addQuestions(LogMessage("Server started on port: $port", LogMessage.TYPE_SYSTEM))
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server: ${e.message}")
                e.printStackTrace()
                addQuestions(LogMessage("Error starting server: ${e.message}", LogMessage.TYPE_ERROR))

                // Try with higher permissions if on a real device
                try {
                    addQuestions(LogMessage("Attempting with elevated privileges...", LogMessage.TYPE_SYSTEM))
                    val process = Runtime.getRuntime().exec("su")
                    server = MyWebSocketServer(InetSocketAddress("0.0.0.0", port))
                    server?.start()
                    addQuestions(LogMessage("Server started with elevated privileges", LogMessage.TYPE_SYSTEM))
                } catch (e2: Exception) {
                    addQuestions(LogMessage("Could not start server: ${e2.message}", LogMessage.TYPE_ERROR))
                }
            }
        }
    }

    private fun checkPortAvailability(port: Int): Boolean {
        return try {
            val serverSocket = ServerSocket(port)
            serverSocket.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Port $port is not available: ${e.message}")
            false
        }
    }

    fun stopServer() {
        server?.let {
            try {
                // Send disconnect message to clients
                val disconnectMessage = WebSocketMessage.createStatusMessage(WebSocketMessage.STATUS_DISCONNECTED)
                it.broadcast(disconnectMessage)

                it.stop()
                server = null
                connectedClients.clear()
                addQuestions(LogMessage("Server stopped", LogMessage.TYPE_SYSTEM))
                isConnected.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server: ${e.message}")
                e.printStackTrace()
                addQuestions(LogMessage("Error stopping server: ${e.message}", LogMessage.TYPE_ERROR))
            }
        }
    }

    fun connectToServer(serverIpAddress: String, portNumber: String, postSnackBar: (String)->Unit) {
        try {
            val portNum = portNumber.toInt()
            connectToServerInternal(serverIpAddress, portNum, postSnackBar)
            isConnected.value = true
            connectedIp.value = serverIp.value

        } catch (e: NumberFormatException) {
            postSnackBar("Connection Error!, make sure port and ip is correct")
        }
    }

    private fun connectToServerInternal(serverIp: String, port: Int, postSnackBar: (String)->Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serverUri = URI("ws://$serverIp:$port")
                client = MyWebSocketClient(serverUri)
                client?.connect()
                Log.d(TAG, "Connecting to $serverIp:$port")

            } catch (e: URISyntaxException) {
                Log.e(TAG, "Error connecting to server: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun disconnectFromServer() {
        client?.let {
            try {
                // Send disconnect message to server
                val disconnectMessage = WebSocketMessage.createStatusMessage(WebSocketMessage.STATUS_DISCONNECTED)
                it.send(disconnectMessage)

                it.close()
                client = null
                addQuestions(LogMessage("Disconnected from server", LogMessage.TYPE_SYSTEM))
                isConnected.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting: ${e.message}")
                e.printStackTrace()
                addQuestions(LogMessage("Error disconnecting: ${e.message}", LogMessage.TYPE_ERROR))
            }
        }
    }

    fun getLocalIpAddress(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()

            for (networkInterface in networkInterfaces) {
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }

                val addresses = networkInterface.inetAddresses.toList()
                for (address in addresses) {
                    if (address.isLoopbackAddress || address.hostAddress.contains(":")) {
                        continue
                    }

                    return address.hostAddress
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address: ${e.message}")
            e.printStackTrace()
            addQuestions(LogMessage("Error getting IP address: ${e.message}", LogMessage.TYPE_ERROR))
        }

        return "Unknown"
    }

    // Process incoming WebSocketMessage
    private fun processMessage(messageJson: String, sender: String) {
        try {
            val message = WebSocketMessage.fromJson(messageJson)
            if (message != null) {
                val formattedTime = dateFormat.format(Date(message.timestamp))

                when (message.type) {
                    WebSocketMessage.TYPE_QUESTION -> {
                        addQuestions(LogMessage("${message.content}", LogMessage.TYPE_QUESTION))
                    }
                    WebSocketMessage.TYPE_STATUS -> {
                        if(message.content == "CONNECTED"){
                            serverSize.intValue += 1
                        } else {
                            serverSize.intValue -= 1
                        }
                    }
                    WebSocketMessage.TYPE_SYSTEM -> {
                        addQuestions(LogMessage("[$formattedTime] System message: ${message.content}", LogMessage.TYPE_SYSTEM))
                    }
                    WebSocketMessage.TYPE_COMMAND -> {
                        addQuestions(LogMessage("[$formattedTime] Command from $sender: ${message.content}", LogMessage.TYPE_COMMAND))
                        // Handle commands - could add special command handling here
                    }
//                    WebSocketMessage.Type_FEEDBACK -> {
//
//                    }
                }
            } else {
                // Handle legacy unstructured messages
                addQuestions(LogMessage("[$sender] $messageJson", LogMessage.TYPE_QUESTION))
            }
        } catch (e: Exception) {
            // If parsing fails, treat as plain text
            addQuestions(LogMessage("[$sender] $messageJson", LogMessage.TYPE_QUESTION))
        }
    }

    fun addQuestions(message: LogMessage) {
        viewModelScope.launch(Dispatchers.Main) {
            questionsList.add(message)
        }
    }

    fun removeQuestion(index: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            questionsList.removeAt(index)
        }
    }

    inner class MyWebSocketServer(address: InetSocketAddress) : WebSocketServer(address) {
        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            val address = conn.remoteSocketAddress.address.hostAddress
            Log.d(TAG, "New connection from: $address")

            // Track the client
            connectedClients.add(conn)

            // Send a welcome message to the client
            val welcomeMessage = WebSocketMessage.createSystemMessage("Welcome to the server!")
            conn.send(welcomeMessage)

            // Notify everyone about the new connection
            val connectMessage = WebSocketMessage.createStatusMessage("Client connected from $address")
            broadcast(connectMessage)

            // Log locally
            addQuestions(LogMessage("New connection from: $address", LogMessage.TYPE_STATUS))
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            val address = conn.remoteSocketAddress?.address?.hostAddress ?: "unknown"
            Log.d(TAG, "Connection closed from $address: $reason")

            // Remove from tracked clients
            connectedClients.remove(conn)

            // Notify other clients
            val disconnectMessage = WebSocketMessage.createStatusMessage("Client disconnected from $address: $reason")
            broadcast(disconnectMessage)

            addQuestions(LogMessage("Connection closed from $address: $reason", LogMessage.TYPE_STATUS))
        }

        override fun onMessage(conn: WebSocket, message: String) {
            val address = conn.remoteSocketAddress.address.hostAddress
            Log.d(TAG, "Received message from $address: $message")

            // Process the structured message
            processMessage(message, "Client ($address)")

            // Echo structured messages back to all clients
            broadcast(message)
        }

        override fun onMessage(conn: WebSocket, message: ByteBuffer) {
            val address = conn.remoteSocketAddress.address.hostAddress
            Log.d(TAG, "Received binary message from $address")
            addQuestions(LogMessage("Received binary message from $address", LogMessage.TYPE_SYSTEM))

            // Could handle binary messages here if needed
        }

        override fun onError(conn: WebSocket?, ex: Exception) {
            val address = conn?.remoteSocketAddress?.address?.hostAddress ?: "unknown"
            Log.e(TAG, "Error occurred with $address: ${ex.message}")
            ex.printStackTrace()
            addQuestions(LogMessage("Error with $address: ${ex.message}", LogMessage.TYPE_ERROR))
//            connectionStatus.value = Status.SERVER_START_FAIL

        }

        override fun onStart() {
            Log.d(TAG, "Server started")
            isConnected.value = true
//            connectionStatus.value = Status.SERVER_START_SUCC
        }
    }

    inner class MyWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {
        override fun onOpen(handshakedata: ServerHandshake) {
            Log.d(TAG, "Connected to server")
            isConnected.value = true
            // Send a connection notification
            val connectMessage = WebSocketMessage.createStatusMessage(WebSocketMessage.STATUS_CONNECTED)
            send(connectMessage)
//            connectionStatus.value = Status.CLIENT_CONNECT_SUCC

        }

        override fun onMessage(message: String) {
            Log.d(TAG, "Received message: $message")

            // Process the structured message
            processMessage(message, "Server")
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.d(TAG, "Connection closed: $reason")
            addQuestions(LogMessage("Connection closed: $reason", LogMessage.TYPE_STATUS))

            // Update UI state if closed by server
            if (remote) {
                isConnected.value = false
            }

        }

        override fun onError(ex: Exception) {
            Log.e(TAG, "Error occurred: ${ex.message}")
            ex.printStackTrace()
            addQuestions(LogMessage("Error: ${ex.message}", LogMessage.TYPE_ERROR))
            isConnected.value = false
//            connectionStatus.value = Status.CLIENT_CONNECT_FAIL

        }
    }

    // Define message types for logs
    data class LogMessage(
        val text: String,
        val type: Int,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        companion object {
            const val TYPE_QUESTION = 0
            const val TYPE_STATUS = 1
            const val TYPE_SYSTEM = 2
            const val TYPE_ERROR = 3
            const val TYPE_COMMAND = 4
        }
    }

    fun cleanup() {
        stopServer()
        disconnectFromServer()
    }
}