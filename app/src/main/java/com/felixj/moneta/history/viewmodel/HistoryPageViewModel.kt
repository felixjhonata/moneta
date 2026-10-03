package com.felixj.moneta.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.felixj.moneta.R
import com.felixj.moneta.history.model.HistoryListItemUiModel
import com.felixj.moneta.history.model.HistoryPageUiEvent
import com.felixj.moneta.history.model.HistoryPageUserEvent
import com.felixj.moneta.shared.model.ActivityItemUiModel
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.room.entity.ActivityWithCategoryIcon
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.util.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryPageViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {
    val pagedHistory: Flow<PagingData<HistoryListItemUiModel>> =
        Pager(config = PagingConfig(pageSize = 20, enablePlaceholders = false)) {
            activityRepository.getActivitiesPaged()
        }.flow
            .map { pagingData -> pagingData.map { item -> toActivityUi(item) } }
            .map { pagingData ->
                pagingData.insertSeparators { before, after -> toHeaderOrNull(before, after) }
            }
            .cachedIn(viewModelScope)

    private val _uiEvent = MutableSharedFlow<HistoryPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: HistoryPageUserEvent) {
        when (userEvent) {
            is HistoryPageUserEvent.NavigateTo -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        HistoryPageUiEvent.NavigateTo(userEvent.destination)
                    )
                }
            }
            is HistoryPageUserEvent.ActivityItemClick -> {
                viewModelScope.launch {
                    _uiEvent.emit(
                        HistoryPageUiEvent.NavigateTo(MonetaRoute.ActivityDetail(userEvent.activityId))
                    )
                }
            }
        }
    }

    private fun toActivityUi(item: ActivityWithCategoryIcon): HistoryListItemUiModel =
        HistoryListItemUiModel.ActivityItem(
            ActivityItemUiModel(
                activityId = item.activity.id,
                icon = item.categoryIcon,
                activityLabel = UiText.DynamicString(item.activity.name),
                activityDate = UiText.DynamicString(DateUtil.formatForDisplay(item.activity.date)),
                amount = UiText.CurrencyAmount(
                    amount = item.activity.amount,
                    prefix = if (item.categoryType == CategoryType.EXPENSE) "- " else "+ "
                ),
                isExpense = item.categoryType == CategoryType.EXPENSE
            )
        )

    private fun toHeaderOrNull(
        before: HistoryListItemUiModel?,
        after: HistoryListItemUiModel?
    ): HistoryListItemUiModel? {
        if (after !is HistoryListItemUiModel.ActivityItem) return null
        val afterHeader = headerFor(after) ?: return null
        if (before == null) return HistoryListItemUiModel.Date(afterHeader)
        val beforeHeader = headerFor(before) ?: return HistoryListItemUiModel.Date(afterHeader)
        return if (beforeHeader != afterHeader) HistoryListItemUiModel.Date(afterHeader) else null
    }

    private fun headerFor(model: HistoryListItemUiModel): String? {
        return when (model) {
            is HistoryListItemUiModel.Date -> model.date
            is HistoryListItemUiModel.ActivityItem -> {
                val display = (model.itemUiModel.activityDate as? UiText.DynamicString)?.value
                    ?: return null
                DateUtil.formatRelativeDate(display).ifEmpty { null }
            }
        }
    }

    companion object {
        fun dummyItems(): List<HistoryListItemUiModel> = listOf(
            HistoryListItemUiModel.Date("Today"),
            HistoryListItemUiModel.ActivityItem(
                ActivityItemUiModel(
                    1,
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Electricity Bills"),
                    UiText.DynamicString("4 September 2026"),
                    UiText.CurrencyAmount(1200000, "- "),
                    true,
                )
            ),
            HistoryListItemUiModel.ActivityItem(
                ActivityItemUiModel(
                    2,
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Water Bills"),
                    UiText.DynamicString("4 September 2026"),
                    UiText.CurrencyAmount(800000, "- "),
                    true,
                )
            ),
            HistoryListItemUiModel.ActivityItem(
                ActivityItemUiModel(
                    3,
                    R.drawable.baseline_account_balance_wallet_24,
                    UiText.DynamicString("Salary"),
                    UiText.DynamicString("4 September 2026"),
                    UiText.CurrencyAmount(13000000, "+ "),
                    false,
                )
            ),
            HistoryListItemUiModel.Date("Yesterday"),
            HistoryListItemUiModel.ActivityItem(
                ActivityItemUiModel(
                    4,
                    R.drawable.baseline_lightbulb_24,
                    UiText.DynamicString("Phone Bills"),
                    UiText.DynamicString("3 September 2026"),
                    UiText.CurrencyAmount(100000, "- "),
                    true
                )
            )
        )
    }
}
