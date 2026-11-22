package top.youzix.bang.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val AgreementPhrase = "我已仔细阅读并同意该协议"

@Composable
fun AgreementScreen(onAccept: () -> Unit) {
    var input by remember { mutableStateOf("") }
    val colors = MiuixTheme.colorScheme
    val accepted = input.trim() == AgreementPhrase

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Card(
                insideMargin = androidx.compose.foundation.layout.PaddingValues(22.dp),
            ) {
                Text(
                    text = "使用前请阅读",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Text(
                    text = "一中帮 · 免责协议",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(top = 4.dp),
                )

                Column(
                    modifier = Modifier.padding(top = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AgreementParagraph(
                        "1. 本应用是个人自用的成绩查询工具，与城固一中及任何学校系统没有隶属或合作关系。",
                    )
                    AgreementParagraph(
                        "2. 成绩、答题卡等数据来自学校查询系统，仅在本机内存中使用；登录链接和 token 不写入日志、不落盘。",
                    )
                    AgreementParagraph(
                        "3. 不得将本应用公开部署、转卖或用于批量获取他人信息；不得传播带 token 的登录链接。",
                    )
                    AgreementParagraph(
                        "4. 所有查询结果仅供参考，最终成绩以学校正式通知为准。因使用本应用造成的任何后果，由使用者自行承担。",
                    )
                    AgreementParagraph(
                        "5. 继续使用即表示你已理解并同意以上内容。",
                    )
                }

                Spacer(Modifier.height(20.dp))
                Text(
                    text = "请完整输入下面这句话后继续：",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariantSummary,
                )
                Text(
                    text = AgreementPhrase,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 6.dp),
                )

                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceContainerHigh)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                ) {
                    if (input.isEmpty()) {
                        Text(
                            text = "在这里输入…",
                            fontSize = 13.sp,
                            color = colors.onSurfaceVariantSummary,
                        )
                    }
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.5.sp, color = colors.onSurface),
                        cursorBrush = SolidColor(colors.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (input.isNotEmpty() && !accepted) {
                    Text(
                        text = "文字不完全一致，不能进入。",
                        fontSize = 11.5.sp,
                        color = colors.error,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = accepted,
                ) {
                    Text("同意并进入", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AgreementParagraph(text: String) {
    Text(
        text = text,
        fontSize = 12.5.sp,
        lineHeight = 20.sp,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    )
}
