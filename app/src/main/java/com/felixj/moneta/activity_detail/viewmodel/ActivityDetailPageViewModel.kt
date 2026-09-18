package com.felixj.moneta.activity_detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.activity_detail.model.ActivityDetailPageUiEvent
import com.felixj.moneta.activity_detail.model.ActivityDetailPageUiState
import com.felixj.moneta.activity_detail.model.ActivityDetailPageUserEvent
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.model.UiText
import com.felixj.moneta.shared.repository.ActivityRepository
import com.felixj.moneta.shared.room.entity.CategoryType
import com.felixj.moneta.shared.util.DateUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ActivityDetailPageViewModel.Factory::class)
class ActivityDetailPageViewModel @AssistedInject constructor(
    private val activityRepository: ActivityRepository,
    @Assisted private val navKey: MonetaRoute.ActivityDetail
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityDetailPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ActivityDetailPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: ActivityDetailPageUserEvent) {
        when (userEvent) {
            ActivityDetailPageUserEvent.LoadData -> loadActivity()
            ActivityDetailPageUserEvent.NavigateBack -> viewModelScope.launch {
                _uiEvent.emit(ActivityDetailPageUiEvent.NavigateBack)
            }
            ActivityDetailPageUserEvent.EditClick -> Unit // TODO: edit activity
            ActivityDetailPageUserEvent.DeleteClick -> Unit // TODO: delete activity
        }
    }

    private fun loadActivity() {
        viewModelScope.launch {
            val item = activityRepository.getActivity(navKey.activityId) ?: return@launch
            _uiState.update {
                it.copy(
                    icon = item.categoryIcon,
                    name = item.activity.name,
                    categoryType = item.categoryType,
                    amount = UiText.CurrencyAmount(
                        amount = item.activity.amount,
                        prefix = if (item.categoryType == CategoryType.EXPENSE) "- " else "+ "
                    ),
                    date = DateUtil.formatForDisplay(item.activity.date),
                    time = DateUtil.formatTimeForDisplay(item.activity.date),
                    note = item.activity.notes
                )
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: MonetaRoute.ActivityDetail): ActivityDetailPageViewModel
    }

    companion object {
        fun dummyUiState() = ActivityDetailPageUiState(
            icon = R.drawable.baseline_lightbulb_24,
            name = "Electricity Bills",
            categoryType = CategoryType.EXPENSE,
            amount = UiText.CurrencyAmount(1200000, "- "),
            date = "4 September 2026",
            time = "14:00",
            note = "Makan di luar dengan keluarga"
        )
    }
}