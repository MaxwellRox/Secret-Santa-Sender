package com.example.secretsantasender

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SecretSantaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendEmails not enough participants emits uiEvent`() = runTest {
        val viewModel = SecretSantaViewModel()
        viewModel.uiEvents.test {
            viewModel.sendEmails()
            assertEquals("Please add at least two participants.", awaitItem())
        }
    }

    @Test
    fun `sendEmails with enough participants emits correct emailData`() = runTest {
        val viewModel = SecretSantaViewModel()
        viewModel.participants = listOf(
            Participant("John", "john@example.com", "A book"),
            Participant("Jane", "jane@example.com", "A scarf")
        )

        viewModel.emailDataFlow.test {
            viewModel.sendEmails()

            val firstEmail = awaitItem()
            val secondEmail = awaitItem()

            // Assert that the Santas are not assigned to themselves
            assertNotEquals(firstEmail.to, "john@example.com")
            assertNotEquals(secondEmail.to, "jane@example.com")

            // Assert that the recipients are correct
            if (firstEmail.to == "jane@example.com") {
                assertEquals("Hi Jane,\n\nYou have been assigned to be the Secret Santa for John.\n\nTheir gift idea is: \"A book\".\n\nHappy Gifting!", firstEmail.body)
            } else {
                assertEquals("Hi John,\n\nYou have been assigned to be the Secret Santa for Jane.\n\nTheir gift idea is: \"A scarf\".\n\nHappy Gifting!", firstEmail.body)
            }
        }
    }
}