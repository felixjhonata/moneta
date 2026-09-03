package com.felixj.moneta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.felixj.moneta.dashboard.view.DashboardPage
import com.felixj.moneta.ui.theme.MonetaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonetaTheme {
                DashboardPage(Modifier.fillMaxSize())
            }
        }
    }
}