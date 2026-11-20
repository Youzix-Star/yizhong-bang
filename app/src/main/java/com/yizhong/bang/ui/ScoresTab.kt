package com.yizhong.bang.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yizhong.bang.MainViewModel
import com.yizhong.bang.UiState
import com.yizhong.bang.data.Sheet
import com.yizhong.bang.data.Stats
import com.yizhong.bang.data.Student
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ScoresTab(state: UiState, viewModel: MainViewModel) {
    val sheet = state.sheet
    val students = remember(sheet, state.query) {
        Stats.filtered(Stats.sorted(sheet?.students.orEmpty()), state.query)
    }
    val listState = rememberLazyListState()
    val colors = MiuixTheme.colorScheme

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            SearchField(
                query = state.query,
                onQueryChange = viewModel::setQuery,
            )
        }
        item {
            Text(
                text = if (sheet == null) {
                    "还没有成绩"
                } else {
                    "共 ${sheet.students.size} 人 · 当前 ${students.size} 人 · 按总分排序"
                },
                fontSize = 11.sp,
                color = colors.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }

        if (sheet == null) {
            item {
                SectionCard {
                    Text(
                        text = "成绩还没到，先看看总览里的筛选条件。",
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariantSummary,
                    )
                }
            }
        } else if (students.isEmpty()) {
            item {
                SectionCard {
                    Text(text = "没找到这个人", fontSize = 14.sp, color = colors.onSurface)
                    Text(
                        text = "换个姓名或考号试试，或者清空搜索。",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariantSummary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        } else {
            items(students, key = { it.no.ifBlank { it.name } }) { student ->
                StudentCard(
                    student = student,
                    sheet = sheet,
                    expanded = state.expanded.contains(student.no),
                    onToggle = { viewModel.toggleExpanded(student.no) },
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val colors = MiuixTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = MiuixIcons.Search,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = colors.onSurfaceVariantSummary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "筛选姓名或考号",
                    fontSize = 13.5.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.5.sp, color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
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

@Composable
private fun StudentCard(
    student: Student,
    sheet: Sheet,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MiuixTheme.colorScheme
    val flags = Stats.flagCount(student)
    val rank = student.totalRank?.classRank

    Card(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        insideMargin = PaddingValues(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = rank?.toString() ?: "—",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (flags > 0) {
                        Text(
                            text = "偏科 $flags",
                            fontSize = 10.sp,
                            color = colors.error,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(colors.error.copy(alpha = 0.12f))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    text = "班 ${rank ?: "—"} / 级 ${student.totalRank?.grade ?: "—"} · 考号 ${student.no}",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Text(
                text = Stats.fmt(student.total, 1),
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.dividerLine),
                )
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    sheet.subjects.forEachIndexed { index, subject ->
                        val full = sheet.full.getOrNull(index) ?: 100.0
                        val score = student.scores.getOrNull(index)
                        val classAvg = sheet.classAvg?.subjects?.getOrNull(index)
                        val rankOfSubject = student.ranks.getOrNull(index)
                        val fraction = Stats.pct(score, full)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = subject,
                                fontSize = 12.5.sp,
                                color = colors.onSurface,
                                modifier = Modifier.width(46.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(colors.surfaceContainerHighest),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.primary),
                                )
                            }
                            Text(
                                text = Stats.fmt(score),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(start = 10.dp).width(46.dp),
                            )
                            Text(
                                text = "班均 ${Stats.fmt(classAvg, 1)}",
                                fontSize = 10.sp,
                                color = colors.onSurfaceVariantSummary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(66.dp),
                            )
                            Text(
                                text = "班${rankOfSubject?.classRank ?: "—"}",
                                fontSize = 10.sp,
                                color = colors.onSurfaceVariantSummary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(38.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
