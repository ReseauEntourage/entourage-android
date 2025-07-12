package social.entourage.android.home.chatbot

sealed class ChatMessage {
    data class User(val message: String): ChatMessage()
    data class Bot(val message: String): ChatMessage()
}
