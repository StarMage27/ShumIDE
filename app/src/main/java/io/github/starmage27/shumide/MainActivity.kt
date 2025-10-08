package io.github.starmage27.shumide

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.starmage27.shumide.routes.HomeRoute
import io.github.starmage27.shumide.ui.HomeScreen
import io.github.starmage27.shumide.ui.theme.ShumIDETheme
import uniffi.treesitterbridge.TsBridge

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: HomeViewModel
    private val tsBridge = TsBridge()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShumIDETheme {
                viewModel = createHomeViewModel(tsBridge, application = this.application)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    private fun MainScreen() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = HomeRoute,
        ) {
            composable<HomeRoute> {
                HomeScreen(navController = navController, viewModel = viewModel)
            }
        }
    }

    @Composable
    fun createHomeViewModel(tsBridge: TsBridge, application: Application): HomeViewModel {
        val owner = LocalViewModelStoreOwner.current
        viewModel = viewModel(
            viewModelStoreOwner = owner!!,
            key = "MainViewModel",
            factory = MainViewModelFactory(tsBridge, application),
        )
        return viewModel
    }

    @Suppress("UNCHECKED_CAST")
    class MainViewModelFactory(
        private val tsBridge: TsBridge,
        private val application: Application
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(tsBridge = tsBridge, application = application) as T
        }
    }
}
