package top.youzix.bang.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.youzix.bang.MainViewModel
import top.youzix.bang.UiState
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GateScreen(state: UiState, viewModel: MainViewModel) {
    var scoreUrl by remember { mutableStateOf("") }
    val colors = MiuixTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .padding(top = 32.dp),
        ) {
            Card(
                insideMargin = androidx.compose.foundation.layout.PaddingValues(22.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoMark(54.dp)
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(
                            text = "一中帮",
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "城固一中 · 成绩查询",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                Text(
                    text = "粘贴成绩分析链接，进入后自动带出答题卡。",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 18.dp),
                )

                Spacer(Modifier.height(16.dp))

                LinkField(
                    value = scoreUrl,
                    onValueChange = { scoreUrl = it },
                    onSubmit = { if (!state.busy) viewModel.submitLink(scoreUrl) },
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.submitLink(scoreUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.busy,
                ) {
                    if (state.busy) {
                        InfiniteProgressIndicator(
                            color = colors.onPrimary,
                            size = 18.dp,
                            strokeWidth = 2.dp,
                            orbitingDotSize = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "连接中…",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Text(
                            text = "进入",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(
                    text = "先看看示例",
                    onClick = { if (!state.busy) viewModel.loadDemo() },
                    modifier = Modifier.fillMaxWidth(),
                )

                val gateError = state.gateError
                if (!gateError.isNullOrBlank()) {
                    Text(
                        text = gateError,
                        fontSize = 12.5.sp,
                        color = colors.error,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(1.dp)
                        .background(colors.dividerLine),
                )

                Text(
                    text = "只读使用，别把带 token 的链接发给别人。",
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }

            Row(
                modifier = Modifier.padding(top = 16.dp, start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("设计参考", fontSize = 11.sp, color = colors.onSurfaceVariantSummary)
                Text(
                    "HyperOS 4 · Miuix · InstallerX Revived",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun LinkField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceContainerHigh)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = MiuixIcons.Link,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.onSurfaceVariantSummary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, top = 14.dp, bottom = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "粘贴成绩分析链接",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.5.sp, color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(onGo = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = MiuixIcons.Clear,
                    contentDescription = "清空",
                    modifier = Modifier.size(18.dp),
                    tint = colors.onSurfaceVariantSummary,
                )
            }
        }
    }
}
