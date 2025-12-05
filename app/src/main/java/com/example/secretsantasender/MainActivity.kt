package com.example.secretsantasender

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.secretsantasender.ui.theme.SecretSantaSenderTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: SecretSantaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecretSantaSenderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SecretSantaScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretSantaScreen(modifier: Modifier = Modifier, viewModel: SecretSantaViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.emailDataFlow.collectLatest { emailData ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.to))
                putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
                putExtra(Intent.EXTRA_TEXT, emailData.body)
            }
            context.startActivity(Intent.createChooser(intent, "Send Email"))
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = viewModel.giftIdea,
            onValueChange = { viewModel.giftIdea = it },
            label = { Text("Gift Idea") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.addParticipant() }) {
                Text("Add Participant")
            }
            Button(onClick = { viewModel.sendEmails() }) {
                Text("Send Emails")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(viewModel.participants) { participant ->
                Text("Name: ${participant.name}, Email: ${participant.email}, Gift Idea: ${participant.giftIdea}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SecretSantaScreenPreview() {
    SecretSantaSenderTheme {
        SecretSantaScreen(viewModel = SecretSantaViewModel())
    }
}
