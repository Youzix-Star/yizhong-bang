package top.youzix.bang

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import top.youzix.bang.ui.AgreementScreen
import top.youzix.bang.ui.AppNavHost
import top.youzix.bang.ui.GateScreen
import top.youzix.bang.ui.MiuixHost
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.youzix.bang.ui.YzTheme
import top.youzix.bang.ui.pageBackdropModifier
import top.yukonga.miuix.kmp.material.isRenderEffectSupported

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val agreementPrefs = getSharedPreferences("yz_agreement", MODE_PRIVATE)

        setContent {
            var agreed by remember {
                mutableStateOf(agreementPrefs.getBoolean("accepted_v1", false))
            }
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
                    if (!agreed) {
                        AgreementScreen(
                            onAccept = {
                                agreementPrefs.edit().putBoolean("accepted_v1", true).apply()
                                agreed = true
                            },
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(scope.pageBackdropModifier),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MiuixTheme.colorScheme.background),
                            )
                            when (state.phase) {
                                Phase.Gate -> GateScreen(state, viewModel)
                                Phase.Main -> AppNavHost(state, viewModel, scope)
                            }
                        }
                    }
                }
            }
        }
    }
}
