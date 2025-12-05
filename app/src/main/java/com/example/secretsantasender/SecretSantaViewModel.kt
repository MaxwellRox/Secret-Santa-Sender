package com.example.secretsantasender

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class EmailData(val to: String, val subject: String, val body: String)

class SecretSantaViewModel : ViewModel() {
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var giftIdea by mutableStateOf("")
    var participants by mutableStateOf(listOf<Participant>())

    private val _emailDataFlow = MutableSharedFlow<EmailData>()
    val emailDataFlow = _emailDataFlow.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun addParticipant() {
        if (name.isNotBlank() && email.isNotBlank()) {
            participants = participants + Participant(name, email, giftIdea)
            name = ""
            email = ""
            giftIdea = ""
        }
    }

    fun sendEmails() {
        if (participants.size < 2) {
            viewModelScope.launch {
                _uiEvents.emit("Please add at least two participants.")
            }
            return
        }

        val assignments = generateAssignments()
        viewModelScope.launch {
            assignments.forEach { (santa, recipient) ->
                val subject = "Your Secret Santa Assignment!"
                val body = "Hi ${santa.name},\n\nYou have been assigned to be the Secret Santa for ${recipient.name}.\n\nTheir gift idea is: \"${recipient.giftIdea}\".\n\nHappy Gifting!"
                _emailDataFlow.emit(EmailData(to = santa.email, subject = subject, body = body))
            }
        }
    }

    private fun generateAssignments(): Map<Participant, Participant> {
        val shuffledParticipants = participants.shuffled()
        val assignments = mutableMapOf<Participant, Participant>()
        for (i in shuffledParticipants.indices) {
            val santa = shuffledParticipants[i]
            val recipient = shuffledParticipants[(i + 1) % shuffledParticipants.size]
            assignments[santa] = recipient
        }
        return assignments
    }
}
