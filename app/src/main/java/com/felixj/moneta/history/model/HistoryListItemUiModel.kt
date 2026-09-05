package com.felixj.moneta.history.model

import com.felixj.moneta.shared.model.ActivityItemUiModel

sealed interface HistoryListItemUiModel {
    data class Date(val date: String): HistoryListItemUiModel
    data class ActivityItem(val itemUiModel: ActivityItemUiModel): HistoryListItemUiModel
}