package social.entourage.android.home.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import social.entourage.android.chatbot.api.MistralApi
import social.entourage.android.databinding.BottomSheetChatbotBinding

class ChatBotBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetChatbotBinding? = null
    private val binding get() = _binding!!
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ChatAdapter(messages)
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMessages.adapter = adapter

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnSend.setOnClickListener {
            val text = binding.inputMessage.text.toString()
            if (text.isNotBlank()) {
                addMessage(ChatMessage.User(text))
                binding.inputMessage.text.clear()
                callMistral(text)
            }
        }

        // Message d'accueil
        addMessage(ChatMessage.Bot("Bonjour ! Que voudriez-vous faire dans l'application ?"))
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerMessages.scrollToPosition(messages.size - 1)
    }

    private fun callMistral(userText: String) {
        coroutineScope.launch {
            val botResponse = MistralApi.ask(userText)
            addMessage(ChatMessage.Bot(botResponse))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        _binding = null
    }
}
