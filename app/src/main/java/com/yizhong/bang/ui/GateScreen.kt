package com.yizhong.bang.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.MainViewModel
import com.yizhong.bang.UiState
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun GateScreen(state: UiState, viewModel: MainViewModel) {
    var scoreUrl by remember { mutableStateOf("") }
    var answerUrl by remember { mutableStateOf("") }
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
                    text = "第一条是成绩查询链接；第二条是试卷分析链接（选填，用来看答题卡）。" +
                        "token 只留在内存里，不写日志、不落盘。",
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 18.dp),
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "成绩链接",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                LinkField(
                    value = scoreUrl,
                    onValueChange = { scoreUrl = it },
                    placeholder = "http://…/GAS/Default.aspx?UserID=…&token=…",
                    onSubmit = { if (!state.busy) viewModel.submitLink(scoreUrl, answerUrl) },
                )

                Text(
                    text = "答题卡链接（选填）",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                )
                LinkField(
                    value = answerUrl,
                    onValueChange = { answerUrl = it },
                    placeholder = "http://…/TPTQ/Default.aspx?UserID=…&SysCode=0402&token=…",
                    onSubmit = { if (!state.busy) viewModel.submitLink(scoreUrl, answerUrl) },
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.submitLink(scoreUrl, answerUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.busy,
                ) {
                    Text(
                        text = if (state.busy) "连接中…" else "进入",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (state.busy) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                    )
                }

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
                    text = "只读、只给自己用。别公开部署，也别把带 token 的链接发给别人。",
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
    placeholder: String = "http://…/SSQ/Default.aspx?UserID=…&token=…",
) {
    val colors = MiuixTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceContainerHigh)
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariantSummary,
                maxLines = 1,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 13.5.sp,
                color = colors.onSurface,
            ),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { onSubmit() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
