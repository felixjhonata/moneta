package com.felixj.moneta.activity.shared.model

sealed interface AddActivityPageDialog {
    data object None : AddActivityPageDialog
    data object DatePickerDialog : AddActivityPageDialog
    data object TimePickerDialog : AddActivityPageDialog
}