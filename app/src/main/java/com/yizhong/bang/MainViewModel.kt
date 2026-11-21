package com.yizhong.bang

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yizhong.bang.data.AnswerSheetRepository
import com.yizhong.bang.data.AppTheme
import com.yizhong.bang.data.DemoData
import com.yizhong.bang.data.Grade
import com.yizhong.bang.data.GradeRepository
import com.yizhong.bang.data.Meta
import com.yizhong.bang.data.SettingsStore
import com.yizhong.bang.data.Sheet
import com.yizhong.bang.data.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // ── 入口 ─────────────────────────────────────────────
    fun submitLink(url: String, answerUrl: String = "") {
        if (_state.value.busy) return
        if (url.isBlank()) {
            _state.update { it.copy(gateError = "先把成绩链接粘进来") }
            return
        }
        _state.update { it.copy(busy = true, gateError = null) }
        viewModelScope.launch {
            try {
                repo.open(url)
                val meta = repo.meta()

                answerRepo.reset()
                var answerBound = false
                var answerError: String? = null
                if (answerUrl.isNotBlank()) {
                    try {
                        answerRepo.bind(answerUrl)
                        answerBound = true
                    } catch (e: Exception) {
                        answerError = friendly(e)
                    }
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
        val id = ++answerRequestId
        if (!snapshot.answerBound) {
            _answer.value = AnswerState(
                target = target,
                error = "还没有绑定答题卡链接。退出后重新粘贴试卷分析链接即可。",
            )
            return
        }
        _answer.value = AnswerState(target = target, loading = true)
        viewModelScope.launch {
            try {
                val pages = answerRepo.load(
                    gradeCode = snapshot.grade,
                    studentNo = student.no,
                    studentName = student.name,
                    subject = subject,
                    subjectIndex = subjectIndex,
                    className = target.className,
                )
                if (id == answerRequestId) {
                    _answer.value = AnswerState(target = target, pages = pages)
                }
            } catch (e: Exception) {
                if (id == answerRequestId) {
                    _answer.value = AnswerState(target = target, error = friendly(e))
                }
            }
        }
    }

    fun closeAnswerSheet() {
        answerRequestId++
        _answer.value = AnswerState()
    }

    fun retryAnswerSheet() {
        val target = _answer.value.target ?: return
        val student = _state.value.sheet?.students?.firstOrNull { it.no == target.studentNo } ?: return
        openAnswerSheet(student, target.subjectIndex)
    }

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
