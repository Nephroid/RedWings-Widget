package com.redwings.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.redwings.widget.ui.GameDashboard
import com.redwings.widget.ui.GameViewModel
import com.redwings.widget.ui.theme.RedWingsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this)[GameViewModel::class.java]
        setContent {
            RedWingsTheme {
                GameDashboard(viewModel = viewModel)
            }
        }
    }
}
