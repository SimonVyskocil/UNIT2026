package com.example.unit2026.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class AddStudySpotDraft(
    val placeName: String = "",
    val comment: String = "",
)

@Composable
fun AddStudySpotScreen(
    modifier: Modifier = Modifier,
    onSubmit: (AddStudySpotDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(AddStudySpotDraft()) }
    val isSubmitEnabled = draft.placeName.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Add new spot",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "Suggest a new study place",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Uživatel zada nazev mista a pripadny komentar. Admin potom muze validni misto dohledat, doplnit souradnice a ulozit ho do databaze.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = draft.placeName,
                    onValueChange = {
                        draft = draft.copy(placeName = it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Place name") },
                    placeholder = { Text("e.g. Campus Library Atrium") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )

                OutlinedTextField(
                    value = draft.comment,
                    onValueChange = {
                        draft = draft.copy(comment = it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    label = { Text("Comment") },
                    placeholder = {
                        Text("Optional note for admin: where it is, why it is useful, when it is best, etc.")
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    ),
                )

                Text(
                    text = "Suggested places should be real and useful study spots. Coordinates and validation will be handled later by admin and database flow.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "What happens next?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "1. User submits the place name.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "2. Admin checks if the place is real and suitable.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "3. Valid places get coordinates and go to the database.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(0.dp))
            Button(
                onClick = { onSubmit(draft) },
                enabled = isSubmitEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Submit spot")
            }
        }
    }
}

@Preview
@Composable
private fun AddStudySpotScreenPreview() {
    MaterialTheme {
        AddStudySpotScreen(
            onSubmit = {},
        )
    }
}
