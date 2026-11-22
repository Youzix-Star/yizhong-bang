package top.youzix.bang

import android.app.Application
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import top.youzix.bang.data.AnswerSheetRepository
import top.youzix.bang.data.AppTheme
import top.youzix.bang.data.DemoData
import top.youzix.bang.data.Grade
import top.youzix.bang.data.GradeRepository
import top.youzix.bang.data.Meta
import top.youzix.bang.data.SettingsStore
import top.youzix.bang.data.Sheet
import top.youzix.bang.data.Student
import java.io.BufferedOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Phase { Gate, Main }

data class UiState(
    val phase: Phase = Phase.Gate,
    val meta: Meta? = null,
    val sheet: Sheet? = null,
    val grade: String = "",
    val exam: String = "",
    val clazz: String = "",
    val query: String = "",
    val expanded: Set<String> = emptySet(),
    val busy: Boolean = false,
    val error: String? = null,
    val gateError: String? = null,
    val demo: Boolean = false,
    val answerBound: Boolean = false,
    val answerError: String? = null,
)

data class AnswerTarget(
    val studentNo: String,
    val studentName: String,
    val subjectIndex: Int,
    val subject: String,
    val className: String,
)

data class AnswerState(
    val target: AnswerTarget? = null,
    val loading: Boolean = false,
    val pages: List<ByteArray> = emptyList(),
    val error: String? = null,
)

data class ZipExportResult(
    val ok: Boolean,
    val message: String,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = GradeRepository()
    private val answerRepo = AnswerSheetRepository()
    private val settingsStore = SettingsStore(application)

    val settings = settingsStore.state

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _answer = MutableStateFlow(AnswerState())
    val answer: StateFlow<AnswerState> = _answer.asStateFlow()

    private var requestId = 0
    private var answerRequestId = 0
    private var answerJob: Job? = null

    // ── 入口 ─────────────────────────────────────────────
    fun submitLink(url: String) {
        if (_state.value.busy) return
        if (url.isBlank()) {
            _state.update { it.copy(gateError = "先把成绩链接粘进来") }
            return
        }
        _state.update { it.copy(busy = true, gateError = null) }
        viewModelScope.launch {
            try {
                val session = repo.open(url)
                val meta = repo.meta()

                answerRepo.reset()
                var answerBound = false
                var answerError: String? = null
                val answerSheetUrl = session.answerSheetUrl
                if (answerSheetUrl != null) {
                    try {
                        answerRepo.bind(answerSheetUrl)
                        answerBound = true
                    } catch (e: Exception) {
                        answerError = friendly(e)
                    }
                } else {
                    answerError = "成绩页里没有找到「试卷分析」入口，请换成「成绩分析」那条链接"
                }

                _state.update {
                    it.copy(
                        meta = meta,
                        demo = false,
                        busy = false,
                        phase = Phase.Main,
                        answerBound = answerBound,
                        answerError = answerError,
                    )
                }
                selectFirst(meta)
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, gateError = friendly(e)) }
            }
        }
    }

    fun loadDemo() {
        if (_state.value.busy) return
        viewModelScope.launch {
            try {
                val payload = DemoData.load(getApplication<Application>())
                _state.update {
                    it.copy(
                        phase = Phase.Main,
                        meta = payload.meta,
                        sheet = payload.sheet.copy(cached = true, at = System.currentTimeMillis()),
                        demo = true,
                        answerBound = false,
                        grade = payload.meta.grades.firstOrNull()?.value ?: "",
                        exam = payload.meta.grades.firstOrNull()?.exams?.firstOrNull()?.value ?: "",
                        clazz = payload.meta.grades.firstOrNull()?.classes?.firstOrNull()?.value ?: "",
                        gateError = null,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(gateError = "示例数据加载失败：${e.message}") }
            }
        }
    }

    private fun selectFirst(meta: Meta) {
        val grade = meta.grades.firstOrNull { it.exams.isNotEmpty() && it.classes.isNotEmpty() }
            ?: meta.grades.firstOrNull()
        if (grade == null) {
            _state.update { it.copy(error = "这个账号没有可查的年级") }
            return
        }
        _state.update {
            it.copy(
                grade = grade.value,
                exam = grade.exams.firstOrNull()?.value ?: "",
                clazz = grade.classes.firstOrNull()?.value ?: "",
                sheet = null,
            )
        }
        loadSheet()
    }

    // ── 取数 ─────────────────────────────────────────────
    fun loadSheet(force: Boolean = false) {
        val snapshot = _state.value
        if (snapshot.exam.isEmpty() || snapshot.clazz.isEmpty()) {
            _state.update { it.copy(error = "这个年级暂时没有考试数据", sheet = null, busy = false) }
            return
        }
        val id = ++requestId
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                val sheet = repo.sheet(snapshot.grade, snapshot.exam, snapshot.clazz, force)
                if (id != requestId) return@launch
                _state.update { it.copy(sheet = sheet, busy = false, expanded = emptySet()) }
            } catch (e: Exception) {
                if (id != requestId) return@launch
                _state.update { it.copy(busy = false, error = friendly(e), sheet = null) }
            }
        }
    }

    fun refresh() {
        val current = _state.value
        if (current.demo) {
            loadDemo()
            return
        }
        if (current.busy) return
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                val meta = repo.meta(force = true)
                val grade = meta.grades.firstOrNull { it.value == current.grade }
                    ?: meta.grades.firstOrNull { it.exams.isNotEmpty() && it.classes.isNotEmpty() }
                    ?: meta.grades.firstOrNull()
                if (grade == null) throw IllegalStateException("这个账号没有可查的年级")
                val exam = grade.exams.firstOrNull { it.value == current.exam }?.value
                    ?: grade.exams.firstOrNull()?.value ?: ""
                val clazz = grade.classes.firstOrNull { it.value == current.clazz }?.value
                    ?: grade.classes.firstOrNull()?.value ?: ""
                _state.update {
                    it.copy(meta = meta, grade = grade.value, exam = exam, clazz = clazz)
                }
                loadSheet(force = true)
            } catch (e: Exception) {
                _state.update { it.copy(busy = false, error = friendly(e)) }
            }
        }
    }

    fun selectGrade(value: String) {
        if (_state.value.demo) return
        val grade = _state.value.meta?.grades?.firstOrNull { it.value == value } ?: return
        _state.update {
            it.copy(
                grade = value,
                exam = grade.exams.firstOrNull()?.value ?: "",
                clazz = grade.classes.firstOrNull()?.value ?: "",
                sheet = null,
                expanded = emptySet(),
            )
        }
        loadSheet()
    }

    fun selectExam(value: String) {
        if (_state.value.demo) return
        _state.update { it.copy(exam = value, sheet = null, expanded = emptySet()) }
        loadSheet()
    }

    fun selectClass(value: String) {
        if (_state.value.demo) return
        _state.update { it.copy(clazz = value, sheet = null, expanded = emptySet()) }
        loadSheet()
    }

    fun grade(): Grade? = _state.value.meta?.grades?.firstOrNull { it.value == _state.value.grade }

    // ── 界面状态 ─────────────────────────────────────────
    fun setQuery(query: String) = _state.update { it.copy(query = query) }

    fun toggleExpanded(no: String) {
        val current = _state.value.expanded
        _state.update {
            it.copy(expanded = if (current.contains(no)) current - no else current + no)
        }
    }

    fun logout() {
        requestId++
        answerRequestId++
        answerJob?.cancel()
        answerJob = null
        _answer.value = AnswerState()
        viewModelScope.launch {
            repo.reset()
            answerRepo.reset()
            _state.value = UiState(phase = Phase.Gate)
        }
    }

    fun openAnswerSheet(student: Student, subjectIndex: Int) {
        val snapshot = _state.value
        val sheet = snapshot.sheet ?: return
        val subject = sheet.subjects.getOrNull(subjectIndex) ?: return
        val target = AnswerTarget(
            studentNo = student.no,
            studentName = student.name,
            subjectIndex = subjectIndex,
            subject = subject,
            className = sheet.meta?.className ?: "",
        )
        answerJob?.cancel()
        val id = ++answerRequestId
        if (!snapshot.answerBound) {
            _answer.value = AnswerState(
                target = target,
                error = "还没有绑定答题卡链接。退出后重新粘贴试卷分析链接即可。",
            )
            return
        }
        _answer.value = AnswerState(target = target, loading = true)
        answerJob = viewModelScope.launch {
            try {
                answerRepo.loadPages(
                    gradeCode = snapshot.grade,
                    studentNo = student.no,
                    studentName = student.name,
                    subject = subject,
                    subjectIndex = subjectIndex,
                    className = target.className,
                ).collect { bytes ->
                    if (id == answerRequestId) {
                        // 取到一页就先显示一页，剩下的继续在后台下载。
                        _answer.update { it.copy(pages = it.pages + bytes) }
                    }
                }
                if (id == answerRequestId) {
                    _answer.update { it.copy(loading = false) }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (id == answerRequestId) {
                    // 已经拿到的页不清掉，只把错误提示挂上去。
                    _answer.update { it.copy(loading = false, error = friendly(e)) }
                }
            }
        }
    }

    fun switchAnswerSubject(subjectIndex: Int) {
        val target = _answer.value.target ?: return
        if (target.subjectIndex == subjectIndex) return
        val student = _state.value.sheet?.students?.firstOrNull { it.no == target.studentNo } ?: return
        openAnswerSheet(student, subjectIndex)
    }

    fun closeAnswerSheet() {
        answerRequestId++
        answerJob?.cancel()
        answerJob = null
        _answer.value = AnswerState()
    }

    fun retryAnswerSheet() {
        val target = _answer.value.target ?: return
        val student = _state.value.sheet?.students?.firstOrNull { it.no == target.studentNo } ?: return
        openAnswerSheet(student, target.subjectIndex)
    }

    suspend fun exportAllAnswerSheets(): ZipExportResult {
        val target = _answer.value.target
            ?: return ZipExportResult(false, "还没有打开答题卡")
        val sheet = _state.value.sheet
            ?: return ZipExportResult(false, "成绩还没加载好")
        val subjects = sheet.subjects
        if (subjects.isEmpty()) return ZipExportResult(false, "这个年级没有科目")
        val gradeCode = _state.value.grade

        return withContext(Dispatchers.IO) {
            val resolver = getApplication<Application>().contentResolver
            val safeName = target.studentName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val displayName = "答题卡_${safeName}_全部科目.zip"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/一中帮",
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext ZipExportResult(false, "无法在下载目录创建压缩包")

            var pageCount = 0
            val failed = mutableListOf<String>()
            try {
                resolver.openOutputStream(uri)?.use { output ->
                    ZipOutputStream(BufferedOutputStream(output)).use { zip ->
                        subjects.forEachIndexed { index, subject ->
                            var subjectPages = 0
                            try {
                                answerRepo.loadPages(
                                    gradeCode = gradeCode,
                                    studentNo = target.studentNo,
                                    studentName = target.studentName,
                                    subject = subject,
                                    subjectIndex = index,
                                    className = target.className,
                                ).collect { bytes ->
                                    val page = subjectPages + 1
                                    val entryName = "${safePart(subject)}/第${"%02d".format(page)}页.jpg"
                                    zip.putNextEntry(ZipEntry(entryName))
                                    zip.write(bytes)
                                    zip.closeEntry()
                                    subjectPages += 1
                                    pageCount += 1
                                }
                            } catch (e: Exception) {
                                // 这个科目取不到就继续导出其他科目。
                            }
                            if (subjectPages == 0) failed += subject
                        }

                        val time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                            .format(Date())
                        val readme = buildString {
                            appendLine("一中帮 · 答题卡导出")
                            appendLine("学生：${target.studentName}")
                            appendLine("班级：${target.className}")
                            appendLine("导出时间：$time")
                            appendLine()
                            appendLine("目录说明：每个科目一个文件夹，里面是按页保存的 JPEG。")
                            appendLine("科目：${subjects.joinToString("、")}")
                            appendLine("成功导出：${subjects.size - failed.size} 科，共 $pageCount 页")
                            if (failed.isNotEmpty()) {
                                appendLine("未导出：${failed.joinToString("、")}")
                            }
                        }
                        zip.putNextEntry(ZipEntry("README.txt"))
                        zip.write(readme.toByteArray(Charsets.UTF_8))
                        zip.closeEntry()
                    }
                } ?: error("无法写入压缩包")

                if (pageCount == 0) {
                    resolver.delete(uri, null, null)
                    return@withContext ZipExportResult(false, "没有取到任何答题卡")
                }

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                val suffix = if (failed.isEmpty()) "" else "，${failed.size} 科未取到"
                ZipExportResult(
                    true,
                    "已保存 ${subjects.size - failed.size} 科 / $pageCount 页到下载目录$suffix",
                )
            } catch (e: Exception) {
                runCatching { resolver.delete(uri, null, null) }
                ZipExportResult(false, e.message ?: "导出压缩包失败")
            }
        }
    }

    private fun safePart(text: String): String =
        text.replace(Regex("[\\\\/:*?\"<>|]"), "_").ifBlank { "未命名" }

    fun setTheme(theme: AppTheme) = settingsStore.setTheme(theme)
    fun setDynamicColor(enabled: Boolean) = settingsStore.setDynamicColor(enabled)
    fun setAmoled(enabled: Boolean) = settingsStore.setAmoled(enabled)

    private fun friendly(e: Exception): String {
        val text = e.message ?: return "网络不太顺，稍后重试"
        return when {
            text.contains("timeout", ignoreCase = true) -> "连接学校服务器超时了"
            text.contains("Unable to resolve host", ignoreCase = true) -> "解析不到学校服务器，检查一下网络"
            else -> text
        }
    }
}
