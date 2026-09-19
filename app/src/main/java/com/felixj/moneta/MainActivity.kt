package com.felixj.moneta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.felixj.moneta.activity.activity_detail.view.ActivityDetailPage
import com.felixj.moneta.activity.activity_detail.viewmodel.ActivityDetailPageViewModel
import com.felixj.moneta.activity.add_activity.view.AddActivityPage
import com.felixj.moneta.activity.edit_activity.view.EditActivityPage
import com.felixj.moneta.activity.edit_activity.viewmodel.EditActivityPageViewModel
import com.felixj.moneta.categories.view.CategoriesPage
import com.felixj.moneta.categories.add_category.view.AddCategoryPage
import com.felixj.moneta.dashboard.view.DashboardPage
import com.felixj.moneta.history.view.HistoryPage
import com.felixj.moneta.settings.model.LocalAppCurrency
import com.felixj.moneta.settings.view.SettingsPage
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import com.felixj.moneta.ui.theme.MonetaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(MonetaRoute.Dashboard)
            val darkMode by userPreferencesRepository.darkModeFlow.collectAsStateWithLifecycle(
                initialValue = userPreferencesRepository.getDarkModePreference()
            )
            val currency by userPreferencesRepository.currencyFlow.collectAsStateWithLifecycle(
                initialValue = userPreferencesRepository.getCurrency()
            )

            CompositionLocalProvider(LocalAppCurrency provides currency) {
                MonetaTheme(darkMode ?: isSystemInDarkTheme()) {
                    NavDisplay(
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = entryProvider {
                            entry<MonetaRoute.Dashboard> {
                                DashboardPage(backStack)
                            }

                            entry<MonetaRoute.History> {
                                HistoryPage(backStack)
                            }

                            entry<MonetaRoute.Settings> {
                                SettingsPage(backStack)
                            }

                            entry<MonetaRoute.AddActivity> {
                                AddActivityPage(backStack)
                            }

                            entry<MonetaRoute.Categories> {
                                CategoriesPage(backStack)
                            }

                            entry<MonetaRoute.AddCategory> {
                                AddCategoryPage(backStack)
                            }

                            entry<MonetaRoute.ActivityDetail> { key ->
                                val viewModel = hiltViewModel<
                                        ActivityDetailPageViewModel,
                                        ActivityDetailPageViewModel.Factory
                                        >(
                                    creationCallback = { factory -> factory.create(key) }
                                )
                                ActivityDetailPage(backStack, viewModel)
                            }

                            entry<MonetaRoute.EditActivity> { key ->
                                val viewModel = hiltViewModel<
                                        EditActivityPageViewModel,
                                        EditActivityPageViewModel.Factory
                                        >(
                                    creationCallback = { factory -> factory.create(key) }
                                )
                                EditActivityPage(backStack, viewModel)
                            }
                        }
                    )
                }
            }
        }
    }
}
