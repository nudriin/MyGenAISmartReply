package com.dicoding.smartreply

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    private val anotherUserID = "101"

    private val _chatHistory = MutableLiveData<ArrayList<Message>>()
    val chatHistory: LiveData<ArrayList<Message>> = _chatHistory

    private val _pretendingAsAnotherUser = MutableLiveData<Boolean>()
    val pretendingAsAnotherUser: LiveData<Boolean> = _pretendingAsAnotherUser

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

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

    companion object {
        private const val TAG = "ChatViewModel"
    }

}
