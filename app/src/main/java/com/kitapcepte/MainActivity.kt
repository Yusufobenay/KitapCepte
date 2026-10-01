package com.kitapcepte

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitapcepte.core.designsystem.theme.KitapCepteTheme
import com.kitapcepte.core.navigation.KitapCepteNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            KitapCepteTheme {
                if (!uiState.isLoading) {
                    KitapCepteNavHost(
                        startDestination = uiState.startDestination,
                        session = uiState.session,
                        cartItemCount = uiState.cartItemCount
                    )
                }
            }
        }
    }
}
