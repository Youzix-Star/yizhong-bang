package com.yizhong.bang

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yizhong.bang.ui.AuroraBackground
import com.yizhong.bang.ui.GateScreen
import com.yizhong.bang.ui.MainScreen
import com.yizhong.bang.ui.MiuixHost
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.yizhong.bang.ui.YzTheme
import com.yizhong.bang.ui.pageBackdropModifier
import top.yukonga.miuix.kmp.material.isRenderEffectSupported

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { MainViewModel(application) }
                },
            )
            val state by viewModel.state.collectAsStateWithLifecycle()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            YzTheme(
                theme = settings.theme,
                dynamicColor = settings.dynamicColor,
                amoled = settings.amoled,
            ) {
                val dark = YzTheme.isDark
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }

                val os4 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    isRenderEffectSupported()

                MiuixHost(
                    useOs4 = os4,
                    useBlur = os4,
                    modifier = Modifier.fillMaxSize(),
                ) { scope ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(scope.pageBackdropModifier),
                    ) {
                        if (settings.whiteBackground) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MiuixTheme.colorScheme.background),
                            )
                        } else {
                            AuroraBackground()
                        }
                        when (state.phase) {
                            Phase.Gate -> GateScreen(state, viewModel)
                            Phase.Main -> MainScreen(state, viewModel, scope)
                        }
                    }
                }
            }
        }
    }
}
