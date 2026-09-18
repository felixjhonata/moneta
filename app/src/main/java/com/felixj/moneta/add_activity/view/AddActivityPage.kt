package com.felixj.moneta.add_activity.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.R
import com.felixj.moneta.add_activity.model.AddActivityCategoryUiModel
import com.felixj.moneta.add_activity.model.AddActivityPageUiEvent
import com.felixj.moneta.add_activity.model.AddActivityPageUiState
import com.felixj.moneta.add_activity.model.AddActivityPageUserEvent
import com.felixj.moneta.add_activity.viewmodel.AddActivityPageViewModel
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.room.entity.ActivityType
import com.felixj.moneta.shared.util.goBack
import com.felixj.moneta.shared.util.rememberCurrencyAmountInputVisualTransformation
import com.felixj.moneta.shared.util.rememberDateInputVisualTransformation
import com.felixj.moneta.shared.util.rememberTimeInputVisualTransformation
import com.felixj.moneta.ui.theme.MonetaTheme

@Composable
fun AddActivityPage(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    viewModel: AddActivityPageViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onUserEvent(AddActivityPageUserEvent.LoadData)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiEvent ->
            when (uiEvent) {
                AddActivityPageUiEvent.NavigateBack -> backStack.goBack()
            }
        }
    }

    AddActivityPageContent(uiState, viewModel::onUserEvent, modifier)
}

@Composable
private fun AddActivityPageContent(
    uiState: AddActivityPageUiState,
    onUserEvent: (AddActivityPageUserEvent) -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(modifier) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton({ onUserEvent(AddActivityPageUserEvent.NavigateBack) }) {
                        Icon(
                            painterResource(R.drawable.baseline_arrow_back_24),
                            "back"
                        )
                    }

                    Text(
                        "Add Activity",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Amount",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.amount.isEmpty()) {
                            Text(
                                UiText.CurrencyAmount(0).asString(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        BasicTextField(
                            uiState.amount,
                            { rawValue ->
                                onUserEvent(AddActivityPageUserEvent.UpdateAmount(rawValue.filter { it.isDigit() }))
                            },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = rememberCurrencyAmountInputVisualTransformation()
                        )
                    }
                }
            }

            item {
                Box(
                    Modifier
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceDim)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        ActivityType.entries.forEach { activityType ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                onClick = { onUserEvent(AddActivityPageUserEvent.SelectActivityType(activityType)) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (activityType == uiState.activityType) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                            ) {
                                Text(
                                    activityType.name,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Category",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Column(
                    Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.categories.chunked(5).forEach { categories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { category ->
                                CategoryCard(
                                    category,
                                    category.id == uiState.selectedCategoryId,
                                    { onUserEvent(AddActivityPageUserEvent.SelectCategory(category.id)) },
                                    Modifier.weight(1f)
                                )
                            }

                            repeat(5 - categories.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = uiState.date,
                        onValueChange = { rawValue ->
                            onUserEvent(AddActivityPageUserEvent.UpdateDate(rawValue.filter { it.isDigit() }.take(8)))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Date") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = rememberDateInputVisualTransformation()
                    )

                    Card(
                        modifier = Modifier.size(58.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_calendar_month_24),
                            "calendar_month",
                            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxHeight()
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = uiState.time,
                        onValueChange = { rawValue ->
                            onUserEvent(AddActivityPageUserEvent.UpdateTime(rawValue.filter { it.isDigit() }.take(4)))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Time") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = rememberTimeInputVisualTransformation()
                    )

                    Card(
                        modifier = Modifier.size(58.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_access_time_24),
                            "access_time",
                            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxHeight()
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    uiState.notes,
                    { onUserEvent(AddActivityPageUserEvent.UpdateNotes(it)) },
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    label = { Text("Notes") },
                    minLines = 4
                )
            }

            item {
                Button(
                    {},
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text("Add Activity")
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: AddActivityCategoryUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            colors = CardDefaults.cardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        ) {
            Icon(
                painterResource(category.icon),
                null,
                modifier = Modifier.fillMaxHeight().align(Alignment.CenterHorizontally)
            )
        }

        Text(
            category.label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun AddActivityPagePreview() {
    MonetaTheme { AddActivityPageContent(AddActivityPageUiState(), {}) }
}

@Preview(
    name = "Dark mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun AddActivityPagePreviewDarkMode() {
    MonetaTheme { AddActivityPageContent(AddActivityPageUiState(), {}) }
}
