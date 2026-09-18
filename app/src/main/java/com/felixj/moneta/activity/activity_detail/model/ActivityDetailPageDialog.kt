package com.felixj.moneta.activity.activity_detail.model

sealed interface ActivityDetailPageDialog {
    data object None : ActivityDetailPageDialog
    data object DeleteConfirmationDialog : ActivityDetailPageDialog
}