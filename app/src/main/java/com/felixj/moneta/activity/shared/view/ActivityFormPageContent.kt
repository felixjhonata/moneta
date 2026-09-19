package com.felixj.moneta.activity.shared.view

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felixj.moneta.R
import com.felixj.moneta.activity.shared.model.AddActivityCategoryUiModel
import com.felixj.moneta.activity.shared.model.AddActivityPageDialog
import com.felixj.moneta.activity.shared.model.AddActivityPageUiState
import com.felixj.moneta.activity.shared.model.AddActivityPageUserEvent
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.util.rememberCurrencyAmountInputVisualTransformation
import com.felixj.moneta.shared.util.rememberDateInputVisualTransformation
import com.felixj.moneta.shared.util.rememberTimeInputVisualTransformation
import com.felixj.moneta.shared.view.CategoryTypeSelector
import com.felixj.moneta.shared.view.PageHeader
import com.felixj.moneta.ui.theme.MonetaTheme
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material3.DatePickerDialog as MaterialDatePickerDialog
import androidx.compose.material3.TimePickerDialog as MaterialTimePickerDialog

@Composable
fun ActivityFormPageContent(
    headerRes: Int,
    buttonRes: Int,
    uiState: AddActivityPageUiState,
    onUserEvent: (AddActivityPageUserEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            item {
                PageHeader(
                    stringResource(headerRes),
                    { onUserEvent(AddActivityPageUserEvent.NavigateBack) }
                )
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
                        stringResource(R.string.amount),
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
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = rememberCurrencyAmountInputVisualTransformation()
                        )
                    }

                    if (uiState.amountError) {
                        Text(
                            stringResource(R.string.amount_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                CategoryTypeSelector(
                    selected = uiState.categoryType,
                    onSelect = { onUserEvent(AddActivityPageUserEvent.SelectCategoryType(it)) },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            item {
                Text(
                    stringResource(R.string.category),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            item {
                Column(
                    Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.categories.filter { it.type == uiState.categoryType }.chunked(5)
                        .forEach { categories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { category ->
                                    CategoryCard(
                                        category,
                                        category.id == uiState.selectedCategoryId,
                                        {
                                            onUserEvent(
                                                AddActivityPageUserEvent.SelectCategory(
                                                    category.id
                                                )
                                            )
                                        },
                                        Modifier.weight(1f)
                                    )
                                }

                                repeat(5 - categories.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                    if (uiState.categoryError) {
                        Text(
                            stringResource(R.string.category_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                DateTimeInputField(
                    value = uiState.date,
                    onValueChange = { rawValue ->
                        onUserEvent(AddActivityPageUserEvent.UpdateDate(rawValue.filter { it.isDigit() }
                            .take(8)))
                    },
                    labelRes = R.string.date,
                    visualTransformation = rememberDateInputVisualTransformation(),
                    iconRes = R.drawable.baseline_calendar_month_24,
                    onIconClick = { onUserEvent(AddActivityPageUserEvent.ShowDatePicker) },
                    error = uiState.dateError,
                    errorTextRes = R.string.date_required
                )
            }

            item {
                DateTimeInputField(
                    value = uiState.time,
                    onValueChange = { rawValue ->
                        onUserEvent(AddActivityPageUserEvent.UpdateTime(rawValue.filter { it.isDigit() }
                            .take(4)))
                    },
                    labelRes = R.string.time,
                    visualTransformation = rememberTimeInputVisualTransformation(),
                    iconRes = R.drawable.baseline_access_time_24,
                    onIconClick = { onUserEvent(AddActivityPageUserEvent.ShowTimePicker) },
                    error = uiState.timeError,
                    errorTextRes = R.string.time_required
                )
            }

            item {
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { onUserEvent(AddActivityPageUserEvent.UpdateNotes(it)) },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.note_optional)) },
                    minLines = 4,
                    maxLines = 4
                )
            }

            item {
                Button(
                    { onUserEvent(AddActivityPageUserEvent.Submit) },
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Text(stringResource(buttonRes))
                }
            }
        }

        when (uiState.dialog) {
            AddActivityPageDialog.None -> Unit
            AddActivityPageDialog.DatePickerDialog -> DatePickerDialog(
                initialDate = uiState.date,
                onConfirm = { onUserEvent(AddActivityPageUserEvent.UpdateDate(it)) },
                onDismiss = { onUserEvent(AddActivityPageUserEvent.DismissDialog) }
            )
            AddActivityPageDialog.TimePickerDialog -> TimePickerDialog(
                initialTime = uiState.time,
                onConfirm = { onUserEvent(AddActivityPageUserEvent.UpdateTime(it)) },
                onDismiss = { onUserEvent(AddActivityPageUserEvent.DismissDialog) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toUtcMillisOrNull()
            ?: System.currentTimeMillis()
    )
    MaterialDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onConfirm(it.toDateString()) }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val now = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.take(2).toIntOrNull() ?: now.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialTime.drop(2).take(2).toIntOrNull() ?: now.get(Calendar.MINUTE),
        is24Hour = true
    )
    MaterialTimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        String.format(
                            Locale.US,
                            "%02d%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    )
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.time)) }
    ) {
        TimePicker(state = timePickerState)
    }
}

@Composable
private fun DateTimeInputField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    visualTransformation: VisualTransformation,
    iconRes: Int,
    onIconClick: () -> Unit,
    errorTextRes: Int,
    modifier: Modifier = Modifier,
    error: Boolean = false
) {
    Spacer(Modifier.height(12.dp))

    Row(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(labelRes)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = visualTransformation,
            isError = error,
            supportingText = if (error) {
                { Text(stringResource(errorTextRes)) }
            } else {
                null
            }
        )

        Card(
            onClick = onIconClick,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(56.dp)
        ) {
            Icon(
                painterResource(iconRes),
                null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxHeight()
            )
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
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            colors = CardDefaults.cardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        ) {
            Icon(
                painterResource(category.icon),
                null,
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally)
            )
        }

        Text(
            category.label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

internal fun dummyUiState() = AddActivityPageUiState(
    amount = "1200000",
    categoryType = CategoryType.EXPENSE,
    categories = listOf(
        AddActivityCategoryUiModel(1, R.drawable.baseline_fastfood_24, "Food & Drinks", CategoryType.EXPENSE),
        AddActivityCategoryUiModel(2, R.drawable.baseline_directions_bus_24, "Transport", CategoryType.EXPENSE),
        AddActivityCategoryUiModel(3, R.drawable.baseline_lightbulb_24, "Utilities", CategoryType.EXPENSE),
        AddActivityCategoryUiModel(4, R.drawable.baseline_access_time_24, "Entertainment", CategoryType.EXPENSE),
        AddActivityCategoryUiModel(5, R.drawable.baseline_history_24, "Shopping", CategoryType.EXPENSE),
        AddActivityCategoryUiModel(6, R.drawable.baseline_account_balance_wallet_24, "Salary", CategoryType.INCOME),
        AddActivityCategoryUiModel(7, R.drawable.baseline_directions_bus_24, "Deposit", CategoryType.INCOME),
        AddActivityCategoryUiModel(8, R.drawable.baseline_lightbulb_24, "Investment", CategoryType.INCOME),
        AddActivityCategoryUiModel(9, R.drawable.baseline_fastfood_24, "Gift", CategoryType.INCOME),
        AddActivityCategoryUiModel(10, R.drawable.baseline_access_time_24, "Refund", CategoryType.INCOME)
    ),
    selectedCategoryId = 3,
    date = "18092026",
    time = "1400",
    note = "Makan di luar dengan keluarga"
)

@Preview(
    name = "Regular",
    showSystemUi = true
)
@Composable
private fun ActivityFormPageContentPreview() {
    MonetaTheme {
        ActivityFormPageContent(
            headerRes = R.string.add_activity,
            buttonRes = R.string.add_activity,
            uiState = dummyUiState(),
            onUserEvent = {}
        )
    }
}

@Preview(
    name = "Dark mode",
    uiMode = UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
private fun ActivityFormPageContentPreviewDarkMode() {
    MonetaTheme {
        ActivityFormPageContent(
            headerRes = R.string.add_activity,
            buttonRes = R.string.add_activity,
            uiState = dummyUiState(),
            onUserEvent = {}
        )
    }
}

private fun String.toUtcMillisOrNull(): Long? {
    if (length != 8) return null
    return try {
        val calendar = Calendar.getInstance().apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
            set(Calendar.YEAR, substring(4, 8).toInt())
            set(Calendar.MONTH, substring(2, 4).toInt() - 1)
            set(Calendar.DAY_OF_MONTH, substring(0, 2).toInt())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        calendar.timeInMillis
    } catch (_: Exception) {
        null
    }
}

private fun Long.toDateString(): String {
    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("UTC")
        timeInMillis = this@toDateString
    }
    return String.format(
        Locale.US,
        "%02d%02d%04d",
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.YEAR)
    )
}