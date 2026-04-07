package com.example.unit2026.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class VisitFeedbackDraft(
    val selectedPlaceId: String? = null,
    val noise: Int = 3,
    val comfort: Int = 3,
    val snacks: Int = 3,
    val hasPowerOutlet: Boolean = false,
)

@Composable
fun VisitFeedbackPrompt(
    places: List<Place>,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirmVisit: (VisitFeedbackDraft) -> Unit,
    ) {
    val safePlaces = places.take(6)
    var draft by remember(safePlaces) {
        mutableStateOf(
            VisitFeedbackDraft(
                selectedPlaceId = safePlaces.firstOrNull()?.id,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f))
            .padding(horizontal = 20.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "Rate this spot",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = "Quick post-visit rating with simple 1 to 5 scoring.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Cafe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        safePlaces.forEach { place ->
                            PlaceChoiceRow(
                                placeName = place.name,
                                selected = draft.selectedPlaceId == place.id,
                                onClick = {
                                    draft = draft.copy(selectedPlaceId = place.id)
                                }
                            )
                        }
                    }
                }                

                RatingSlider(
                    label = "Noise",
                    value = draft.noise,
                    onValueChange = { draft = draft.copy(noise = it) },
                    lowLabel = "1 Quiet",
                    highLabel = "5 Loud",
                )
                RatingSlider(
                    label = "Comfort",
                    value = draft.comfort,
                    onValueChange = { draft = draft.copy(comfort = it) },
                    lowLabel = "1 Basic",
                    highLabel = "5 Cozy",
                )
                RatingSlider(
                    label = "Snacks",
                    value = draft.snacks,
                    onValueChange = { draft = draft.copy(snacks = it) },
                    lowLabel = "1 None",
                    highLabel = "5 Great",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Power outlet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (draft.hasPowerOutlet) "Available" else "Not available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = draft.hasPowerOutlet,
                        onCheckedChange = {
                            draft = draft.copy(hasPowerOutlet = it)
                        },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier,
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirmVisit(draft) },
                        modifier = Modifier,
                        enabled = draft.selectedPlaceId != null,
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceChoiceRow(
    placeName: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = placeName,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = CircleShape,
                )
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape,
                )
                .padding(5.dp),
        )
    }
}

@Composable
private fun RatingSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    lowLabel: String,
    highLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(1, 5)) },
            valueRange = 1f..5f,
            steps = 3,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(lowLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(highLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun VisitFeedbackPromptPreview() {
    MaterialTheme {
        VisitFeedbackPrompt(
            places = listOf(
                Place(
                    id = "1",
                    name = "Kolej Hub",
                    description = "",
                    rating = 4.4,
                    noise = 3.0,
                    comfort = 4.0,
                    refreshments = 3.0,
                    openingHours = "24/7",
                    images = emptyList(),
                    certified = true,
                    powerOutlet = true,
                ),
            ),
            onDismiss = {},
            onConfirmVisit = {},
        )
    }
}
