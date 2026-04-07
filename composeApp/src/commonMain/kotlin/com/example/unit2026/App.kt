package com.example.unit2026

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.unit2026.database.SpotRepository
import com.example.unit2026.database.SupabaseClientProvider
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()
    val spotRepository = remember { SpotRepository(SupabaseClientProvider.client) }
    var spotsOutput by remember { mutableStateOf("Stiskni tlacitko pro nacteni spots.") }
    var isLoading by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val spots = spotRepository.getSpots()
                        spotsOutput = if (spots.isEmpty()) {
                            "getSpots vratilo prazdny seznam."
                        } else {
                            spots.joinToString(separator = "\n\n") { it.toString() }
                        }
                        isLoading = false
                    }
                }
            ) {
                Text(if (isLoading) "Nacitam..." else "Vypsat getSpots()")
            }

            Text(
                text = spotsOutput,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
}
