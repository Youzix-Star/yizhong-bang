package top.youzix.bang.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import top.youzix.bang.MainViewModel
import top.youzix.bang.UiState
import top.youzix.bang.data.Sheet
import top.youzix.bang.data.Stats
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType

@Composable
fun OverviewTab(state: UiState, viewModel: MainViewModel) {
    val sheet = state.sheet
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (sheet == null) {
            item { SectionTitle(if (state.busy) "正在取数" else "班级总览") }
            item {
                SectionCard {
                    Text(
                        text = if (state.busy) "正在替你点学校那几个按钮…" else "还没有拿到成绩，先选一次考试。",
                        fontSize = 13.5.sp,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
            item { FilterCard(state, viewModel) }
        } else {
            item { SectionTitle(sheet.meta?.term ?: "班级总览") }
            item { HeroCard(sheet) }
            item { FilterCard(state, viewModel) }
            item { SectionTitle("各科均分 · 本班与年级") }
            item { ChartCard(sheet) }
            item { HitokotoCard() }
        }
    }
}

@Composable
private fun HeroCard(sheet: Sheet) {
    val colors = MiuixTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        insideMargin = PaddingValues(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "班均分",
                fontSize = 11.5.sp,
                color = colors.onSurfaceVariantSummary,
            )
            Spacer(Modifier.weight(1f))
            DeltaBadge(Stats.delta(sheet))
        }
        val totalFull = sheet.full.mapNotNull { it }.sum()
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = Stats.fmt(Stats.avg(sheet), 1),
                fontSize = 46.sp,
                lineHeight = 50.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface,
            )
            if (totalFull > 0.0) {
                Text(
                    text = "/ 满分 ${Stats.fmt(totalFull, 0)}",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                    modifier = Modifier.padding(start = 6.dp, bottom = 9.dp),
                )
            }
        }
        Text(
            text = Stats.comment(sheet),
            fontSize = 12.5.sp,
            lineHeight = 19.sp,
            color = colors.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 4.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatsTile("参考人数", sheet.students.size.toString(), "人", Modifier.weight(1f))
            StatsTile("偏科人次", sheet.students.sumOf { Stats.flagCount(it) }.toString(), "次", Modifier.weight(1f))
            StatsTile("年级均分", Stats.fmt(Stats.gradeAvg(sheet), 1), null, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterCard(state: UiState, viewModel: MainViewModel) {
    val grades = state.meta?.grades.orEmpty()
    val grade = grades.firstOrNull { it.value == state.grade }
    val exams = grade?.exams.orEmpty()
    val classes = grade?.classes.orEmpty()

    val gradeIndex = grades.indexOfFirst { it.value == state.grade }.coerceAtLeast(0)
    val examIndex = exams.indexOfFirst { it.value == state.exam }.coerceAtLeast(0)
    val classIndex = classes.indexOfFirst { it.value == state.clazz }.coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        insideMargin = PaddingValues(vertical = 4.dp),
    ) {
        YzSpinnerPreference(
            items = grades.map { DropdownItem(title = it.label) },
            selectedIndex = gradeIndex,
            title = "年级",
            enabled = grades.size > 1,
            onSelectedIndexChange = { index ->
                grades.getOrNull(index)?.let { viewModel.selectGrade(it.value) }
            },
        )
        if (exams.isEmpty()) {
            BasicComponent(title = "考试", summary = "这个年级暂时没有考试数据")
        } else {
            YzSpinnerPreference(
                items = exams.map { DropdownItem(title = it.label) },
                selectedIndex = examIndex,
                title = "考试",
                onSelectedIndexChange = { index ->
                    exams.getOrNull(index)?.let { viewModel.selectExam(it.value) }
                },
            )
        }
        if (classes.isEmpty()) {
            BasicComponent(title = "班级", summary = "这个年级暂时没有班级数据")
        } else {
            YzSpinnerPreference(
                items = classes.map { DropdownItem(title = it.label) },
                selectedIndex = classIndex,
                title = "班级",
                onSelectedIndexChange = { index ->
                    classes.getOrNull(index)?.let { viewModel.selectClass(it.value) }
                },
            )
        }
    }
}

@Composable
private fun ChartCard(sheet: Sheet) {
    val colors = MiuixTheme.colorScheme
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "百分制换算，满分不同的科目也能放一起看",
                fontSize = 11.sp,
                color = colors.onSurfaceVariantSummary,
                modifier = Modifier.weight(1f),
            )
            LegendDot(colors.onSurfaceVariantSummary.copy(alpha = 0.4f), "年级")
            Spacer(Modifier.size(10.dp))
            LegendDot(colors.primary, "本班")
        }
        SubjectBars(
            rows = Stats.subjects(sheet),
            modifier = Modifier.padding(top = 12.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .height(1.dp)
                .background(colors.dividerLine),
        )
        Text(
            text = "细柱子是年级均分，粗一点的是本班；每科都按自己的满分归一化。",
            fontSize = 10.5.sp,
            color = colors.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = text,
            fontSize = 10.5.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

private val hitokotoClient = OkHttpClient.Builder()
    .connectTimeout(8, TimeUnit.SECONDS)
    .readTimeout(8, TimeUnit.SECONDS)
    .build()

@Composable
private fun HitokotoCard() {
    val colors = MiuixTheme.colorScheme
    var quote by remember { mutableStateOf("从 Hitokoto 获取一言…") }
    var source by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var reload by remember { mutableIntStateOf(0) }

    LaunchedEffect(reload) {
        loading = true
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url("https://v1.hitokoto.cn/?encode=json")
                    .header("User-Agent", "YiZhongBang/1.0 (Android)")
                    .build()
                hitokotoClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("HTTP ${response.code}")
                    val body = response.body?.string() ?: error("empty body")
                    val json = JSONObject(body)
                    val text = json.optString("hitokoto").ifBlank { error("empty quote") }
                    val from = json.optString("from").ifBlank { json.optString("from_who") }
                    text to from
                }
            }.getOrNull()
        }
        if (result != null) {
            quote = result.first
            source = result.second
        } else {
            quote = "从 Hitokoto 获取一言失败，稍后再试。"
            source = ""
        }
        loading = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        insideMargin = PaddingValues(18.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        showIndication = true,
        onClick = { if (!loading) reload += 1 },
    ) {
        Text(
            text = quote,
            fontSize = 12.5.sp,
            lineHeight = 19.sp,
            color = colors.onSurface,
        )
        if (source.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "—— $source",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
        }
    }
}
