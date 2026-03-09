package com.babytime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.babytime.app.ui.navigation.BabyTimeNavigation
import com.babytime.app.ui.theme.BabyTimeTheme
import com.babytime.app.viewmodel.BabyViewModel
import com.babytime.app.viewmodel.BabyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as BabyTimeApp
        setContent {
            BabyTimeTheme {
                val viewModel: BabyViewModel = viewModel(
                    factory = BabyViewModelFactory(app.repository)
                )
                BabyTimeNavigation(viewModel)
            }
        }
    }
}
