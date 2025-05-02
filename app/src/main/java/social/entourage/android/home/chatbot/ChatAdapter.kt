package social.entourage.android.chatbot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.ItemChatBotMessageBinding
import social.entourage.android.databinding.ItemChatUserMessageBinding
import social.entourage.android.chatbot.model.ChatMessage

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int) = when(messages[position]) {
        is ChatMessage.User -> 0
        is ChatMessage.Bot -> 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val binding = ItemChatUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemChatBotMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            BotViewHolder(binding)
        }
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind((msg as ChatMessage.User).message)
            is BotViewHolder -> holder.bind((msg as ChatMessage.Bot).message)
        }
    }

    class UserViewHolder(private val binding: ItemChatUserMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: String) { binding.userMessage.text = msg }
    }

    class BotViewHolder(private val binding: ItemChatBotMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: String) { binding.botMessage.text = msg }
    }
}
