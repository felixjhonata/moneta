package com.felixj.moneta.shared.view

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.felixj.moneta.R
import com.felixj.moneta.ui.theme.MonetaTheme

enum class BottomNavigationBarDestination {
    Dashboard, History, Settings
}

@Composable
fun BottomNavigationBar(
    currentPage: BottomNavigationBarDestination,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier) {
        NavigationBarItem(
            selected = currentPage == BottomNavigationBarDestination.Dashboard,
            onClick = {},
            icon = {
                Icon(
                    painterResource(R.drawable.baseline_home_filled_24),
                    "home"
                )
            },
            label = { Text(stringResource(R.string.dashboard)) }
        )

        NavigationBarItem(
            selected = currentPage == BottomNavigationBarDestination.History,
            onClick = {},
            icon = {
                Icon(
                    painterResource(R.drawable.baseline_history_24),
                    "history"
                )
            },
            label = { Text(stringResource(R.string.history)) }
        )

        NavigationBarItem(
            selected = currentPage == BottomNavigationBarDestination.Settings,
            onClick = {},
            icon = {
                Icon(
                    painterResource(R.drawable.baseline_settings_24),
                    "settings"
                )
            },
            label = { Text(stringResource(R.string.settings)) }
        )
    }
}

@Preview
@Composable
private fun BottomNavigationBarPreview() {
    MonetaTheme {
        BottomNavigationBar(BottomNavigationBarDestination.Dashboard)
    }
}