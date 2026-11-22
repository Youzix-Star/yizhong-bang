package top.youzix.bang.data

import java.util.Locale
import kotlin.math.abs

data class SubjectRow(
    val name: String,
    val full: Double,
    val clsAvg: Double?,
    val gradeAvg: Double?,
    val clsPct: Float,
    val gradePct: Float,
)

/** 纯计算：差值、偏科数、最强/最弱科目、一句话点评、筛选排序。 */
object Stats {

    fun avg(sheet: Sheet?): Double? = sheet?.classAvg?.total
    fun gradeAvg(sheet: Sheet?): Double? = sheet?.gradeAvg?.total

    fun delta(sheet: Sheet?): Double? {
        val a = avg(sheet) ?: return null
        val g = gradeAvg(sheet) ?: return null
        return a - g
    }

    fun flagCount(student: Student?): Int = student?.flags?.count { it } ?: 0

    fun subjects(sheet: Sheet?): List<SubjectRow> {
        if (sheet == null) return emptyList()
        return sheet.subjects.mapIndexed { index, name ->
            val full = sheet.full.getOrNull(index) ?: 100.0
            val cls = sheet.classAvg?.subjects?.getOrNull(index)
            val grade = sheet.gradeAvg?.subjects?.getOrNull(index)
            SubjectRow(
                name = name,
                full = full,
                clsAvg = cls,
                gradeAvg = grade,
                clsPct = pct(cls, full),
                gradePct = pct(grade, full),
            )
        }
    }

    fun pct(score: Double?, full: Double): Float {
        if (score == null || full <= 0.0) return 0f
        return (score / full).coerceIn(0.0, 1.0).toFloat()
    }

    fun comment(sheet: Sheet?): String {
        if (sheet == null) return "选一次考试，这里会告诉你班级整体是稳还是偏。"
        val rows = subjects(sheet).filter { it.clsAvg != null && it.gradeAvg != null }
            .sortedByDescending { it.clsPct }
        val strongest = rows.firstOrNull()
        val weakest = rows.lastOrNull()?.takeIf { rows.size > 1 }
        val d = delta(sheet)
        val count = sheet.students.size
        if (d == null) {
            return "$count 人参考，${strongest?.name ?: "整体"}是这次相对最好的一科。"
        }
        val strong = strongest?.name ?: "整体"
        val weak = weakest?.name ?: "整体"
        return when {
            d >= 8 -> "比年级均分高 ${signed(d)} 分，$strong 是这次最稳的一科" +
                if (weak != strong) "，$weak 还能再往上走一点。" else "。"
            d <= -8 -> "比年级均分低 ${signed(d)} 分，$weak 最需要补一补" +
                if (strong != weak) "，$strong 至少守住了。" else "。"
            else -> "基本贴着年级线（${signed(d)} 分），$strong 略强，$weak 还有空间。"
        }
    }

    fun signed(value: Double): String {
        val rounded = String.format(Locale.US, "%.1f", value)
        return if (value > 0) "+$rounded" else rounded
    }

    fun fmt(value: Double?, digits: Int = 2): String {
        if (value == null || value.isNaN()) return "—"
        val text = String.format(Locale.US, "%.${digits}f", value)
        return if (text.contains('.')) text.trimEnd('0').trimEnd('.') else text
    }

    fun int(value: Double?): String = if (value == null) "—" else value.toInt().toString()

    fun sorted(students: List<Student>): List<Student> =
        students.sortedWith(compareByDescending<Student> { it.total ?: -1.0 }.thenBy { it.name })

    fun filtered(students: List<Student>, query: String): List<Student> {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return students
        return students.filter {
            it.name.lowercase().contains(needle) || it.no.lowercase().contains(needle)
        }
    }

    fun same(a: Double?, b: Double?): Boolean =
        a != null && b != null && abs(a - b) < 0.05
}
