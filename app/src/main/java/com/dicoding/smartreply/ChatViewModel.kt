package com.dicoding.smartreply

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplyGenerator
import com.google.mlkit.nl.smartreply.SmartReplySuggestion
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult
import com.google.mlkit.nl.smartreply.TextMessage
import java.lang.Exception

class ChatViewModel : ViewModel() {

    private val anotherUserID = "101"

    private val _chatHistory = MutableLiveData<ArrayList<Message>>()
    val chatHistory: LiveData<ArrayList<Message>> = _chatHistory

    private val _pretendingAsAnotherUser = MutableLiveData<Boolean>()
    val pretendingAsAnotherUser: LiveData<Boolean> = _pretendingAsAnotherUser

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val smartReply: SmartReplyGenerator = SmartReply.getClient()

    init {
        _pretendingAsAnotherUser.value = false
    }

    fun switchUser() {
        val value = _pretendingAsAnotherUser.value!!
        _pretendingAsAnotherUser.value = !value
    }

    fun setMessages(messages: ArrayList<Message>) {
        _chatHistory.value = messages
    }

    fun addMessage(message: String) {
        val user = _pretendingAsAnotherUser.value!!

        var list: ArrayList<Message> = chatHistory.value ?: ArrayList()
        list.add(Message(message, !user, System.currentTimeMillis()))

        _chatHistory.value = list
    }

    private fun generateSmartReplyOptions(
        messages: List<Message>,
        isPretendingAsAnotherUser: Boolean
    ): Task<List<SmartReplySuggestion>> {
        val lastMessgae = messages.last()

        if(lastMessgae.isLocalUser != isPretendingAsAnotherUser) {
            return Tasks.forException(Exception("Tidak menjalankan smart replyy!"))
        }

        val chatConversations = ArrayList<TextMessage>()
        for(message in messages) {
            if(message.isLocalUser != isPretendingAsAnotherUser) {
                chatConversations.add(TextMessage.createForLocalUser(message.text, message.timestamp))
            } else {
                chatConversations.add(
                    TextMessage.createForRemoteUser(message.text, message.timestamp, anotherUserID)
                )
            }
        }

        return smartReply.suggestReplies(chatConversations)
            .continueWith{ task ->
                val result = task.result
                when(result.status) {
                    SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE ->
                        _errorMessage.value = "Unable to generate options due to a non-English language was used"
                    SmartReplySuggestionResult.STATUS_NO_REPLY ->
                        _errorMessage.value = "Unable to generate options due to no appropriate response found"
                }
                result.suggestions
            }
            .addOnFailureListener{e ->
                _errorMessage.value = "An error has occurred on Smart Reply Instance"
            }
    }

    override fun onCleared() {
        super.onCleared()
        smartReply.close()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }

}
