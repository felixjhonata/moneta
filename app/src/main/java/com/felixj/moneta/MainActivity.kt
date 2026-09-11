package com.felixj.moneta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.felixj.moneta.dashboard.view.DashboardPage
import com.felixj.moneta.history.view.HistoryPage
import com.felixj.moneta.settings.view.SettingsPage
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.shared.repository.UserPreferencesRepository
import com.felixj.moneta.ui.theme.MonetaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(MonetaRoute.Dashboard)
            val darkMode by userPreferencesRepository.darkModeFlow.collectAsStateWithLifecycle(false)

            MonetaTheme(darkMode) {
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
                    }
                )
            }
        }
    }
}