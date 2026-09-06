package com.felixj.moneta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.felixj.moneta.dashboard.view.DashboardPage
import com.felixj.moneta.history.view.HistoryPage
import com.felixj.moneta.settings.view.SettingsPage
import com.felixj.moneta.shared.model.MonetaRoute
import com.felixj.moneta.ui.theme.MonetaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack()

            MonetaTheme {
                NavDisplay(
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<MonetaRoute.Dashboard> {
                            DashboardPage()
                        }

                        entry<MonetaRoute.History> {
                            HistoryPage()
                        }

                        entry<MonetaRoute.Settings> {
                            SettingsPage()
                        }
                    }
                )
            }
        }
    }
}