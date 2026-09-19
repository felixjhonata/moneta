package com.felixj.moneta.categories.root.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felixj.moneta.R
import com.felixj.moneta.categories.root.model.CategoriesPageUiEvent
import com.felixj.moneta.categories.root.model.CategoriesPageUiState
import com.felixj.moneta.categories.root.model.CategoriesPageUserEvent
import com.felixj.moneta.settings.model.CategoryUiModel
import com.felixj.moneta.shared.repository.CategoryRepository
import com.felixj.moneta.shared.room.entity.CategoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesPageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoriesPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CategoriesPageUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onUserEvent(userEvent: CategoriesPageUserEvent) {
        when (userEvent) {
            CategoriesPageUserEvent.LoadData -> {
                viewModelScope.launch {
                    val categories = categoryRepository.getCategories().map { category ->
                        CategoryUiModel(
                            icon = category.icon,
                            label = category.name,
                            isExpense = category.type == CategoryType.EXPENSE,
                            id = category.id
                        )
                    }
                    _uiState.update { it.copy(categories = categories) }
                }
            }
            CategoriesPageUserEvent.NavigateBack -> {
                viewModelScope.launch {
                    _uiEvent.emit(CategoriesPageUiEvent.NavigateBack)
                }
            }
            CategoriesPageUserEvent.NavigateToAddCategory -> {
                viewModelScope.launch {
                    _uiEvent.emit(CategoriesPageUiEvent.NavigateToAddCategory)
                }
            }
            is CategoriesPageUserEvent.NavigateToCategoryDetail -> {
                viewModelScope.launch {
                    _uiEvent.emit(CategoriesPageUiEvent.NavigateToCategoryDetail(userEvent.categoryId))
                }
            }
        }
    }

    companion object {
        fun dummyUiState() = CategoriesPageUiState(
            categories = listOf(
                CategoryUiModel(R.drawable.baseline_lightbulb_24, "Utilities", true),
                CategoryUiModel(R.drawable.baseline_fastfood_24, "Food", true),
                CategoryUiModel(R.drawable.baseline_directions_bus_24, "Transport", true),
                CategoryUiModel(R.drawable.baseline_account_balance_wallet_24, "Salary", false),
                CategoryUiModel(R.drawable.baseline_home_filled_24, "Home", true)
            )
        )
    }
}