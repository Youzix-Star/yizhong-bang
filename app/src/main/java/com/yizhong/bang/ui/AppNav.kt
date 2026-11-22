package com.yizhong.bang.ui

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yizhong.bang.MainViewModel
import com.yizhong.bang.UiState
import com.yizhong.bang.data.Student
import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.NavKey
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Serializable
sealed interface YzRoute : NavKey {
    @Serializable
    data object Main : YzRoute

    @Serializable
    data object About : YzRoute

    @Serializable
    data object License : YzRoute

    @Serializable
    data object Answer : YzRoute
}

@Composable
fun AppNavHost(state: UiState, viewModel: MainViewModel, scope: MiuixComponentScope) {
    val backStack = rememberNavBackStack<YzRoute>(YzRoute.Main)
    val activity = LocalContext.current as? Activity
    val colors = MiuixTheme.colorScheme
    val navCornerRadius = rememberDeviceCornerRadius(defaultRadius = 0.dp)
    val effects = remember(colors.surface, navCornerRadius) {
        NavDisplayEffects(
            enableCornerClip = true,
            cornerClipRadius = navCornerRadius,
            cornerClipMode = NavCornerClipMode.Leading,
            dimAmount = 0.5f,
            backdropColor = colors.surface,
            blockInputDuringTransition = false,
        )
    }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else {
                activity?.finish()
            }
        },
        transition = NavTransitions.MiuixDefault,
        effects = effects,
    ) {
        entry<YzRoute.Main> {
            MainScreen(
                state = state,
                viewModel = viewModel,
                scope = scope,
                isTop = backStack.lastOrNull() is YzRoute.Main,
                onOpenAbout = { backStack.add(YzRoute.About) },
                onOpenAnswerSheet = { student, index ->
                    viewModel.openAnswerSheet(student, index)
                    if (backStack.lastOrNull() !is YzRoute.Answer) {
                        backStack.add(YzRoute.Answer)
                    }
                },
            )
        }
        entry<YzRoute.About> {
            AboutScreen(
                onClose = { backStack.removeLastOrNull() },
                onOpenLicenses = { backStack.add(YzRoute.License) },
            )
        }
        entry<YzRoute.License> {
            LicenseScreen(onClose = { backStack.removeLastOrNull() })
        }
        entry<YzRoute.Answer> {
            DisposableEffect(Unit) {
                onDispose { viewModel.closeAnswerSheet() }
            }
            AnswerSheetScreen(
                state = viewModel.answer.collectAsStateWithLifecycle().value,
                subjects = state.sheet?.subjects.orEmpty(),
                onSelectSubject = viewModel::switchAnswerSubject,
                onClose = { backStack.removeLastOrNull() },
                onRetry = viewModel::retryAnswerSheet,
            )
        }
    }
}
