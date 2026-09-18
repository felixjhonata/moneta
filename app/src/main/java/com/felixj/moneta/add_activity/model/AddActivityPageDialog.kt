package com.felixj.moneta.add_activity.model

sealed interface AddActivityPageDialog {
    data object None : AddActivityPageDialog
    data object DatePickerDialog : AddActivityPageDialog
    data object TimePickerDialog : AddActivityPageDialog
}