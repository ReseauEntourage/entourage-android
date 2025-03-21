package social.entourage.android.chatbot.model

sealed class ChatMessage {
    data class User(val message: String): ChatMessage()
    data class Bot(val message: String): ChatMessage()
}
