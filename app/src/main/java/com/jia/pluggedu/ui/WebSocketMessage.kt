package com.jia.pluggedu.ui

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Base class for structured WebSocket messages
 */
data class WebSocketMessage(
    @SerializedName("type") val type: String,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("content") val content: String = ""
) {
    companion object {


        private val gson = Gson()

        // Message types

        const val TYPE_QUESTION = "CHAT"
        const val TYPE_STATUS = "STATUS"
        const val TYPE_SYSTEM = "SYSTEM"
        const val TYPE_COMMAND = "COMMAND"
        const val TYPE_FEEDBACK = "FEEDBACK"

        // Status types
        const val STATUS_CONNECTED = "CONNECTED"
        const val STATUS_DISCONNECTED = "DISCONNECTED"

        // Factory methods for creating different message types
        fun createChatMessage(content: String): String {
            return gson.toJson(WebSocketMessage(TYPE_QUESTION, content = content))
        }

        fun createStatusMessage(status: String): String {
            return gson.toJson(WebSocketMessage(TYPE_STATUS, content = status))
        }

        fun createSystemMessage(content: String): String {
            return gson.toJson(WebSocketMessage(TYPE_SYSTEM, content = content))
        }
        fun createFeedbackMessage(content: String): String {
            return gson.toJson(WebSocketMessage(TYPE_FEEDBACK, content = content))
        }


        fun fromJson(json: String): WebSocketMessage? {
            return try {
                gson.fromJson(json, WebSocketMessage::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}