package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var crashMessage by mutableStateOf<String?>(null)

        // Catch any crash on launch
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            crashMessage = throwable.stackTraceToString()
        }

        setContent {
            if (crashMessage != null) {
                Text(
                    text = "CRASH LOG:\n\n$crashMessage",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                )
            } else {
                Text(
                    text = "Trading Journal App Running Successfully!",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
