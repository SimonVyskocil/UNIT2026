package com.example.unit2026.presentation

import androidx.compose.foundation.background
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

data class VisitFeedbackDraft(
    val noise: Float = 0.5f,
    val comfort: Float = 0.5f,
    val snacks: Float = 0.5f,
    val hasPowerOutlet: Boolean = false,
)

private enum class VisitFeedbackStep {
    Confirmation,
    Rating,
}

@Composable
fun VisitFeedbackPrompt(
    placeName: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirmVisit: (VisitFeedbackDraft) -> Unit,
) {
    var step by remember { mutableStateOf(VisitFeedbackStep.Confirmation) }
    var draft by remember { mutableStateOf(VisitFeedbackDraft()) }

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
                    text = "Visit check",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                )

                when (step) {
                    VisitFeedbackStep.Confirmation -> {
                        Text(
                            text = "Navštívil jsi opravdu místo $placeName?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Popup je navržený pro zobrazení přibližně 3 hodiny po startu navigace, aby byla vyšší šance na reálnou návštěvu.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier,
                            ) {
                                Text("Ne ještě")
                            }
                            Button(
                                onClick = { step = VisitFeedbackStep.Rating },
                                modifier = Modifier,
                            ) {
                                Text("Ano, byl")
                            }
                        }
                    }

                    VisitFeedbackStep.Rating -> {
                        Text(
                            text = "Rychlé hodnocení",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Krátké feedback summary po návštěvě místa.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        RatingSlider(
                            label = "Noise",
                            value = draft.noise,
                            onValueChange = { draft = draft.copy(noise = it) },
                            lowLabel = "Quiet",
                            highLabel = "Loud",
                        )
                        RatingSlider(
                            label = "Comfort",
                            value = draft.comfort,
                            onValueChange = { draft = draft.copy(comfort = it) },
                            lowLabel = "Basic",
                            highLabel = "Cozy",
                        )
                        RatingSlider(
                            label = "Snacks",
                            value = draft.snacks,
                            onValueChange = { draft = draft.copy(snacks = it) },
                            lowLabel = "None",
                            highLabel = "Great",
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
                                onClick = { step = VisitFeedbackStep.Confirmation },
                                modifier = Modifier,
                            ) {
                                Text("Zpět")
                            }
                            Button(
                                onClick = { onConfirmVisit(draft) },
                                modifier = Modifier,
                            ) {
                                Text("Odeslat")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
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
                text = "${(value * 100).toInt()}%",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
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
            placeName = "Kolej Hub",
            onDismiss = {},
            onConfirmVisit = {},
        )
    }
}
